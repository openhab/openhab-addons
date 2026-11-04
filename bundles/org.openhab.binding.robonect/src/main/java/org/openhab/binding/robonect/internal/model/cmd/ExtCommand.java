/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model.cmd;

/** Queries optional GPIO and output extension state. */
public class ExtCommand implements Command {
    /**
     * Builds the extension state URL for this {@link Command}.
     *
     * @param baseUrl base URL to which the command parameters are appended
     * @return the complete request URL
     */
    @Override
    public String toCommandURL(String baseUrl) {
        return baseUrl + "?cmd=ext";
    }
}
