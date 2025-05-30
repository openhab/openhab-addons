/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.tellstick.internal.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tellstick.internal.TelldusBindingException;
import org.openhab.binding.tellstick.internal.handler.TelldusDeviceController;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tellstick.JNA;
import org.tellstick.device.TellstickDevice;
import org.tellstick.device.TellstickDeviceEvent;
import org.tellstick.device.TellstickException;
import org.tellstick.device.TellstickSensorEvent;
import org.tellstick.device.iface.Device;
import org.tellstick.device.iface.DeviceChangeListener;
import org.tellstick.device.iface.DimmableDevice;
import org.tellstick.device.iface.SensorListener;
import org.tellstick.device.iface.SwitchableDevice;

/**
 * Device controller for telldus core (Basic and Duo).
 * This communicates with the telldus DLL using the javatellstick
 * library.
 *
 * @author Jarle Hjortland, Elias Gabrielsson - Initial contribution
 */
@NonNullByDefault
public class TelldusCoreDeviceController implements DeviceChangeListener, SensorListener, TelldusDeviceController {
    private final Logger logger = LoggerFactory.getLogger(TelldusCoreDeviceController.class);
    private long lastSend = 0;
    long resendInterval = 100;
    public static final long DEFAULT_INTERVAL_BETWEEN_SEND = 250;

    private TelldusCoreWorker telldusCoreWorker;
    private Thread workerThread;
    private SortedMap<Device, TelldusCoreSendEvent> messageQue;

    public TelldusCoreDeviceController(long resendInterval, final String threadName) {
        this.resendInterval = resendInterval;
        messageQue = Collections.synchronizedSortedMap(new TreeMap<>());
        telldusCoreWorker = new TelldusCoreWorker(messageQue);
        workerThread = new Thread(telldusCoreWorker, threadName);
    }

    @Override
    public void dispose() {
        workerThread.interrupt();
    }

    @Override
    public void handleSendEvent(Device device, int resendCount, boolean isDimmer, Command command)
            throws TellstickException {
        if (!workerThread.isAlive()) {
            workerThread.start();
        }

        Long eventTime = System.currentTimeMillis();
        synchronized (messageQue) {
            messageQue.put(device, new TelldusCoreSendEvent(device, resendCount, isDimmer, command, eventTime));
            messageQue.notify();
        }
    }

    @Override
    public @Nullable State calcState(Device dev) {
        TellstickDevice device = (TellstickDevice) dev;
        State st = null;
        switch (device.getStatus()) {
            case JNA.CLibrary.TELLSTICK_TURNON:
                st = OnOffType.ON;
                break;
            case JNA.CLibrary.TELLSTICK_TURNOFF:
                st = OnOffType.OFF;
                break;
            case JNA.CLibrary.TELLSTICK_DIM:
                BigDecimal dimValue = new BigDecimal(device.getData());
                if (dimValue.intValue() == 0) {
                    st = OnOffType.OFF;
                } else if (dimValue.intValue() >= 255) {
                    st = OnOffType.ON;
                } else {
                    st = OnOffType.ON;
                }
                break;
            default:
                logger.warn("Could not handle {} for {}", device.getStatus(), device);
        }
        return st;
    }

    @Override
    public BigDecimal calcDimValue(Device device) {
        BigDecimal dimValue = new BigDecimal(0);
        switch (((TellstickDevice) device).getStatus()) {
            case JNA.CLibrary.TELLSTICK_TURNON:
                dimValue = new BigDecimal(100);
                break;
            case JNA.CLibrary.TELLSTICK_TURNOFF:
                break;
            case JNA.CLibrary.TELLSTICK_DIM:
                dimValue = new BigDecimal(((TellstickDevice) device).getData());
                dimValue = dimValue.multiply(new BigDecimal(100));
                dimValue = dimValue.divide(new BigDecimal(255), 0, RoundingMode.HALF_UP);
                break;
            default:
                logger.warn("Could not handle {} for {}", ((TellstickDevice) device).getStatus(), device);
        }
        return dimValue;
    }

    public long getLastSend() {
        return lastSend;
    }

    public void setLastSend(long currentTimeMillis) {
        lastSend = currentTimeMillis;
    }

    @Override
    public void onRequest(@NonNullByDefault({}) TellstickSensorEvent newDevices) {
        setLastSend(newDevices.getTimestamp());
    }

    @Override
    public void onRequest(@NonNullByDefault({}) TellstickDeviceEvent newDevices) {
        setLastSend(newDevices.getTimestamp());
    }

    private void sendEvent(Device device, int resendCount, boolean isdimmer, Command command)
            throws TellstickException {
        for (int i = 0; i < resendCount; i++) {
            checkLastAndWait(resendInterval);
            logger.debug("Send {} to {} times={}", command, device, i);
            if (device instanceof DimmableDevice) {
                if (command == OnOffType.ON) {
                    turnOn(device);
                } else if (command == OnOffType.OFF) {
                    turnOff(device);
                } else if (command instanceof PercentType percentCommand) {
                    dim(device, percentCommand);
                } else if (command instanceof IncreaseDecreaseType increaseDecreaseCommand) {
                    increaseDecrease(device, increaseDecreaseCommand);
                }
            } else if (device instanceof SwitchableDevice) {
                if (command == OnOffType.ON) {
                    if (isdimmer) {
                        logger.debug("Turn off first in case it is allready on");
                        turnOff(device);
                        checkLastAndWait(resendInterval);
                    }
                    turnOn(device);
                } else if (command == OnOffType.OFF) {
                    turnOff(device);
                }
            } else {
                logger.warn("Cannot send to {}", device);
            }
        }
    }

    private void increaseDecrease(Device dev, IncreaseDecreaseType increaseDecreaseType) throws TellstickException {
        double value = 0;
        if (dev instanceof TellstickDevice device) {
            String strValue = device.getData();
            if (strValue != null) {
                value = Double.valueOf(strValue);
            }
        }
        int percent = (int) Math.round((value / 255) * 100);
        if (IncreaseDecreaseType.INCREASE == increaseDecreaseType) {
            percent = Math.min(percent + 10, 100);
        } else if (IncreaseDecreaseType.DECREASE == increaseDecreaseType) {
            percent = Math.max(percent - 10, 0);
        }

        dim(dev, new PercentType(percent));
    }

    private void dim(Device dev, PercentType command) throws TellstickException {
        double value = command.doubleValue();

        // 0 means OFF and 100 means ON
        if (value == 0 && dev instanceof SwitchableDevice device) {
            device.off();
        } else if (value == 100 && dev instanceof SwitchableDevice device) {
            device.on();
        } else if (dev instanceof DimmableDevice device) {
            long tdVal = Math.round((value / 100) * 255);
            device.dim((int) tdVal);
        } else {
            throw new TelldusBindingException("Cannot send DIM to " + dev);
        }
    }

    private void turnOff(Device dev) throws TellstickException {
        if (dev instanceof SwitchableDevice device) {
            device.off();
        } else {
            throw new TelldusBindingException("Cannot send OFF to " + dev);
        }
    }

    private void turnOn(Device dev) throws TellstickException {
        if (dev instanceof SwitchableDevice device) {
            device.on();
        } else {
            throw new TelldusBindingException("Cannot send ON to " + dev);
        }
    }

    private void checkLastAndWait(long resendInterval) {
        while ((System.currentTimeMillis() - lastSend) < resendInterval) {
            logger.debug("Wait for {} millisec", resendInterval);
            try {
                Thread.sleep(resendInterval);
            } catch (InterruptedException e) {
                logger.error("Failed to sleep", e);
            }
        }
        lastSend = System.currentTimeMillis();
    }

    /**
     * This class is a worker which execute the commands sent to the TelldusCoreDeviceController.
     * This enables separation between Telldus Core and openHAB for preventing latency on the bus.
     * The Tellstick have a send pace of 4 Hz which is far slower then the bus itself.
     *
     * @author Elias Gabrielsson
     *
     */
    private class TelldusCoreWorker implements Runnable {
        private SortedMap<Device, TelldusCoreSendEvent> messageQue;

        public TelldusCoreWorker(SortedMap<Device, TelldusCoreSendEvent> messageQue) {
            this.messageQue = messageQue;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    TelldusCoreSendEvent sendEvent;
                    // Get event to send
                    synchronized (messageQue) {
                        while (messageQue.isEmpty()) {
                            messageQue.wait();
                        }
                        sendEvent = messageQue.remove(messageQue.firstKey());
                    }
                    // Send event
                    try {
                        sendEvent(sendEvent.getDevice(), sendEvent.getResendCount(), sendEvent.getDimmer(),
                                sendEvent.getCommand());
                    } catch (TellstickException e) {
                        logger.error("Failed to send msg:{} to {}", sendEvent.getCommand(), sendEvent.getDevice(), e);
                    }

                } catch (InterruptedException ie) {
                    break; // Terminate
                }
            }
        }
    }

    /**
     * This is a wrapper class to enable queuing of send events between the controller and the working thread.
     *
     * @author Elias Gabrielsson
     *
     */
    private class TelldusCoreSendEvent implements Comparable<TelldusCoreSendEvent> {
        private Device device;
        private int resendCount;
        private boolean isDimmer;
        private Command command;
        private Long eventTime;

        public TelldusCoreSendEvent(Device device, int resendCount, boolean isDimmer, Command command, Long eventTime) {
            this.device = device;
            this.resendCount = resendCount;
            this.isDimmer = isDimmer;
            this.command = command;
            this.eventTime = eventTime;
        }

        public Device getDevice() {
            return device;
        }

        public int getResendCount() {
            return resendCount;
        }

        public boolean getDimmer() {
            return isDimmer;
        }

        public Command getCommand() {
            return command;
        }

        public Long getEventTime() {
            return eventTime;
        }

        @Override
        public int compareTo(TelldusCoreSendEvent o) {
            return eventTime.compareTo(o.getEventTime());
        }
    }
}
