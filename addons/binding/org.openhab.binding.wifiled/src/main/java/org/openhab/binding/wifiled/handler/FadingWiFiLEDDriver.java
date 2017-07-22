/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
    public static final int KEEPCOMMOPENINMS = 1000; // After last fade keep communication channel open fs XXX MS

    private static final InternalLedState blackState = new InternalLedState();

    private boolean power = false;
    private InternalLedState currentState = new InternalLedState(); // USe to not update the controller with the same value
    private InternalLedState currentFaderState = new InternalLedState();
    private InternalLedState targetState = new InternalLedState();
    private LEDStateDTO dtoState = LEDStateDTO.valueOf(0, 0, 0, 0, 0, 0, 0, 0);
    private ScheduledExecutorService waiterFuture = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService faderFuture = Executors.newSingleThreadScheduledExecutor();
    private LEDFaderRunner ledfaderThread = null;
    private final Semaphore ledUpdateSyncSemaphore = new Semaphore(1, false);
    private final int fadeDurationInMs;
    private final int fadeSteps;

    static {
       // executorService.setRemoveOnCancelPolicy(true);
    }

    public FadingWiFiLEDDriver(String host, int port, AbstractWiFiLEDDriver.Protocol protocol, int fadeDurationInMs,
                               int fadeSteps) {
        super(host, port, protocol);
        this.fadeDurationInMs = fadeDurationInMs;
        this.fadeSteps = fadeSteps;
    }

    @Override
    public void init() throws IOException {
        try {
            LEDState s = getLEDState();
            dtoState = LEDStateDTO.valueOf(s.state, s.program, s.programSpeed, s.red, s.green, s.blue, s.white,
                    s.white2);
            power = (s.state & 0x01) != 0;
            currentFaderState = currentState = InternalLedState.fromRGBW(s.red, s.green, s.blue, s.white, s.white2);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void dispose() {
        waiterFuture.shutdown();
        faderFuture.shutdown();
        try {
            if (!waiterFuture.awaitTermination((fadeDurationInMs / fadeSteps) * 2, TimeUnit.MILLISECONDS)) {
                waiterFuture.shutdownNow();
            }
            if (!faderFuture.awaitTermination((fadeDurationInMs / fadeSteps) * 2, TimeUnit.MILLISECONDS)) {
                faderFuture.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.warn("InterruptedException", e);
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
        protected Logger logger = LoggerFactory.getLogger(LEDFaderRunner.class);
        protected final int steps;

        // Keep track of LED states
        protected int fadeStepper = 1;
        protected InternalLedState currentFadeState;
        protected InternalLedState toState;
        protected InternalLedState fromState;

        // After the last update keep the communication channel open for XXX ms
        protected long keepCommOpenForMS = 1000;
        // Keep track of when last communication happened
        protected long lastComTime = 0;
        // Function that will be called to update LED
        protected final Function<InternalLedState, Boolean> ledSender;

        protected final Lock lock = new ReentrantLock();

        public LEDFaderRunner(InternalLedState fromState, InternalLedState toState, int steps, int keepCommOpenForMS, Function<InternalLedState, Boolean> ledSender) {
            this.currentFadeState = fromState;
            this.fromState = fromState;
            this.toState = toState;
            this.steps = steps;
            this.ledSender = ledSender;
            this.keepCommOpenForMS = keepCommOpenForMS;
        }

        public void setToState(InternalLedState toState) {
            lock.lock();
            this.fromState = currentFadeState;
            this.toState = toState;
            this.fadeStepper = 1;
            lock.unlock();
        }

        public void run() {
            lock.lock();
            if (fadeStepper <= steps) {
                currentFadeState = fromState.fade(toState, (double) fadeStepper / steps);
                lock.unlock();

                logger.debug("currentFadeState: {}", currentFadeState);
                if (!ledSender.apply(currentFadeState)) {
                    logger.warn("Failed sending at step {}", fadeStepper);
                    throw new RuntimeException("Failed sending at step " + fadeStepper);
                }
                lastComTime = System.currentTimeMillis();
            } else {
                lock.unlock();
                if (lastComTime < (System.currentTimeMillis() - keepCommOpenForMS)) {
                    throw new RuntimeException("Reached end step");
                }
            }
            fadeStepper++;
        }
    }

    synchronized private void fadeToState(final InternalLedState newTargetState) throws IOException {

        if (ledUpdateSyncSemaphore.tryAcquire(1)) {
            final Future<?> future;
            final Socket socket;

            // Create and Execute a new LED Fader
            try {
                socket = new Socket(host, port);
                socket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT);
                final DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                logger.debug("Connected to '{}'", socket);

                // ensure controller is on
                sendRaw(getBytesForPower(true), outputStream);

                ledfaderThread = new LEDFaderRunner(
                        currentFaderState,
                        newTargetState,
                        fadeSteps,
                        KEEPCOMMOPENINMS,
                        (fs) -> {
                            try {
                                sendLEDData(fs, outputStream);
                                currentFaderState = fs;
                                // logger.info("Current: {} {} {} {}", fs.getR(), fs.getG(), fs.getB(), fs.getWhite());
                                return true;
                            } catch (IOException e) {
                                logger.warn("IOException", e);
                                return false;
                            }
                        });
                future = faderFuture.scheduleAtFixedRate(ledfaderThread, 0, fadeDurationInMs / fadeSteps, TimeUnit.MILLISECONDS);

                // Wait untill LED Thread has finished, when so cleanup socket and remove ledfaderThread
                waiterFuture.schedule(() -> {
                    try {
                        future.get();
                    } catch (Exception e) {
                        // Expected during shutdown of driver or when the Runnable decides to terminate
                    }
                    if (socket!=null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            logger.warn("IOException", e);
                        }
                    }
                    ledfaderThread = null;
                    ledUpdateSyncSemaphore.release(1);
                }, 0, TimeUnit.MILLISECONDS);

            } catch (NoRouteToHostException e) {
                logger.warn("No route to host {}:{}", host, port, e);
                ledUpdateSyncSemaphore.release(1);
            } catch (SocketException e) {
                logger.warn("SocketException", e);
                ledUpdateSyncSemaphore.release(1);
            } catch (Exception e) {
                logger.warn("An error occurred", e);
                ledUpdateSyncSemaphore.release(1);
            }

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
