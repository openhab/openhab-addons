package org.openhab.binding.insteonplm.handler;

import java.io.IOException;
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
import org.openhab.binding.insteonplm.internal.message.FieldException;
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
        for (DeviceFeature feature : m_features.values()) {
            if (feature.isReferencedByItem(channelUID.getId())) {
                feature.handleCommand(channelUID, command);
            }
        }

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

    /**
     * Helper method to make standard message
     *
     * @param flags
     * @param cmd1
     * @param cmd2
     * @return standard message
     * @throws FieldException
     * @throws IOException
     */
    public Msg makeStandardMessage(byte flags, byte cmd1, byte cmd2) throws FieldException, IOException {
        return (makeStandardMessage(flags, cmd1, cmd2, -1));
    }

    /**
     * Helper method to make standard message, possibly with group
     *
     * @param flags
     * @param cmd1
     * @param cmd2
     * @param group (-1 if not a group message)
     * @return standard message
     * @throws FieldException
     * @throws IOException
     */
    public Msg makeStandardMessage(byte flags, byte cmd1, byte cmd2, int group) throws FieldException, IOException {
        Msg m = Msg.s_makeMessage("SendStandardMessage");
        InsteonAddress addr = null;
        if (group != -1) {
            flags |= 0xc0; // mark message as group message
            // and stash the group number into the address
            addr = new InsteonAddress((byte) 0, (byte) 0, (byte) (group & 0xff));
        } else {
            addr = getAddress();
        }
        m.setAddress("toAddress", addr);
        m.setByte("messageFlags", flags);
        m.setByte("command1", cmd1);
        m.setByte("command2", cmd2);
        return m;
    }

    public Msg makeX10Message(byte rawX10, byte X10Flag) throws FieldException, IOException {
        Msg m = Msg.s_makeMessage("SendX10Message");
        m.setByte("rawX10", rawX10);
        m.setByte("X10Flag", X10Flag);
        m.setQuietTime(300L);
        return m;
    }

    /**
     * Helper method to make extended message
     *
     * @param flags
     * @param cmd1
     * @param cmd2
     * @return extended message
     * @throws FieldException
     * @throws IOException
     */
    public Msg makeExtendedMessage(byte flags, byte cmd1, byte cmd2) throws FieldException, IOException {
        return makeExtendedMessage(flags, cmd1, cmd2, new byte[] {});
    }

    /**
     * Helper method to make extended message
     *
     * @param flags
     * @param cmd1
     * @param cmd2
     * @param data array with userdata
     * @return extended message
     * @throws FieldException
     * @throws IOException
     */
    public Msg makeExtendedMessage(byte flags, byte cmd1, byte cmd2, byte[] data) throws FieldException, IOException {
        Msg m = Msg.s_makeMessage("SendExtendedMessage");
        m.setAddress("toAddress", getAddress());
        m.setByte("messageFlags", (byte) (((flags & 0xff) | 0x10) & 0xff));
        m.setByte("command1", cmd1);
        m.setByte("command2", cmd2);
        m.setUserData(data);
        m.setCRC();
        return m;
    }

    /**
     * Helper method to make extended message, but with different CRC calculation
     *
     * @param flags
     * @param cmd1
     * @param cmd2
     * @param data array with user data
     * @return extended message
     * @throws FieldException
     * @throws IOException
     */
    public Msg makeExtendedMessageCRC2(byte flags, byte cmd1, byte cmd2, byte[] data)
            throws FieldException, IOException {
        Msg m = Msg.s_makeMessage("SendExtendedMessage");
        m.setAddress("toAddress", getAddress());
        m.setByte("messageFlags", (byte) (((flags & 0xff) | 0x10) & 0xff));
        m.setByte("command1", cmd1);
        m.setByte("command2", cmd2);
        m.setUserData(data);
        m.setCRC2();
        return m;
    }
}
