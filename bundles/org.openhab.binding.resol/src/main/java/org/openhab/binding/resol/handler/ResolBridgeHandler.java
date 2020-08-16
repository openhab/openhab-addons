/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.resol.internal.ResolBindingConstants;
import org.openhab.binding.resol.internal.ResolConfiguration;
import org.openhab.binding.resol.internal.discovery.ResolDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.resol.vbus.Connection;
import de.resol.vbus.ConnectionAdapter;
import de.resol.vbus.Packet;
import de.resol.vbus.Specification;
import de.resol.vbus.Specification.PacketFieldValue;
import de.resol.vbus.SpecificationFile;
import de.resol.vbus.SpecificationFile.Language;
import de.resol.vbus.TcpDataSource;
import de.resol.vbus.TcpDataSourceProvider;

/**
 * The {@link ResolBridgeHandler} class handles the connection to the
 * optolink adapter.
 *
 * @author Raphael Mack - Initial contribution
 */
public class ResolBridgeHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(ResolBridgeHandler.class);

    private Language lang;
    private Locale locale;

    private String ipAddress;
    private String password;
    private int refreshInterval = 900; /* 15 mins for refreshing the available things should be enough */
    private boolean isConnected = false;
    private String unconnectedReason = "";

    public ResolBridgeHandler(Bridge bridge, LocaleProvider localeProvider) {
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

    @Override
    public void updateStatus(ThingStatus status) {
        super.updateStatus(status);
        updateThingHandlersStatus(status);
    }

    public void updateStatus() {
        if (isConnected) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, unconnectedReason);
        }
    }

    // Managing Thing Discovery Service

    private ResolDiscoveryService discoveryService = null;

    public void registerDiscoveryService(ResolDiscoveryService discoveryService) {

        if (discoveryService == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null ThingDiscoveryListener.");
        } else {
            this.discoveryService = discoveryService;
            logger.trace("register Discovery Service");
        }
    }

    public void unregisterDiscoveryService() {
        discoveryService = null;
        logger.trace("unregister Discovery Service");
    }

    // Handles Thing discovery

    private void createThing(String thingType, String thingID, String name) {
        logger.trace("Create thing Type='{}' id='{}'", thingType, thingID);
        if (discoveryService != null) {
            discoveryService.addResolThing(thingType, thingID, name);
        }
    }

    // Managing ThingHandler

    private Map<String, ResolThingHandler> thingHandlerMap = new HashMap<String, ResolThingHandler>();

    public void registerResolThingListener(ResolThingHandler thingHandler) {
        if (thingHandler == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null ThingHandler.");
        } else {
            Thing t = thingHandler.getThing();

            String thingType = t.getProperties().get("type");

            if (thingHandlerMap.get(thingType) == null) {
                thingHandlerMap.put(thingType, thingHandler);
                logger.trace("register thingHandler for thing: {}", thingType);
                updateThingHandlerStatus(thingHandler, this.getStatus());
            } else {
                logger.trace("thingHandler for thing: '{}' allready registerd", thingType);
            }

        }
    }

    public void unregisterThingListener(ResolThingHandler thingHandler) {
        if (thingHandler != null) {
            String thingID = thingHandler.getThing().getUID().getId();
            if (thingHandlerMap.remove(thingID) == null) {
                logger.trace("thingHandler for thing: {} not registered", thingID);
            } else {
                thingHandler.updateStatus(ThingStatus.OFFLINE);
            }
        }
    }

    private void updateThingHandlerStatus(@NonNull ResolThingHandler thingHandler, @NonNull ThingStatus status) {
        thingHandler.updateStatus(status);
    }

    private void updateThingHandlersStatus(@NonNull ThingStatus status) {
        for (Map.Entry<String, ResolThingHandler> entry : thingHandlerMap.entrySet()) {
            entry.getValue().updateStatus(status);
        }
    }

    // Background Runables

    private ScheduledFuture<?> pollingJob;

    private TcpDataSource dataSource;
    private Connection tcpConnection;
    private Specification spec;
    private Set<String> availableDevices = new HashSet<String>();

    private Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            logger.trace("Polling job called");
            if (!isConnected) {
                try {
                    dataSource = TcpDataSourceProvider.fetchInformation(InetAddress.getByName(ipAddress), 500);
                    dataSource.setLivePassword(password);
                    tcpConnection = dataSource.connectLive(0, 0x0020);

                    Thread.sleep(5000); // Wait
                                        // for
                                        // connection
                                        // .

                    // Add a listener to the Connection to monitor state changes and
                    // add incoming packets to the HeaderSetConsolidator
                    tcpConnection.addListener(new ConnectionAdapter() {

                        @Override
                        public void connectionStateChanged(Connection connection) {
                            isConnected = (tcpConnection.getConnectionState()
                                    .equals(Connection.ConnectionState.CONNECTED));
                            logger.trace("Connection state changed to: {} isConnected = {}",
                                    tcpConnection.getConnectionState().toString(), isConnected);
                            if (isConnected) {
                                unconnectedReason = "";
                            } else {
                                unconnectedReason = "TCP Connection problem";
                            }
                            updateStatus();
                        }

                        @Override
                        public void packetReceived(Connection connection, Packet packet) {
                            String thingType = spec.getSourceDeviceSpec(packet).getName(); // use En here

                            thingType = thingType.replace(" [", "-");
                            thingType = thingType.replace("]", "");
                            thingType = thingType.replace(" #", "-");
                            thingType = thingType.replace(" ", "_");
                            thingType = thingType.replace("/", "_");
                            thingType = thingType.replaceAll("[^A-Za-z0-9_-]+", "_");

                            /*
                             * It would be nice for the combination of MX and EM devices to filter only those with a
                             * peerAddress of 0x10
                             * because the MX redelivers the data from the EM to the DFA
                             * See https://github.com/ramack/openhab2-addons/issues/23 to see why this is not so nice.
                             */
                            /* if (spec.getSourceDeviceSpec(packet).getPeerAddress() == 0x10) { */
                            logger.trace("Received Data from {} (0x{}/0x{}) naming it {}",
                                    spec.getSourceDeviceSpec(packet).getName(lang),
                                    Integer.toHexString(spec.getSourceDeviceSpec(packet).getSelfAddress()),
                                    Integer.toHexString(spec.getSourceDeviceSpec(packet).getPeerAddress()), thingType);
                            /*
                             * } else {
                             * logger.trace("Ignoring Data from {} (0x{}/0x{}) naming it {}",
                             * spec.getSourceDeviceSpec(packet).getName(lang),
                             * Integer.toHexString(spec.getSourceDeviceSpec(packet).getSelfAddress()),
                             * Integer.toHexString(spec.getSourceDeviceSpec(packet).getPeerAddress()),
                             * thingType);
                             * return;
                             * }
                             */
                            // TODO: if the thing gets deleted, we should also remove it from this list...
                            if (!availableDevices.contains(thingType)) {
                                // register new device
                                createThing(ResolBindingConstants.THING_ID_DEVICE, thingType,
                                        spec.getSourceDeviceSpec(packet).getName(lang));
                                availableDevices.add(thingType);
                            }

                            PacketFieldValue[] pfvs = spec.getPacketFieldValuesForHeaders(new Packet[] { packet });
                            for (PacketFieldValue pfv : pfvs) {
                                logger.trace("Id: {}, Name: {}, Raw: {}, Text: {}", pfv.getPacketFieldId(),
                                        pfv.getName(lang), pfv.getRawValueDouble(),
                                        pfv.formatTextValue(null, Locale.getDefault()));
                                ResolThingHandler thingHandler = thingHandlerMap.get(thingType);
                                if (thingHandler != null) {
                                    @NonNull
                                    String channelId = pfv.getName(); // use english here
                                    channelId = channelId.replace(" [", "-");
                                    channelId = channelId.replace("]", "");
                                    channelId = channelId.replace("(", "-");
                                    channelId = channelId.replace(")", "");
                                    channelId = channelId.replace(" #", "-");
                                    channelId = channelId.replaceAll("[^A-Za-z0-9_-]+", "_");

                                    ChannelTypeUID channelTypeUID;

                                    if (pfv.getPacketFieldSpec().getUnit().getUnitId() >= 0) {
                                        channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID,
                                                pfv.getPacketFieldSpec().getUnit().getUnitCodeText());
                                        // TODO: add precision
                                        // TODO: add special handling of unit percent as PercentType
                                    } else if (pfv.getPacketFieldSpec().getType() == SpecificationFile.Type.Number) {
                                        if (pfv.getEnumVariant() != null) {
                                            // Do not auto-link the numeric value, if there is an enum for it
                                            channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID,
                                                    "NoneHidden");
                                        } else {
                                            channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID,
                                                    "None");
                                        }

                                    } else if (pfv.getPacketFieldSpec().getType() == SpecificationFile.Type.DateTime) {
                                        channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID,
                                                "DateTime");
                                        /*
                                         * so far there seems no reasonable type for WeekDay and Time types, so we just
                                         * make them strings
                                         */
                                        /*
                                         * } else if (pfv.getPacketFieldSpec().getType() ==
                                         * SpecificationFile.Type.WeekTime) {
                                         * channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID,
                                         * "WeekTime");
                                         * } else if (pfv.getPacketFieldSpec().getType() == SpecificationFile.Type.Time)
                                         * {
                                         * channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID,
                                         * "Time");
                                         */
                                    } else {
                                        channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID, "None");
                                    }
                                    // TODO: use StringListType for interpreted String lists like Operation Status?

                                    String acceptedItemType = "String";

                                    Thing thing = thingHandler.getThing();
                                    switch (pfv.getPacketFieldSpec().getType()) {
                                        case DateTime:
                                            acceptedItemType = "DateTime";
                                            break;
                                        case WeekTime:
                                        case Number:
                                            acceptedItemType = "Number";
                                            break;
                                        case Time:
                                        default:
                                            acceptedItemType = "String";
                                            break;
                                    }
                                    if (thing.getChannel(channelId) == null && pfv.getRawValueDouble() != null) {
                                        ThingBuilder thingBuilder = thingHandler.editThing();

                                        ChannelUID channelUID = new ChannelUID(thing.getUID(), channelId);
                                        Channel channel = ChannelBuilder.create(channelUID, acceptedItemType)
                                                .withType(channelTypeUID).withLabel(pfv.getName(lang)).build();

                                        thingBuilder.withChannel(channel).withLabel(thing.getLabel());

                                        thingHandler.updateThing(thingBuilder.build());
                                    }

                                    switch (pfv.getPacketFieldSpec().getType()) {
                                        case Number:
                                            Double dd = pfv.getRawValueDouble();
                                            if (dd != null) {
                                                if (isSpecialValue(dd)) {
                                                    /* some error occurred in the measurement - ignore the value */
                                                } else {
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
                                            ChannelTypeUID enumChannelTypeUID = new ChannelTypeUID(
                                                    ResolBindingConstants.BINDING_ID, "None");
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
                                    logger.trace("ThingHandler for {} not registered.", thingType);
                                }
                            }
                        }
                    });

                    // Establish the connection
                    tcpConnection.connect();
                    Thread.sleep(1000); // after a reconnect wait 1 sec
                    isConnected = (tcpConnection.getConnectionState().equals(Connection.ConnectionState.CONNECTED));
                } catch (IOException e) {
                    logger.trace("Connection failed", e);
                    unconnectedReason = e.getMessage();
                    isConnected = false;
                } catch (InterruptedException e) {
                    isConnected = (tcpConnection.getConnectionState().equals(Connection.ConnectionState.CONNECTED));
                } catch (Exception e) {
                    isConnected = (tcpConnection.getConnectionState().equals(Connection.ConnectionState.CONNECTED));
                    unconnectedReason = e.getMessage();
                }
                if (!isConnected) {
                    logger.info("Cannot establish connection to {} ({})", ipAddress, unconnectedReason);
                } else {
                    unconnectedReason = "";
                }
                updateStatus();
            }
        }
    };

    /* check if the given value is a special one like 888.8 or 999.9 for shortcut or open load on a sensor wire */
    private boolean isSpecialValue(Double dd) {
        if ((Math.abs(dd - 888.8) < 1) || (Math.abs(dd - (-888.8)) < 1)) {
            /* value out of range */
            return true;
        }
        if (Math.abs(dd - 999.9) < 1) {
            /* sensor not reachable */
            return true;
        }
        return false;
    }

    private synchronized void startAutomaticRefresh() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, refreshInterval, TimeUnit.SECONDS);
        }
    }

    // Methods for ThingHandler
    public ThingStatus getStatus() {
        return getThing().getStatus();
    }

    // internal Methods

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No channels - nothing to do
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Resol bridge handler {}", this.toString());
        updateStatus();
        ResolConfiguration configuration = getConfigAs(ResolConfiguration.class);
        ipAddress = configuration.ipAddress;
        refreshInterval = configuration.refreshInterval;
        password = configuration.password;
        startAutomaticRefresh();
    }

    @Override
    public void dispose() {
        logger.debug("Dispose Resol bridge handler{}", this.toString());

        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            try {
                if (tcpConnection != null) {
                    tcpConnection.disconnect();
                }
            } catch (IOException ioe) {
                // we don't care here
            }
            pollingJob = null;
        }
        updateStatus(ThingStatus.OFFLINE); // Set all State to offline
    }
}
