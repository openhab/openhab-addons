/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.model.cmd;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Queries or updates the module push service.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class PushCommand implements Command {
    private @Nullable String parameter;
    private @Nullable String value;

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
