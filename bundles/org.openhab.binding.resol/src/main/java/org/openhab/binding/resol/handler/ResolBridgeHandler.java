/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.resol.internal.ResolBindingConstants;
import org.openhab.binding.resol.internal.ResolBridgeConfiguration;
import org.openhab.binding.resol.internal.discovery.ResolDiscoveryService;
import org.openhab.binding.resol.internal.providers.ResolChannelTypeProvider;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.resol.vbus.Connection;
import de.resol.vbus.Connection.ConnectionState;
import de.resol.vbus.ConnectionAdapter;
import de.resol.vbus.Packet;
import de.resol.vbus.Specification;
import de.resol.vbus.Specification.PacketFieldValue;
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

    private Language lang;
    private Locale locale;

    private String ipAddress = "";
    private String password = "";
    private int refreshInterval;
    private boolean isConnected = false;
    private String unconnectedReason = "";

    // Background Runable
    private @Nullable ScheduledFuture<?> pollingJob;

    private @Nullable Connection tcpConnection;
    private Specification spec;
    private Set<String> availableDevices = new HashSet<>();

    private Map<String, @Nullable ResolThingHandler> thingHandlerMap = new HashMap<>();
    private Map<Integer, ResolEmuEMThingHandler> emThingHandlerMap = new HashMap<>();

    // Managing Thing Discovery Service
    private @Nullable ResolDiscoveryService discoveryService = null;

    public ResolBridgeHandler(Bridge bridge, @Nullable LocaleProvider localeProvider) {
        super(bridge);
        spec = Specification.getDefaultSpecification();
        if (localeProvider != null) {
            locale = localeProvider.getLocale();
            lang = SpecificationFile.getLanguageForLocale(locale);

        } else {
            locale = Locale.getDefault();
            lang = Language.En;
        }
    }

    public void updateStatus() {
        if (isConnected) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, unconnectedReason);
        }
    }

    public void registerDiscoveryService(ResolDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    public void unregisterDiscoveryService() {
        discoveryService = null;
    }

    private void createThing(String thingType, String thingID, String name) {
        ResolDiscoveryService service = discoveryService;
        logger.trace("Create thing Type='{}' id='{}'", thingType, thingID);

        if (service != null) {
            service.addResolThing(thingType, thingID, name);
        }
    }

    public void registerResolThingListener(ResolThingHandler thingHandler) {
        Thing t = thingHandler.getThing();

        String thingType = t.getUID().getId();

        if (!thingHandlerMap.containsKey(thingType)) {
            thingHandlerMap.put(thingType, thingHandler);
            logger.trace("register thingHandler for thing: {}", thingType);
            updateThingHandlerStatus(thingHandler, this.getStatus());
        } else {
            logger.trace("thingHandler for thing: '{}' allready registerd", thingType);
        }
    }

    public void unregisterThingListener(ResolThingHandler thingHandler) {
        String thingID = thingHandler.getThing().getUID().getId();
        if (!thingHandlerMap.containsKey(thingID)) {
            logger.warn("thingHandler for thing: {} not registered", thingID);
        } else {
            thingHandler.updateStatus(ThingStatus.OFFLINE);
        }
    }

    public void registerResolThingListener(ResolEmuEMThingHandler resolEmuEMThingHandler) {
        emThingHandlerMap.put(resolEmuEMThingHandler.getVbusAddress(), resolEmuEMThingHandler);
        synchronized (this) {
            Connection con = tcpConnection;
            if (con != null) {
                resolEmuEMThingHandler.useConnection(con);
            }
        }
    }

    public void unregisterThingListener(ResolEmuEMThingHandler resolEmuEMThingHandler) {
        if (!emThingHandlerMap.containsKey(resolEmuEMThingHandler.getVbusAddress())) {
            logger.warn("thingHandler for vbus address {} not registered", resolEmuEMThingHandler.getVbusAddress());
        } else {
            emThingHandlerMap.remove(resolEmuEMThingHandler.getVbusAddress());
        }
    }

    private void updateThingHandlerStatus(ResolThingHandler thingHandler, ThingStatus status) {
        thingHandler.updateStatus(status);
    }

    private void pollingRunnable() {
        if (!isConnected) {
            synchronized (ResolBridgeHandler.this) {
                Connection connection = tcpConnection;
                /* first cleanup in case there is an old but failed TCP connection around */
                try {
                    if (connection != null) {
                        connection.disconnect();
                        for (int x : emThingHandlerMap.keySet()) {
                            ResolEmuEMThingHandler emu = emThingHandlerMap.get(x);
                            if (emu != null) {
                                emu.stop();
                            }
                        }

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
                        // now set the connection the thing handlers for the emulated EMs
                        for (int x : emThingHandlerMap.keySet()) {
                            ResolEmuEMThingHandler emu = emThingHandlerMap.get(x);
                            if (emu != null) {
                                emu.useConnection(connection);
                            }
                        }
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

    /* check if the given value is a special one like 888.8 or 999.9 for shortcut or open load on a sensor wire */
    private boolean isSpecialValue(Double dd) {
        if ((Math.abs(dd - 888.8) < 0.1) || (Math.abs(dd - (-888.8)) < 0.1)) {
            /* value out of range */
            return true;
        }
        if (Math.abs(dd - 999.9) < 0.1) {
            /* sensor not reachable */
            return true;
        }
        return false;
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
                for (int x : emThingHandlerMap.keySet()) {
                    ResolEmuEMThingHandler emu = emThingHandlerMap.get(x);
                    if (emu != null) {
                        emu.stop();
                    }
                }
            }
        } catch (IOException ioe) {
            // we don't care about exceptions on disconnect in dispose
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
                    logger.info("Connection state changed to: {}", connState.toString());

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
            logger.trace("Received Data from {} (0x{}/0x{}) naming it {}",
                    spec.getSourceDeviceSpec(packet).getName(lang),
                    Integer.toHexString(spec.getSourceDeviceSpec(packet).getSelfAddress()),
                    Integer.toHexString(spec.getSourceDeviceSpec(packet).getPeerAddress()), thingType);

            if (emThingHandlerMap.containsKey(spec.getSourceDeviceSpec(packet).getPeerAddress())) {
                ResolEmuEMThingHandler emThingHandler = emThingHandlerMap
                        .get(spec.getSourceDeviceSpec(packet).getPeerAddress());
                if (emThingHandler != null) {
                    emThingHandler.handle(packet);
                }
            } else {
                // a generic packet was received, so let's handle it here
                if (!availableDevices.contains(thingType)) {
                    // register the seen device
                    createThing(ResolBindingConstants.THING_ID_DEVICE, thingType,
                            spec.getSourceDeviceSpec(packet).getName(lang));
                    availableDevices.add(thingType);
                }

                PacketFieldValue[] pfvs = spec.getPacketFieldValuesForHeaders(new Packet[] { packet });
                for (PacketFieldValue pfv : pfvs) {
                    logger.trace("Id: {}, Name: {}, Raw: {}, Text: {}", pfv.getPacketFieldId(), pfv.getName(lang),
                            pfv.getRawValueDouble(), pfv.formatTextValue(null, Locale.getDefault()));

                    ResolThingHandler thingHandler = thingHandlerMap.get(thingType);
                    if (thingHandler != null) {
                        String channelId = pfv.getName(); // use English here
                        channelId = channelId.replace(" [", "-");
                        channelId = channelId.replace("]", "");
                        channelId = channelId.replace("(", "-");
                        channelId = channelId.replace(")", "");
                        channelId = channelId.replace(" #", "-");
                        channelId = channelId.replaceAll("[^A-Za-z0-9_-]+", "_");

                        channelId = channelId.toLowerCase(Locale.ENGLISH);

                        ChannelTypeUID channelTypeUID;

                        if (pfv.getPacketFieldSpec().getUnit().getUnitId() >= 0) {
                            channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID,
                                    pfv.getPacketFieldSpec().getUnit().getUnitCodeText());
                        } else if (pfv.getPacketFieldSpec().getType() == SpecificationFile.Type.Number) {
                            if (pfv.getEnumVariant() != null) {
                                // Do not auto-link the numeric value, if there is an enum for it
                                channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID, "NoneHidden");
                            } else {
                                channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID, "None");
                            }

                        } else if (pfv.getPacketFieldSpec().getType() == SpecificationFile.Type.DateTime) {
                            channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID, "DateTime");
                        } else {
                            channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID, "None");
                        }

                        String acceptedItemType = "String";

                        Thing thing = thingHandler.getThing();
                        switch (pfv.getPacketFieldSpec().getType()) {
                            case DateTime:
                                acceptedItemType = "DateTime";
                                break;
                            case WeekTime:
                            case Number:
                                acceptedItemType = ResolChannelTypeProvider
                                        .itemTypeForUnit(pfv.getPacketFieldSpec().getUnit());
                                break;
                            case Time:
                            default:
                                acceptedItemType = "String";
                                break;
                        }
                        Channel a = thing.getChannel(channelId);

                        if (a == null && pfv.getRawValueDouble() != null) {
                            ThingBuilder thingBuilder = thingHandler.editThing();

                            ChannelUID channelUID = new ChannelUID(thing.getUID(), channelId);
                            Channel channel = ChannelBuilder.create(channelUID, acceptedItemType)
                                    .withType(channelTypeUID).withLabel(pfv.getName(lang)).build();

                            thingBuilder.withChannel(channel).withLabel(thing.getLabel());
                            thingHandler.updateThing(thingBuilder.build());

                            logger.debug("Creating channel: {}", channelUID);
                        }

                        switch (pfv.getPacketFieldSpec().getType()) {
                            case Number:
                                Double dd = pfv.getRawValueDouble();
                                if (dd != null) {
                                    if (!isSpecialValue(dd)) {
                                        /* only set the value if no error occured */
                                        thingHandler.setChannelValue(channelId, dd.doubleValue());
                                    }
                                } else {
                                    /*
                                     * field not available in this packet, e. g. old firmware version
                                     * not (yet) transmitting it
                                     */
                                }
                                break;
                            case DateTime:
                                thingHandler.setChannelValue(channelId, pfv.getRawValueDate());
                                break;
                            case WeekTime:
                            case Time:
                            default:
                                thingHandler.setChannelValue(channelId,
                                        pfv.formatTextValue(pfv.getPacketFieldSpec().getUnit(), locale));
                        }

                        if (pfv.getEnumVariant() != null) {
                            // if we have an enum, we additionally add that as channel
                            String enumChannelId = channelId + "-str";
                            if (thing.getChannel(enumChannelId) == null) {
                                ChannelTypeUID enumChannelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID,
                                        "None");
                                ThingBuilder thingBuilder = thingHandler.editThing();

                                ChannelUID enumChannelUID = new ChannelUID(thing.getUID(), enumChannelId);
                                Channel channel = ChannelBuilder.create(enumChannelUID, "String")
                                        .withType(enumChannelTypeUID).withLabel(pfv.getName(lang)).build();

                                thingBuilder.withChannel(channel).withLabel(thing.getLabel());

                                thingHandler.updateThing(thingBuilder.build());
                            }

                            thingHandler.setChannelValue(enumChannelId, pfv.getEnumVariant().getText(lang));
                        }
                    } else {
                        // logger.debug("ThingHandler for {} not registered.", thingType);
                    }
                }
            }
        }
    }
}
