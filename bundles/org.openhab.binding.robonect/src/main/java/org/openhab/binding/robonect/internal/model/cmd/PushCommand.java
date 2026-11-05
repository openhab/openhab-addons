/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model.cmd;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Queries or updates the module push service.
 *
 * @author Nico Brüttner - Initial contribution
 */
public class PushCommand implements Command {
    private String parameter;
    private String value;

    /**
     * Sets a push-service property.
     *
     * @param parameter property name
     * @param value property value
     * @return this command
     */
    public PushCommand withValue(String parameter, String value) {
        this.parameter = parameter;
        this.value = value;
        return this;
    }

    /**
     * Builds the push service URL for this {@link Command}.
     *
     * @param baseUrl base URL to which the command parameters are appended
     * @return the complete request URL
     */
    @Override
    public String toCommandURL(String baseUrl) {
        if (parameter == null || value == null) {
            return baseUrl + "?cmd=push";
        }
        return baseUrl + "?cmd=push&" + parameter + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
