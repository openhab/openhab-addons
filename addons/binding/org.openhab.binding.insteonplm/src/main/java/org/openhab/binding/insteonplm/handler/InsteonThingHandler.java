package org.openhab.binding.insteonplm.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.joda.time.DateTime;
import org.openhab.binding.insteonplm.InsteonPLMBindingConstants;
import org.openhab.binding.insteonplm.InsteonPLMBindingConstants.ExtendedData;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.InsteonAddress;
import org.openhab.binding.insteonplm.internal.device.MessageDispatcher;
import org.openhab.binding.insteonplm.internal.device.PollHandler;
import org.openhab.binding.insteonplm.internal.message.Message;
import org.openhab.binding.insteonplm.internal.message.MessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class InsteonThingHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(InsteonThingHandler.class);
    /** need to wait after query to avoid misinterpretation of duplicate replies */
    private static final int QUIET_TIME_DIRECT_MESSAGE = 2000;
    /** how far to space out poll messages */
    private static final int TIME_BETWEEN_POLL_MESSAGES = 1500;

    private Map<ChannelUID, List<DeviceFeature>> featureChannelMapping = Maps.newHashMap();
    private Map<ChannelUID, PollHandler> pollHandlers = Maps.newHashMap();
    private PriorityQueue<InsteonThingMessageQEntry> requestQueue = new PriorityQueue<InsteonThingMessageQEntry>();
    private Long lastTimePolled;
    // Default to 10 days ago.
    private DateTime lastMessageReceived = DateTime.now().minusDays(10);
    private InsteonAddress address;

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
     */
    public void handleMessage(Message message) {
        lastMessageReceived = DateTime.now();
        // Send the message to all the features on this thing to be processed.
        for (List<DeviceFeature> features : featureChannelMapping.values()) {
            for (DeviceFeature feature : features) {
                feature.handleMessage(this, message);
            }
        }
        // Update the status of the last message received channel.
        updateState(new ChannelUID("lastMessageReceived"), new DateTimeType(lastMessageReceived.toGregorianCalendar()));
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
     * Execute poll on this device: create an array of messages,
     * add them to the request queue, and schedule the queue
     * for processing.
     *
     * @param delay scheduling delay (in milliseconds)
     */
    public void doPoll(long timeDelayPollRelatedMsec) {
        long now = System.currentTimeMillis();
        ArrayList<InsteonThingMessageQEntry> l = new ArrayList<InsteonThingMessageQEntry>();
        synchronized (featureChannelMapping) {
            int spacing = 0;
            for (List<DeviceFeature> i : featureChannelMapping.values()) {
                for (DeviceFeature feature : i) {
                    Message m = feature.makePollMsg(this);
                    if (m != null) {
                        l.add(new InsteonThingMessageQEntry(feature, m, now + timeDelayPollRelatedMsec + spacing));
                        spacing += TIME_BETWEEN_POLL_MESSAGES;
                    }
                }
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

    /** The message factory to use when making messages. */
    public MessageFactory getMessageFactory() {
        return getInsteonBridge().getMessageFactory();
    }

    static class FeatureDetails {
        private final DeviceFeature feature;
        private final MessageDispatcher dispatcher;

        public FeatureDetails(DeviceFeature feature, MessageDispatcher dispatcher) {
            this.feature = feature;
            this.dispatcher = dispatcher;
        }

        public DeviceFeature getFeature() {
            return feature;
        }

        public MessageDispatcher getDispatcher() {
            return dispatcher;
        }
    }

    /**
     * Adds this message into the output queue for this thing.
     *
     * @param message The message to queue
     * @param feature The feature it is associated with (for acks)
     */
    public void enqueueMessage(Message message, DeviceFeature feature) {
        enqueueDelayedMessage(message, feature, 0L);
    }

    /**
     * Adds this message into the output queue for this thing with a delay.
     *
     * @param message The message to queue
     * @param feature The feature it is associated with (for acks)
     */
    public void enqueueDelayedMessage(Message message, DeviceFeature feature, long delay) {
        synchronized (requestQueue) {
            requestQueue.add(new InsteonThingMessageQEntry(feature, message, System.currentTimeMillis() + delay));
        }
        if (!message.isBroadcast()) {
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
    public void pollFeature(DeviceFeature feature, boolean doItNow) {
        Message mess = feature.getPollHandler().makeMsg(this);
        if (mess != null) {
            if (doItNow) {
                enqueueMessage(mess, feature);
            } else {
                enqueueDelayedMessage(mess, feature, 1000);
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
                String[] typeArgs = pollHandlerData.split(":");
                if (typeArgs.length != 2) {
                    logger.error("NODE {}: Invalid poll data for channel {} -- {}", getAddress(), channel.getUID(),
                            pollHandlerData);
                } else {
                    String[] args = typeArgs[1].split(",");
                    ExtendedData extendedData = InsteonPLMBindingConstants.ExtendedData.valueOf(args[0]);
                    byte cmd1 = fromHexString(args[1]);
                    byte cmd2 = fromHexString(args[2]);
                    PollHandler pollHandler = getInsteonBridge().getDeviceFeatureFactory().makePollHandler(args[0]);
                    pollHandler.setCmd1(cmd1);
                    pollHandler.setCmd2(cmd2);
                    pollHandler.setExtended(extendedData);
                    if (args.length > 3) {
                        byte data1 = fromHexString(args[3]);
                        byte data2 = fromHexString(args[4]);
                        byte data3 = fromHexString(args[5]);
                        pollHandler.setData1(data1);
                        pollHandler.setData2(data2);
                        pollHandler.setData3(data3);
                    }
                    // Now remember the poll handler...
                    pollHandlers.put(channel.getUID(), pollHandler);
                }
            }
        }
        doPoll(0);
    }

    private byte fromHexString(String str) {
        if (str.startsWith("0x")) {
            return Byte.parseByte(str.substring(2));
        } else {
            return Byte.parseByte(str);
        }
    }

    public PriorityQueue<InsteonThingMessageQEntry> getRequestQueue() {
        return requestQueue;
    }
}
