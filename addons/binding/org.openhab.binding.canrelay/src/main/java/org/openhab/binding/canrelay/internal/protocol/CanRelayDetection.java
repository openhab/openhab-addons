/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canrelay.internal.protocol;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.canrelay.internal.canbus.CanMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for data needed in the can relay detection. Used to hold information needed during the detection phase
 * between the possibly blocked/waiting threads and various listeners for events
 *
 * @author Lubos Housa - Initial contribution
 */
@NonNullByDefault
public class CanRelayDetection {
    private static final Logger logger = LoggerFactory.getLogger(CanRelayDetection.class);

    /**
     * how long to wait for the device to get ready from when this access method was called. Longer time to allow any
     * user actions like enabling the HW bridge.
     */
    private static final int DEVICE_READY_TIMEOUT_MINUTES = 2;

    /**
     * How long to wait for CANBUS message replies over CANBUS
     */
    private static final int CANREPLY_TIMEOUT_SECONDS = 2;

    /**
     * Locks used in detect configurations. Multiple conditions have to be met in order for the detection to finish
     */
    private final Lock lock = new ReentrantLock();
    private final Condition deviceReadyCondition = lock.newCondition();
    private final Condition mappingCondition = lock.newCondition();
    private final Condition outputsCondition = lock.newCondition();

    /**
     * Data needed by the various phases in the detection sequence
     */
    @NonNullByDefault({})
    private Floor floor;
    // the likelihood of concurrent access is fairly low, but in theory can happen (multiple wrong mappings sent with
    // markers before last message and this main thread still being very slow in processing (longer than a CANBUS
    // message transmit time which is a few ms;
    private final Queue<CanMessage> canMessages = new ConcurrentLinkedQueue<>();

    /**
     * Actual status of this detection, what phase are we at
     */
    private Status status = Status.IDLE;

    /**
     *
     * /**
     * Light state cache with distinct values for all floors.
     */
    @NonNullByDefault({})
    private final LightStateCache lightStateCache = new LightStateCache();

    public Floor getFloor() {
        return floor;
    }

    public void waitForDevice() {
        this.status = Status.WAITING_DEVICE;
        runInLock(() -> {
            deviceReadyCondition.await(DEVICE_READY_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        });
    }

    public void waitForMappingReplies() {
        this.status = Status.WAITING_MAPPINGS;
        runInLock(() -> {
            mappingCondition.await(CANREPLY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        });
    }

    public void waitForOutputsReply() {
        this.status = Status.WAITING_OUTPUS;
        runInLock(() -> {
            outputsCondition.await(CANREPLY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        });
    }

    public void signalDeviceReady() {
        runInLock(() -> {
            deviceReadyCondition.signalAll();
        });
    }

    public void signalMappingReplies() {
        runInLock(() -> {
            mappingCondition.signalAll();
        });
    }

    public void signalOutputsReply() {
        runInLock(() -> {
            outputsCondition.signalAll();
        });
    }

    public void processingMappingReplies() {
        this.status = Status.PROCESSING_MAPPINGS;
    }

    public void processingOutputReply() {
        this.status = Status.PROCESSING_OUTPUTS;
    }

    public void finishedProcessingFloor() {
        this.status = Status.FINISHED_PROCESSING_FLOOR;
    }

    /**
     * Finish detection
     */
    public void finish() {
        this.status = Status.FINISHED;
        this.canMessages.clear();
        this.floor = null;
    }

    /**
     * Start detection process for a given floor
     *
     * @param floor floor to store internally during the detection process
     */
    public void start(Floor floor) {
        this.status = Status.START_PROCESSING_FLOOR;
        this.canMessages.clear();
        this.floor = floor;
    }

    /**
     * Cancel any potential currently running detection. Handy to trigger e.g. during disconnect of the device since we
     * use long timeout to wait for the user/device. Here we simply try to wake up any of the conditions and set a
     * special status to let the other awaken thread know to exit its current detection right after waking up
     */
    public void cancel() {
        // only cancel if it is indeed processing, otherwise leave things as they are
        if (status.isProcessing()) {
            this.status = Status.CANCELLED;
            runInLock(() -> {
                deviceReadyCondition.signalAll();
                mappingCondition.signalAll();
                outputsCondition.signalAll();
            });
        }
    }

    /**
     * Clears the cache
     */
    public void clear() {
        lightStateCache.clear();
    }

    /**
     * Detects whether the underlying cache of light is empty or not
     *
     * @return true if so, false otherwise
     */
    public boolean isCacheEmpty() {
        return lightStateCache.isEmpty();
    }

    /**
     * Get all received can messages. Mind that the returned queue is thread safe.
     *
     * @return queue with all detected CanMessages
     */
    public Queue<CanMessage> getReceivedCanMessages() {
        return canMessages;
    }

    public void canMessageReceived(CanMessage canMessage) {
        this.canMessages.add(canMessage);
    }

    public Status getStatus() {
        return status;
    }

    public void runInLock(RunnableInterruptible logic) {
        lock.lock();
        try {
            logic.run();
        } catch (InterruptedException e) {
            logger.warn("Interrupted while running a logic in the lock during detection.", e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get light state cache
     *
     * @return requested light state cache
     */
    public LightStateCache getLightStateCache() {
        return lightStateCache;
    }

    public enum Status {
        CANCELLED,
        FINISHED,
        IDLE,
        START_PROCESSING_FLOOR,
        WAITING_DEVICE,
        WAITING_MAPPINGS,
        PROCESSING_MAPPINGS,
        WAITING_OUTPUS,
        PROCESSING_OUTPUTS,
        FINISHED_PROCESSING_FLOOR;

        public boolean isProcessing() {
            return this.compareTo(IDLE) > 0;
        }
    }
}
