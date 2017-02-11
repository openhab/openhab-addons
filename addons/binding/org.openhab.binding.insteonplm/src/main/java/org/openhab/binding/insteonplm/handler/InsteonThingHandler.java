package org.openhab.binding.insteonplm.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.InsteonPLMBindingConstants;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.InsteonAddress;
import org.openhab.binding.insteonplm.internal.message.Msg;

public class InsteonThingHandler extends BaseThingHandler {
    /** need to wait after query to avoid misinterpretation of duplicate replies */
    private static final int QUIET_TIME_DIRECT_MESSAGE = 2000;
    /** how far to space out poll messages */
    private static final int TIME_BETWEEN_POLL_MESSAGES = 1500;

    private HashMap<String, DeviceFeature> m_features = new HashMap<String, DeviceFeature>();
    private PriorityQueue<QEntry> m_requestQueue = new PriorityQueue<QEntry>();
    private Long m_lastTimePolled;

    public InsteonThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Lookup the features to do stuff.
    }

    /** The address for this thing. */
    public InsteonAddress getAddress() {
        String data = getThing().getProperties().get(InsteonPLMBindingConstants.PROPERTY_INSTEON_ADDRESS);
        return new InsteonAddress(data);
    }

    /**
     * The feature for this specific thing.
     */
    public String getFeature() {
        return getThing().getProperties().get(InsteonPLMBindingConstants.PROPERTY_INSTEON_FEATURE);
    }

    /**
     * The produce key for this specific thing.
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
        ArrayList<QEntry> l = new ArrayList<QEntry>();
        synchronized (m_features) {
            int spacing = 0;
            for (DeviceFeature i : m_features.values()) {
                if (i.hasListeners()) {
                    Msg m = i.makePollMsg();
                    if (m != null) {
                        l.add(new QEntry(i, m, now + timeDelayPollRelatedMsec + spacing));
                        spacing += TIME_BETWEEN_POLL_MESSAGES;
                    }
                }
            }
        }
        if (l.isEmpty()) {
            return;
        }
        synchronized (m_requestQueue) {
            for (QEntry e : l) {
                m_requestQueue.add(e);
            }
        }
        getInsteonBridge().addQueue(this, now + timeDelayPollRelatedMsec);

        if (!l.isEmpty()) {
            synchronized (m_lastTimePolled) {
                m_lastTimePolled = now;
            }
        }
    }

    InsteonPLMBridgeHandler getInsteonBridge() {
        return (InsteonPLMBridgeHandler) getBridge();
    }

    /**
     * Queue entry helper class
     *
     * @author Bernd Pfrommer
     */
    static class QEntry implements Comparable<QEntry> {
        private DeviceFeature m_feature = null;
        private Msg m_msg = null;
        private long m_expirationTime = 0L;

        public DeviceFeature getFeature() {
            return m_feature;
        }

        public Msg getMsg() {
            return m_msg;
        }

        public long getExpirationTime() {
            return m_expirationTime;
        }

        public QEntry(DeviceFeature f, Msg m, long t) {
            m_feature = f;
            m_msg = m;
            m_expirationTime = t;
        }

        @Override
        public int compareTo(QEntry a) {
            return (int) (m_expirationTime - a.m_expirationTime);
        }
    }
}
