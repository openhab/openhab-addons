/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wifiled.handler;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * The {@link FadingWiFiLEDDriver} class is responsible for the communication with the WiFi LED controller.
 * It utilizes color fading when changing colors or turning the light on of off.
 *
 * @author Stefan Endrullis
 * @author Ries van Twisk
 */
public class FadingWiFiLEDDriver extends AbstractWiFiLEDDriver {

    public static final int DEFAULT_FADE_DURATION_IN_MS = 1000;
    public static final int DEFAULT_FADE_STEPS = 100;
    public static final int KEEP_COMMUNICATION_OPEN_FOR_MS = 1000;

    private static final InternalLedState blackState = new InternalLedState();

    private boolean power = false;
    private InternalLedState currentState = new InternalLedState(); // USe to not update the controller with the same value
    private InternalLedState currentFaderState = new InternalLedState();
    private InternalLedState targetState = new InternalLedState();
    private LEDStateDTO dtoState = LEDStateDTO.valueOf(0, 0, 0, 0, 0, 0, 0, 0);
    private final ScheduledExecutorService waiterExecutor = Executors.newSingleThreadScheduledExecutor();;
    private final ScheduledExecutorService faderExecutor = Executors.newSingleThreadScheduledExecutor();;
    private LEDFaderRunner ledfaderThread = null;
    private final Semaphore ledUpdateSyncSemaphore = new Semaphore(1, false);
    private final int fadeDurationInMs;
    private final int totalFadingSteps;

    public FadingWiFiLEDDriver(String host, int port, AbstractWiFiLEDDriver.Protocol protocol, int fadeDurationInMs,
                               int totalFadingSteps) {
        super(host, port, protocol);
        this.fadeDurationInMs = fadeDurationInMs < 10 ? 10 : fadeDurationInMs;
        this.totalFadingSteps = totalFadingSteps < 1 ? 1 : totalFadingSteps;
    }

    @Override
    public void init() throws IOException {
        try {
            LEDState s = getLEDState();
            dtoState = LEDStateDTO.valueOf(s.state, s.program, s.programSpeed, s.red, s.green, s.blue, s.white,
                    s.white2);
            power = (s.state & 0x01) != 0;
            currentState = InternalLedState.fromRGBW(s.red, s.green, s.blue, s.white, s.white2);
            currentFaderState = currentState;
        } catch (IOException e) {
            logger.warn("IOException", e);
        }
    }

    @Override
    public void shutdown() {
        waiterExecutor.shutdown();
        faderExecutor.shutdown();
        try {
            if (!waiterExecutor.awaitTermination((fadeDurationInMs / totalFadingSteps) * 2, TimeUnit.MILLISECONDS)) {
                waiterExecutor.shutdownNow();
            }
            if (!faderExecutor.awaitTermination((fadeDurationInMs / totalFadingSteps) * 2, TimeUnit.MILLISECONDS)) {
                faderExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            // Ignored
        }

    }

    @Override
    public void setColor(HSBType color) throws IOException {
        dtoState = dtoState.withColor(color);
        changeState(targetState.withColor(color));
    }

    @Override
    public void setBrightness(PercentType brightness) throws IOException {
        dtoState = dtoState.withBrightness(brightness);
        changeState(targetState.withBrightness(brightness.doubleValue() / 100));
    }

    @Override
    public void incBrightness(int step) throws IOException {
        dtoState = dtoState.withIncrementedBrightness(step);
        changeState(targetState.withBrightness(targetState.getBrightness() + ((double) step / 100)));
    }

    @Override
    public void decBrightness(int step) throws IOException {
        dtoState = dtoState.withIncrementedBrightness(-step);
        changeState(targetState.withBrightness(targetState.getBrightness() - ((double) step / 100)));
    }

    @Override
    public void setWhite(PercentType white) throws IOException {
        dtoState = dtoState.withWhite(white);
        changeState(targetState.withWhite(white.doubleValue() / 100));
    }

    @Override
    public void incWhite(int step) throws IOException {
        dtoState = dtoState.withIncrementedWhite(step);
        changeState(targetState.withWhite(targetState.getWhite() + ((double) step / 100)));
    }

    @Override
    public void setWhite2(PercentType white2) throws IOException {
        dtoState = dtoState.withWhite2(white2);
        changeState(targetState.withWhite2(white2.doubleValue() / 100));
    }

    @Override
    public void incWhite2(int step) throws IOException {
        dtoState = dtoState.withIncrementedWhite2(step);
        changeState(targetState.withWhite2(targetState.getWhite2() + ((double) step / 100)));
    }

    @Override
    public void setProgram(StringType program) throws IOException {
    }

    @Override
    public void setProgramSpeed(PercentType speed) throws IOException {
    }

    @Override
    public void incProgramSpeed(int step) throws IOException {
    }

    @Override
    public void setPower(OnOffType command) throws IOException {
        dtoState = dtoState.withPower(command);
        power = command == OnOffType.ON;
        fadeToState(power ? targetState : blackState);
    }

    @Override
    public LEDStateDTO getLEDStateDTO() throws IOException {
        return dtoState;
    }

    private void changeState(final InternalLedState newState) throws IOException {
        targetState = newState;
        if (power) {
            fadeToState(targetState);
        }
    }

    /**
     * Runnable that takes care of fading of the LED's
     */
    final static class LEDFaderRunner implements Runnable {
        private Logger logger = LoggerFactory.getLogger(LEDFaderRunner.class);

        private String host;
        private int port;
        private InternalLedState fromState;
        private InternalLedState toState;
        private final int totalFadingSteps;
        private final long keepCommOpenForMS;
        private final Function<DataOutputStream, Boolean> powerOnFunc;
        private final BiFunction<DataOutputStream, InternalLedState, Boolean> ledSender;

        private long lastCommunicationTime = 0;

        private int currentFadingStep = 1;

        private final Lock lock = new ReentrantLock();

        private InternalLedState currentFadeState;
        private Socket socket;
        private DataOutputStream outputStream;

        public LEDFaderRunner(String host, int port,
                              InternalLedState fromState, InternalLedState toState,
                              int totalFadingSteps, int keepCommOpenForMS,
                              Function<DataOutputStream, Boolean> powerOnFunc,
                              BiFunction<DataOutputStream, InternalLedState, Boolean> ledSender) {
            this.host = host;
            this.port = port;
            this.fromState = fromState;
            this.toState = toState;
            this.totalFadingSteps = totalFadingSteps;
            this.keepCommOpenForMS = keepCommOpenForMS;
            this.powerOnFunc = powerOnFunc;
            this.ledSender = ledSender;
        }

        /**
         * Call before starting a thre`ad, it will initialise a socket and power on the LEDs
         *
         * @throws IOException
         */
        public void init() throws IOException {
            socket = new Socket(host, port);
            socket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT);
            outputStream = new DataOutputStream(socket.getOutputStream());
            logger.debug("Connected to '{}'", socket);
            powerOnFunc.apply(outputStream);
            currentFadeState = fromState;
        }

        public void setToState(InternalLedState toState) {
            lock.lock();
            this.fromState = currentFadeState;
            this.toState = toState;
            this.currentFadingStep = 1;
            lock.unlock();
        }

        public void run() {
            lock.lock();
            if (currentFadingStep <= totalFadingSteps) {
                currentFadeState = fromState.fade(toState, (double) currentFadingStep / totalFadingSteps);
                lock.unlock();

                logger.debug("currentFadeState: {}", currentFadeState);
                if (!ledSender.apply(outputStream, currentFadeState)) {
                    logger.warn("Failed sending at step {}", currentFadingStep);
                    throw new RuntimeException("Failed sending at step " + currentFadingStep);
                }
                lastCommunicationTime = System.currentTimeMillis();
            } else {
                lock.unlock();
                if (lastCommunicationTime < (System.currentTimeMillis() - keepCommOpenForMS)) {
                    throw new RuntimeException("Reached end step");
                }
            }
            currentFadingStep++;
        }

        public void shutdown() {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignored
                }
            }
        }
    }

    synchronized private void fadeToState(final InternalLedState newTargetState) throws IOException {

        if (ledUpdateSyncSemaphore.tryAcquire(1)) {

            // Create and Execute a new LED Fader
            ledfaderThread = new LEDFaderRunner(
                    host,
                    port,
                    currentFaderState,
                    newTargetState,
                    totalFadingSteps,
                    KEEP_COMMUNICATION_OPEN_FOR_MS,
                    (outputStream) -> {
                        try {
                            sendRaw(getBytesForPower(true), outputStream);
                            return true;
                        } catch (IOException e) {
                            logger.warn("IOException", e);
                            return false;
                        }
                    },
                    (outputStream, fs) -> {
                        try {
                            sendLEDData(fs, outputStream);
                            currentFaderState = fs;
                            logger.trace("Current: {} {} {} {}", fs.getR(), fs.getG(), fs.getB(), fs.getWhite());
                            return true;
                        } catch (IOException e) {
                            logger.warn("IOException", e);
                            return false;
                        }
                    });
            ledfaderThread.init();
            final int period = fadeDurationInMs / totalFadingSteps;
            final Future<?> future = faderExecutor.scheduleAtFixedRate(ledfaderThread, 0, period < 1 ? 1 : period, TimeUnit.MILLISECONDS);

            // Wait untill LED Thread has finished, when so shutdown fader
            waiterExecutor.schedule(() -> {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    // Ignored
                } catch (Exception e) {
                    logger.warn("Exception", e);
                }
                ledfaderThread.shutdown();
                ledfaderThread = null;
                ledUpdateSyncSemaphore.release(1);
            }, 0, TimeUnit.MILLISECONDS);
        } else {
            ledfaderThread.setToState(newTargetState);
        }
    }

    private void sendLEDData(InternalLedState ledState, DataOutputStream out) throws IOException {
        logger.debug("Setting LED State to {}", ledState);

        if (!ledState.equals(currentState)) {
            // "normal" program: set color etc.
            byte r = (byte) (ledState.getR() & 0xFF);
            byte g = (byte) (ledState.getG() & 0xFF);
            byte b = (byte) (ledState.getB() & 0xFF);
            byte w = (byte) (ledState.getW() & 0xFF);
            byte w2 = (byte) (ledState.getW2() & 0xFF);

            logger.debug("RGBW: {}, {}, {}, {}, {}", r, g, b, w, w2);

            byte[] bytes = getBytesForColor(r, g, b, w, w2);
            sendRaw(bytes, out);
        }

        currentState = ledState;
    }

}
