/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.handler;

import static org.openhab.binding.zwave.ZWaveBindingConstants.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.validation.ConfigValidationException;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.zwave.discovery.ZWaveDiscoveryService;
import org.openhab.binding.zwave.internal.ZWaveNetworkMonitor;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEventListener;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkStateEvent;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZWaveControllerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Jackson - Initial contribution
 */
public abstract class ZWaveControllerHandler extends BaseBridgeHandler implements ZWaveEventListener {

    private Logger logger = LoggerFactory.getLogger(ZWaveControllerHandler.class);

    private ZWaveDiscoveryService discoveryService;
    private ServiceRegistration discoveryRegistration;

    private volatile ZWaveController controller;

    // Network monitoring class
    ZWaveNetworkMonitor networkMonitor;

    private Boolean isMaster;
    private Boolean isSUC;

    public ZWaveControllerHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing ZWave Controller.");

        isMaster = (Boolean) getConfig().get(CONFIGURATION_MASTER);
        if (isMaster == null) {
            isMaster = true;
        }

        isSUC = (Boolean) getConfig().get(CONFIGURATION_SUC);
        if (isSUC == null) {
            isSUC = false;
        }

        super.initialize();
    }

    /**
     * Common initialisation point for all ZWave controllers.
     * Called by bridges after they have initialised their interfaces.
     *
     */
    protected void initializeNetwork() {
        logger.debug("Initialising ZWave controller");

        // Create config parameters
        Map<String, String> config = new HashMap<String, String>();
        config.put("masterController", isMaster.toString());

        // TODO: Handle soft reset better!
        controller = new ZWaveController(this, config);
        controller.addEventListener(this);

        // The network monitor service needs to know the controller...
        this.networkMonitor = new ZWaveNetworkMonitor(controller);
        // if(healtime != null) {
        // this.networkMonitor.setHealTime(healtime);
        // }
        // if(aliveCheckPeriod != null) {
        // this.networkMonitor.setPollPeriod(aliveCheckPeriod);
        // }
        // if(softReset != false) {
        // this.networkMonitor.resetOnError(softReset);
        // }

        // The config service needs to know the controller and the network monitor...
        // this.zConfigurationService = new ZWaveConfiguration(this.zController, this.networkMonitor);
        // zController.addEventListener(this.zConfigurationService);

        // Start the discovery service
        discoveryService = new ZWaveDiscoveryService(this);
        discoveryService.activate();

        // And register it as an OSGi service
        discoveryRegistration = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<String, Object>());
    }

    @Override
    public void dispose() {
        // Remove the discovery service
        if (discoveryService != null) {
            discoveryService.deactivate();
        }

        if (discoveryRegistration != null) {
            discoveryRegistration.unregister();
        }

        // if (this.converterHandler != null) {
        // this.converterHandler = null;
        // }

        ZWaveController controller = this.controller;
        if (controller != null) {
            this.controller = null;
            controller.removeEventListener(this);
        }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters)
            throws ConfigValidationException {
        logger.debug("Controller Configuration update received");

        // Perform checking on the configuration
        validateConfigurationParameters(configurationParameters);

        boolean reinitialise = false;

        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
            Object value = configurationParameter.getValue();
            logger.debug("Controller Configuration update {} to {}", configurationParameter.getKey(), value);
            String[] cfg = configurationParameter.getKey().split("_");
            if ("controller".equals(cfg[0])) {
                if (controller != null) {
                    logger.warn("Trying to send controller command, but controller is not initialised");
                    continue;
                }

                if (cfg[1].equals("softreset")) {
                    controller.requestSoftReset();
                } else if (cfg[1].equals("hardreset")) {
                    controller.requestHardReset();
                } else if (cfg[1].equals("exclude")) {
                    controller.requestRemoveNodesStart();
                }

                value = "";
            }

            if ("port".equals(cfg[0])) {
                reinitialise = true;
            }

            configuration.put(configurationParameter.getKey(), value);
        }

        // Persist changes
        updateConfiguration(configuration);

        if (reinitialise == true) {
            dispose();
            initialize();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // if(channelUID.getId().equals(CHANNEL_1)) {
        // TODO: handle command
        // }
    }

    public void startDeviceDiscovery() {
        if (controller == null) {
            return;
        }
        controller.requestAddNodesStart();
    }

    public void stopDeviceDiscovery() {
        if (controller == null) {
            return;
        }
        controller.requestAddNodesStop();
    }

    @Override
    public void ZWaveIncomingEvent(ZWaveEvent event) {
        if (event instanceof ZWaveNetworkStateEvent) {
            if (((ZWaveNetworkStateEvent) event).getNetworkState() == true) {
                updateStatus(ThingStatus.ONLINE);
                Bridge bridge = this.getThing();
                for (Thing child : bridge.getThings()) {
                    ((ZWaveThingHandler) child.getHandler()).bridgeHandlerInitialized(this, bridge);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        }

    }

    protected void incomingMessage(SerialMessage serialMessage) {
        if (controller == null) {
            return;
        }
        controller.incomingPacket(serialMessage);
    }

    public abstract void sendPacket(SerialMessage serialMessage);

    public void deviceDiscovered(int nodeId) {
        if (discoveryService == null) {
            return;
        }
        // discoveryService.deviceDiscovered(nodeId);
    }

    public void deviceAdded(ZWaveNode node) {
        if (discoveryService == null) {
            return;
        }
        // ThingUID newThing =
        discoveryService.deviceAdded(node);
        // if (newThing == null) {
        // return;
        // }

        // ThingType thingType = ZWaveConfigProvider.getThingType(newThing.getThingTypeUID());

        // thingType.getProperties()
    }

    public int getOwnNodeId() {
        if (controller == null) {
            return 0;
        }
        return controller.getOwnNodeId();
    }

    public ZWaveNode getNode(int node) {
        if (controller == null) {
            return null;
        }

        return controller.getNode(node);
    }

    public Collection<ZWaveNode> getNodes() {
        if (controller == null) {
            return null;
        }
        return controller.getNodes();
    }

    public void sendData(SerialMessage message) {
        if (controller == null) {
            return;
        }
        controller.sendData(message);
    }

    public boolean addEventListener(ZWaveThingHandler zWaveThingHandler) {
        if (controller == null) {
            return false;
        }
        controller.addEventListener(zWaveThingHandler);
        return true;
    }

    public boolean removeEventListener(ZWaveThingHandler zWaveThingHandler) {
        if (controller == null) {
            return false;
        }
        controller.removeEventListener(zWaveThingHandler);
        return true;
    }

    public UID getUID() {
        return thing.getUID();
    }

    public void removeFailedNode(int nodeId) {
        controller.requestRemoveFailedNode(nodeId);
    }

    public void replaceFailedNode(int nodeId) {
        controller.requestRemoveFailedNode(nodeId);
    }

    public void reinitialiseNode(int nodeId) {
        controller.reinitialiseNode(nodeId);
    }
}
