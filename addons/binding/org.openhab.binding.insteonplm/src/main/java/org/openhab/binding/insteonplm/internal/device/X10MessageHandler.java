package org.openhab.binding.insteonplm.internal.device;

import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.insteonplm.handler.X10ThingHandler;
import org.openhab.binding.insteonplm.internal.message.X10Command;
import org.openhab.binding.insteonplm.internal.message.modem.X10MessageReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class X10MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    X10Command cmd;
    int cmd2 = -1;
    X10DeviceFeature feature;

    /**
     * Constructor
     *
     * @param p state publishing object for dissemination of state changes
     */
    protected X10MessageHandler(X10DeviceFeature p) {
        this.feature = p;
    }

    /**
     * Method that processes incoming message. The cmd1 parameter
     * has been extracted earlier already (to make a decision which message handler to call),
     * and is passed in as an argument so cmd1 does not have to be extracted from the message again.
     *
     * @param message the x10 message
     * @param channel the DeviceFeature to which this message handler is attached
     */
    public abstract void handleMessage(X10ThingHandler handler, X10MessageReceived message, Channel channel);

    /**
     * The feature associated with this message.
     */
    public X10DeviceFeature getFeature() {
        return feature;
    }

    public void setCmd1(String factor) {
        try {
            cmd = X10Command.valueOf(factor);
        } catch (NumberFormatException e) {
            logger.error("Unable to parse {}", e, factor);
        }

    }

    public void setCmd2(String factor) {
        try {
            cmd2 = Byte.valueOf(factor);
        } catch (NumberFormatException e) {
            logger.error("Unable to parse {}", e, factor);
        }

    }

    /**
     * Test if message matches the filter parameters
     *
     * @param message message to be tested against
     * @return true if message matches
     */
    public boolean matches(X10MessageReceived message) {
        if (message.isCommand()) {
            if (message.getCmd() == cmd) {
                return true;
            }
        }
        return false;
    }

    /**
     * Shorthand to return class name for logging purposes
     *
     * @return name of the class
     */
    protected String nm() {
        return (this.getClass().getSimpleName());
    }
}
