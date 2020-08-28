/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.gce.internal.handler;

import static org.openhab.binding.gce.internal.GCEBindingConstants.*;

import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles message translation to and from the IPX.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Ipx800MessageParser {
    private final Logger logger = LoggerFactory.getLogger(Ipx800MessageParser.class);
    private static final String IO_DESCRIPTOR = "(\\d{32})";
    private static final Pattern IO_PATTERN = Pattern.compile(IO_DESCRIPTOR);
    private static final Pattern VALIDATION_PATTERN = Pattern
            .compile("I=" + IO_DESCRIPTOR + "&O=" + IO_DESCRIPTOR + "&([AC]\\d{1,2}=\\d+&)*[^I]*");

    private String expectedResponse = "";
    private final Ipx800DeviceConnector connector;

    private final Optional<Ipx800EventListener> listener;

    public Ipx800MessageParser(Ipx800DeviceConnector connector, @Nullable Ipx800EventListener ipx800EventListener) {
        this.connector = connector;
        connector.setParser(this);
        if (ipx800EventListener != null) {
            this.listener = Optional.of(ipx800EventListener);
        } else {
            this.listener = Optional.empty();
        }
    }

    /**
     * Set output of the device sending the corresponding command
     *
     * @param targetPort
     * @param targetValue
     */
    public void setOutput(String targetPort, int targetValue, boolean pulse) {
        logger.debug("Sending {} to {}", targetValue, targetPort);
        int port = Integer.parseInt(targetPort);
        String command = String.format("Set%02d%d%s", port, targetValue, pulse ? "p" : "");
        connector.send(command);
    }

    /**
     *
     * @param data
     */
    public void unsollicitedUpdate(String data) {
        if (IO_PATTERN.matcher(data).matches()) {
            String portKind = "GetOutputs".equalsIgnoreCase(expectedResponse) ? RELAY_OUTPUT
                    : "GetInputs".equalsIgnoreCase(expectedResponse) ? DIGITAL_INPUT : null;
            if (portKind != null) {
                for (int count = 0; count < data.length(); count++) {
                    setStatus(portKind + String.valueOf(count + 1), new Double(data.charAt(count) - '0'));
                }
            }
        } else if (VALIDATION_PATTERN.matcher(data).matches()) {
            for (String status : data.split("&")) {
                String statusPart[] = status.split("=");
                String portKind = statusPart[0].substring(0, 1);
                int portNumShift = 0;
                switch (portKind) {
                    case DIGITAL_INPUT:
                    case RELAY_OUTPUT: {
                        for (int count = 0; count < statusPart[1].length(); count++) {
                            setStatus(portKind + String.valueOf(count + 1),
                                    new Double(statusPart[1].charAt(count) - '0'));
                        }
                        break;
                    }
                    case COUNTER:
                        portNumShift = -1; // Align counters on 1 based array
                    case ANALOG_INPUT: {
                        int portNumber = Integer.parseInt(statusPart[0].substring(1));
                        setStatus(portKind + String.valueOf(portNumber + portNumShift + 1),
                                Double.parseDouble(statusPart[1]));
                    }
                }
            }
        } else if (!"".equals(expectedResponse)) {
            setStatus(expectedResponse, Double.parseDouble(data));
        }

        expectedResponse = "";
    }

    private void setStatus(String port, Double value) {
        logger.debug("Received {} : {}", port, value.toString());
        listener.ifPresent(l -> l.dataReceived(port, value));
    }

    public void setExpectedResponse(String expectedResponse) {
        if (expectedResponse.endsWith("s")) { // GetInputs or GetOutputs
            this.expectedResponse = expectedResponse;
        } else { // GetAnx or GetCountx
            this.expectedResponse = expectedResponse.replaceAll("GetAn", ANALOG_INPUT).replaceAll("GetCount", COUNTER)
                    .replaceAll("GetIn", DIGITAL_INPUT).replaceAll("GetOut", RELAY_OUTPUT);
        }
    }

    /**
     * Resets the counter value to 0
     *
     * @param targetCounter
     */
    public void resetCounter(int targetCounter) {
        logger.debug("Resetting counter {} to 0", targetCounter);
        connector.send(String.format("ResetCount%d", targetCounter));
        try {
            Thread.sleep(200);
            String request = String.format("GetCount%d", targetCounter);
            setExpectedResponse(request);
            connector.send(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void errorOccurred(Exception e) {
        logger.warn("Error received from connector : {}", e.getMessage());
        listener.ifPresent(l -> l.errorOccurred(e));
    }

    public synchronized void getValue(String channelId) {
        logger.debug("Requested value for {}", channelId);
        if ("".equals(expectedResponse)) { // Do not send a request is something is expected
            String[] elements = channelId.split("#");
            String command = "Get" + elements[0].replaceAll(ANALOG_INPUT, "An").replaceAll(COUNTER, "Count")
                    .replaceAll(DIGITAL_INPUT, "An").replaceAll(RELAY_OUTPUT, "Out") + elements[1];
            setExpectedResponse(command);
            connector.send(command);
        }
    }

    public void getOutputs() {
        String command = "GetOutputs";
        setExpectedResponse(command);
        connector.send(command);
    }

    public void getInputs() {
        String command = "GetInputs";
        setExpectedResponse(command);
        connector.send(command);
    }

    public void reset() {
        connector.send("Reset");
    }
}
