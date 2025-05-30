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
package org.openhab.binding.gce.internal.model;

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.gce.internal.handler.Ipx800EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles message translation to and from the IPX.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class M2MMessageParser {
    private static final String IO_DESCRIPTOR = "(\\d{32})";
    private static final Pattern IO_PATTERN = Pattern.compile(IO_DESCRIPTOR);
    private static final Pattern VALIDATION_PATTERN = Pattern
            .compile("I=" + IO_DESCRIPTOR + "&O=" + IO_DESCRIPTOR + "&([AC]\\d{1,2}=\\d+&)*[^I]*");

    private final Logger logger = LoggerFactory.getLogger(M2MMessageParser.class);
    private final Ipx800EventListener listener;

    private String expectedResponse = "";

    public M2MMessageParser(Ipx800EventListener listener) {
        this.listener = listener;
    }

    public void unsolicitedUpdate(String data) {
        if ("OK".equals(data)) { // If OK, do nothing special
        } else if ("? Bad command".equals(data)) {
            logger.warn("{}", data);
        } else if (IO_PATTERN.matcher(data).matches()) {
            PortDefinition portDefinition = PortDefinition.fromM2MCommand(expectedResponse);
            decodeDataLine(portDefinition, data);
        } else if (VALIDATION_PATTERN.matcher(data).matches()) {
            for (String status : data.split("&")) {
                String[] statusPart = status.split("=");
                int portNumShift = 1;
                PortDefinition portDefinition = PortDefinition.fromPortName(statusPart[0].substring(0, 1));
                switch (portDefinition) {
                    case CONTACT:
                    case RELAY: {
                        decodeDataLine(portDefinition, statusPart[1]);
                        break;
                    }
                    case COUNTER:
                        portNumShift = 0; // Align counters on 1 based array
                    case ANALOG: {
                        int portNumber = Integer.parseInt(statusPart[0].substring(1)) + portNumShift;
                        setStatus(portDefinition.portName + portNumber, Double.parseDouble(statusPart[1]));
                    }
                }
            }
        } else if (!expectedResponse.isEmpty()) {
            setStatus(expectedResponse, Double.parseDouble(data));
        } else {
            logger.warn("Unable to handle data received: {}", data);
        }
        expectedResponse = "";
    }

    private void decodeDataLine(PortDefinition portDefinition, String data) {
        for (int count = 0; count < data.length(); count++) {
            setStatus(portDefinition.portName + (count + 1), (double) data.charAt(count) - '0');
        }
    }

    private void setStatus(String port, double value) {
        logger.debug("Received {} on port {}", value, port);
        listener.dataReceived(port, value);
    }

    public void setExpectedResponse(String expectedResponse) {
        if (expectedResponse.endsWith("s")) { // GetInputs or GetOutputs
            this.expectedResponse = expectedResponse;
            return;
        }
        // GetAnx or GetCountx
        PortDefinition portType = PortDefinition.fromM2MCommand(expectedResponse);
        this.expectedResponse = expectedResponse.replaceAll(portType.m2mCommand, portType.portName);
    }
}
