/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.insteonplm.internal.device;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A command handler translates an openHAB command into a insteon message
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public abstract class CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);
    DeviceFeature m_feature = null; // related DeviceFeature
    HashMap<String, String> m_parameters = new HashMap<String, String>();

    /**
     * Constructor
     *
     * @param feature The DeviceFeature for which this command was intended.
     *            The openHAB commands are issued on an openhab item. The .items files bind
     *            an openHAB item to a DeviceFeature.
     */
    protected CommandHandler(DeviceFeature feature) {
        m_feature = feature;
    }

    /**
     * Implements what to do when an openHAB command is received
     *
     * @param config the configuration for the item that generated the command
     * @param cmd the openhab command issued
     * @param device the Insteon device to which this command applies
     */
    public abstract void handleCommand(ChannelUID channel, Command cmd, InsteonThingHandler handler);

    /**
     * Returns parameter as integer
     *
     * @param key key of parameter
     * @param def default
     * @return value of parameter
     */
    protected int getIntParameter(String key, int def) {
        String val = m_parameters.get(key);
        if (val == null) {
            return (def); // param not found
        }
        int ret = def;
        try {
            ret = Utils.strToInt(val);
        } catch (NumberFormatException e) {
            logger.error("malformed int parameter in command handler: {}", key);
        }
        return ret;
    }

    /**
     * Returns parameter as String
     *
     * @param key key of parameter
     * @param def default
     * @return value of parameter
     */
    protected String getStringParameter(String key, String def) {
        return (m_parameters.get(key) == null ? def : m_parameters.get(key));
    }

    /**
     * Shorthand to return class name for logging purposes
     *
     * @return name of the class
     */
    protected String nm() {
        return (this.getClass().getSimpleName());
    }

    protected int getMaxLightLevel(InsteonThingHandler conf, int defaultLevel) {
        Map<String, String> params = conf.getThing().getProperties();
        if (conf.getFeature().contains("dimmer") && params.containsKey("dimmermax")) {
            String item = conf.getThing().getLabel();
            String dimmerMax = params.get("dimmermax");
            try {
                int i = Integer.parseInt(dimmerMax);
                if (i > 1 && i <= 99) {
                    int level = (int) Math.ceil((i * 255.0) / 100); // round up
                    if (level < defaultLevel) {
                        logger.info("item {}: using dimmermax value of {}", item, dimmerMax);
                        return level;
                    }
                } else {
                    logger.error("item {}: dimmermax must be between 1-99 inclusive: {}", item, dimmerMax);
                }
            } catch (NumberFormatException e) {
                logger.error("item {}: invalid int value for dimmermax: {}", item, dimmerMax);
            }
        }

        return defaultLevel;
    }

    void setParameters(HashMap<String, String> hm) {
        m_parameters = hm;
    }

    /**
     * Returns the feature of this command.
     *
     * @return
     */
    public DeviceFeature getFeature() {
        return m_feature;
    }

    /**
     * Helper function to extract the group parameter from the binding config,
     *
     * @param c the binding configuration to test
     * @return the value of the "group" parameter, or -1 if none
     */
    protected int getGroup(InsteonThingHandler c) {
        String v = c.getThing().getProperties().get("group");
        int iv = -1;
        try {
            iv = (v == null) ? -1 : Utils.strToInt(v);
        } catch (NumberFormatException e) {
            logger.error("malformed int parameter in for item {}", c.getThing().getLabel());
        }
        return iv;
    }
}
