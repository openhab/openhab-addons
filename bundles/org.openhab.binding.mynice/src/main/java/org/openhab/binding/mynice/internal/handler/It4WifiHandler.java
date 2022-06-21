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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mynice.internal.config.It4WifiConfiguration;
import org.openhab.binding.mynice.internal.discovery.MyNiceDiscoveryService;
import org.openhab.binding.mynice.internal.xml.MyNiceXStream;
import org.openhab.binding.mynice.internal.xml.RequestBuilder;
import org.openhab.binding.mynice.internal.xml.dto.CommandType;
import org.openhab.binding.mynice.internal.xml.dto.Device;
import org.openhab.binding.mynice.internal.xml.dto.Event;
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
    private @NonNullByDefault({}) RequestBuilder reqBuilder;
    private final MyNiceXStream xstream = new MyNiceXStream();
    private List<Device> devices = new ArrayList<>();
    private boolean connected;

    public It4WifiHandler(Bridge thing) {
        super(thing);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(MyNiceDiscoveryService.class);
    }

    public void registerDataListener(MyNiceDataListener dataListener) {
        dataListeners.add(dataListener);
        notifyListeners(devices);
    }

    public void unregisterDataListener(MyNiceDataListener dataListener) {
        dataListeners.remove(dataListener);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // we do not handle commands
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        config = getConfigAs(It4WifiConfiguration.class);
        connector = new It4WifiConnector(config.hostname, this, scheduler);
        connected = false;
        if (config.username.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Please define a username for this thing");
        } else {
            reqBuilder = new RequestBuilder(config.macAddress, config.username);
            connector.start();
        }
    }

    public void received(String command) {
        logger.debug("Received : {}", command);
        Event event = xstream.deserialize(command);
        if (event.error != null) {
            logger.warn("Error code {} received : {}", event.error.code, event.error.info);
        } else {
            if (event instanceof Response) {
                handleResponse((Response) event);
            } else {
                notifyListeners(event.getDevices());
            }
        }
    }

    private void handleResponse(Response response) {
        switch (response.type) {
            case PAIR:
                Configuration thingConfig = editConfiguration();
                thingConfig.put(It4WifiConfiguration.PASSWORD, response.authentication.pwd);
                updateConfiguration(thingConfig);
                logger.info("Pairing key updated in Configuration.");
                sendCommand(CommandType.VERIFY);
                return;
            case VERIFY:
                if (connected) {
                    return;
                }
                switch (response.authentication.perm) {
                    case admin:
                    case user:
                        sendCommand(CommandType.CONNECT);
                        return;
                    case wait:
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                                "Please validate the user on the MyNice application");
                        scheduler.schedule(() -> handShaked(), 15, TimeUnit.SECONDS);
                        return;
                    default:
                        return;
                }
            case CONNECT:
                String sc = response.authentication.sc;
                if (sc != null) {
                    reqBuilder.setChallenges(sc, response.authentication.id, config.password);
                    connected = true;
                    sendCommand(CommandType.INFO);
                }
                return;
            case INFO:
                updateStatus(ThingStatus.ONLINE);
                if (thing.getProperties().isEmpty()) {
                    Map<String, String> properties = Map.of(PROPERTY_VENDOR, response.intf.manuf, PROPERTY_MODEL_ID,
                            response.intf.prod, PROPERTY_SERIAL_NUMBER, response.intf.serialNr,
                            PROPERTY_HARDWARE_VERSION, response.intf.versionHW, PROPERTY_FIRMWARE_VERSION,
                            response.intf.versionFW);
                    updateProperties(properties);
                }
                notifyListeners(response.getDevices());
                return;
            case STATUS:
                notifyListeners(response.getDevices());
                break;
            case CHANGE:
                logger.debug("Change command accepted");
                break;
            default:
                logger.info("Unhandled response type : {}", response.type);
        }
    }

    public void handShaked() {
        config = getConfigAs(It4WifiConfiguration.class);
        sendCommand(config.password.isBlank() ? CommandType.PAIR : CommandType.VERIFY);
    }

    private void notifyListeners(List<Device> list) {
        devices = list;
        dataListeners.forEach(listener -> listener.onDataFetched(devices));
    }

    public void sendCommand(CommandType command) {
        connector.sendCommand(reqBuilder.buildMessage(command));
    }

    public void sendCommand(String id, String command) {
        connector.sendCommand(reqBuilder.buildMessage(id, command.toLowerCase()));
    }
}
