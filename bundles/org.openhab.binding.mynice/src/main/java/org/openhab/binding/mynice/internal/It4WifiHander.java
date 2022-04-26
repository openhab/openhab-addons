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
package org.openhab.binding.mynice.internal;

import static org.openhab.core.thing.Thing.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mynice.internal.dto.CommandType;
import org.openhab.binding.mynice.internal.dto.Interface;
import org.openhab.binding.mynice.internal.dto.Response;
import org.openhab.binding.mynice.internal.xml.MyNiceXStream;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link It4WifiHander} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class It4WifiHander extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(It4WifiHander.class);
    // Name for client device
    // Username for client
    String nice_username = "Python";
    // User description
    String nice_descr = "script";
    private final String clientChallenge = Utils.intToHexString(new Random().nextInt());; // UUID.randomUUID().toString().substring(0,
                                                                                          // 8);
    private @Nullable String serverChallenge;
    private @NonNullByDefault({}) MyNiceConfiguration config;
    private @NonNullByDefault({}) It4WifiConnector connector;
    private final MyNiceXStream xstream = new MyNiceXStream();

    public It4WifiHander(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // if (CHANNEL_1.equals(channelUID.getId())) {
        // if (command instanceof RefreshType) {
        // TODO: handle data refresh
        // }

        // TODO: handle command

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information:
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");
        // }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        config = getConfigAs(MyNiceConfiguration.class);
        connector = new It4WifiConnector(config.hostname, config.macAddress, getThing().getUID(), this);
        connector.start();
    }

    public void received(String command) {
        logger.debug("Received : " + command);
        Response response = xstream.deserialize(command);
        try {
            switch (response.type) {
                case PAIR:
                    Configuration thingConfig = editConfiguration();
                    thingConfig.put(MyNiceConfiguration.KEY_PAIR, response.authentication.pwd);
                    updateConfiguration(thingConfig);
                    logger.info("Pairing key updated in Configuration.");
                    connector.buildMessage(CommandType.VERIFY, nice_username);
                    break;
                case VERIFY:
                    switch (response.authentication.perm) {
                        case admin:
                        case user:
                            connector.buildMessage(CommandType.CONNECT, nice_username, clientChallenge);
                            break;
                        case wait:
                            logger.info("Please validate the user on the MyNice application");
                            scheduler.schedule(() -> {
                                try {
                                    connector.buildMessage(CommandType.VERIFY, nice_username);
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }, 5, TimeUnit.SECONDS);
                            break;
                        default:
                            break;

                    }
                    boolean notify = response.authentication.notify;
                case CONNECT:
                    serverChallenge = response.authentication.sc;
                    if (clientChallenge.length() == 8 && serverChallenge != null) {
                        connector.setChallenges(clientChallenge, serverChallenge, config.keyPair);
                        connector.buildMessage(CommandType.INFO, "");
                    }
                    break;
                case INFO:
                    if (thing.getProperties().isEmpty()) {
                        Map<String, String> properties = discoverAttributes(response.intf);
                        updateProperties(properties);
                    }
                    break;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Map<String, String> discoverAttributes(Interface intf) {
        Map<String, String> properties = new HashMap<>();
        properties.put(PROPERTY_VENDOR, intf.manuf);
        properties.put(PROPERTY_MODEL_ID, intf.prod);
        properties.put(PROPERTY_SERIAL_NUMBER, intf.serialNr);
        properties.put(PROPERTY_HARDWARE_VERSION, intf.versionHW);
        properties.put(PROPERTY_FIRMWARE_VERSION, intf.versionFW);
        return properties;
    }

    public void handShaked() {
        updateStatus(ThingStatus.ONLINE);
        try {
            if (config.keyPair.isBlank()) {
                connector.buildMessage(CommandType.PAIR, nice_username, nice_descr);
            } else {
                connector.buildMessage(CommandType.VERIFY, nice_username);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
