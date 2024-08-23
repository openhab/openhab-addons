/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.dsmr.internal.device;

import java.util.Objects;
import java.util.concurrent.Semaphore;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dsmr.internal.device.connector.DSMRErrorStatus;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1TelegramListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DSMRDeviceRunnable} runs a {@link DSMRDevice} and blocks until it is restarted or shutdown. If it is
 * restarted it will restart the {@link DSMRDevice}. If it is shutdown the run will end. By using a semaphore to restart
 * and shutdown this class handles the actual {@link DSMRDevice}, while threads calling restart and shutdown can finish
 * fast.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class DSMRDeviceRunnable implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(DSMRDeviceRunnable.class);
    private final Semaphore semaphore = new Semaphore(0);
    private final DSMRDevice device;
    private final P1TelegramListener portEventListener;

    /**
     * Keeps state of running. If false run will stop.
     */
    private boolean running;

    /**
     * Constructor
     *
     * @param device the device to control
     * @param eventListener listener to used ot report errors.
     */
    public DSMRDeviceRunnable(final DSMRDevice device, final P1TelegramListener eventListener) {
        this.device = device;
        this.portEventListener = eventListener;
    }

    /**
     * Sets state to restart the dsmr device.
     */
    public void restart() {
        releaseSemaphore();
    }

    /**
     * Sets state to shutdown the dsmr device.
     */
    public void stop() {
        running = false;
        releaseSemaphore();
    }

    /**
     * Controls the dsmr device. Runs until shutdown.
     */
    @Override
    public void run() {
        try {
            running = true;
            device.start();
            while (running && !Thread.interrupted()) {
                semaphore.acquire();
                // Just drain all other permits to make sure it's not called twice
                semaphore.drainPermits();
                if (running) {
                    logger.trace("Restarting device");
                    device.restart();
                }
            }
            logger.trace("Device shutdown");
        } catch (final RuntimeException e) {
            logger.warn("DSMRDeviceRunnable stopped with a RuntimeException", e);
            portEventListener.onError(DSMRErrorStatus.SERIAL_DATA_READ_ERROR,
                    Objects.requireNonNullElse(e.getMessage(), ""));
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            device.stop();
        }
    }

    /**
     * Wrapper around semaphore to only release when no permits available.
     */
    private void releaseSemaphore() {
        synchronized (semaphore) {
            if (semaphore.availablePermits() == 0) {
                semaphore.release();
            }
        }
    }
}
