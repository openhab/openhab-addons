/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.handler;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.mysensors.config.MySensorsBridgeConfiguration;
import org.openhab.binding.mysensors.discovery.MySensorsDiscoveryService;
import org.openhab.binding.mysensors.factory.MySensorsCacheFactory;
import org.openhab.binding.mysensors.internal.event.MySensorsGatewayEventListener;
import org.openhab.binding.mysensors.internal.gateway.MySensorsGateway;
import org.openhab.binding.mysensors.internal.gateway.MySensorsGatewayConfig;
import org.openhab.binding.mysensors.internal.gateway.MySensorsGatewayType;
import org.openhab.binding.mysensors.internal.protocol.MySensorsAbstractConnection;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

/**
 * MySensorsBridgeHandler is used to initialize a new bridge (in MySensors: Gateway)
 * The sensors are connected via the gateway/bridge to the controller
 *
 * @author Tim Oberföll
 *
 */
public class MySensorsBridgeHandler extends BaseBridgeHandler implements MySensorsGatewayEventListener {

    private Logger logger = LoggerFactory.getLogger(MySensorsBridgeHandler.class);

    // Gateway instance
    private MySensorsGateway myGateway;

    // Configuration from thing file
    private MySensorsBridgeConfiguration myBridgeConfiguration;

    // Service discovery registration
    private ServiceRegistration<?> discoveryServiceRegistration;

    // Discovery service
    private MySensorsDiscoveryService discoveryService;

    private MySensorsCacheFactory cacheFactory;

    public MySensorsBridgeHandler(Bridge bridge) {
        super(bridge);
        cacheFactory = new MySensorsCacheFactory(ConfigConstants.getUserDataFolder());
    }

    @Override
    public void initialize() {
        logger.debug("Initialization of the MySensors bridge");

        myBridgeConfiguration = getConfigAs(MySensorsBridgeConfiguration.class);

        myGateway = new MySensorsGateway(loadCacheFile());

        if (myGateway.setup(openhabToMySensorsGatewayConfig(myBridgeConfiguration, getThing().getThingTypeUID()))) {
            myGateway.startup();

            myGateway.addEventListener(this);

            registerDeviceDiscoveryService();

            logger.debug("Initialization of the MySensors bridge DONE!");
        } else {
            logger.error("Failed to initialize MySensors bridge");
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing of the MySensors bridge");

        unregisterDeviceDiscoveryService();

        if (myGateway != null) {
            myGateway.removeEventListener(this);
            myGateway.shutdown();
        }

        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Getter for the configuration of the bridge.
     *
     * @return Configuration of the MySensors bridge.
     */
    public MySensorsBridgeConfiguration getBridgeConfiguration() {
        return myBridgeConfiguration;
    }

    /**
     * Getter for the connection to the MySensors bridge / gateway.
     * Used for receiving (register handler) and sending of messages.
     *
     * @return Connection to the MySensors bridge / gateway.
     */
    public MySensorsGateway getMySensorsGateway() {
        return myGateway;
    }

    @Override
    public void connectionStatusUpdate(MySensorsAbstractConnection connection, boolean connected) throws Exception {
        if (connected) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }

        updateCacheFile();
    }

    @Override
    public void nodeIdReservationDone(Integer reservedId) throws Exception {
        updateCacheFile();
    }

    @Override
    public void newNodeDiscovered(MySensorsNode node, MySensorsChild child) throws Exception {
        updateCacheFile();
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        logger.debug("Configuation update for bridge: {}", configurationParameters);
        super.handleConfigurationUpdate(configurationParameters);
    }

    private void updateCacheFile() {
        List<Integer> givenIds = myGateway.getGivenIds();

        cacheFactory.writeCache(MySensorsCacheFactory.GIVEN_IDS_CACHE_FILE, givenIds.toArray(new Integer[] {}),
                Integer[].class);
    }

    private Map<Integer, MySensorsNode> loadCacheFile() {
        Map<Integer, MySensorsNode> nodes = new HashMap<Integer, MySensorsNode>();

        List<Integer> givenIds = cacheFactory.readCache(MySensorsCacheFactory.GIVEN_IDS_CACHE_FILE,
                new ArrayList<Integer>(), new TypeToken<ArrayList<Integer>>() {
                }.getType());

        for (Integer i : givenIds) {
            if (i != null) {
                nodes.put(i, new MySensorsNode(i));
            }
        }

        return nodes;
    }

    private MySensorsGatewayConfig openhabToMySensorsGatewayConfig(MySensorsBridgeConfiguration conf,
            ThingTypeUID bridgeuid) {
        MySensorsGatewayConfig gatewayConfig = new MySensorsGatewayConfig();

        if (bridgeuid.equals(THING_TYPE_BRIDGE_SER)) {
            gatewayConfig.setGatewayType(MySensorsGatewayType.SERIAL);
            gatewayConfig.setBaudRate(conf.baudRate);
            gatewayConfig.setSerialPort(conf.serialPort);
            gatewayConfig.setHardReset(conf.hardReset);
        } else if (bridgeuid.equals(THING_TYPE_BRIDGE_ETH)) {
            gatewayConfig.setGatewayType(MySensorsGatewayType.IP);
            gatewayConfig.setIpAddress(conf.ipAddress);
            gatewayConfig.setTcpPort(conf.tcpPort);
        } else if (bridgeuid.equals(THING_TYPE_BRIDGE_MQTT)) {
            gatewayConfig.setGatewayType(MySensorsGatewayType.MQTT);
            gatewayConfig.setBrokerName(conf.brokerName);
            gatewayConfig.setTopicPublish(conf.topicPublish);
            gatewayConfig.setTopicSubscribe(conf.topicSubscribe);
        } else {
            throw new IllegalArgumentException("BridgeUID is unknown: " + bridgeuid);
        }

        gatewayConfig.setSendDelay(conf.sendDelay);
        gatewayConfig.setEnableNetworkSanCheck(conf.networkSanCheckEnabled);
        gatewayConfig.setImperial(conf.imperial);
        gatewayConfig.setStartupCheck(conf.startupCheckEnabled);
        gatewayConfig.setSanityCheckerInterval(conf.networkSanCheckInterval);
        gatewayConfig.setSanCheckConnectionFailAttempts(conf.networkSanCheckConnectionFailAttempts);
        gatewayConfig.setSanCheckSendHeartbeat(conf.networkSanCheckSendHeartbeat);
        gatewayConfig.setSanCheckSendHeartbeatFailAttempts(conf.networkSanCheckSendHeartbeatFailAttempts);

        return gatewayConfig;
    }

    private void registerDeviceDiscoveryService() {
        if (bundleContext != null) {
            logger.trace("Registering MySensorsDiscoveryService for bridge '{}'", getThing().getUID().getId());
            discoveryService = new MySensorsDiscoveryService(this);
            discoveryServiceRegistration = bundleContext.registerService(DiscoveryService.class.getName(),
                    discoveryService, new Hashtable<String, Object>());
            discoveryService.activate();
        }
    }

    private void unregisterDeviceDiscoveryService() {
        if (discoveryServiceRegistration != null && discoveryService != null) {
            logger.trace("Unregistering MySensorsDiscoveryService for bridge '{}'", getThing().getUID().getId());

            discoveryService.deactivate();

            discoveryServiceRegistration.unregister();
            discoveryServiceRegistration = null;
            discoveryService = null;
        }
    }
}
