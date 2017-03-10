package org.openhab.binding.insteonplm.handler;

import org.openhab.binding.insteonplm.internal.message.modem.SendInsteonMessage;

/**
 * Queue entry helper class
 *
 * @author Bernd Pfrommer
 */
class InsteonThingMessageQEntry implements Comparable<InsteonThingMessageQEntry> {
    private SendInsteonMessage m_msg = null;
    private long m_expirationTime = 0L;

    public SendInsteonMessage getMsg() {
        return m_msg;
    }

    public long getExpirationTime() {
        return m_expirationTime;
    }

    public InsteonThingMessageQEntry(SendInsteonMessage message, long t) {
        m_msg = message;
        m_expirationTime = t;
    }

    @Override
    public int compareTo(InsteonThingMessageQEntry a) {
        return (int) (m_expirationTime - a.m_expirationTime);
    }
}
