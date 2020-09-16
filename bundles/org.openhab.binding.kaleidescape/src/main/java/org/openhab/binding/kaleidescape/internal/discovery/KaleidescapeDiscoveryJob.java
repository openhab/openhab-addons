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
package org.openhab.binding.kaleidescape.internal.discovery;

import static org.openhab.binding.kaleidescape.internal.KaleidescapeBindingConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KaleidescapeDiscoveryJob} class allow manual discovery of
 * Kaleidescape components for a single IP address. This is used
 * for threading to make discovery faster.
 *
 * @author Chris Graham - Initial contribution
 * @author Michael Lobstein - Adapted for the Kaleidescape binding
 * 
 */
@NonNullByDefault
public class KaleidescapeDiscoveryJob implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(KaleidescapeDiscoveryJob.class);

    // Component Types
    private static final String PLAYER = "Player";
    private static final String CINEMA_ONE = "Cinema One";
    private static final String ALTO = "Alto";
    private static final String STRATO = "Strato";
    private static final String STRATO_S = "Strato S";
    private static final String DISC_VAULT = "Disc Vault";

    private static final Set<String> ALLOWED_DEVICES = new HashSet<String>(
            Arrays.asList(PLAYER, CINEMA_ONE, ALTO, STRATO, STRATO_S, DISC_VAULT));

    private KaleidescapeDiscoveryService discoveryClass;

    private ThingTypeUID thingTypeUid = THING_TYPE_PLAYER;
    private String ipAddress = EMPTY;
    private String friendlyName = EMPTY;
    private String serialNumber = EMPTY;

    public KaleidescapeDiscoveryJob(KaleidescapeDiscoveryService service, String ip) {
        this.discoveryClass = service;
        this.ipAddress = ip;
    }

    @Override
    public void run() {
        if (hasKaleidescapeDevice(this.ipAddress)) {
            discoveryClass.submitDiscoveryResults(this.thingTypeUid, this.ipAddress, this.friendlyName,
                    this.serialNumber);
        }
    }

    /**
     * Determines if a Kaleidescape component with a movie player zone is available at a given IP address.
     *
     * @param ip IP address of the Kaleidescape component as a string.
     * @return True if a component is found, false if not.
     */
    private boolean hasKaleidescapeDevice(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);

            if (isKaleidescapeDevice(address, DEFAULT_API_PORT)) {
                return true;
            } else {
                logger.debug("No Kaleidescape component found at IP address ({})", ip);
                return false;
            }
        } catch (UnknownHostException e) {
            logger.debug("Unknown host: {} - {}", ip, e.getMessage());
            return false;
        }
    }

    /**
     * Tries to establish a connection to a hostname and port and then interrogate the component
     *
     * @param host Hostname or IP address to connect to.
     * @param port Port to attempt to connect to.
     * @return True if the component found is one the binding supports
     */
    private boolean isKaleidescapeDevice(InetAddress host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), DISCOVERY_DEFAULT_IP_TIMEOUT_RATE_MS);

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            // query the component to see if it has video zones, the device type, friendly name, and serial number
            writer.println("01/1/GET_NUM_ZONES:");
            writer.println("01/1/GET_DEVICE_TYPE_NAME:");
            writer.println("01/1/GET_FRIENDLY_NAME:");
            writer.println("01/1/GET_DEVICE_INFO:");

            InputStream input = socket.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            String componentType = EMPTY;
            String line;
            String videoZone = null;
            String audioZone = null;
            int lineCount = 0;

            while ((line = reader.readLine()) != null) {
                String[] strArr = line.split(":");

                if (strArr.length >= 4) {
                    switch (strArr[1]) {
                        case "NUM_ZONES":
                            videoZone = strArr[2];
                            audioZone = strArr[3];
                            break;
                        case "DEVICE_TYPE_NAME":
                            componentType = strArr[2];
                            break;
                        case "FRIENDLY_NAME":
                            friendlyName = strArr[2];
                            break;
                        case "DEVICE_INFO":
                            serialNumber = strArr[3].trim(); // take off leading zeros
                            break;
                    }
                } else {
                    logger.debug("isKaleidescapeDevice() - Unable to process line: {}", line);
                }

                lineCount++;

                // stop after reading four lines
                if (lineCount > 3) {
                    break;
                }
            }

            // see if we have a video zone
            if ("01".equals(videoZone)) {
                // now check if we are one of the allowed types
                if (ALLOWED_DEVICES.contains(componentType)) {
                    if (STRATO_S.equals(componentType) || STRATO.equals(componentType)) {
                        thingTypeUid = THING_TYPE_STRATO;
                        return true;
                    }

                    // A 'Player' without an audio zone is really a Strato C
                    // does not work yet, Strato C erroneously reports "01" for audio zones
                    // so we are unable to differentiate a Strato C from a Premiere player
                    if ("00".equals(audioZone) && PLAYER.equals(componentType)) {
                        thingTypeUid = THING_TYPE_STRATO;
                        return true;
                    }

                    // Alto
                    if (ALTO.equals(componentType)) {
                        thingTypeUid = THING_TYPE_ALTO;
                        return true;
                    }

                    // Cinema One
                    if (CINEMA_ONE.equals(componentType)) {
                        thingTypeUid = THING_TYPE_CINEMA_ONE;
                        return true;
                    }

                    // A Disc Vault with a video zone (the M700 vault), just call it a THING_TYPE_PLAYER
                    if (DISC_VAULT.equals(componentType)) {
                        thingTypeUid = THING_TYPE_PLAYER;
                        return true;
                    }

                    // default returns THING_TYPE_PLAYER
                    return true;
                }
            }
        } catch (IOException e) {
            logger.debug("isKaleidescapeDevice() IOException: {}", e.getMessage());
            return false;
        }

        return false;
    }
}
