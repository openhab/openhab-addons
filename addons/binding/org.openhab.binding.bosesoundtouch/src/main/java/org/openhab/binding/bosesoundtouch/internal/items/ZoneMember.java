package org.openhab.binding.bosesoundtouch.internal.items;

import org.openhab.binding.bosesoundtouch.handler.BoseSoundTouchHandler;

/**
 * @author Christian Niessner - Initial contribution
 */
public class ZoneMember {
    private String ip;
    private String mac;
    private BoseSoundTouchHandler handler;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public BoseSoundTouchHandler getHandler() {
        return handler;
    }

    public void setHandler(BoseSoundTouchHandler handler) {
        this.handler = handler;
    }
}
