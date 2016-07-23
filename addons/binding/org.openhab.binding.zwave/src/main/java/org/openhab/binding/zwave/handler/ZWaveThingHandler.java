/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.handler;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.config.core.validation.ConfigValidationException;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.ZWaveBindingConstants;
import org.openhab.binding.zwave.handler.ZWaveThingChannel.DataType;
import org.openhab.binding.zwave.internal.ZWaveConfigProvider;
import org.openhab.binding.zwave.internal.ZWaveProduct;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveAssociation;
import org.openhab.binding.zwave.internal.protocol.ZWaveAssociationGroup;
import org.openhab.binding.zwave.internal.protocol.ZWaveConfigurationParameter;
import org.openhab.binding.zwave.internal.protocol.ZWaveEventListener;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAssociationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveConfigurationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveConfigurationCommandClass.ZWaveConfigurationParameterEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveDoorLockCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveNodeNamingCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWavePlusCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWavePowerLevelCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWavePowerLevelCommandClass.ZWavePowerLevelCommandClassChangeEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveSwitchAllCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveUserCodeCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveWakeUpCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveWakeUpCommandClass.ZWaveWakeUpEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveAssociationEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveDelayedPollEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveInclusionEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveInitializationStateEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNodeStatusEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveTransactionCompletedEvent;
import org.openhab.binding.zwave.internal.protocol.initialization.ZWaveNodeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class ZWaveThingHandler extends ConfigStatusThingHandler implements ZWaveEventListener {
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet();

    private Logger logger = LoggerFactory.getLogger(ZWaveThingHandler.class);

    private ZWaveControllerHandler controllerHandler;

    private boolean finalTypeSet = false;

    private int nodeId;
    private List<ZWaveThingChannel> thingChannelsCmd = Collections.emptyList();
    private List<ZWaveThingChannel> thingChannelsState = Collections.emptyList();
    private List<ZWaveThingChannel> thingChannelsPoll = Collections.emptyList();

    private Map<Integer, ZWaveConfigSubParameter> subParameters = new HashMap<Integer, ZWaveConfigSubParameter>();
    private Map<String, Object> pendingCfg = new HashMap<String, Object>();

    private ScheduledFuture<?> pollingJob = null;
    private final long POLLING_PERIOD_MIN = 15;
    private final long POLLING_PERIOD_MAX = 7200;
    private final long POLLING_PERIOD_DEFAULT = 1800;
    private final long DELAYED_POLLING_PERIOD_MAX = 10;
    private final long REFRESH_POLL_DELAY = 50;
    private long pollingPeriod = POLLING_PERIOD_DEFAULT;

    public ZWaveThingHandler(Thing zwaveDevice) {
        super(zwaveDevice);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing ZWave thing handler.");

        String nodeParm = this.getThing().getProperties().get(ZWaveBindingConstants.PROPERTY_NODEID);
        if (nodeParm == null) {
            logger.error("NodeID is not set in {}", this.getThing().getUID());
            return;
        }

        try {
            nodeId = Integer.parseInt(nodeParm);
        } catch (final NumberFormatException ex) {
            logger.error("NodeID ({}) cannot be parsed in {}", nodeParm, this.getThing().getUID());
            return;
        }

        if (nodeId == 0) {
            logger.error("NodeID ({}) cannot be 0", nodeParm, this.getThing().getUID());
            return;
        }

        updateThingType();

        // We need to set the status to OFFLINE so that the framework calls our notification handlers
        updateStatus(ThingStatus.OFFLINE);

        // TODO: Shouldn't the framework do this for us???
        Bridge bridge = getBridge();
        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof ZWaveControllerHandler) {
                ZWaveControllerHandler bridgeHandler = (ZWaveControllerHandler) handler;
                if (bridgeHandler.getOwnNodeId() != 0) {
                    bridgeStatusChanged(bridge.getStatusInfo());
                }
            }
        }
    }

    void initialiseNode() {
        logger.warn("NODE {}: Initialising Thing Node...", nodeId);

        // Note that for dynamic channels, it seems that defaults can either be not set, or set with the incorrect
        // type. So, we read back as an Object to avoid casting problems.
        pollingPeriod = POLLING_PERIOD_DEFAULT;

        final Object pollParm = getConfig().get(ZWaveBindingConstants.CONFIGURATION_POLLPERIOD);
        if (pollParm instanceof BigDecimal) {
            try {
                pollingPeriod = ((BigDecimal) pollParm).intValue();
            } catch (final NumberFormatException ex) {
                logger.warn("NODE {}: pollingPeriod ({}) cannot be parsed - using default", nodeId, pollParm);
            }
        }

        // Create the channels list to simplify processing incoming events
        // synchronized (thingChannelsState) {
        thingChannelsCmd = new ArrayList<ZWaveThingChannel>();
        thingChannelsPoll = new ArrayList<ZWaveThingChannel>();
        thingChannelsState = new ArrayList<ZWaveThingChannel>();
        for (Channel channel : getThing().getChannels()) {
            // Process the channel properties and configuration
            Map<String, String> properties = channel.getProperties();
            Configuration configuration = channel.getConfiguration();

            logger.debug("NODE {}: Initialising channel {}", nodeId, channel.getUID());

            for (String key : properties.keySet()) {
                String[] bindingType = key.split(":");
                if (bindingType.length != 3) {
                    continue;
                }
                if (!ZWaveBindingConstants.CHANNEL_CFG_BINDING.equals(bindingType[0])) {
                    continue;
                }

                String[] bindingProperties = properties.get(key).split(";");

                // TODO: Check length???

                // Get the command classes - comma separated
                String[] cmdClasses = bindingProperties[0].split(",");

                // Convert the arguments to a map
                // - comma separated list of arguments "arg1=val1, arg2=val2"
                Map<String, String> argumentMap = new HashMap<String, String>();
                if (bindingProperties.length == 2) {
                    String[] arguments = bindingProperties[1].split(",");
                    for (String arg : arguments) {
                        String[] prop = arg.split("=");
                        argumentMap.put(prop[0], prop[1]);
                        // logger.debug("Adding Argument {}=={}", prop[0], prop[1]);
                    }
                }

                // Process the user configuration and add it to the argument map
                for (String configName : configuration.getProperties().keySet()) {
                    argumentMap.put(configName, configuration.get(configName).toString());
                }

                // Add all the command classes...
                boolean first = true;
                for (String cc : cmdClasses) {
                    String[] ccSplit = cc.split(":");
                    int endpoint = 0;

                    if (ccSplit.length == 2) {
                        endpoint = Integer.parseInt(ccSplit[1]);
                    }

                    // Get the data type
                    DataType dataType = DataType.DecimalType;
                    try {
                        dataType = DataType.valueOf(bindingType[2]);
                    } catch (IllegalArgumentException e) {
                        logger.warn("NODE {}: Invalid item type defined ({}). Assuming DecimalType", nodeId, dataType);
                    }

                    // logger.debug("Creating - arg map is {} long", argumentMap.size());
                    ZWaveThingChannel chan = new ZWaveThingChannel(controllerHandler, channel.getUID(), dataType,
                            ccSplit[0], endpoint, argumentMap);

                    // First time round, and this is a command - then add the command
                    if (first && ("*".equals(bindingType[1]) || "Command".equals(bindingType[1]))) {
                        thingChannelsCmd.add(chan);
                        logger.debug("NODE {}: Initialising cmd channel {}", nodeId, channel.getUID());
                    }

                    // First time round, then add the polling class
                    // TODO: Probably should check for duplicates
                    if (first) {
                        thingChannelsPoll.add(chan);
                        logger.debug("NODE {}: Initialising poll channel {}", nodeId, channel.getUID());
                    }

                    // Add the state and polling handlers
                    if ("*".equals(bindingType[1]) || "State".equals(bindingType[1])) {
                        logger.debug("NODE {}: Initialising state channel {}", nodeId, channel.getUID());
                        thingChannelsState.add(chan);
                    }

                    first = false;
                }
            }
            // }
        }

        startPolling();
    }

    /**
     * Check the thing type and change it if it's wrong
     */
    private void updateThingType() {
        // If the thing type is still the default, then see if we can change
        if (getThing().getThingTypeUID().equals(ZWaveBindingConstants.ZWAVE_THING_UID) == false) {
            finalTypeSet = true;
            return;
        }

        // Get the properties for the comparison
        String parmManufacturer = this.getThing().getProperties().get(ZWaveBindingConstants.PROPERTY_MANUFACTURER);
        if (parmManufacturer == null) {
            logger.debug("NODE {}: MANUFACTURER not set", nodeId);
            return;
        }
        String parmDeviceType = this.getThing().getProperties().get(ZWaveBindingConstants.PROPERTY_DEVICETYPE);
        if (parmDeviceType == null) {
            logger.debug("NODE {}: TYPE not set", nodeId);
            return;
        }
        String parmDeviceId = this.getThing().getProperties().get(ZWaveBindingConstants.PROPERTY_DEVICEID);
        if (parmDeviceId == null) {
            logger.debug("NODE {}: ID not set", nodeId);
            return;
        }
        String parmVersion = this.getThing().getProperties().get(ZWaveBindingConstants.PROPERTY_VERSION);
        if (parmVersion == null) {
            logger.debug("NODE {}: VERSION not set {}", nodeId);
            return;
        }

        int deviceType;
        int deviceId;
        int deviceManufacturer;

        try {
            deviceManufacturer = Integer.parseInt(parmManufacturer);
            deviceType = Integer.parseInt(parmDeviceType);
            deviceId = Integer.parseInt(parmDeviceId);
        } catch (final NumberFormatException ex) {
            logger.debug("NODE {}: Unable to parse device data", nodeId);
            return;
        }

        ZWaveProduct foundProduct = null;
        for (ZWaveProduct product : ZWaveConfigProvider.getProductIndex()) {
            if (product == null) {
                continue;
            }
            // logger.debug("Checking {}", product.getThingTypeUID());
            if (product.match(deviceManufacturer, deviceType, deviceId, parmVersion) == true) {
                foundProduct = product;
                break;
            }
        }

        // Did we find the thing type?
        if (foundProduct == null) {
            return;
        }

        // We need a change...
        changeThingType(foundProduct.getThingTypeUID(), getConfig());
        finalTypeSet = true;
    }

    /**
     * Start polling with an initial delay
     *
     * @param initialPeriod time to start in milliseconds
     */
    private void startPolling(long initialPeriod) {
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }

        if (pollingPeriod < POLLING_PERIOD_MIN) {
            logger.debug("NODE {}: Polling period was set below minimum value. Using minimum.", nodeId);

            pollingPeriod = POLLING_PERIOD_MIN;
        }

        if (pollingPeriod > POLLING_PERIOD_MAX) {
            logger.debug("NODE {}: Polling period was set above maximum value. Using maximum.", nodeId);

            pollingPeriod = POLLING_PERIOD_MAX;
        }

        Runnable pollingRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    // TODO: If/when this code changes, we should only poll channels that are linked.
                    logger.debug("NODE {}: Polling...", nodeId);
                    ZWaveNode node = controllerHandler.getNode(nodeId);
                    if (node == null || node.isInitializationComplete() == false) {
                        logger.debug("NODE {}: Polling deferred until initialisation complete", nodeId);
                        return;
                    }

                    List<SerialMessage> messages = new ArrayList<SerialMessage>();
                    for (ZWaveThingChannel channel : thingChannelsPoll) {
                        logger.debug("NODE {}: Polling {}", nodeId, channel.getUID());
                        if (channel.converter == null) {
                            logger.debug("NODE {}: Polling aborted as no converter found for {}", nodeId,
                                    channel.getUID());
                        } else {
                            List<SerialMessage> poll = channel.converter.executeRefresh(channel, node);
                            if (poll != null) {
                                messages.addAll(poll);
                            }
                        }
                    }

                    // Send all the messages
                    for (SerialMessage message : messages) {
                        controllerHandler.sendData(message);
                    }
                } catch (Exception e) {
                    logger.warn(String.format("NODE %d: Polling aborted due to exception", nodeId), e);
                }
            }
        };

        pollingJob = scheduler.scheduleAtFixedRate(pollingRunnable, initialPeriod, pollingPeriod * 1000,
                TimeUnit.MILLISECONDS);
        logger.debug("NODE {}: Polling intialised at {} seconds - start in {} milliseconds.", nodeId, pollingPeriod,
                initialPeriod);
    }

    private void startPolling() {
        startPolling(pollingPeriod * 1000);
    }

    @Override
    public void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
        logger.debug("NODE {}: Controller initialised.", nodeId);

        bridgeStatusChanged(bridge.getStatusInfo());
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("NODE {}: Controller status changed to {}.", nodeId, bridgeStatusInfo.getStatus());

        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE);
            logger.debug("NODE {}: Controller is not online.", nodeId, bridgeStatusInfo.getStatus());
            return;
        }

        logger.debug("NODE {}: Controller is ONLINE. Starting device initialisation.", nodeId);

        ZWaveControllerHandler bridgeHandler = (ZWaveControllerHandler) getBridge().getHandler();
        // We might not be notified that the controller is online until it's completed a lot of initialisation, so
        // make sure we know the device state.
        ZWaveNode node = bridgeHandler.getNode(nodeId);
        if (node == null) {
            updateStatus(ThingStatus.OFFLINE);
        } else {
            switch (node.getNodeState()) {
                case INITIALIZING:
                case ALIVE:
                    updateStatus(ThingStatus.ONLINE);
                    break;
                case DEAD:
                case FAILED:
                    updateStatus(ThingStatus.OFFLINE);
                    break;
            }
        }

        if (controllerHandler != null) {
            return;
        }

        if (node != null) {
            updateNodeNeighbours();
            updateNodeProperties();
        }
        controllerHandler = bridgeHandler;

        // Add the listener for ZWave events.
        // This ensures we get called whenever there's an event we might be interested in
        if (bridgeHandler.addEventListener(this) == false) {
            logger.warn("NODE {}: Controller failed to register event handler.", nodeId);
            return;
        }

        // Initialise the node - create all the channel links
        initialiseNode();
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed. Unregistering listener.");
        if (nodeId != 0) {
            if (controllerHandler != null) {
                controllerHandler.removeEventListener(this);
            }
            nodeId = 0;
        }

        if (pollingJob != null) {
            pollingJob.cancel(true);
        }

        controllerHandler = null;
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters)
            throws ConfigValidationException {
        logger.debug("NODE {}: Configuration update received", nodeId);

        // Perform checking on the configuration
        validateConfigurationParameters(configurationParameters);

        if (controllerHandler == null) {
            return;
        }
        ZWaveNode node = controllerHandler.getNode(nodeId);
        if (node == null) {
            return;
        }

        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
            Object valueObject = configurationParameter.getValue();
            logger.debug("NODE {}: Configuration update {} to {}", nodeId, configurationParameter.getKey(),
                    valueObject);
            String[] cfg = configurationParameter.getKey().split("_");
            if ("config".equals(cfg[0])) {
                if (cfg.length < 3) {
                    logger.warn("NODE {}: Configuration invalid {}", nodeId, configurationParameter.getKey());
                    continue;
                }

                ZWaveConfigurationCommandClass configurationCommandClass = (ZWaveConfigurationCommandClass) node
                        .getCommandClass(CommandClass.CONFIGURATION);
                if (configurationCommandClass == null) {
                    logger.error("NODE {}: Error getting configurationCommandClass", nodeId);
                    continue;
                }

                // Get the size
                int size = Integer.parseInt(cfg[2]);
                if (size == 0 || size > 4) {
                    logger.error("NODE {}: Size error ({}) from {}", nodeId, size, configurationParameter.getKey());
                    continue;
                }

                // Convert to integer
                Integer value;
                if (configurationParameter.getValue() instanceof BigDecimal) {
                    value = ((BigDecimal) configurationParameter.getValue()).intValue();
                } else if (configurationParameter.getValue() instanceof String) {
                    value = Integer.parseInt((String) configurationParameter.getValue());
                } else {
                    logger.error("NODE {}: Error converting config value from {}", nodeId,
                            configurationParameter.getValue().getClass());
                    continue;
                }

                Integer parameterIndex = Integer.valueOf(cfg[1]);

                boolean writeOnly = false;
                if (Arrays.asList(cfg).contains("wo")) {
                    writeOnly = true;
                }

                // If we have specified a bitmask, then we need to process this and save for later
                if (cfg.length >= 4 && cfg[3].length() == 8) {
                    int bitmask = 0xffffffff;
                    try {
                        bitmask = Integer.parseInt(cfg[3], 16);
                    } catch (NumberFormatException e) {
                        logger.error("NODE {}: Error parsing bitmask for {}", nodeId, configurationParameter.getKey());
                    }

                    boolean requestUpdate = false;
                    ZWaveConfigSubParameter subParameter = subParameters.get(parameterIndex);
                    if (subParameter == null) {
                        subParameter = new ZWaveConfigSubParameter();
                        requestUpdate = true;
                    }

                    logger.debug("NODE {}: Set sub-parameter {} from {} / {}", nodeId, parameterIndex, value,
                            String.format("%08X", bitmask));

                    logger.debug("NODE {}: Parameter {} set value {} mask {}", nodeId, parameterIndex,
                            String.format("%08X", value), String.format("%08X", bitmask));

                    subParameter.addBitmask(bitmask, value);
                    subParameters.put(parameterIndex, subParameter);

                    // Request the value. When this is received, we'll update the relevant bits
                    // and send the SET command.
                    // Only send the request if there's not already a request outstanding
                    if (requestUpdate == true) {
                        controllerHandler.sendData(configurationCommandClass.getConfigMessage(parameterIndex));
                    }
                } else {
                    ZWaveConfigurationParameter cfgParameter = configurationCommandClass.getParameter(parameterIndex);
                    if (cfgParameter == null) {
                        cfgParameter = new ZWaveConfigurationParameter(parameterIndex, value, size);
                    } else {
                        cfgParameter.setValue(value);
                    }

                    // Set the parameter and request a read-back if it's not a write only parameter
                    controllerHandler.sendData(configurationCommandClass.setConfigMessage(cfgParameter));
                    if (writeOnly == false) {
                        controllerHandler.sendData(configurationCommandClass.getConfigMessage(parameterIndex));
                    }
                }

                pendingCfg.put(configurationParameter.getKey(), valueObject);
            } else if ("group".equals(cfg[0])) {
                if (cfg.length < 2) {
                    logger.warn("NODE{}: Association invalid {}", nodeId, configurationParameter.getKey());
                    continue;
                }

                Integer groupIndex = Integer.valueOf(cfg[1]);

                // Get the association command class
                ZWaveAssociationCommandClass associationCommandClass = (ZWaveAssociationCommandClass) node
                        .getCommandClass(CommandClass.ASSOCIATION);
                // ZWaveAssociationCommandClass associationCommandClassMulti = (ZWaveAssociationCommandClass)
                // node
                // .getCommandClass(CommandClass.ASSOCIATION);

                // Get the configuration information.
                // This should be an array of nodes, and/or nodes and endpoints
                ArrayList<String> paramValues = new ArrayList<String>();
                Object parameter = configurationParameter.getValue();
                if (parameter instanceof List) {
                    paramValues.addAll((List) configurationParameter.getValue());
                } else if (parameter instanceof String) {
                    paramValues.add((String) parameter);
                }

                ZWaveAssociationGroup currentMembers = associationCommandClass.getGroupMembers(groupIndex);
                ZWaveAssociationGroup newMembers = new ZWaveAssociationGroup(groupIndex);

                // Loop over all the parameters
                for (String paramValue : paramValues) {
                    String[] groupCfg = paramValue.split("_");

                    // Make sure this is a correctly formatted option
                    if (!"node".equals(groupCfg[0])) {
                        continue;
                    }

                    // Get the node Id and endpoint Id
                    int associationNodeId = Integer.parseInt(groupCfg[1]);
                    int associationEndpointId = Integer.parseInt(groupCfg[2]);

                    newMembers.addAssociation(associationNodeId, associationEndpointId);
                }

                // Loop through the current members and remove anything that's not in the new members list
                for (ZWaveAssociation member : currentMembers.getAssociations()) {
                    // Is the current association still in the newMembers list?
                    if (newMembers.isAssociated(member.getNode(), member.getEndpoint()) == false) {
                        // No - so it needs to be removed
                        controllerHandler.sendData(
                                associationCommandClass.removeAssociationMessage(groupIndex, member.getNode()));
                    }
                }

                // Now loop through the new members and add anything not in the current members list
                for (ZWaveAssociation member : newMembers.getAssociations()) {
                    // Is the new association still in the currentMembers list?
                    if (currentMembers.isAssociated(member.getNode(), member.getEndpoint()) == false) {
                        // No - so it needs to be added
                        controllerHandler
                                .sendData(associationCommandClass.setAssociationMessage(groupIndex, member.getNode()));
                    }
                }

                // Request an update to the association group
                controllerHandler.sendData(associationCommandClass.getAssociationMessage(groupIndex));
                pendingCfg.put(configurationParameter.getKey(), valueObject);
            } else if ("wakeup".equals(cfg[0])) {
                ZWaveWakeUpCommandClass wakeupCommandClass = (ZWaveWakeUpCommandClass) node
                        .getCommandClass(CommandClass.WAKE_UP);
                if (wakeupCommandClass == null) {
                    logger.error("NODE {}: Error getting wakeupCommandClass", nodeId);
                    continue;
                }

                Integer value;
                if (configurationParameter.getValue() instanceof BigDecimal) {
                    value = ((BigDecimal) configurationParameter.getValue()).intValue();
                } else if (configurationParameter.getValue() instanceof String) {
                    value = Integer.parseInt((String) configurationParameter.getValue());
                } else {
                    logger.error("NODE {}: Error converting wakeup value from {}", nodeId,
                            configurationParameter.getValue().getClass());
                    continue;
                }

                logger.debug("NODE {}: Set wakeup interval to '{}'", nodeId, value);

                // Set the wake-up interval
                controllerHandler.sendData(wakeupCommandClass.setInterval(value));
                // And request a read-back
                controllerHandler.sendData(wakeupCommandClass.getIntervalMessage());
                pendingCfg.put(configurationParameter.getKey(), valueObject);
            } else if ("nodename".equals(cfg[0])) {
                ZWaveNodeNamingCommandClass nameCommandClass = (ZWaveNodeNamingCommandClass) node
                        .getCommandClass(CommandClass.NODE_NAMING);
                if (nameCommandClass == null) {
                    logger.error("NODE {}: Error getting NodeNamingCommandClass", nodeId);
                    continue;
                }

                if ("name".equals(cfg[1])) {
                    controllerHandler
                            .sendData(nameCommandClass.setNameMessage(configurationParameter.getValue().toString()));
                }
                if ("location".equals(cfg[1])) {
                    controllerHandler.sendData(
                            nameCommandClass.setLocationMessage(configurationParameter.getValue().toString()));
                }
                pendingCfg.put(configurationParameter.getKey(), valueObject);
            } else if ("switchall".equals(cfg[0])) {
                ZWaveSwitchAllCommandClass switchallCommandClass = (ZWaveSwitchAllCommandClass) node
                        .getCommandClass(CommandClass.SWITCH_ALL);
                if (switchallCommandClass == null) {
                    logger.error("NODE {}: Error getting SwitchAllCommandClass", nodeId);
                    continue;
                }

                if ("mode".equals(cfg[1])) {
                    controllerHandler.sendData(switchallCommandClass
                            .setValueMessage(Integer.parseInt(configurationParameter.getValue().toString())));
                }
                pendingCfg.put(configurationParameter.getKey(), valueObject);
            } else if ("powerlevel".equals(cfg[0])) {
                ZWavePowerLevelCommandClass powerlevelCommandClass = (ZWavePowerLevelCommandClass) node
                        .getCommandClass(CommandClass.POWERLEVEL);
                if (powerlevelCommandClass == null) {
                    logger.error("NODE {}: Error getting PowerLevelCommandClass", nodeId);
                    continue;
                }

                // Since both level and timeout are set in a single command, we first check if the value exists in the
                // pending list, and if not, use the value already stored in the command class
                if ("level".equals(cfg[1])) {
                    Integer timeout = (Integer) pendingCfg.get(ZWaveBindingConstants.CONFIGURATION_POWERLEVEL_TIMEOUT);
                    if (timeout == null) {
                        timeout = powerlevelCommandClass.getTimeout();
                    }
                    controllerHandler.sendData(powerlevelCommandClass.setValueMessage(
                            (Integer.parseInt(configurationParameter.getValue().toString())), timeout));
                }
                if ("timeout".equals(cfg[1])) {
                    Integer level = (Integer) pendingCfg.get(ZWaveBindingConstants.CONFIGURATION_POWERLEVEL_LEVEL);
                    if (level == null) {
                        level = powerlevelCommandClass.getLevel();
                    }
                    controllerHandler.sendData(powerlevelCommandClass.setValueMessage(level,
                            (Integer.parseInt(configurationParameter.getValue().toString()))));
                }
                controllerHandler.sendData(powerlevelCommandClass.getValueMessage());
                pendingCfg.put(configurationParameter.getKey(), valueObject);
            } else if ("doorlock".equals(cfg[0])) {
                ZWaveDoorLockCommandClass commandClass = (ZWaveDoorLockCommandClass) node
                        .getCommandClass(CommandClass.DOOR_LOCK);
                if (commandClass == null) {
                    logger.error("NODE {}: Error getting ZWaveDoorLockCommandClass", nodeId);
                    continue;
                }

                if ("timeout".equals(cfg[1])) {
                    boolean timeoutEnabled;

                    try {
                        int value = Integer.parseInt((String) valueObject);
                        if (value == 0) {
                            timeoutEnabled = false;
                        } else {
                            timeoutEnabled = true;
                        }
                        controllerHandler.sendData(commandClass.setConfigMessage(timeoutEnabled, value));
                        controllerHandler.sendData(commandClass.getConfigMessage());
                        pendingCfg.put(ZWaveBindingConstants.CONFIGURATION_DOORLOCKTIMEOUT, valueObject);
                    } catch (NumberFormatException e) {
                        logger.error("Number format exception parsing doorlock_timeout '{}'", valueObject);
                    }
                }
            } else if ("usercode".equals(cfg[0])) {
                ZWaveUserCodeCommandClass commandClass = (ZWaveUserCodeCommandClass) node
                        .getCommandClass(CommandClass.USER_CODE);
                if (commandClass == null) {
                    logger.error("NODE {}: Error getting ZWaveUserCodeCommandClass", nodeId);
                    continue;
                }

                try {
                    int code = Integer.parseInt(cfg[1]);
                    if (code == 0 || code > commandClass.getNumberOfSupportedCodes()) {
                        logger.error("NODE {}: Attempt to set code ID outside of range", nodeId);
                        continue;
                    }
                    controllerHandler.sendData(commandClass.setUserCode(code, (String) valueObject));
                    controllerHandler.sendData(commandClass.getUserCode(code));
                    pendingCfg.put(configurationParameter.getKey(), valueObject);
                } catch (NumberFormatException e) {
                    logger.error("Number format exception parsing user code ID '{}'", configurationParameter.getKey());
                }
            } else if ("binding".equals(cfg[0])) {
                if ("pollperiod".equals(cfg[1])) {
                    pollingPeriod = POLLING_PERIOD_DEFAULT;
                    try {
                        pollingPeriod = Integer.parseInt(configurationParameter.getValue().toString());
                    } catch (final NumberFormatException ex) {
                        logger.warn("NODE {}: pollingPeriod ({}) cannot be set - using default", nodeId,
                                configurationParameter.getValue().toString());
                    }
                    if (pollingPeriod < POLLING_PERIOD_MIN) {
                        pollingPeriod = POLLING_PERIOD_MIN;
                    }
                    if (pollingPeriod > POLLING_PERIOD_MAX) {
                        pollingPeriod = POLLING_PERIOD_MAX;
                    }
                    valueObject = new BigDecimal(pollingPeriod);

                    // Restart polling so we use the new value
                    startPolling();
                }
            } else if ("action".equals(cfg[0])) {
                if ("failed".equals(cfg[1]) && valueObject instanceof BigDecimal
                        && ((BigDecimal) valueObject).intValue() == ZWaveBindingConstants.ACTION_CHECK_VALUE) {
                    controllerHandler.replaceFailedNode(nodeId);
                }
                if ("remove".equals(cfg[1]) && valueObject instanceof BigDecimal
                        && ((BigDecimal) valueObject).intValue() == ZWaveBindingConstants.ACTION_CHECK_VALUE) {
                    controllerHandler.removeFailedNode(nodeId);
                    controllerHandler.checkNodeFailed(nodeId);
                }
                if ("reinit".equals(cfg[1]) && valueObject instanceof BigDecimal
                        && ((BigDecimal) valueObject).intValue() == ZWaveBindingConstants.ACTION_CHECK_VALUE) {
                    logger.debug("NODE {}: Re-initialising node!", nodeId);

                    // Delete the saved XML
                    ZWaveNodeSerializer nodeSerializer = new ZWaveNodeSerializer();
                    nodeSerializer.DeleteNode(nodeId);

                    controllerHandler.reinitialiseNode(nodeId);
                }

                if ("heal".equals(cfg[1]) && valueObject instanceof BigDecimal
                        && ((BigDecimal) valueObject).intValue() == ZWaveBindingConstants.ACTION_CHECK_VALUE) {
                    logger.debug("NODE {}: Starting heal on node!", nodeId);

                    controllerHandler.healNode(nodeId);
                }

                // Don't save the value
                valueObject = "";
            } else {
                logger.warn("NODE{}: Configuration invalid {}", nodeId, configurationParameter.getKey());
            }

            configuration.put(configurationParameter.getKey(), valueObject);
        }

        // Persist changes
        updateConfiguration(configuration);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("NODE {}: Command received {} --> {}", nodeId, channelUID, command);
        if (controllerHandler == null) {
            logger.warn("Controller handler not found. Cannot handle command without ZWave controller.");
            return;
        }

        if (command == RefreshType.REFRESH) {
            startPolling(REFRESH_POLL_DELAY);
            return;
        }

        DataType dataType;
        try {
            dataType = DataType.valueOf(command.getClass().getSimpleName());
        } catch (IllegalArgumentException e) {
            logger.warn("NODE {}: Command received with no implementation ({}).", nodeId,
                    command.getClass().getSimpleName());
            return;
        }

        // Find the channel
        ZWaveThingChannel cmdChannel = null;
        for (ZWaveThingChannel channel : thingChannelsCmd) {
            if (channel.getUID().equals(channelUID) && channel.getDataType() == dataType) {
                cmdChannel = channel;
                break;
            }
        }

        if (cmdChannel == null) {
            logger.warn("NODE {}: Command for unknown channel {} with {}", nodeId, channelUID, dataType);
            return;
        }

        ZWaveNode node = controllerHandler.getNode(nodeId);
        if (node == null) {
            logger.warn("NODE {}: Node is not found for {}", nodeId, channelUID);
            return;
        }

        if (cmdChannel.converter == null) {
            logger.warn("NODE {}: No converter set for command {} type {}", nodeId, channelUID, dataType);
            return;
        }

        List<SerialMessage> messages = null;
        messages = cmdChannel.converter.receiveCommand(cmdChannel, node, command);

        if (messages == null) {
            logger.warn("NODE {}: No messages returned from converter", nodeId);
            return;
        }

        // Send all the messages
        for (SerialMessage message : messages) {
            controllerHandler.sendData(message);
        }
    }

    @Override
    public void ZWaveIncomingEvent(ZWaveEvent incomingEvent) {
        // Check if this event is for this device
        if (incomingEvent.getNodeId() != nodeId) {
            return;
        }

        logger.debug("NODE {}: Got an event from Z-Wave network: {}", nodeId, incomingEvent.getClass().getSimpleName());

        // Handle command class value events.
        if (incomingEvent instanceof ZWaveCommandClassValueEvent) {
            // Cast to a command class event
            ZWaveCommandClassValueEvent event = (ZWaveCommandClassValueEvent) incomingEvent;

            String commandClass = event.getCommandClass().getLabel();

            logger.debug(
                    "NODE {}: Got a value event from Z-Wave network, endpoint = {}, command class = {}, value = {}",
                    nodeId, event.getEndpoint(), commandClass, event.getValue());

            // If this is a configuration parameter update, process it before the channels
            Configuration configuration = editConfiguration();
            boolean cfgUpdated = false;
            switch (event.getCommandClass()) {
                case CONFIGURATION:
                    ZWaveConfigurationParameter parameter = ((ZWaveConfigurationParameterEvent) event).getParameter();
                    if (parameter == null) {
                        return;
                    }

                    logger.debug("NODE {}: Update CONFIGURATION {}/{} to {}", nodeId, parameter.getIndex(),
                            parameter.getSize(), parameter.getValue());

                    // Check for any sub parameter processing...
                    // If we have requested the current state of a parameter and t's waiting to be updated, then we
                    // check
                    // this here, update the value and send the request...
                    // Do this first so we only process the data if we're not waiting to send
                    ZWaveConfigSubParameter subParameter = subParameters.get(parameter.getIndex());

                    if (subParameter != null) {
                        // Get the new value based on the sub-parameter bitmask
                        int value = subParameter.getValue(parameter.getValue());
                        logger.debug("NODE {}: Updating sub-parameter {} to {}", nodeId, parameter.getIndex(), value);

                        // Remove the sub parameter so we don't loop forever!
                        subParameters.remove(parameter.getIndex());

                        ZWaveNode node = controllerHandler.getNode(nodeId);
                        ZWaveConfigurationCommandClass configurationCommandClass = (ZWaveConfigurationCommandClass) node
                                .getCommandClass(CommandClass.CONFIGURATION);
                        if (configurationCommandClass == null) {
                            logger.error("NODE {}: Error getting configurationCommandClass", nodeId);
                            return;
                        }

                        ZWaveConfigurationParameter cfgParameter = configurationCommandClass
                                .getParameter(parameter.getIndex());
                        if (cfgParameter == null) {
                            cfgParameter = new ZWaveConfigurationParameter(parameter.getIndex(), value,
                                    parameter.getSize());
                        } else {
                            cfgParameter.setValue(value);
                        }

                        logger.debug("NODE {}: Setting parameter {} to {}", nodeId, cfgParameter.getIndex(),
                                cfgParameter.getValue());
                        controllerHandler.sendData(configurationCommandClass.setConfigMessage(cfgParameter));
                        controllerHandler.sendData(configurationCommandClass.getConfigMessage(parameter.getIndex()));

                        // Don't process the data - it hasn't been updated yet!
                        break;
                    }

                    updateConfigurationParameter(configuration, parameter.getIndex(), parameter.getSize(),
                            parameter.getValue());
                    break;

                case ASSOCIATION:
                    int groupId = ((ZWaveAssociationEvent) event).getGroupId();
                    List<ZWaveAssociation> groupMembers = ((ZWaveAssociationEvent) event).getGroupMembers();
                    if (groupMembers != null) {
                        logger.debug("NODE {}: Update ASSOCIATION group_{}", nodeId, groupId);

                        List<String> group = new ArrayList<String>();

                        // Build the configuration value
                        for (ZWaveAssociation groupMember : groupMembers) {
                            logger.debug("NODE {}: Update ASSOCIATION group_{}: Adding node_{}_{}", nodeId, groupId,
                                    groupMember.getNode(), groupMember.getEndpoint());
                            group.add("node_" + groupMember.getNode() + "_" + groupMember.getEndpoint());
                        }
                        logger.debug("NODE {}: Update ASSOCIATION group_{}: {} members", nodeId, groupId, group.size());

                        cfgUpdated = true;
                        configuration.put("group_" + groupId, group);
                        pendingCfg.remove("group_" + groupId);
                    }
                    break;

                case SWITCH_ALL:
                    cfgUpdated = true;
                    configuration.put(ZWaveBindingConstants.CONFIGURATION_SWITCHALLMODE, event.getValue());
                    pendingCfg.remove(ZWaveBindingConstants.CONFIGURATION_SWITCHALLMODE);
                    break;

                case NODE_NAMING:
                    switch ((ZWaveNodeNamingCommandClass.Type) event.getType()) {
                        case NODENAME_LOCATION:
                            cfgUpdated = true;
                            configuration.put(ZWaveBindingConstants.CONFIGURATION_NODELOCATION, event.getValue());
                            pendingCfg.remove(ZWaveBindingConstants.CONFIGURATION_NODELOCATION);
                            break;
                        case NODENAME_NAME:
                            cfgUpdated = true;
                            configuration.put(ZWaveBindingConstants.CONFIGURATION_NODENAME, event.getValue());
                            pendingCfg.remove(ZWaveBindingConstants.CONFIGURATION_NODENAME);
                            break;
                    }
                    break;

                case DOOR_LOCK:
                    switch ((ZWaveDoorLockCommandClass.Type) event.getType()) {
                        case DOOR_LOCK_TIMEOUT:
                            cfgUpdated = true;
                            configuration.put(ZWaveBindingConstants.CONFIGURATION_DOORLOCKTIMEOUT, event.getValue());
                            pendingCfg.remove(ZWaveBindingConstants.CONFIGURATION_DOORLOCKTIMEOUT);
                            break;
                        default:
                            break;
                    }
                    break;

                case POWERLEVEL:
                    ZWavePowerLevelCommandClassChangeEvent powerEvent = (ZWavePowerLevelCommandClassChangeEvent) event;
                    cfgUpdated = true;
                    configuration.put(ZWaveBindingConstants.CONFIGURATION_POWERLEVEL_LEVEL, powerEvent.getLevel());
                    pendingCfg.remove(ZWaveBindingConstants.CONFIGURATION_POWERLEVEL_LEVEL);
                    configuration.put(ZWaveBindingConstants.CONFIGURATION_POWERLEVEL_TIMEOUT, powerEvent.getTimeout());
                    pendingCfg.remove(ZWaveBindingConstants.CONFIGURATION_POWERLEVEL_TIMEOUT);
                    break;

                default:
                    break;
            }
            if (cfgUpdated == true) {
                logger.debug("NODE {}: Config updated", nodeId);
                updateConfiguration(configuration);
            }

            if (thingChannelsState == null) {
                logger.error("NODE {}: No state handlers!", nodeId);
                return;
            }

            // Process the channels to see if we're interested
            for (ZWaveThingChannel channel : thingChannelsState) {
                // logger.debug("NODE {}: Checking channel {}", nodeId, channel.getUID());

                if (channel.getEndpoint() != event.getEndpoint()) {
                    continue;
                }

                // Is this command class associated with this channel?
                if (!channel.getCommandClass().equals(commandClass)) {
                    continue;
                }

                if (channel.converter == null) {
                    logger.warn("NODE {}: No converter set for channel {}", nodeId, channel.getUID());
                    return;
                }

                // logger.debug("NODE {}: Processing event as channel {} {}", nodeId, channel.getUID(),
                // channel.dataType);
                State state = channel.converter.handleEvent(channel, event);
                if (state != null) {
                    logger.debug("NODE {}: Updating channel state {} to {} [{}]", nodeId, channel.getUID(), state,
                            state.getClass().getSimpleName());

                    updateState(channel.getUID(), state);
                }
            }

            return;
        }

        // Handle transaction complete events.
        if (incomingEvent instanceof ZWaveTransactionCompletedEvent) {
            return;
        }

        // Handle wakeup notification events.
        if (incomingEvent instanceof ZWaveWakeUpEvent) {
            ZWaveNode node = controllerHandler.getNode(nodeId);
            if (node == null) {
                return;
            }

            switch (((ZWaveWakeUpEvent) incomingEvent).getEvent()) {
                case ZWaveWakeUpCommandClass.WAKE_UP_NOTIFICATION:
                    Map<String, String> properties = editProperties();
                    properties.put(ZWaveBindingConstants.PROPERTY_WAKEUP_TIME, getISO8601StringForCurrentDate());
                    updateProperties(properties);
                    break;
                case ZWaveWakeUpCommandClass.WAKE_UP_INTERVAL_REPORT:
                    ZWaveWakeUpCommandClass commandClass = (ZWaveWakeUpCommandClass) node
                            .getCommandClass(CommandClass.WAKE_UP);
                    Configuration configuration = editConfiguration();
                    configuration.put(ZWaveBindingConstants.CONFIGURATION_WAKEUPINTERVAL, commandClass.getInterval());
                    pendingCfg.remove(ZWaveBindingConstants.CONFIGURATION_WAKEUPINTERVAL);
                    configuration.put(ZWaveBindingConstants.CONFIGURATION_WAKEUPNODE, commandClass.getTargetNodeId());
                    pendingCfg.remove(ZWaveBindingConstants.CONFIGURATION_WAKEUPNODE);
                    updateConfiguration(configuration);
                    break;
            }
            return;
        }

        // Handle node state change events.
        if (incomingEvent instanceof ZWaveNodeStatusEvent) {
            // Cast to a command class event
            ZWaveNodeStatusEvent event = (ZWaveNodeStatusEvent) incomingEvent;

            switch (event.getState()) {
                case INITIALIZING:
                case ALIVE:
                    logger.debug("NODE {}: Setting ONLINE", nodeId);
                    updateStatus(ThingStatus.ONLINE);
                    break;
                case DEAD:
                case FAILED:
                    logger.debug("NODE {}: Setting OFFLINE", nodeId);
                    updateStatus(ThingStatus.OFFLINE);
                    break;
            }

            return;
        }

        if (incomingEvent instanceof ZWaveInitializationStateEvent) {
            ZWaveInitializationStateEvent initEvent = (ZWaveInitializationStateEvent) incomingEvent;
            switch (initEvent.getStage()) {
                case DONE:
                    // Update some properties first...
                    updateNodeNeighbours();
                    updateNodeProperties();

                    // Do we need to change type?
                    if (finalTypeSet == false) {
                        updateThingType();
                    }

                    // Set ourselves online if we have the final thing type set
                    if (finalTypeSet) {
                        logger.debug("NODE {}: Setting ONLINE", nodeId);
                        updateStatus(ThingStatus.ONLINE);

                        // Now that this node is completely initialised, we want to re-process all channels
                        initialiseNode();
                    }
                    break;
                default:
                    if (finalTypeSet) {
                        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, initEvent.getStage().toString());
                    }
                    break;
            }
        }

        if (incomingEvent instanceof ZWaveNetworkEvent) {
            ZWaveNetworkEvent networkEvent = (ZWaveNetworkEvent) incomingEvent;

            if (networkEvent.getEvent() == ZWaveNetworkEvent.Type.NodeRoutingInfo) {
                updateNodeNeighbours();
            }

            if (networkEvent.getEvent() == ZWaveNetworkEvent.Type.DeleteNode) {
                updateStatus(ThingStatus.REMOVED);
            }
        }

        if (incomingEvent instanceof ZWaveDelayedPollEvent) {
            long delay = ((ZWaveDelayedPollEvent) incomingEvent).getDelay();
            TimeUnit unit = ((ZWaveDelayedPollEvent) incomingEvent).getUnit();

            // Don't create a poll beyond our max value
            if (unit.toSeconds(delay) > DELAYED_POLLING_PERIOD_MAX) {
                delay = DELAYED_POLLING_PERIOD_MAX;
                unit = TimeUnit.SECONDS;
            }

            startPolling(unit.toMillis(delay));
        }

        // Handle exclusion of this node
        if (incomingEvent instanceof ZWaveInclusionEvent) {
            ZWaveInclusionEvent incEvent = (ZWaveInclusionEvent) incomingEvent;
            if (incEvent.getNodeId() != nodeId) {
                return;
            }

            switch (incEvent.getEvent()) {
                case ExcludeDone:
                    // Let our users know we're gone!
                    updateStatus(ThingStatus.REMOVED, ThingStatusDetail.NONE, "Node was excluded from the controller");

                    // Stop polling
                    if (pollingJob != null) {
                        pollingJob.cancel(true);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void updateNodeNeighbours() {
        if (controllerHandler == null) {
            return;
        }

        ZWaveNode node = controllerHandler.getNode(nodeId);
        if (node == null) {
            return;
        }

        String neighbours = "";
        for (Integer neighbour : node.getNeighbors()) {
            if (neighbours.length() != 0) {
                neighbours += ',';
            }
            neighbours += neighbour;
        }
        updateProperty(ZWaveBindingConstants.PROPERTY_NEIGHBOURS, neighbours);
    }

    private void updateNodeProperties() {
        if (controllerHandler == null) {
            logger.debug("NODE {}: Updating node properties. Controller not found.", nodeId);
            return;
        }

        ZWaveNode node = controllerHandler.getNode(nodeId);
        if (node == null) {
            logger.debug("NODE {}: Updating node properties. Node not found.", nodeId);
            return;
        }

        logger.debug("NODE {}: Updating node properties.", nodeId);

        // Update property information about this device
        Map<String, String> properties = editProperties();

        logger.debug("NODE {}: Updating node properties. MAN={}", nodeId, node.getManufacturer());
        if (node.getManufacturer() != Integer.MAX_VALUE) {
            logger.debug("NODE {}: Updating node properties. MAN={}. SET. Was {}", nodeId, node.getManufacturer(),
                    properties.get(ZWaveBindingConstants.PROPERTY_MANUFACTURER));
            properties.put(ZWaveBindingConstants.PROPERTY_MANUFACTURER, Integer.toString(node.getManufacturer()));
        }
        if (node.getDeviceType() != Integer.MAX_VALUE) {
            properties.put(ZWaveBindingConstants.PROPERTY_DEVICETYPE, Integer.toString(node.getDeviceType()));
        }
        if (node.getDeviceId() != Integer.MAX_VALUE) {
            properties.put(ZWaveBindingConstants.PROPERTY_DEVICEID, Integer.toString(node.getDeviceId()));
        }
        properties.put(ZWaveBindingConstants.PROPERTY_VERSION, node.getApplicationVersion());

        properties.put(ZWaveBindingConstants.PROPERTY_CLASS_BASIC,
                node.getDeviceClass().getBasicDeviceClass().toString());
        properties.put(ZWaveBindingConstants.PROPERTY_CLASS_GENERIC,
                node.getDeviceClass().getGenericDeviceClass().toString());
        properties.put(ZWaveBindingConstants.PROPERTY_CLASS_SPECIFIC,
                node.getDeviceClass().getSpecificDeviceClass().toString());
        properties.put(ZWaveBindingConstants.PROPERTY_LISTENING, Boolean.toString(node.isListening()));
        properties.put(ZWaveBindingConstants.PROPERTY_FREQUENT, Boolean.toString(node.isFrequentlyListening()));
        properties.put(ZWaveBindingConstants.PROPERTY_BEAMING, Boolean.toString(node.isBeaming()));
        properties.put(ZWaveBindingConstants.PROPERTY_ROUTING, Boolean.toString(node.isRouting()));

        // If this is a Z-Wave Plus device, then also add its class
        ZWavePlusCommandClass cmdClassZWavePlus = (ZWavePlusCommandClass) node
                .getCommandClass(CommandClass.ZWAVE_PLUS_INFO);
        if (cmdClassZWavePlus != null) {
            properties.put(ZWaveBindingConstants.PROPERTY_ZWPLUS_DEVICETYPE,
                    cmdClassZWavePlus.getZWavePlusDeviceType().toString());
        }

        boolean update = false;
        Map<String, String> originalProperties = editProperties();
        for (String property : originalProperties.keySet()) {
            if (properties.get(property).equals(originalProperties.get(property)) == false) {
                update = true;
                break;
            }
        }

        if (update == true) {
            logger.debug("NODE {}: Properties synchronised", nodeId);
            updateProperties(properties);
        }

        // We need to synchronise the configuration between the ZWave library and ESH.
        // This is especially important when the device is first added as the ESH representation of the config
        // will be set to defaults. We will also not have any defaults for association groups, wakeup etc.
        Configuration config = editConfiguration();

        // Process CONFIGURATION
        ZWaveConfigurationCommandClass configurationCommandClass = (ZWaveConfigurationCommandClass) node
                .getCommandClass(CommandClass.CONFIGURATION);
        if (configurationCommandClass != null) {
            // Iterate over all parameters and process
            for (int paramId : configurationCommandClass.getParameters().keySet()) {
                ZWaveConfigurationParameter parameter = configurationCommandClass.getParameter(paramId);
                updateConfigurationParameter(config, parameter.getIndex(), parameter.getSize(), parameter.getValue());
            }
        }

        // Process ASSOCIATION
        ZWaveAssociationCommandClass associationCommandClass = (ZWaveAssociationCommandClass) node
                .getCommandClass(CommandClass.ASSOCIATION);
        if (associationCommandClass != null) {
            for (int groupId : associationCommandClass.getAssociations().keySet()) {
                List<String> group = new ArrayList<String>();

                // Build the configuration value
                for (ZWaveAssociation groupMember : associationCommandClass.getGroupMembers(groupId)
                        .getAssociations()) {
                    logger.debug("NODE {}: Update ASSOCIATION group_{}: Adding node_{}_{}", nodeId, groupId,
                            groupMember.getNode(), groupMember.getEndpoint());
                    group.add("node_" + groupMember.getNode() + "_" + groupMember.getEndpoint());
                }

                config.put("group_" + groupId, group);
            }
        }

        // Process WAKE_UP
        ZWaveWakeUpCommandClass wakeupCommandClass = (ZWaveWakeUpCommandClass) node
                .getCommandClass(CommandClass.WAKE_UP);
        if (wakeupCommandClass != null) {
            config.put(ZWaveBindingConstants.CONFIGURATION_WAKEUPINTERVAL, wakeupCommandClass.getInterval());
            config.put(ZWaveBindingConstants.CONFIGURATION_WAKEUPNODE, wakeupCommandClass.getTargetNodeId());
        }

        // Process SWITCH_ALL
        ZWaveSwitchAllCommandClass switchallCommandClass = (ZWaveSwitchAllCommandClass) node
                .getCommandClass(CommandClass.SWITCH_ALL);
        if (switchallCommandClass != null) {
            if (switchallCommandClass.getMode() != null) {
                config.put(ZWaveBindingConstants.CONFIGURATION_SWITCHALLMODE, switchallCommandClass.getMode());
            }
        }

        // Process NODE_NAMING
        ZWaveNodeNamingCommandClass nodenamingCommandClass = (ZWaveNodeNamingCommandClass) node
                .getCommandClass(CommandClass.NODE_NAMING);
        if (nodenamingCommandClass != null) {
            if (nodenamingCommandClass.getLocation() != null) {
                config.put(ZWaveBindingConstants.CONFIGURATION_NODELOCATION, nodenamingCommandClass.getLocation());
            }
            if (nodenamingCommandClass.getName() != null) {
                config.put(ZWaveBindingConstants.CONFIGURATION_NODENAME, nodenamingCommandClass.getName());
            }
        }

        // Only update if configuration has changed
        Configuration originalConfig = editConfiguration();

        update = false;
        for (String property : config.getProperties().keySet()) {
            if (config.get(property).equals(originalConfig.get(property)) == false) {
                update = true;
                break;
            }
        }

        if (update == true) {
            logger.debug("NODE {}: Configuration synchronised", nodeId);
            updateConfiguration(config);
        }
    }

    private boolean updateConfigurationParameter(Configuration configuration, int paramIndex, int paramSize,
            int paramValue) {

        boolean cfgUpdated = false;

        logger.debug("NODE {}: Config about to update {} parameters...", nodeId, configuration.keySet().size());
        for (String key : configuration.keySet()) {
            logger.debug("NODE {}: Processing {}", nodeId, key);
            String[] cfg = key.split("_");
            // Check this is a config parameter
            if (!"config".equals(cfg[0])) {
                continue;
            }
            logger.debug("NODE {}: Processing {} len={}", nodeId, key, cfg.length);

            if (cfg.length < 3) {
                logger.warn("NODE {}: Configuration invalid {}", nodeId, key);
                continue;
            }

            logger.debug("NODE {}: Processing {} - id = '{}'", nodeId, key, cfg[1]);

            // Check this is for the right parameter
            if (Integer.parseInt(cfg[1]) != paramIndex) {
                continue;
            }

            logger.debug("NODE {}: Processing {} - size = '{}'", nodeId, key, cfg[2]);

            // Get the size
            int size = Integer.parseInt(cfg[2]);
            if (size != paramSize) {
                logger.error("NODE {}: Size error {}<>{} from {}", nodeId, size, paramSize, key);
                continue;
            }

            // Get the bitmask
            int bitmask = 0xffffffff;
            if (cfg.length >= 4) {
                logger.debug("NODE {}: Processing {} - bitmask = '{}'", nodeId, key, cfg[3]);
                try {
                    bitmask = Integer.parseInt(cfg[3], 16);
                    logger.debug("NODE {}: Processing {} - dec = '{}'", nodeId, key, bitmask);

                } catch (NumberFormatException e) {
                    logger.error("NODE {}: Error parsing bitmask for {}", nodeId, key);
                }
            }

            int value = paramValue & bitmask;
            logger.debug("NODE {}: Sub-parameter {} is {}", nodeId, key, String.format("%08X", value));

            logger.debug("NODE {}: Pre-processing  {}>>{}", nodeId, String.format("%08X", value),
                    String.format("%08X", bitmask));

            // Shift the value
            int bits = bitmask;
            while ((bits & 0x01) == 0) {
                value = value >> 1;
                bits = bits >> 1;
            }

            // And the bitmask to get rid of any sign extension
            // value &= bits;

            logger.debug("NODE {}: Post-processing {}>>{}", nodeId, String.format("%08X", value),
                    String.format("%08X", bitmask));

            logger.debug("NODE {}: Sub-parameter setting {} is {} [{}]", nodeId, key, String.format("%08X", value),
                    value);

            cfgUpdated = true;
            configuration.put(key, value);
            pendingCfg.remove(key);
        }

        return cfgUpdated;
    }

    private class ZWaveConfigSubParameter {
        private int bitmask = 0;
        private int value = 0;

        public void addBitmask(int bitmask, int value) {
            if (bitmask == 0) {
                return;
            }

            // Clear the relevant bits
            this.value &= this.value & ~bitmask;

            // Shift the value
            int bits = bitmask;
            while ((bits & 0x01) == 0) {
                value = value << 1;
                bits = bits >> 1;
            }

            // Add the new sub-parameter value
            this.value |= value & bitmask;
            this.bitmask |= bitmask;
        }

        /**
         * Get the updated value, given the current value, and updating it based on the internal bitmask/value
         *
         * @param value
         * @return
         */
        public int getValue(int value) {
            return (value & ~this.bitmask) + this.value;
        }
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        Collection<ConfigStatusMessage> configStatus = new ArrayList<>();

        // Loop through the pending list
        // TODO: Do we want to handle other states?????
        for (String config : pendingCfg.keySet()) {
            configStatus.add(ConfigStatusMessage.Builder.pending(config).build());
        }

        return configStatus;
    }

    /**
     * Return an ISO 8601 combined date and time string for current date/time
     *
     * @return String with format "yyyy-MM-dd'T'HH:mm:ss'Z'"
     */
    public static String getISO8601StringForCurrentDate() {
        Date now = new Date();
        return getISO8601StringForDate(now);
    }

    /**
     * Return an ISO 8601 combined date and time string for specified date/time
     *
     * @param date
     *            Date
     * @return String with format "yyyy-MM-dd'T'HH:mm:ss'Z'"
     */
    private static String getISO8601StringForDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }
}
