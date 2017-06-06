package org.openhab.binding.insteonplm.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.joda.time.DateTime;
import org.openhab.binding.insteonplm.InsteonPLMBindingConstants;
import org.openhab.binding.insteonplm.internal.config.PollingHandlerInfo;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.GroupMessageStateMachine;
import org.openhab.binding.insteonplm.internal.device.InsteonAddress;
import org.openhab.binding.insteonplm.internal.device.MessageHandler;
import org.openhab.binding.insteonplm.internal.device.PollHandler;
import org.openhab.binding.insteonplm.internal.driver.Port;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.InsteonFlags;
import org.openhab.binding.insteonplm.internal.message.InsteonFlags.MessageType;
import org.openhab.binding.insteonplm.internal.message.modem.SendInsteonMessage;
import org.openhab.binding.insteonplm.internal.message.modem.StandardMessageReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class InsteonThingHandler extends InsteonPlmBaseThing {
    private static Logger logger = LoggerFactory.getLogger(InsteonThingHandler.class);

    /** need to wait after query to avoid misinterpretation of duplicate replies */
    private static final int QUIET_TIME_DIRECT_MESSAGE = 2000;
    /** how far to space out poll messages */
    private static final int TIME_BETWEEN_POLL_MESSAGES = 1500;

    private Map<ChannelUID, List<DeviceFeature>> featureChannelMapping = Maps.newHashMap();
    private Map<ChannelUID, PollingHandlerInfo> pollHandlers = Maps.newHashMap();
    public Map<Integer, GroupMessageStateMachine> groupState = Maps.newHashMap();
    private Long lastTimePolled;
    // Default to 10 days ago.
    private DateTime lastMessageReceived = DateTime.now().minusDays(10);
    private InsteonAddress address;
    private SendInsteonMessage requestMessage;
    private PriorityQueue<InsteonThingMessageQEntry> requestQueue = new PriorityQueue<InsteonThingMessageQEntry>();
    private long lastTimeQueried = 0;
    private int noReplyToRequest = 0;
    private int directMessageSent = 0;
    private int broadcastMessageSent = 0;
    private int messagesReceived = 0;

    public InsteonThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        address = new InsteonAddress(
                this.getThing().getProperties().get(InsteonPLMBindingConstants.PROPERTY_INSTEON_ADDRESS));
        if (address == null) {
            logger.error("Address is not set in {}", this.getThing().getUID());
            return;
        }

        String productKey = this.getThing().getProperties()
                .get(InsteonPLMBindingConstants.PROPERTY_INSTEON_PRODUCT_KEY);
        if (productKey == null) {
            logger.error("Product Key is not set in {}", this.getThing().getUID());
            return;
        }

        // TODO: Shouldn't the framework do this for us???
        Bridge bridge = getBridge();
        if (bridge != null) {
            bridgeStatusChanged(bridge.getStatusInfo());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Lookup the features to do stuff.
        for (DeviceFeature feature : featureChannelMapping.get(channelUID)) {
            feature.handleCommand(this, channelUID, command);
        }
    }

    /**
     * Handles the message received over the channel to the modem.
     *
     * @throws FieldException
     */
    public void handleMessage(StandardMessageReceived message) throws FieldException {
        lastMessageReceived = DateTime.now();
        messagesReceived++;

        // We have an ack and we have a request message. Yay.
        if (message.getFlags().getMessageType() == MessageType.AckOfDirect && requestMessage != null) {
            // We have the request message and this is an ack for it. Yay. Let it fall
            // all the way down to the handlers too.
            requestMessage = null;
        }

        // For normal direct messages we do this. Yay!
        for (ChannelUID channelId : featureChannelMapping.keySet()) {
            List<DeviceFeature> features = featureChannelMapping.get(channelId);
            for (DeviceFeature feature : features) {
                List<MessageHandler> allHandlers = feature.getMsgHandlers().get(message.getCmd1());
                if (allHandlers != null) {
                    for (MessageHandler handler : allHandlers) {
                        if (handler.matches(message)) {
                            handler.handleMessage(this, -1, message, getThing().getChannel(channelId.getId()));
                        }
                    }
                }
            }
        }

        // Update the status of the last message received channel.
        updateState(InsteonPLMBindingConstants.CHANNEL_LAST_MESSAGE_RECIEVED,
                new DateTimeType(lastMessageReceived.toGregorianCalendar()));
        updateState(InsteonPLMBindingConstants.CHANNEL_SUCCESSFULY_SENT,
                new DecimalType(this.directMessageSent - this.noReplyToRequest));
        updateState(InsteonPLMBindingConstants.CHANNEL_LAST_MESSAGE_RECIEVED, new DecimalType(this.noReplyToRequest));
    }

    /** The address for this thing. */
    public InsteonAddress getAddress() {
        return address;
    }

    /**
     * The product key for this specific thing.
     */
    public String getProductKey() {
        return getThing().getProperties().get(InsteonPLMBindingConstants.PROPERTY_INSTEON_PRODUCT_KEY);
    }

    /**
     * The device category key for this specific thing.
     */
    public String getDeviceCategory() {
        return getThing().getProperties().get(InsteonPLMBindingConstants.PROPERTY_INSTEON_CATEGORY);
    }

    /**
     * The device category key for this specific thing.
     */
    public String getDeviceSubCategory() {
        return getThing().getProperties().get(InsteonPLMBindingConstants.PROPERTY_INSTEON_SUBCATEGORY);
    }

    /**
     * Execute poll on this device: create an array of messages,
     * add them to the request queue, and schedule the queue
     * for processing.
     *
     * @param delay scheduling delay (in milliseconds)
     */
    public void doPoll(long timeDelayPollRelatedMsec) {
        long now = System.currentTimeMillis();
        ArrayList<InsteonThingMessageQEntry> l = new ArrayList<InsteonThingMessageQEntry>();
        synchronized (pollHandlers) {
            int spacing = 0;
            for (PollingHandlerInfo i : pollHandlers.values()) {
                PollHandler pollHandler = i.getPollHandler();
                SendInsteonMessage message = pollHandler.makeMsg(this);
                l.add(new InsteonThingMessageQEntry(message, now + timeDelayPollRelatedMsec + spacing));
                spacing += TIME_BETWEEN_POLL_MESSAGES;
            }
        }
        if (l.isEmpty()) {
            return;
        }
        synchronized (requestQueue) {
            for (InsteonThingMessageQEntry e : l) {
                requestQueue.add(e);
            }
        }
        // Update the data to send if needed.
        getInsteonBridge().addThingToSendingQueue(this, requestQueue.peek().getExpirationTime());
        if (!l.isEmpty()) {
            synchronized (lastTimePolled) {
                lastTimePolled = now;
            }
        }
    }

    /** The bridge associated with this thing. */
    InsteonPLMBridgeHandler getInsteonBridge() {
        return (InsteonPLMBridgeHandler) getBridge();
    }

    /**
     * Adds this message into the output queue for this thing.
     *
     * @param mess The message to queue
     * @param feature The feature it is associated with (for acks)
     */
    public void enqueueMessage(SendInsteonMessage mess) {
        enqueueDelayedMessage(mess, 0L);
    }

    /**
     * Adds this message into the output queue for this thing with a delay.
     *
     * @param message The message to queue
     * @param feature The feature it is associated with (for acks)
     */
    public void enqueueDelayedMessage(SendInsteonMessage message, long delay) {
        synchronized (requestQueue) {
            requestQueue.add(new InsteonThingMessageQEntry(message, System.currentTimeMillis() + delay));
            updateState(InsteonPLMBindingConstants.CHANNEL_PENDING_WRITE, new DecimalType(this.requestQueue.size()));
        }
        if (message.getFlags().getMessageType() != InsteonFlags.MessageType.BroadcastMessage
                && message.getFlags().getMessageType() != InsteonFlags.MessageType.GroupBroadcastMessage) {
            message.setQuietTime(QUIET_TIME_DIRECT_MESSAGE);
        }
        getInsteonBridge().addThingToSendingQueue(this, requestQueue.peek().getExpirationTime());
        logger.trace("enqueueing message with delay {}", delay);
    }

    public DeviceFeature getFeatureQueried() {
        // TODO Auto-generated method stub
        return null;
    }

    /** The max value to set the dimmer too. This can be configured on a per-thing basis. */
    public int getDimmerMax() {
        return 100;
    }

    /** The group the insteon product is in. */
    public int getInsteonGroup() {
        return -1;
    }

    public byte getX10HouseCode() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getX10UnitCode() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Updates the channel based on the new input state. This will lookup the correct channel
     * from the feature details and then update it.
     *
     * @param f the feature details to use for lookup
     * @param newState the new state to broadcast
     */
    public void updateFeatureState(Channel channel, State newState) {
        updateState(channel.getUID(), newState);
    }

    /**
     * Do a poll for this specific feature.
     *
     * @param doItNow If we should do the poll now, or wait a bit
     */
    public void pollChannel(Channel channel, boolean doItNow) {
        // Find the channel the feature is on.
        PollingHandlerInfo handler = pollHandlers.get(channel.getUID().getId());
        if (handler != null) {
            SendInsteonMessage mess = handler.getPollHandler().makeMsg(this);
            if (doItNow) {
                enqueueMessage(mess);
            } else {
                enqueueDelayedMessage(mess, 1000);
            }
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            logger.debug("NODE {}: controller is offline.", getAddress().toString());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        logger.debug("NODE {}: controller is online, starting initialization.", getAddress().toString());
        initializeInsteon();
    }

    private void initializeInsteon() {
        featureChannelMapping = Maps.newHashMap();
        for (Channel channel : getThing().getChannels()) {
            // Process the channel properties and configuration
            Map<String, String> properties = channel.getProperties();

            logger.debug("NODE {}: Initialising channel {}", getAddress(), channel.getUID());

            // Get the property that defines the feature to use for this channel.
            String featureNames = properties.get(InsteonPLMBindingConstants.PROPERTY_CHANNEL_FEATURE);

            String[] features = featureNames.split(",");
            featureChannelMapping.put(channel.getUID(), Lists.<DeviceFeature> newArrayList());
            for (String name : features) {
                DeviceFeature feature = getInsteonBridge().getDeviceFeatureFactory().makeDeviceFeature(name);
                featureChannelMapping.get(channel.getUID()).add(feature);
            }

            String pollHandlerData = properties.get(InsteonPLMBindingConstants.PROPERTY_CHANNEL_POLL_HANDLER);
            if (pollHandlerData != null) {
                PollingHandlerInfo info = new PollingHandlerInfo(pollHandlerData);
                if (info.getPollHandlerType() != null) {
                    PollHandler pollHandler = getInsteonBridge().getDeviceFeatureFactory()
                            .makePollHandler(info.getPollHandlerType());
                    info.setPollHandler(pollHandler);
                    pollHandlers.put(channel.getUID(), info);
                }
            }
        }
        doPoll(0);
    }

    /** Work out the ramp time from another channel. */
    public double getRampTime() {

        return 1;
    }

    /**
     * @return The default flags to send the message with.
     */
    public InsteonFlags getDefaultFlags() {
        InsteonFlags flags = new InsteonFlags();
        if (getInsteonGroup() != -1) {
            flags.setMessageType(InsteonFlags.MessageType.GroupBroadcastMessage);
        }
        return flags;
    }

    /**
     * The request queue for all the pending messages to be sent.
     */
    public PriorityQueue<InsteonThingMessageQEntry> getRequestQueue() {
        return requestQueue;
    }

    /**
     * Goes through the request queue and handles sending the next message, or not.
     *
     * @return the updated wait time for this queue
     * @throws IOException
     */
    @Override
    public long processRequestQueue(Port port, long now) throws IOException {
        synchronized (requestQueue) {
            if (requestQueue.isEmpty()) {
                return 0;
            }
            // We have a pending message.
            if (requestMessage != null) {
                long dt = now - (lastTimeQueried + requestMessage.getDirectAckTimeout());
                if (dt < 0) {
                    logger.debug("Still waiting for query reply from {} for another {} usec", getAddress(), -dt);
                    // Try again in -dt milliseconds
                    return System.currentTimeMillis() - dt + 20;
                } else {
                    logger.debug("gave up waiting for query reply from device {}", getAddress());
                    // TODO: track stats on how often messages fail to various devices.
                    noReplyToRequest++;
                }
            }
            InsteonThingMessageQEntry entry = requestQueue.poll();
            if (entry.getMsg().getFlags().getMessageType() == MessageType.BroadcastMessage
                    || entry.getMsg().getFlags().getMessageType() == MessageType.GroupBroadcastMessage) {
                // Broadcast message. Yay!
                logger.debug("Broadcast message on queue {} {}", getAddress(), entry.getMsg());
                requestMessage = null;
                broadcastMessageSent++;
            } else {
                // Direct message, exciting.
                logger.debug("Direct message on queue {} {}", getAddress(), entry.getMsg());
                lastTimeQueried = now;
                requestMessage = entry.getMsg();
                directMessageSent++;
            }
            port.writeMessage(entry.getMsg());
            updateState(InsteonPLMBindingConstants.CHANNEL_PENDING_WRITE, new DecimalType(this.requestQueue.size()));
        }
        return 0;
    }
}
