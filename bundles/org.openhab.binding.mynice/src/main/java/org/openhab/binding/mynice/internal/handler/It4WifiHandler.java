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
package org.openhab.binding.mynice.internal.handler;

import static org.openhab.core.thing.Thing.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mynice.internal.config.It4WifiConfiguration;
import org.openhab.binding.mynice.internal.discovery.MyNiceDiscoveryService;
import org.openhab.binding.mynice.internal.xml.It4WifiConnector;
import org.openhab.binding.mynice.internal.xml.It4WifiSession;
import org.openhab.binding.mynice.internal.xml.MyNiceXStream;
import org.openhab.binding.mynice.internal.xml.dto.CommandType;
import org.openhab.binding.mynice.internal.xml.dto.Device;
import org.openhab.binding.mynice.internal.xml.dto.Response;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link It4WifiHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class It4WifiHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(It4WifiHandler.class);
    private final List<MyNiceDataListener> dataListeners = new CopyOnWriteArrayList<>();

    private @NonNullByDefault({}) It4WifiConfiguration config;
    private @NonNullByDefault({}) It4WifiConnector connector;
    private final MyNiceXStream xstream = new MyNiceXStream();
    private final It4WifiSession session = new It4WifiSession();
    private List<Device> devices = List.of();

    public It4WifiHandler(Bridge thing) {
        super(thing);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(MyNiceDiscoveryService.class);
    }

    public boolean registerDataListener(MyNiceDataListener dataListener) {
        boolean result = dataListeners.add(dataListener);
        notifiyListeners(devices);
        return result;
    }

    public boolean unregisterDataListener(MyNiceDataListener dataListener) {
        return dataListeners.remove(dataListener);
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
        config = getConfigAs(It4WifiConfiguration.class);
        connector = new It4WifiConnector(config.hostname, config.macAddress, session, this);
        connector.start();
    }

    public void received(String command) {
        logger.debug("Received : {}", command);
        Response response = xstream.deserialize(command);
        try {
            if (response.error != null) {
                logger.warn("Error code {} received : {}", response.error.code, response.error.info);
            } else {
                switch (response.type) {
                    case PAIR:
                        Configuration thingConfig = editConfiguration();
                        thingConfig.put(It4WifiConfiguration.PASSWORD, response.authentication.pwd);
                        updateConfiguration(thingConfig);
                        logger.info("Pairing key updated in Configuration.");
                        connector.buildMessage(CommandType.VERIFY);
                        break;
                    case VERIFY:
                        switch (response.authentication.perm) {
                            case admin:
                            case user:
                                connector.buildMessage(CommandType.CONNECT);
                                break;
                            case wait:
                                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                                        "Please validate the user on the MyNice application");
                                scheduler.schedule(() -> handShaked(), 5, TimeUnit.SECONDS);
                                break;
                            default:
                                break;
                        }
                        boolean notify = response.authentication.notify;
                    case CONNECT:
                        String sc = response.authentication.sc;
                        if (sc != null) {
                            session.setChallenges(sc, response.authentication.id, config.password);
                            connector.buildMessage(CommandType.INFO);
                        }
                        break;
                    case INFO:
                        updateStatus(ThingStatus.ONLINE);
                        if (thing.getProperties().isEmpty()) {
                            Map<String, String> properties = Map.of(PROPERTY_VENDOR, response.intf.manuf,
                                    PROPERTY_MODEL_ID, response.intf.prod, PROPERTY_SERIAL_NUMBER,
                                    response.intf.serialNr, PROPERTY_HARDWARE_VERSION, response.intf.versionHW,
                                    PROPERTY_FIRMWARE_VERSION, response.intf.versionFW);
                            updateProperties(properties);
                        }
                        notifiyListeners(response.getDevices());
                        break;
                    case STATUS:
                        notifiyListeners(response.getDevices());
                        break;
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
        }
    }

    private void notifiyListeners(List<Device> list) {
        devices = list;
        dataListeners.forEach(listener -> listener.onDataFetched(thing.getUID(), devices));
    }

    public void handShaked() {
        request(config.password.isBlank() ? CommandType.PAIR : CommandType.VERIFY);
    }

    public void request(CommandType command) {
        try {
            connector.buildMessage(command);
        } catch (Exception e) {
            // TODO Auto-generated catch block
        }
    }
}
