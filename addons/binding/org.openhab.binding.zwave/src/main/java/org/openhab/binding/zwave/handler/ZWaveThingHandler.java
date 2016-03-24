/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.handler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.ZWaveBindingConstants;
import org.openhab.binding.zwave.internal.ZWaveConfigProvider;
import org.openhab.binding.zwave.internal.ZWaveProduct;
import org.openhab.binding.zwave.internal.converter.ZWaveCommandClassConverter;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveAssociation;
import org.openhab.binding.zwave.internal.protocol.ZWaveAssociationGroup;
import org.openhab.binding.zwave.internal.protocol.ZWaveConfigurationParameter;
import org.openhab.binding.zwave.internal.protocol.ZWaveEventListener;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAssociationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveConfigurationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveConfigurationCommandClass.ZWaveConfigurationParameterEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveNodeNamingCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveSwitchAllCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveWakeUpCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveWakeUpCommandClass.ZWaveWakeUpEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveAssociationEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveDelayedPollEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
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
    private List<ZWaveThingChannel> thingChannelsCmd;
    private List<ZWaveThingChannel> thingChannelsState;
    private List<ZWaveThingChannel> thingChannelsPoll;

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

        updateThingType();

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
                    ZWaveThingChannel chan = new ZWaveThingChannel(channel.getUID(), dataType, ccSplit[0], endpoint,
                            argumentMap);

                    // First time round, and this is a command - then add the command
                    if (first && ("*".equals(bindingType[1]) || "Command".equals(bindingType[1]))) {
                        thingChannelsCmd.add(chan);
                        logger.debug("NODE {}: Initialising cmd channel {}", nodeId, channel.getUID());
                    }

                    // First time round, then add the polling class
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
        }

        Bridge bridge = getBridge();
        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof ZWaveControllerHandler) {
                // ZWaveControllerHandler bridgeHandler = (ZWaveControllerHandler) handler;
                // if (bridgeHandler.getOwnNodeId() != 0) {
                bridgeHandlerInitialized(handler, bridge);
                // }
            }
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
            logger.debug("NODE {}: MANUFACTURER not set {}", nodeId);
            return;
        }
        String parmDeviceType = this.getThing().getProperties().get(ZWaveBindingConstants.PROPERTY_DEVICETYPE);
        if (parmDeviceType == null) {
            logger.debug("NODE {}: TYPE not set {}", nodeId);
            return;
        }
        String parmDeviceId = this.getThing().getProperties().get(ZWaveBindingConstants.PROPERTY_DEVICEID);
        if (parmDeviceId == null) {
            logger.debug("NODE {}: ID not set {}", nodeId);
            return;
        }
        String parmVersion = this.getThing().getProperties().get(ZWaveBindingConstants.PROPERTY_VERSION);
        if (parmVersion == null) {
            logger.debug("NODE {}: ID not set {}", nodeId);
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
                        logger.debug("NODE {}: Polling aborted as no converter found for {}", nodeId, channel.getUID());
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
        logger.debug("NODE {}: Controller initialised. Starting device intialisation.", nodeId);

        // We might not be notified that the controller is online until it's completed a lot of initialisation, so
        // make sure we know the device state.
        ZWaveNode node = ((ZWaveControllerHandler) thingHandler).getNode(nodeId);
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

        if (controllerHandler != null) {// || controllerHandler.getOwnNodeId() == 0) {
            return;
        }

        // Add the listener for ZWave events.
        // This ensures we get called whenever there's an event we might be interested in
        if (((ZWaveControllerHandler) thingHandler).addEventListener(this) == true) {
            controllerHandler = (ZWaveControllerHandler) thingHandler;
            updateNeighbours();
        } else {
            logger.warn("NODE {}: Controller failed to register event handler.", nodeId);
        }
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
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters)
            throws ConfigValidationException {
        logger.debug("NODE {}: Configuration update received", nodeId);

        // Perform checking on the configuration
        validateConfigurationParameters(configurationParameters);

        ZWaveNode node = controllerHandler.getNode(nodeId);

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
                    return;
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
                    return;
                }

                if ("name".equals(cfg[1])) {
                    nameCommandClass.setNameMessage(configurationParameter.getValue().toString());
                }
                if ("location".equals(cfg[1])) {
                    nameCommandClass.setLocationMessage(configurationParameter.getValue().toString());
                }
                pendingCfg.put(configurationParameter.getKey(), valueObject);
            } else if ("switchall".equals(cfg[0])) {
                ZWaveSwitchAllCommandClass switchallCommandClass = (ZWaveSwitchAllCommandClass) node
                        .getCommandClass(CommandClass.SWITCH_ALL);
                if (switchallCommandClass == null) {
                    logger.error("NODE {}: Error getting SwitchAllCommandClass", nodeId);
                    return;
                }

                if ("mode".equals(cfg[1])) {
                    switchallCommandClass
                            .setValueMessage(Integer.parseInt(configurationParameter.getValue().toString()));
                }
                pendingCfg.put(configurationParameter.getKey(), valueObject);
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
                    valueObject = Integer.toString((int) pollingPeriod);

                    // Restart polling so we use the new value
                    startPolling();
                }
            } else if ("action".equals(cfg[0])) {
                if ("failed".equals(cfg[1]) && "GO".equals(valueObject)) {
                    controllerHandler.replaceFailedNode(nodeId);
                }
                if ("remove".equals(cfg[1]) && "GO".equals(valueObject)) {
                    controllerHandler.removeFailedNode(nodeId);
                }
                if ("reinit".equals(cfg[1]) && "GO".equals(valueObject)) {
                    logger.debug("NODE {}: Re-initialising node!", nodeId);

                    // Delete the saved XML
                    ZWaveNodeSerializer nodeSerializer = new ZWaveNodeSerializer();
                    nodeSerializer.DeleteNode(nodeId);

                    controllerHandler.reinitialiseNode(nodeId);
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
            logger.warn("NODE {}: No converter set for command {} {}", nodeId, channelUID, dataType);
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

                    logger.debug("NODE {}: Config about to update {} parameters...", nodeId,
                            configuration.keySet().size());
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
                        if (Integer.parseInt(cfg[1]) != parameter.getIndex()) {
                            continue;
                        }

                        logger.debug("NODE {}: Processing {} - size = '{}'", nodeId, key, cfg[2]);

                        // Get the size
                        int size = Integer.parseInt(cfg[2]);
                        if (size != parameter.getSize()) {
                            logger.error("NODE {}: Size error {}<>{} from {}", nodeId, size, parameter.getSize(), key);
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

                        int value = parameter.getValue() & bitmask;
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

                        logger.debug("NODE {}: Sub-parameter setting {} is {} [{}]", nodeId, key,
                                String.format("%08X", value), value);

                        configuration.put(key, value);
                        pendingCfg.remove(key);
                    }
                    break;

                case ASSOCIATION:
                    int groupId = ((ZWaveAssociationEvent) event).getGroupId();
                    List<ZWaveAssociation> groupMembers = ((ZWaveAssociationEvent) event).getGroupMembers();
                    if (groupMembers != null) {
                        logger.debug("NODE {}: Update ASSOCIATION group_{}", groupId);

                        List<String> group = new ArrayList<String>();

                        // Build the configuration value
                        for (ZWaveAssociation groupMember : groupMembers) {
                            logger.debug("NODE {}: Update ASSOCIATION group_{}: Adding node_{}_{}", nodeId, groupId,
                                    groupMember.getNode(), groupMember.getEndpoint());
                            group.add("node_" + groupMember.getNode() + "_" + groupMember.getEndpoint());
                        }
                        logger.debug("NODE {}: Update ASSOCIATION group_{}: {} members", groupId, group.size());

                        configuration.put("group_" + groupId, group);
                        pendingCfg.remove("group_" + groupId);
                    }
                    break;

                case SWITCH_ALL:
                    configuration.put(ZWaveBindingConstants.CONFIGURATION_SWITCHALLMODE, event.getValue());
                    pendingCfg.remove(ZWaveBindingConstants.CONFIGURATION_SWITCHALLMODE);
                    break;

                case NODE_NAMING:
                    switch ((ZWaveNodeNamingCommandClass.Type) event.getType()) {
                        case NODENAME_LOCATION:
                            configuration.put(ZWaveBindingConstants.CONFIGURATION_NODELOCATION, event.getValue());
                            pendingCfg.remove(ZWaveBindingConstants.CONFIGURATION_NODELOCATION);
                            break;
                        case NODENAME_NAME:
                            configuration.put(ZWaveBindingConstants.CONFIGURATION_NODENAME, event.getValue());
                            pendingCfg.remove(ZWaveBindingConstants.CONFIGURATION_NODENAME);
                            break;
                    }
                    break;

                case DOOR_LOCK:
                    configuration.put(ZWaveBindingConstants.CONFIGURATION_DOORLOCKTIMEOUT, event.getValue());
                    pendingCfg.remove(ZWaveBindingConstants.CONFIGURATION_DOORLOCKTIMEOUT);
                    break;

                default:
                    break;
            }
            logger.debug("NODE {}: Config updated", nodeId);
            updateConfiguration(configuration);

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
                    logger.warn("NODE {}: No converter set for state {}", nodeId, channel.getUID());
                    return;
                }

                // logger.debug("NODE {}: Processing event as channel {} {}", nodeId, channel.getUID(),
                // channel.dataType);
                State state = channel.converter.handleEvent(channel, event);
                if (state != null) {
                    logger.debug("Updating {} to {}", channel.getUID(), state);

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
            // We're only interested in the report
            if (((ZWaveWakeUpEvent) incomingEvent)
                    .getEvent() != ZWaveWakeUpCommandClass.WAKE_UP_INTERVAL_CAPABILITIES_REPORT
                    && ((ZWaveWakeUpEvent) incomingEvent)
                            .getEvent() != ZWaveWakeUpCommandClass.WAKE_UP_INTERVAL_REPORT) {
                return;
            }

            ZWaveNode node = controllerHandler.getNode(((ZWaveWakeUpEvent) incomingEvent).getNodeId());
            if (node == null) {
                return;
            }

            ZWaveWakeUpCommandClass commandClass = (ZWaveWakeUpCommandClass) node.getCommandClass(CommandClass.WAKE_UP);
            Configuration configuration = editConfiguration();
            configuration.put(ZWaveBindingConstants.CONFIGURATION_WAKEUPINTERVAL, commandClass.getInterval());
            pendingCfg.remove(ZWaveBindingConstants.CONFIGURATION_WAKEUPINTERVAL);
            configuration.put(ZWaveBindingConstants.CONFIGURATION_WAKEUPNODE, commandClass.getTargetNodeId());
            pendingCfg.remove(ZWaveBindingConstants.CONFIGURATION_WAKEUPNODE);
            updateConfiguration(configuration);
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
                    updateNeighbours();

                    ZWaveNode node = controllerHandler.getNode(nodeId);
                    if (node == null) {
                        return;
                    }

                    // Update property information about this device
                    Map<String, String> properties = editProperties();
                    properties.put(ZWaveBindingConstants.PROPERTY_CLASS_BASIC,
                            node.getDeviceClass().getBasicDeviceClass().toString());
                    properties.put(ZWaveBindingConstants.PROPERTY_CLASS_GENERIC,
                            node.getDeviceClass().getGenericDeviceClass().toString());
                    properties.put(ZWaveBindingConstants.PROPERTY_CLASS_SPECIFIC,
                            node.getDeviceClass().getSpecificDeviceClass().toString());
                    properties.put(ZWaveBindingConstants.PROPERTY_LISTENING, Boolean.toString(node.isListening()));
                    properties.put(ZWaveBindingConstants.PROPERTY_FREQUENT,
                            Boolean.toString(node.isFrequentlyListening()));
                    properties.put(ZWaveBindingConstants.PROPERTY_BEAMING, Boolean.toString(node.isBeaming()));
                    properties.put(ZWaveBindingConstants.PROPERTY_ROUTING, Boolean.toString(node.isRouting()));
                    super.updateProperties(properties);

                    // Do we need to change type?
                    if (finalTypeSet == false) {
                        updateThingType();
                    }

                    // Set ourselves online if we have the final thing type set
                    if (finalTypeSet) {
                        logger.debug("NODE {}: Setting ONLINE", nodeId);
                        updateStatus(ThingStatus.ONLINE);
                    }
                    break;
                default:
                    if (finalTypeSet) {
                        logger.debug("NODE {}: Setting ONLINE (INITIALIZING): {}", nodeId, initEvent.getStage());
                        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, initEvent.getStage().toString());
                    }
                    break;
            }
        }

        if (incomingEvent instanceof ZWaveNetworkEvent) {
            ZWaveNetworkEvent networkEvent = (ZWaveNetworkEvent) incomingEvent;

            if (networkEvent.getEvent() == ZWaveNetworkEvent.Type.NodeRoutingInfo) {
                updateNeighbours();
            }
        }

        if (incomingEvent instanceof ZWaveDelayedPollEvent) {
            long delay = ((ZWaveDelayedPollEvent) incomingEvent).getDelay();
            TimeUnit unit = ((ZWaveDelayedPollEvent) incomingEvent).getUnit();

            // don't create a poll beyond our max value
            if (unit.toSeconds(delay) > DELAYED_POLLING_PERIOD_MAX) {
                delay = DELAYED_POLLING_PERIOD_MAX;
                unit = TimeUnit.SECONDS;
            }

            startPolling(unit.toMillis(delay));
        }
    }

    private void updateNeighbours() {
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

    public class ZWaveThingChannel {
        ChannelUID uid;
        int endpoint;
        String commandClass;
        ZWaveCommandClassConverter converter;
        DataType dataType;
        Map<String, String> arguments;

        ZWaveThingChannel(ChannelUID uid, DataType dataType, String commandClassName, int endpoint,
                Map<String, String> arguments) {
            this.uid = uid;
            this.arguments = arguments;
            this.commandClass = commandClassName;
            this.endpoint = endpoint;
            this.dataType = dataType;

            // Get the converter
            CommandClass commandClass = ZWaveCommandClass.CommandClass.getCommandClass(commandClassName);
            if (commandClass == null) {
                logger.warn("NODE {}: Error finding command class '{}'", nodeId, uid, commandClassName);
            }
            this.converter = ZWaveCommandClassConverter.getConverter(commandClass);
            if (this.converter == null) {
                logger.warn("NODE {}: No converter found for {}, class {}", nodeId, uid, commandClassName);
            }
        }

        public ChannelUID getUID() {
            return uid;
        }

        public String getCommandClass() {
            return commandClass;
        }

        public int getEndpoint() {
            return endpoint;
        }

        public DataType getDataType() {
            return dataType;
        }

        public Map<String, String> getArguments() {
            return arguments;
        }
    }

    public enum DataType {
        DecimalType,
        HSBType,
        IncreaseDecreaseType,
        OnOffType,
        OpenClosedType,
        PercentType,
        StringType,
        DateTimeType,
        UpDownType,
        StopMoveType;
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
            configStatus
                    .add(ConfigStatusMessage.Builder.pending(config).withMessageKey("CONFIG_STATUS_PENDING").build());
        }

        return configStatus;
    }
}
