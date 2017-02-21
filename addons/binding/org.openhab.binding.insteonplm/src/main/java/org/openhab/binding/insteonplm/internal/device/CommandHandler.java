/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.insteonplm.internal.device;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
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
    public abstract void handleCommand(InsteonThingHandler handler, ChannelUID channel, Command cmd);

    /**
     * Shorthand to return class name for logging purposes
     *
     * @return name of the class
     */
    protected String nm() {
        return (this.getClass().getSimpleName());
    }

    protected int getMaxLightLevel(InsteonThingHandler conf, int defaultLevel) {
        String item = conf.getThing().getLabel();
        int dimmerMax = conf.getDimmerMax();
        if (dimmerMax > 1 && dimmerMax <= 99) {
            int level = (int) Math.ceil((dimmerMax * 255.0) / 100); // round up
            if (level < defaultLevel) {
                logger.info("item {}: using dimmermax value of {}", item, dimmerMax);
                return level;
            }
        } else {
            logger.error("item {}: dimmermax must be between 1-99 inclusive: {}", item, dimmerMax);
        }
        return defaultLevel;
    }

    /**
     * Returns the feature of this command.
     *
     * @return
     */
    public DeviceFeature getFeature() {
        return m_feature;
    }
}
