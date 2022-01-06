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
package org.openhab.binding.resol.handler;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.resol.internal.ResolBindingConstants;
import org.openhab.binding.resol.internal.ResolBridgeConfiguration;
import org.openhab.binding.resol.internal.discovery.ResolDeviceDiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.resol.vbus.Connection;
import de.resol.vbus.Connection.ConnectionState;
import de.resol.vbus.ConnectionAdapter;
import de.resol.vbus.Packet;
import de.resol.vbus.Specification;
import de.resol.vbus.SpecificationFile;
import de.resol.vbus.SpecificationFile.Language;
import de.resol.vbus.TcpDataSource;
import de.resol.vbus.TcpDataSourceProvider;

/**
 * The {@link ResolBridgeHandler} class handles the connection to the VBUS/LAN adapter.
 *
 * @author Raphael Mack - Initial contribution
 */
@NonNullByDefault
public class ResolBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(ResolBridgeHandler.class);

    private String ipAddress = "";
    private String password = "";
    private int refreshInterval;
    private boolean isConnected = false;
    private String unconnectedReason = "";

    // Background Runnable
    private @Nullable ScheduledFuture<?> pollingJob;

    private @Nullable Connection tcpConnection;
    private final Specification spec;

    // Managing Thing Discovery Service
    private @Nullable ResolDeviceDiscoveryService discoveryService = null;

    private boolean scanning;

    private final @Nullable LocaleProvider localeProvider;

    public ResolBridgeHandler(Bridge bridge, @Nullable LocaleProvider localeProvider) {
        super(bridge);
        spec = Specification.getDefaultSpecification();
        this.localeProvider = localeProvider;
    }

    public void updateStatus() {
        if (isConnected) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, unconnectedReason);
        }
    }

    public void registerDiscoveryService(ResolDeviceDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    public void unregisterDiscoveryService() {
        discoveryService = null;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(ResolDeviceDiscoveryService.class);
    }

    public void registerResolThingListener(ResolEmuEMThingHandler resolEmuEMThingHandler) {
        synchronized (this) {
            Connection con = tcpConnection;
            if (con != null) {
                resolEmuEMThingHandler.useConnection(con);
            }
        }
    }

    private void pollingRunnable() {
        if (!isConnected) {
            synchronized (ResolBridgeHandler.this) {
                Connection connection = tcpConnection;
                /* first cleanup in case there is an old but failed TCP connection around */
                try {
                    if (connection != null) {
                        connection.disconnect();

                        getThing().getThings().stream().forEach(thing -> {
                            ThingHandler th = thing.getHandler();
                            if (th instanceof ResolEmuEMThingHandler) {
                                ((ResolEmuEMThingHandler) th).stop();
                            }
                        });

                        connection = null;
                        tcpConnection = null;
                    }
                } catch (IOException e) {
                    logger.warn("TCP disconnect failed: {}", e.getMessage());
                }
                TcpDataSource source = null;
                /* now try to establish a new TCP connection */
                try {
                    source = TcpDataSourceProvider.fetchInformation(InetAddress.getByName(ipAddress), 500);
                    if (source != null) {
                        source.setLivePassword(password);
                    }
                } catch (IOException e) {
                    isConnected = false;
                    unconnectedReason = Objects.requireNonNullElse(e.getMessage(), "");
                }
                if (source != null) {
                    try {
                        logger.debug("Opening a new connection to {} {} @{}", source.getProduct(),
                                source.getDeviceName(), source.getAddress());
                        connection = source.connectLive(0, 0x0020);
                        tcpConnection = connection;
                    } catch (Exception e) {
                        // this generic Exception catch is required, as TcpDataSource.connectLive throws this
                        // generic type
                        isConnected = false;
                        unconnectedReason = Objects.requireNonNullElse(e.getMessage(), "");
                    }

                    if (connection != null) {
                        // Add a listener to the Connection to monitor state changes and
                        // read incoming frames
                        connection.addListener(new ResolConnectorAdapter());
                    }
                }
                // Establish the connection
                if (connection != null) {
                    try {
                        connection.connect();
                        final Connection c = connection;
                        // now set the connection the thing handlers for the emulated EMs

                        getThing().getThings().stream().forEach(thing -> {
                            ThingHandler th = thing.getHandler();
                            if (th instanceof ResolEmuEMThingHandler) {
                                ((ResolEmuEMThingHandler) th).useConnection(c);
                            }
                        });
                    } catch (IOException e) {
                        unconnectedReason = Objects.requireNonNullElse(e.getMessage(), "");
                        isConnected = false;
                    }
                } else {
                    isConnected = false;
                }
                if (!isConnected) {
                    logger.debug("Cannot establish connection to {} ({})", ipAddress, unconnectedReason);
                } else {
                    unconnectedReason = "";
                }
                updateStatus();
            }
        }
    }

    private synchronized void startAutomaticRefresh() {
        ScheduledFuture<?> job = pollingJob;
        if (job == null || job.isCancelled()) {
            pollingJob = scheduler.scheduleWithFixedDelay(this::pollingRunnable, 0, refreshInterval, TimeUnit.SECONDS);
        }
    }

    public ThingStatus getStatus() {
        return getThing().getStatus();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No commands supported - nothing to do
    }

    @Override
    public void initialize() {
        updateStatus();
        ResolBridgeConfiguration configuration = getConfigAs(ResolBridgeConfiguration.class);
        ipAddress = configuration.ipAddress;
        refreshInterval = configuration.refreshInterval;
        password = configuration.password;
        startAutomaticRefresh();
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> job = pollingJob;
        if (job != null) {
            job.cancel(true);
            pollingJob = null;
        }
        try {
            Connection connection = tcpConnection;
            if (connection != null) {
                connection.disconnect();
                getThing().getThings().stream().forEach(thing -> {
                    ThingHandler th = thing.getHandler();
                    if (th instanceof ResolEmuEMThingHandler) {
                        ((ResolEmuEMThingHandler) th).stop();
                    }
                });

            }
        } catch (IOException ioe) {
            // we don't care about exceptions on disconnect in dispose
        }
    }

    Locale getLocale() {
        if (localeProvider != null) {
            return localeProvider.getLocale();
        } else {
            return Locale.getDefault();
        }
    }

    /* adapter to react on connection state changes and handle received packets */
    private class ResolConnectorAdapter extends ConnectionAdapter {
        @Override
        public void connectionStateChanged(@Nullable Connection connection) {
            synchronized (ResolBridgeHandler.this) {
                if (connection == null) {
                    isConnected = false;
                } else {
                    ConnectionState connState = connection.getConnectionState();
                    if (ConnectionState.CONNECTED.equals(connState)) {
                        isConnected = true;
                    } else if (ConnectionState.DISCONNECTED.equals(connState)
                            || ConnectionState.INTERRUPTED.equals(connState)) {
                        isConnected = false;
                    }
                    logger.debug("Connection state changed to: {}", connState.toString());

                    if (isConnected) {
                        unconnectedReason = "";
                    } else {
                        unconnectedReason = "TCP connection failed: " + connState.toString();
                    }
                }
                updateStatus();
            }
        }

        @Override
        public void packetReceived(@Nullable Connection connection, @Nullable Packet packet) {
            if (connection == null || packet == null) {
                return;
            }
            Language lang = SpecificationFile.getLanguageForLocale(getLocale());
            boolean packetHandled = false;
            String thingType = spec.getSourceDeviceSpec(packet).getName(); // use En here

            thingType = thingType.replace(" [", "-");
            thingType = thingType.replace("]", "");
            thingType = thingType.replace(" #", "-");
            thingType = thingType.replace(" ", "_");
            thingType = thingType.replace("/", "_");
            thingType = thingType.replaceAll("[^A-Za-z0-9_-]+", "_");

            /*
             * It would be nice for the combination of MX and EM devices to filter only those with a peerAddress of
             * 0x10, because the MX redelivers the data from the EM to the DFA.
             * But the MX is the exception in this case and many other controllers do not redeliver data, so we keep it.
             */
            if (logger.isTraceEnabled()) {
                logger.trace("Received Data from {} (0x{}/0x{}) naming it {}",
                        spec.getSourceDeviceSpec(packet).getName(lang),
                        Integer.toHexString(spec.getSourceDeviceSpec(packet).getSelfAddress()),
                        Integer.toHexString(spec.getSourceDeviceSpec(packet).getPeerAddress()), thingType);
            }

            for (Thing t : getThing().getThings()) {
                ResolBaseThingHandler th = (ResolBaseThingHandler) t.getHandler();
                boolean isEM = t instanceof ResolEmuEMThingHandler;

                if (t.getUID().getId().contentEquals(thingType)
                        || (isEM && th != null && spec.getSourceDeviceSpec(packet)
                                .getPeerAddress() == ((ResolEmuEMThingHandler) th).getVbusAddress())) {
                    if (th != null) {
                        th.packetReceived(spec, lang, packet);
                        packetHandled = true;
                    }
                }
            }
            ResolDeviceDiscoveryService discovery = discoveryService;
            if (!packetHandled && scanning && discovery != null) {
                // register the seen device
                discovery.addThing(getThing().getUID(), ResolBindingConstants.THING_ID_DEVICE, thingType,
                        spec.getSourceDeviceSpec(packet).getName(lang));
            }
        }
    }

    public void startScan() {
        scanning = true;
    }

    public void stopScan() {
        scanning = false;
    }
}
