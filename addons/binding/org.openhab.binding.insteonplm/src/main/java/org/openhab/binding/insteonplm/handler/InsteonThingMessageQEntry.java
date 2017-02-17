package org.openhab.binding.insteonplm.handler;

import org.openhab.binding.insteonplm.internal.message.Message;

/**
 * Queue entry helper class
 *
 * @author Bernd Pfrommer
 */
class InsteonThingMessageQEntry implements Comparable<InsteonThingMessageQEntry> {
    private Message m_msg = null;
    private long m_expirationTime = 0L;

    public Message getMsg() {
        return m_msg;
    }

    public long getExpirationTime() {
        return m_expirationTime;
    }

    public InsteonThingMessageQEntry(Message m, long t) {
        m_msg = m;
        m_expirationTime = t;
    }

    @Override
    public int compareTo(InsteonThingMessageQEntry a) {
        return (int) (m_expirationTime - a.m_expirationTime);
    }
}
