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
package org.openhab.binding.metofficedatahub.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Implementations of this interface, allow the responses of a SiteAPI request to
 * be processed
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public interface ISiteResponseListener {

    /**
     * This is invoked to notify implementations of this interface, new daily response data has been received.
     * It is at the implementations discretion whether the data is of interest based on the pollId, which is set
     * from when the original poll was requested.
     *
     * @param content is the daily response JSON content returned for a site API request.
     * @param pollId is the ID associated to the request this was requested with.
     */
    void processDailyResponse(final String content, final String pollId);

    /**
     * This is invoked to notify implementations of this interface, new hourly response data has been received.
     * It is at the implementations discretion whether the data is of interest based on the pollId, which is set
     * from when the original poll was requested.
     *
     * @param content is the hourly response JSON content returned for a site API request.
     * @param pollId is the ID associated to the request this was requested with.
     */
    void processHourlyResponse(final String content, final String pollId);
}
