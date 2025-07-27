/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.bambulab.internal;

import static org.openhab.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;
import static pl.grzeslowski.jbambuapi.camera.CameraConfig.BAMBU_CERTIFICATE;
import static pl.grzeslowski.jbambuapi.mqtt.PrinterClientConfig.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import pl.grzeslowski.jbambuapi.camera.CameraConfig;

/**
 * The {@link PrinterConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class PrinterConfiguration {
    public Series series = Series.A;
    public String serial = "";
    public String hostname = "";
    public String accessCode = "";
    public String username = LOCAL_USERNAME;
    public int reconnectTime = 300;
    public int reconnectMax = 5;
    // MQTT
    public String scheme = SCHEME;
    public int port = DEFAULT_PORT;
    // Camera
    public int cameraPort = CameraConfig.DEFAULT_PORT;
    public List<String> cameraCertificate = List.of(BAMBU_CERTIFICATE.split("\n"));

    public void validateSerial() throws InitializationException {
        if (serial.isBlank()) {
            throw new InitializationException(CONFIGURATION_ERROR, "@text/printer.handler.init.noSerial");
        }
    }

    public void validateHostname() throws InitializationException {
        if (hostname.isBlank()) {
            throw new InitializationException(CONFIGURATION_ERROR, "@text/printer.handler.init.noHostname");
        }
    }

    public void validateAccessCode() throws InitializationException {
        if (accessCode.isBlank()) {
            throw new InitializationException(CONFIGURATION_ERROR, "@text/printer.handler.init.noAccessCode");
        }
    }

    public void validateUsername() {
        if (username.isBlank()) {
            username = LOCAL_USERNAME;
        }
    }

    public URI buildUri() throws InitializationException {
        var rawUri = "%s%s:%d".formatted(scheme, hostname, port);
        try {
            return new URI(rawUri);
        } catch (URISyntaxException e) {
            throw new InitializationException(CONFIGURATION_ERROR,
                    "@text/printer.handler.init.invalidHostname [\"%s\"]".formatted(rawUri), e);
        }
    }

    public static enum Series {
        A,
        P,
        X
    }
}
