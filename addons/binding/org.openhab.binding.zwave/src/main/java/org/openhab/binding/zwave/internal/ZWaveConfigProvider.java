/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterGroup;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry;
import org.eclipse.smarthome.config.core.ConfigOptionProvider;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.openhab.binding.zwave.ZWaveBindingConstants;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveUserCodeCommandClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

public class ZWaveConfigProvider implements ConfigDescriptionProvider, ConfigOptionProvider {
    private final static Logger logger = LoggerFactory.getLogger(ZWaveConfigProvider.class);

    private static ThingRegistry thingRegistry;
    private static ThingTypeRegistry thingTypeRegistry;
    private static ConfigDescriptionRegistry configDescriptionRegistry;

    private static Set<ThingTypeUID> zwaveThingTypeUIDList = new HashSet<ThingTypeUID>();
    private static List<ZWaveProduct> productIndex = new ArrayList<ZWaveProduct>();

    private static final Object productIndexLock = new Object();

    // The following is a list of classes that are controllable.
    // This is used to filter endpoints so that when we display a list of nodes/endpoints
    // for configuring associations, we only list endpoints that are useful
    private static final Set<ZWaveCommandClass.CommandClass> controllableClasses = ImmutableSet.of(CommandClass.BASIC,
            CommandClass.SWITCH_BINARY, CommandClass.SWITCH_MULTILEVEL, CommandClass.SWITCH_TOGGLE_BINARY,
            CommandClass.SWITCH_TOGGLE_MULTILEVEL, CommandClass.CHIMNEY_FAN, CommandClass.THERMOSTAT_HEATING,
            CommandClass.THERMOSTAT_MODE, CommandClass.THERMOSTAT_OPERATING_STATE, CommandClass.THERMOSTAT_SETPOINT,
            CommandClass.THERMOSTAT_FAN_MODE, CommandClass.THERMOSTAT_FAN_STATE, CommandClass.FIBARO_FGRM_222);

    protected void setThingRegistry(ThingRegistry thingRegistry) {
        ZWaveConfigProvider.thingRegistry = thingRegistry;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        ZWaveConfigProvider.thingRegistry = null;
    }

    protected void setThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        ZWaveConfigProvider.thingTypeRegistry = thingTypeRegistry;
    }

    protected void unsetThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        ZWaveConfigProvider.thingTypeRegistry = null;
    }

    protected void setConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        ZWaveConfigProvider.configDescriptionRegistry = configDescriptionRegistry;
    }

    protected void unsetConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        ZWaveConfigProvider.configDescriptionRegistry = null;
    }

    @Override
    public Collection<ConfigDescription> getConfigDescriptions(Locale locale) {
        logger.debug("getConfigDescriptions called");
        return Collections.emptySet();
    }

    @Override
    public ConfigDescription getConfigDescription(URI uri, Locale locale) {
        if (uri == null) {
            return null;
        }

        if ("thing".equals(uri.getScheme()) == false) {
            return null;
        }

        ThingUID thingUID = new ThingUID(uri.getSchemeSpecificPart());
        ThingType thingType = thingTypeRegistry.getThingType(thingUID.getThingTypeUID());
        if (thingType == null) {
            return null;
        }

        // Is this a zwave thing?
        if (!thingUID.getBindingId().equals(ZWaveBindingConstants.BINDING_ID)) {
            return null;
        }

        // And make sure this is a node because we want to get the id off the end...
        if (!thingUID.getId().startsWith("node")) {
            return null;
        }
        int nodeId = Integer.parseInt(thingUID.getId().substring(4));

        Thing thing = getThing(thingUID);
        if (thing == null) {
            return null;
        }
        ThingUID bridgeUID = thing.getBridgeUID();

        // Get the controller for this thing
        Thing bridge = getThing(bridgeUID);
        if (bridge == null) {
            return null;
        }

        // Get its handler and node
        ZWaveControllerHandler handler = (ZWaveControllerHandler) bridge.getHandler();
        ZWaveNode node = handler.getNode(nodeId);
        if (node == null) {
            logger.error("NODE {}: Node not found in getConfigDescription", nodeId);
            return null;
        }

        List<ConfigDescriptionParameterGroup> groups = new ArrayList<ConfigDescriptionParameterGroup>();
        List<ConfigDescriptionParameter> parameters = new ArrayList<ConfigDescriptionParameter>();

        groups.add(new ConfigDescriptionParameterGroup("actions", "", false, "Actions", null));
        groups.add(new ConfigDescriptionParameterGroup("thingcfg", "home", false, "Device Configuration", null));

        parameters.add(ConfigDescriptionParameterBuilder
                .create(ZWaveBindingConstants.CONFIGURATION_POLLPERIOD, Type.INTEGER).withLabel("Polling Period")
                .withDescription("Set the minimum polling period for this device<BR/>"
                        + "Note that the polling period may be longer than set since the binding treats "
                        + "polls as the lowest priority data within the network.")
                .withDefault("1800").withMinimum(new BigDecimal(15)).withMaximum(new BigDecimal(7200))
                .withGroupName("thingcfg").build());

        // If we support the wakeup class, then add the configuration
        if (node.getCommandClass(ZWaveCommandClass.CommandClass.WAKE_UP) != null) {
            groups.add(new ConfigDescriptionParameterGroup("wakeup", "sleep", false, "Wakeup Configuration", null));

            parameters.add(ConfigDescriptionParameterBuilder
                    .create(ZWaveBindingConstants.CONFIGURATION_WAKEUPINTERVAL, Type.TEXT).withLabel("Wakeup Interval")
                    .withDescription("Sets the number of seconds that the device will wakeup<BR/>"
                            + "Setting a shorter time will allow openHAB to configure the device more regularly, but may use more battery power.<BR>"
                            + "<B>Note:</B> This setting does not impact device notifications such as alarms.")
                    .withDefault("").withGroupName("wakeup").build());

            parameters.add(
                    ConfigDescriptionParameterBuilder.create(ZWaveBindingConstants.CONFIGURATION_WAKEUPNODE, Type.TEXT)
                            .withLabel("Wakeup Node").withAdvanced(true)
                            .withDescription("Sets the wakeup node to which the device will send notifications.<BR/>"
                                    + "This should normally be set to the openHAB controller - "
                                    + "if it isn't, openHAB will not receive notifications when the device wakes up, "
                                    + "and will not be able to configure the device.")
                            .withDefault("").withGroupName("wakeup").build());
        }

        // If we support the node name class, then add the configuration
        if (node.getCommandClass(ZWaveCommandClass.CommandClass.NODE_NAMING) != null) {
            parameters.add(
                    ConfigDescriptionParameterBuilder.create(ZWaveBindingConstants.CONFIGURATION_NODENAME, Type.TEXT)
                            .withLabel("Node Name").withDescription("Sets a string for the device name")
                            .withGroupName("thingcfg").withDefault("").build());
            parameters.add(ConfigDescriptionParameterBuilder
                    .create(ZWaveBindingConstants.CONFIGURATION_NODELOCATION, Type.TEXT)
                    .withDescription("Sets a string for the device location").withLabel("Node Location").withDefault("")
                    .withGroupName("thingcfg").build());
        }

        // If we support the switch_all class, then add the configuration
        if (node.getCommandClass(ZWaveCommandClass.CommandClass.SWITCH_ALL) != null) {
            List<ParameterOption> options = new ArrayList<ParameterOption>();
            options.add(new ParameterOption("0", "Exclude from All On and All Off groups"));
            options.add(new ParameterOption("1", "Include in All On group"));
            options.add(new ParameterOption("2", "Include in All Off group"));
            options.add(new ParameterOption("255", "Include in All On and All Off groups"));
            parameters.add(ConfigDescriptionParameterBuilder
                    .create(ZWaveBindingConstants.CONFIGURATION_SWITCHALLMODE, Type.TEXT).withLabel("Switch All Mode")
                    .withDescription("Set the mode for the switch when receiving SWITCH ALL commands.").withDefault("0")
                    .withGroupName("thingcfg").withOptions(options).withLimitToOptions(true).build());
        }

        // If we support the powerlevel class, then add the configuration
        if (node.getCommandClass(ZWaveCommandClass.CommandClass.POWERLEVEL) != null) {
            List<ParameterOption> options = new ArrayList<ParameterOption>();
            options.add(new ParameterOption("0", "Normal"));
            options.add(new ParameterOption("1", "Minus 1dB"));
            options.add(new ParameterOption("2", "Minus 2dB"));
            options.add(new ParameterOption("3", "Minus 3dB"));
            options.add(new ParameterOption("4", "Minus 4dB"));
            options.add(new ParameterOption("5", "Minus 5dB"));
            options.add(new ParameterOption("6", "Minus 6dB"));
            options.add(new ParameterOption("7", "Minus 7dB"));
            options.add(new ParameterOption("8", "Minus 8dB"));
            options.add(new ParameterOption("9", "Minus 9dB"));
            parameters.add(ConfigDescriptionParameterBuilder
                    .create(ZWaveBindingConstants.CONFIGURATION_POWERLEVEL_LEVEL, Type.INTEGER).withLabel("Power Level")
                    .withDescription(
                            "Set the RF output level - Normal is maximum power<br>Setting the power to a lower level may be useful to reduce overloading of the receiver in adjacent nodes where they are close together, or if maximum power is not required for battery devices, it may extend battery life by reducing the transmit power.")
                    .withDefault("0").withGroupName("thingcfg").withOptions(options).withLimitToOptions(true).build());

            parameters.add(ConfigDescriptionParameterBuilder
                    .create(ZWaveBindingConstants.CONFIGURATION_POWERLEVEL_TIMEOUT, Type.INTEGER)
                    .withLabel("Power Level Timeout")
                    .withDescription(
                            "Set the power level timeout in seconds<br>The node will reset to the normal power level if communications is not made within the specified number of seconds.")
                    .withDefault("0").withGroupName("thingcfg").build());
        }

        // If we support DOOR_LOCK - add options
        if (node.getCommandClass(ZWaveCommandClass.CommandClass.DOOR_LOCK) != null) {
            parameters.add(ConfigDescriptionParameterBuilder
                    .create(ZWaveBindingConstants.CONFIGURATION_DOORLOCKTIMEOUT, Type.INTEGER).withLabel("Lock Timeout")
                    .withDescription("Set the timeout on the lock.").withDefault("30").withGroupName("thingcfg")
                    .build());
        }

        ZWaveUserCodeCommandClass userCodeClass = (ZWaveUserCodeCommandClass) node
                .getCommandClass(ZWaveCommandClass.CommandClass.USER_CODE);
        if (userCodeClass != null && userCodeClass.getNumberOfSupportedCodes() > 0) {
            groups.add(new ConfigDescriptionParameterGroup("usercode", "lock", false, "User Code", null));

            for (int code = 1; code <= userCodeClass.getNumberOfSupportedCodes(); code++) {
                parameters.add(ConfigDescriptionParameterBuilder
                        .create(ZWaveBindingConstants.CONFIGURATION_USERCODE + code, Type.TEXT)
                        .withLabel("Code " + code).withDescription("Set the user code (4 to 10 numbers)")
                        .withDefault("").withGroupName("usercode").build());
            }
        }

        List<ParameterOption> options = new ArrayList<ParameterOption>();
        options.add(new ParameterOption(ZWaveBindingConstants.ACTION_CHECK_VALUE.toString(), "Do"));

        // If we're FAILED, allow removing from the controller
        // if (node.getNodeState() == ZWaveNodeState.FAILED) {
        parameters.add(ConfigDescriptionParameterBuilder.create("action_remove", Type.INTEGER)
                .withLabel("Remove device from controller").withAdvanced(true).withOptions(options)
                .withDefault("-232323").withGroupName("actions").build());
        // } else {
        // Otherwise, allow us to put this on the failed list
        parameters.add(ConfigDescriptionParameterBuilder.create("action_failed", Type.INTEGER)
                .withLabel("Set device as FAILed").withAdvanced(true).withOptions(options).withDefault("-232323")
                .withGroupName("actions").build());
        // }

        if (node.isInitializationComplete() == true) {
            parameters.add(ConfigDescriptionParameterBuilder.create("action_reinit", Type.INTEGER)
                    .withLabel("Reinitialise the device").withAdvanced(true).withOptions(options).withDefault("-232323")
                    .withGroupName("actions").build());
        }
        parameters.add(ConfigDescriptionParameterBuilder.create("action_heal", Type.INTEGER)
                .withLabel("Heal the device").withAdvanced(true).withOptions(options).withDefault("-232323")
                .withGroupName("actions").build());

        return new ConfigDescription(uri, parameters, groups);

    }

    private static void initialiseZWaveThings() {
        // Check that we know about the registry
        if (thingTypeRegistry == null) {
            return;
        }

        synchronized (productIndexLock) {
            zwaveThingTypeUIDList = new HashSet<ThingTypeUID>();
            productIndex = new ArrayList<ZWaveProduct>();

            // Get all the thing types
            Collection<ThingType> thingTypes = thingTypeRegistry.getThingTypes();
            for (ThingType thingType : thingTypes) {
                // Is this for our binding?
                if (ZWaveBindingConstants.BINDING_ID.equals(thingType.getBindingId()) == false) {
                    continue;
                }

                // Create a list of all things supported by this binding
                zwaveThingTypeUIDList.add(thingType.getUID());

                // Get the properties
                Map<String, String> thingProperties = thingType.getProperties();

                if (thingProperties.get(ZWaveBindingConstants.PROPERTY_XML_REFERENCES) == null) {
                    logger.debug("ZWave product {} has no references!", thingType.getUID());
                    continue;
                }

                String[] references = thingProperties.get(ZWaveBindingConstants.PROPERTY_XML_REFERENCES).split(",");
                for (String ref : references) {
                    String[] values = ref.split(":");
                    Integer type;
                    Integer id = null;
                    if (values.length != 2) {
                        logger.debug("ZWave product {} has invalid references! '{}'", thingType.getUID(),
                                thingProperties.get(ZWaveBindingConstants.PROPERTY_XML_REFERENCES));
                        continue;
                    }

                    type = Integer.parseInt(values[0], 16);
                    if (!values[1].trim().equals("*")) {
                        id = Integer.parseInt(values[1], 16);
                    }
                    String versionMin = thingProperties.get(ZWaveBindingConstants.PROPERTY_XML_VERSIONMIN);
                    String versionMax = thingProperties.get(ZWaveBindingConstants.PROPERTY_XML_VERSIONMAX);
                    productIndex.add(new ZWaveProduct(thingType.getUID(),
                            Integer.parseInt(thingProperties.get(ZWaveBindingConstants.PROPERTY_XML_MANUFACTURER), 16),
                            type, id, versionMin, versionMax));
                }
            }
        }
    }

    public static synchronized List<ZWaveProduct> getProductIndex() {
        if (productIndex.size() == 0) {
            initialiseZWaveThings();
        }
        return productIndex;
    }

    public static Set<ThingTypeUID> getSupportedThingTypes() {
        if (zwaveThingTypeUIDList.size() == 0) {
            initialiseZWaveThings();
        }
        return zwaveThingTypeUIDList;
    }

    public static ThingType getThingType(ThingTypeUID thingTypeUID) {
        // Check that we know about the registry
        if (thingTypeRegistry == null) {
            return null;
        }

        return thingTypeRegistry.getThingType(thingTypeUID);
    }

    public static ThingType getThingType(ZWaveNode node) {
        // Check that we know about the registry
        if (thingTypeRegistry == null) {
            return null;
        }

        for (ZWaveProduct product : ZWaveConfigProvider.getProductIndex()) {
            if (product.match(node) == true) {
                return thingTypeRegistry.getThingType(product.thingTypeUID);
            }
        }
        return null;
    }

    public static ConfigDescription getThingTypeConfig(ThingType type) {
        // Check that we know about the registry
        if (configDescriptionRegistry == null) {
            return null;
        }

        return configDescriptionRegistry.getConfigDescription(type.getConfigDescriptionURI());
    }

    public static Thing getThing(ThingUID thingUID) {
        // Check that we know about the registry
        if (thingRegistry == null) {
            return null;
        }

        return thingRegistry.get(thingUID);
    }

    /**
     * Check if this node supports a controllable command class
     *
     * @param node the {@link ZWaveNode)
     * @return true if a controllable class is supported
     */
    private boolean supportsControllableClass(ZWaveNode node) {
        for (CommandClass commandClass : controllableClasses) {
            if (node.supportsCommandClass(commandClass) == true) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Collection<ParameterOption> getParameterOptions(URI uri, String param, Locale locale) {
        // We need to update the options of all requests for association groups...
        if (!"thing".equals(uri.getScheme())) {
            return null;
        }

        ThingUID thingUID = new ThingUID(uri.getSchemeSpecificPart());
        ThingType thingType = thingTypeRegistry.getThingType(thingUID.getThingTypeUID());
        if (thingType == null) {
            return null;
        }

        // Is this a zwave thing?
        if (!thingUID.getBindingId().equals(ZWaveBindingConstants.BINDING_ID)) {
            return null;
        }

        // And is it an association group?
        if (!param.startsWith("group_")) {
            return null;
        }

        // And make sure this is a node because we want to get the id off the end...
        if (!thingUID.getId().startsWith("node")) {
            return null;
        }
        int nodeId = Integer.parseInt(thingUID.getId().substring(4));

        Thing thing = getThing(thingUID);
        ThingUID bridgeUID = thing.getBridgeUID();

        // Get the controller for this thing
        Thing bridge = getThing(bridgeUID);
        if (bridge == null) {
            return null;
        }

        // Get its handler
        ZWaveControllerHandler handler = (ZWaveControllerHandler) bridge.getHandler();

        boolean supportsMultiInstanceAssociation = false;
        ZWaveNode myNode = handler.getNode(nodeId);
        if (myNode.getCommandClass(CommandClass.MULTI_INSTANCE_ASSOCIATION) != null) {
            supportsMultiInstanceAssociation = true;
        }

        List<ParameterOption> options = new ArrayList<ParameterOption>();

        // Add the controller (ie openHAB) to the top of the list
        options.add(new ParameterOption("node_" + handler.getOwnNodeId() + "_0", "openHAB Controller"));

        // And iterate over all its nodes
        Collection<ZWaveNode> nodes = handler.getNodes();
        for (ZWaveNode node : nodes) {
            // Don't add its own id or the controller
            if (node.getNodeId() == nodeId || node.getNodeId() == handler.getOwnNodeId()) {
                continue;
            }

            // Get this nodes thing so we can find the name
            // TODO: Add this when thing names are supported!
            // Thing thingNode = getThing(thingUID);

            // Add the node for the standard association class if it supports a controllable class
            if (supportsControllableClass(node)) {
                // TODO: Use the node name
                options.add(new ParameterOption("node_" + node.getNodeId() + "_0", "Node " + node.getNodeId()));
            }

            // If the device supports multi_instance_association class, then add all controllable endpoints as well...
            // If this node also supports multi_instance class
            if (supportsMultiInstanceAssociation == true && node.getCommandClass(CommandClass.MULTI_INSTANCE) != null) {
                // Loop through all the endpoints for this device and add any that are controllable

                // for(node.get)
                // options.add(new ParameterOption("node" + node.getNodeId() + "." + endpointId, "Node " +
                // node.getNodeId()));
            }
        }

        return Collections.unmodifiableList(options);
    }
}
