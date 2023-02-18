/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package main.java.org.openhab.binding.metofficedatahub.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MetOfficeDataHubSiteApiHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class RequestLimiter {

    public static final int INVALID_REQUEST_ID = -1;

    int requestLimit = 0;
    int currentRequestCount = 0;

    public int getCurrentRequestCount() {
        return currentRequestCount;
    }

    public RequestLimiter() {
    }

    public void resetLimiter() {
        resetLimiter(requestLimit);
    }

    public synchronized void resetLimiter(int newLimit) {
        requestLimit = newLimit;
        currentRequestCount = 0;
    }

    public synchronized void updateLimit(int newLimit) {
        requestLimit = newLimit;
    }

    public synchronized int getRequestId() {
        final int requestId = currentRequestCount;
        ++currentRequestCount;
        if (currentRequestCount > requestLimit) {
            return INVALID_REQUEST_ID;
        }
        return requestId;
    }

    public boolean isInvalidRequestId(final int requestId) {
        return INVALID_REQUEST_ID == requestId;
    }
}
