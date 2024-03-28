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
package org.openhab.binding.lgthinq.lgservices.model;

/**
 * The {@link MonitoringResultFormat}
 *
 * @author Nemer Daud - Initial contribution
 */
public enum MonitoringResultFormat {
    JSON_FORMAT(""),
    BINARY_FORMAT("BINARY(BYTE)"),
    UNKNOWN_FORMAT("UNKNOWN_FORMAT");

    final String format;

    MonitoringResultFormat(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    public static MonitoringResultFormat getFormatOf(String formatValue) {
        switch (formatValue) {
            case "BINARY(BYTE)":
                return BINARY_FORMAT;
            case "JSON":
            case "Full ModelJson":
            case "RAC ModelJson":
                return JSON_FORMAT;
            default:
                return UNKNOWN_FORMAT;

        }
    }
}
