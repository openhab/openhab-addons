/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.milight.internal.handler;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.milight.internal.MilightBindingConstants;
import org.openhab.binding.milight.internal.protocol.MilightV6SessionManager;
import org.openhab.binding.milight.internal.protocol.MilightV6SessionManager.ISessionState;
import org.openhab.binding.milight.internal.protocol.MilightV6SessionManager.SessionState;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * The {@link BridgeV6Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class BridgeV6Handler extends AbstractBridgeHandler implements ISessionState {
    private @NonNullByDefault({}) MilightV6SessionManager session;
    final DateTimeFormatter timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
    private String offlineReason = "";
    private @Nullable ScheduledFuture<?> scheduledFuture;

    public BridgeV6Handler(Bridge bridge, int bridgeOffset) {
        super(bridge, bridgeOffset);
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
    protected void startConnectAndKeepAlive() {
        if (!config.bridgeid.matches("^([0-9A-Fa-f]{12})$")) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "bridgeID invalid!");
            return;
        }

        if (config.port == 0) {
            config.port = MilightBindingConstants.PORT_VER6;
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Waiting for session");
        int refreshTime = Math.max(Math.min(config.refreshTime, MilightV6SessionManager.TIMEOUT_MS), 100);
        this.session = new MilightV6SessionManager(config.bridgeid, this, address, config.port, refreshTime,
                new byte[] { (byte) config.passwordByte1, (byte) config.passwordByte2 });
        session.start().thenAccept(d -> this.socket = d);
    }

    @Override
    public void dispose() {
        stopMakeOfflineTimer();
        try {
            session.close();
        } catch (IOException ignore) {
        }
        this.session = null;
        super.dispose();
    }

    public MilightV6SessionManager getSessionManager() {
        return session;
    }

    @Override
    public void sessionStateChanged(SessionState state, @Nullable InetAddress newAddress) {
        stopMakeOfflineTimer();
        switch (state) {
            case SESSION_VALID_KEEP_ALIVE:
                preventReinit = true;
                Instant lastSessionTime = session.getLastSessionValidConfirmation();
                LocalDateTime date = LocalDateTime.ofInstant(lastSessionTime, ZoneId.systemDefault());
                updateProperty(MilightBindingConstants.PROPERTY_SESSIONCONFIRMED, date.format(timeFormat));
                preventReinit = false;
                break;
            case SESSION_VALID:
                if (newAddress == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No IP address received");
                    break;
                }
                if (!newAddress.equals(address) || !thing.getStatus().equals(ThingStatus.ONLINE)) {
                    updateStatus(ThingStatus.ONLINE);
                    this.address = newAddress;
                    // As soon as the session is valid, update the user visible configuration of the host IP.
                    Configuration c = editConfiguration();
                    c.put(BridgeHandlerConfig.CONFIG_HOST_NAME, newAddress.getHostAddress());
                    thing.setProperty(MilightBindingConstants.PROPERTY_SESSIONID, session.getSession());
                    thing.setProperty(MilightBindingConstants.PROPERTY_SESSIONCONFIRMED,
                            String.valueOf(session.getLastSessionValidConfirmation()));
                    preventReinit = true;
                    updateConfiguration(c);
                    preventReinit = false;
                }
                break;
            case SESSION_INVALID:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Session could not be established");

                break;
            default:
                // Delay putting the session offline
                offlineReason = state.name();
                scheduledFuture = scheduler.schedule(
                        () -> updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, offlineReason),
                        1000, TimeUnit.MILLISECONDS);
                break;
        }
    }

    private void stopMakeOfflineTimer() {
        ScheduledFuture<?> future = scheduledFuture;
        if (future != null) {
            future.cancel(false);
            scheduledFuture = null;
        }
    }
}
