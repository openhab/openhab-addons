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
package org.openhab.binding.lcn.internal.connection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages timeout and retry logic for an LCN request.
 *
 * @author Tobias Jüttner - Initial Contribution
 * @author Fabian Wolter - Migration to OH2
 */
@NonNullByDefault
public class RequestStatus {
    private final Logger logger = LoggerFactory.getLogger(RequestStatus.class);
    /** Interval for forced updates. -1 if not used. */
    private volatile long maxAgeMSec;

    /** Tells how often a request will be sent if no response was received. */
    private final int numTries;

    /** true if request logic is activated. */
    private volatile boolean isActive;

    /** The time the current request was sent out or 0. */
    private volatile long currRequestTimeStamp;

    /** The time stamp of the next scheduled request or 0. */
    private volatile long nextRequestTimeStamp;

    /** Number of retries left until the request is marked as failed. */
    private volatile int numRetriesLeft;
    private final String label;

    /**
     * Constructor.
     *
     * @param maxAgeMSec the forced-updates interval (-1 if not used)
     * @param numTries the maximum number of tries until the request is marked as failed
     */
    RequestStatus(long maxAgeMSec, int numTries, String label) {
        this.maxAgeMSec = maxAgeMSec;
        this.numTries = numTries;
        this.label = label;
        this.reset();
    }

    /** Resets the runtime data to the initial states. */
    public synchronized void reset() {
        this.isActive = false;
        this.currRequestTimeStamp = 0;
        this.nextRequestTimeStamp = 0;
        this.numRetriesLeft = 0;
    }

    /**
     * Checks whether the request logic is active.
     *
     * @return true if active
     */
    public boolean isActive() {
        return this.isActive;
    }

    /**
     * Checks whether a request is waiting for a response.
     *
     * @return true if waiting for a response
     */
    boolean isPending() {
        return this.currRequestTimeStamp != 0;
    }

    /**
     * Checks whether the request is active and ran into timeout while waiting for a response.
     *
     * @param timeoutMSec the timeout in milliseconds
     * @param currTime the current time stamp
     * @return true if request timed out
     */
    synchronized boolean isTimeout(long timeoutMSec, long currTime) {
        return this.isPending() && currTime - this.currRequestTimeStamp >= timeoutMSec;
    }

    /**
     * Checks for failed requests (active and out of retries).
     *
     * @param timeoutMSec the timeout in milliseconds
     * @param currTime the current time stamp
     * @return true if no response was received and no retries are left
     */
    synchronized boolean isFailed(long timeoutMSec, long currTime) {
        return this.isTimeout(timeoutMSec, currTime) && this.numRetriesLeft == 0;
    }

    /**
     * Schedules the next request.
     *
     * @param delayMSec the delay in milliseconds
     * @param currTime the current time stamp
     */
    public synchronized void nextRequestIn(long delayMSec, long currTime) {
        this.isActive = true;
        this.nextRequestTimeStamp = currTime + delayMSec;
    }

    /**
     * Schedules a request to retrieve the current value.
     */
    public synchronized void refresh() {
        nextRequestIn(0, System.currentTimeMillis());
        this.numRetriesLeft = this.numTries;
    }

    /**
     * Checks whether sending a new request is required (should be called periodically).
     *
     * @param timeoutMSec the time to wait for a response before retrying the request
     * @param currTime the current time stamp
     * @return true to indicate a new request should be sent
     * @throws LcnException when a status request timed out
     */
    synchronized boolean shouldSendNextRequest(long timeoutMSec, long currTime) throws LcnException {
        if (this.isActive) {
            if (this.nextRequestTimeStamp != 0 && currTime >= this.nextRequestTimeStamp) {
                return true;
            }
            // Retry of current request (after no response was received)
            if (this.isTimeout(timeoutMSec, currTime)) {
                if (this.numRetriesLeft > 0) {
                    return true;
                } else if (isPending()) {
                    currRequestTimeStamp = 0;
                    throw new LcnException(label + ": Failed finally after " + numTries + " tries");
                }
            }
        }
        return false;
    }

    /**
     * Must be called right after a new request has been sent.
     * Must be activated first.
     *
     * @param currTime the current time stamp
     */
    public synchronized void onRequestSent(long currTime) {
        if (!this.isActive) {
            logger.warn("Tried to send a request which is not active");
        }
        // Updates retry counter
        if (this.currRequestTimeStamp == 0) {
            this.numRetriesLeft = this.numTries - 1;
        } else if (this.numRetriesLeft > 0) { // Should not happen if used correctly
            --this.numRetriesLeft;
        }
        // Mark request as pending
        this.currRequestTimeStamp = currTime;
        // Schedule next request
        if (this.maxAgeMSec != -1) {
            this.nextRequestIn(this.maxAgeMSec, currTime);
        } else {
            this.nextRequestTimeStamp = 0;
        }
    }

    /** Must be called when a response (requested or not) has been received. */
    public synchronized void onResponseReceived() {
        if (this.isActive) {
            this.currRequestTimeStamp = 0; // Mark request (if any) as successful

            // Reset timer for next transmission
            if (this.maxAgeMSec != -1) {
                this.nextRequestIn(this.maxAgeMSec, System.currentTimeMillis());
            }
        }
    }

    /**
     * Sets the timeout of this RequestStatus.
     *
     * @param maxAgeMSec the timeout in ms
     */
    public void setMaxAgeMSec(long maxAgeMSec) {
        this.maxAgeMSec = maxAgeMSec;
    }
}
