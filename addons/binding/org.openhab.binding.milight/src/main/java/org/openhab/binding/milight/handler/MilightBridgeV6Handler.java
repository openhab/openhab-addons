/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.milight.handler;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.milight.MilightBindingConstants;
import org.openhab.binding.milight.internal.protocol.MilightV6SessionManager;
import org.openhab.binding.milight.internal.protocol.MilightV6SessionManager.ISessionState;
import org.openhab.binding.milight.internal.protocol.MilightV6SessionManager.SessionState;

/**
 * The {@link MilightBridgeV6Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Graeff - Initial contribution
 */
public class MilightBridgeV6Handler extends AbstractMilightBridgeHandler implements ISessionState {
    private MilightV6SessionManager session;

    public MilightBridgeV6Handler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);

        if (session == null) {
            return;
        }

        BigDecimal pwByte1 = (BigDecimal) thing.getConfiguration().get(MilightBindingConstants.CONFIG_PASSWORD_BYTE_1);
        BigDecimal pwByte2 = (BigDecimal) thing.getConfiguration().get(MilightBindingConstants.CONFIG_PASSWORD_BYTE_2);
        if (pwByte1 != null && pwByte2 != null && pwByte1.intValue() >= 0 && pwByte1.intValue() <= 255
                && pwByte2.intValue() >= 0 && pwByte2.intValue() <= 255) {
            session.setPasswordBytes((byte) pwByte1.intValue(), (byte) pwByte2.intValue());
        }
    }

    @Override
    public void thingUpdated(Thing thing) {
        super.thingUpdated(thing);
    }

    /**
     * Creates a session manager object and the send queue. The initial IP address may be null
     * or is not matching with the real IP address of the bridge. The session manager will send
     * a broadcast packet to find the bridge with the respective bridge ID and will change the
     * IP address of the send queue object accordingly.
     *
     * The keep alive timer that is also setup here, will send keep alive packets periodically.
     * If the bridge doesn't respond anymore (e.g. DHCP IP change), the initial session handshake
     * starts all over again.
     */
    @Override
    protected void startConnectAndKeepAlive(InetAddress addr) {
        dispose();

        int port = getPort(MilightBindingConstants.PORT_VER6);
        if (port == 0) {
            return;
        }

        com.setAddress(addr);
        com.setPort(port);
        com.start();

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Waiting for session");
        session = new MilightV6SessionManager(com, bridgeid, scheduler, this, addr);
    }

    @Override
    protected Runnable getKeepAliveRunnable() {
        return () -> {
            try {
                session.keepAlive(refreshIntervalSec * 1000);
            } catch (InterruptedException e) {
                // Someone wants to end this thread
                return;
            }
            updateProperty(MilightBindingConstants.PROPERTY_SESSIONID, session.getSession());
            updateProperty(MilightBindingConstants.PROPERTY_SESSIONCONFIRMED,
                    String.valueOf(session.getLastSessionValidConfirmation()));
        };
    }

    @Override
    public void dispose() {
        if (session != null) {
            session.dispose();
            session = null;
        }
        super.dispose();
    }

    public MilightV6SessionManager getSessionManager() {
        return session;
    }

    // A bridge may be connected/paired to white/rgbw/rgb bulbs. Unfortunately the bridge does
    // not know which bulbs are present and the bulbs do not have a bidirectional communication.
    // Therefore we present the user all possible bulbs
    // (4 groups each for white/rgbw and 1 for the obsolete rgb bulb ).
    private void addBulbsToDiscovery() {
        // The iBox has an integrated bridge lamp
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.RGB_IBOX_THING_TYPE, this.getThing().getUID(), "0"),
                "Color (iBox)");

        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.RGB_CW_WW_THING_TYPE, this.getThing().getUID(), "0"),
                "All rgbww color");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.RGB_CW_WW_THING_TYPE, this.getThing().getUID(), "1"),
                "Rgbww Color (Zone 1)");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.RGB_CW_WW_THING_TYPE, this.getThing().getUID(), "2"),
                "Rgbww Color (Zone 2)");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.RGB_CW_WW_THING_TYPE, this.getThing().getUID(), "3"),
                "Rgbww Color (Zone 3)");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.RGB_CW_WW_THING_TYPE, this.getThing().getUID(), "4"),
                "Rgbww Color (Zone 4)");

        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.RGB_W_THING_TYPE, this.getThing().getUID(), "0"),
                "All rgbw color");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.RGB_W_THING_TYPE, this.getThing().getUID(), "1"),
                "Rgbw Color (Zone 1)");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.RGB_W_THING_TYPE, this.getThing().getUID(), "2"),
                "Rgbw Color (Zone 2)");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.RGB_W_THING_TYPE, this.getThing().getUID(), "3"),
                "Rgbw Color (Zone 3)");
        thingDiscoveryService.addDevice(
                new ThingUID(MilightBindingConstants.RGB_W_THING_TYPE, this.getThing().getUID(), "4"),
                "Rgbw Color (Zone 4)");
    }

    @Override
    public void sessionStateChanged(SessionState state) {
        switch (state) {
            case SESSION_VALID:
                updateStatus(ThingStatus.ONLINE);
                // Setup the keep alive timer as soon as we have established a valid session
                BigDecimal refreshSec = (BigDecimal) thing.getConfiguration()
                        .get(MilightBindingConstants.CONFIG_REFRESH_SEC);
                setupRefreshTimer(refreshSec == null ? refreshIntervalSec : refreshSec.intValue());
                addBulbsToDiscovery();
                // As soon as the session is valid, update the user visible configuration of the host IP.
                Configuration c = editConfiguration();
                c.put(MilightBindingConstants.CONFIG_HOST_NAME, com.getAddr().getHostAddress());
                updateConfiguration(c);
                break;
            case SESSION_INVALID:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Session could not be established");
                break;
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, state.name());
                break;
        }
    }
}
