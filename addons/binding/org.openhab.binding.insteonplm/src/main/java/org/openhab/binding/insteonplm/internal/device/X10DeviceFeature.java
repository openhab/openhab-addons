package org.openhab.binding.insteonplm.internal.device;

import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.X10ThingHandler;
import org.openhab.binding.insteonplm.internal.device.commands.X10NoOpCommandHandler;
import org.openhab.binding.insteonplm.internal.device.messages.X10DefaultMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class X10DeviceFeature {
    private static final Logger logger = LoggerFactory.getLogger(X10DeviceFeature.class);

    private String name = "INVALID_FEATURE_NAME";
    private int directAckTimeout = 6000;

    private X10MessageHandler defaultMsgHandler = new X10DefaultMessageHandler(this);
    private X10CommandHandler defaultCommandHandler = new X10NoOpCommandHandler(this);

    private Map<Integer, List<X10MessageHandler>> msgHandlers = Maps.newHashMap();
    private Map<Class<? extends Command>, X10CommandHandler> commandHandlers = Maps.newHashMap();

    /**
     * Constructor
     *
     * @param name descriptive name for that feature
     */
    public X10DeviceFeature(String name) {
        this.name = name;
    }

    // various simple getters
    public String getName() {
        return name;
    }

    public int getDirectAckTimeout() {
        return directAckTimeout;
    }

    public X10MessageHandler getDefaultMsgHandler() {
        return defaultMsgHandler;
    }

    public Map<Integer, List<X10MessageHandler>> getMsgHandlers() {
        return this.msgHandlers;
    }

    // various simple setters
    public void setDefaultCommandHandler(X10CommandHandler ch) {
        defaultCommandHandler = ch;
    }

    public void setDefaultMsgHandler(X10MessageHandler mh) {
        defaultMsgHandler = mh;
    }

    public void setTimeout(String s) {
        if (s != null && !s.isEmpty()) {
            try {
                directAckTimeout = Integer.parseInt(s);
                logger.trace("ack timeout set to {}", directAckTimeout);
            } catch (NumberFormatException e) {
                logger.error("invalid number for timeout: {}", s);
            }
        }
    }

    /**
     * Called when an openhab command arrives for this device feature
     *
     * @param c the channel the command is on
     * @param cmd the command to be exectued
     */
    public void handleCommand(X10ThingHandler handler, ChannelUID c, Command cmd) {
        Class<? extends Command> key = cmd.getClass();
        X10CommandHandler h = commandHandlers.containsKey(key) ? commandHandlers.get(key) : defaultCommandHandler;
        logger.trace("{} uses {} to handle command {} for {}", getName(), h.getClass().getSimpleName(),
                key.getSimpleName(), handler.getAddress());
        h.handleCommand(handler, c, cmd);
    }

    /**
     * Adds a message handler to this device feature.
     *
     * @param cm1 The insteon cmd1 of the incoming message for which the handler should be used
     * @param handler the handler to invoke
     */
    public void addMessageHandler(int cm1, X10MessageHandler handler) {
        synchronized (msgHandlers) {
            List<X10MessageHandler> handlers = msgHandlers.get(cm1);
            if (handlers == null) {
                handlers = Lists.newArrayList();
                msgHandlers.put(cm1, handlers);
            }
            handlers.add(handler);
        }
    }

    /**
     * Adds a command handler to this device feature
     *
     * @param c the command for which this handler is invoked
     * @param handler the handler to call
     */
    public void addCommandHandler(Class<? extends Command> c, X10CommandHandler handler) {
        synchronized (commandHandlers) {
            commandHandlers.put(c, handler);
        }
    }

    /**
     * Turn DeviceFeature into String
     */
    @Override
    public String toString() {
        return name + " (" + commandHandlers.size() + ":" + msgHandlers.size() + ")";
    }
}
