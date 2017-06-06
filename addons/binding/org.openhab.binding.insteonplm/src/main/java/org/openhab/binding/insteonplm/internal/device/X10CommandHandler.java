package org.openhab.binding.insteonplm.internal.device;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.X10ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class X10CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);
    X10DeviceFeature feature = null; // related DeviceFeature

    /**
     * Constructor
     *
     * @param feature The DeviceFeature for which this command was intended.
     *            The openHAB commands are issued on an openhab item. The .items files bind
     *            an openHAB item to a DeviceFeature.
     */
    protected X10CommandHandler(X10DeviceFeature feature) {
        this.feature = feature;
    }

    /**
     * Implements what to do when an openHAB command is received
     *
     * @param config the configuration for the item that generated the command
     * @param cmd the openhab command issued
     * @param device the Insteon device to which this command applies
     */
    public abstract void handleCommand(X10ThingHandler handler, ChannelUID channel, Command cmd);

    /**
     * Shorthand to return class name for logging purposes
     *
     * @return name of the class
     */
    protected String nm() {
        return (this.getClass().getSimpleName());
    }

    /**
     * Returns the feature of this command.
     *
     * @return
     */
    public X10DeviceFeature getFeature() {
        return feature;
    }
}
