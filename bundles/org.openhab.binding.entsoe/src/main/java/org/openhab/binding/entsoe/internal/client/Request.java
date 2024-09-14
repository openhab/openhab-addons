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
package org.openhab.binding.entsoe.internal.client;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Miika Jukka - Initial contribution
 *
 */
@NonNullByDefault
public class Request {

    private static DateTimeFormatter REQUEST_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private static final String BASE_URL = "https://web-api.tp.entsoe.eu/api?";
    private static final String DOCUMENT_TYPE_PRICE = "A44";

    private final String securityToken;
    private final String area;
    private final ZonedDateTime periodStart;
    private final ZonedDateTime periodEnd;

    public Request(String securityToken, String area, ZonedDateTime periodStart, ZonedDateTime periodEnd) {
        this.securityToken = securityToken;
        this.area = area;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    // @formatter:off
    public String toUrl() {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL)
                .append("securityToken=").append(securityToken)
                .append("&documentType=").append(DOCUMENT_TYPE_PRICE)
                .append("&in_domain=").append(area)
                .append("&out_domain=").append(area)
                .append("&periodStart=").append(periodStart.format(REQUEST_FORMAT))
                .append("&periodEnd=").append(periodEnd.format(REQUEST_FORMAT));
        return urlBuilder.toString();
    }

    @Override
    public String toString() {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL)
                .append("securityToken=").append("xxxxx-xxxxx-xxxxx")
                .append("&documentType=").append(DOCUMENT_TYPE_PRICE)
                .append("&in_domain=").append(area)
                .append("&out_domain=").append(area)
                .append("&periodStart=").append(periodStart.format(REQUEST_FORMAT))
                .append("&periodEnd=").append(periodEnd.format(REQUEST_FORMAT));
        return urlBuilder.toString();
     // @formatter:on
    }
}
