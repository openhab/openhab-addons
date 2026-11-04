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
package org.openhab.binding.robonect.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.openhab.binding.robonect.internal.model.ExtInfo;
import org.openhab.binding.robonect.internal.model.ModelParser;
import org.openhab.binding.robonect.internal.model.PushInfo;
import org.openhab.binding.robonect.internal.model.cmd.ExtCommand;
import org.openhab.binding.robonect.internal.model.cmd.PushCommand;

import com.google.gson.JsonParseException;

/**
 * API operations for push-service and extension endpoints.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public final class RobonectPushApi {
    private final RobonectTransport transport;

    /**
     * Creates an endpoint API using the shared transport.
     *
     * @param transport the transport used for requests and parsing
     */
    RobonectPushApi(RobonectTransport transport) {
        this.transport = transport;
    }

    /**
     * Reads GPIO and output extension state.
     *
     * @return the extension information and its success status
     */
    public ExtInfo getExtInfo() {
        return ModelParser.parse(transport.sendCommand(new ExtCommand()), ExtInfo.class);
    }

    /**
     * Reads the module push-service settings.
     *
     * @return the push-service information and its success status
     */
    public PushInfo getPushInfo() {
        return ModelParser.parse(transport.sendCommand(new PushCommand()), PushInfo.class);
    }

    /**
     * Updates one push-service property.
     *
     * @param parameter the API parameter name
     * @param value the value to send
     * @return the push-service response and its success status
     */
    public PushInfo updatePush(String parameter, String value) {
        boolean sensitive = "pass".equals(parameter);
        try {
            return ModelParser.parse(transport.sendCommand(new PushCommand().withValue(parameter, value),
                    transport.baseUrl, !sensitive, sensitive), PushInfo.class);
        } catch (JsonParseException e) {
            if (sensitive) {
                throw new RobonectCommunicationException("Could not parse response to push-service password update");
            }
            throw e;
        }
    }

    /**
     * Reads the push-service mode from the unofficial web interface.
     *
     * @return the selected mode, or {@code null} if absent or invalid
     */
    public @Nullable Integer getPushModeViaWebInterface() {
        return requestPushMode(null);
    }

    /**
     * Updates the push-service mode through the unofficial web interface.
     *
     * @param mode the mode value from {@code 0} to {@code 2}
     * @return the selected mode from the response, or {@code null} if absent or invalid
     */
    public @Nullable Integer updatePushModeViaWebInterface(int mode) {
        if (mode < 0 || mode > 2) {
            throw new IllegalArgumentException("Push mode must be between 0 and 2");
        }
        return requestPushMode(mode);
    }

    /**
     * Reads the selected push mode, optionally updating it first.
     *
     * @param mode the mode to set, or {@code null} to read without updating
     * @return the selected mode, or {@code null} if absent or invalid
     */
    private @Nullable Integer requestPushMode(@Nullable Integer mode) {
        // The push settings page can contain credentials, so its response body must not be logged.
        String html = transport.sendCommand(base -> base + "/push" + (mode == null ? "" : "&mode=" + mode),
                transport.webBaseUrl, false);
        Element selected = Jsoup.parse(html).selectFirst("input[type=radio][name=mode][checked=checked]");
        if (selected == null) {
            return null;
        }
        return switch (selected.attr("value")) {
            case "0" -> 0;
            case "1" -> 1;
            case "2" -> 2;
            default -> null;
        };
    }
}
