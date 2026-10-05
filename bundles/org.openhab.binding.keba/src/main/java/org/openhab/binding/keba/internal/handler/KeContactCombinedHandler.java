/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.keba.internal.handler;

import static org.openhab.binding.keba.internal.KebaBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.keba.internal.handler.modbus.KeContactModbusHandler;
import org.openhab.binding.keba.internal.handler.modbus.KebaModbusReadRegister;
import org.openhab.binding.keba.internal.handler.modbus.KebaModbusWriteRegister;
import org.openhab.binding.keba.internal.handler.rest.KeContactRestHandler;
import org.openhab.binding.keba.internal.handler.udp.KeContactActions;
import org.openhab.binding.keba.internal.handler.udp.KeContactHandler;
import org.openhab.binding.keba.internal.handler.udp.KeContactTransceiver;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.modbus.ModbusManager;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Coordinates one wallbox, preferring Modbus and requesting only supplemental UDP/REST data.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class KeContactCombinedHandler extends BaseThingHandler {

    enum Protocol {
        MODBUS,
        UDP,
        REST
    }

    private static final Set<String> UDP_REPORT_1 = Set.of("backend", "timequality", "bootflag", "dipswitch1",
            "dipswitch2");
    private static final Set<String> UDP_REPORT_2 = Set.of("enabledsystem", "enableduser", "maxpresetcurrent",
            "maxpresetcurrentrange", "error1", "error2", "maxpilotcurrent", "maxpilotcurrentdutycyle",
            "maxsystemcurrent", "failsafecurrent", "failsafetimeout", "currtimer", "currtimertimeout", "output",
            "input", "uptime", "authreq", "authon");
    private static final Set<String> UDP_REPORT_100 = Set.of("sessionrfidclass", "sessionid");
    private static final Set<String> REST_ONLY = Set.of("reststate", "session", "error", "reserved", "temperature",
            "restinput", "sessionstart", "sessionduration", "externalmeter", "maxphases", "phaseconfiguration",
            "dipswitchsettings", "dipswitchinterpretation", "permanentlylocked", "start", "stop", "reboot", "unlock",
            "togglephaseswitch");
    private static final Set<String> UDP_COMMANDS = Set.of("display", "output", "authenticate", "maxpresetcurrent",
            "maxpresetcurrentrange");
    private static final Set<String> REST_SHARED = Set.of("state", "I1", "I2", "I3", "U1", "U2", "U3", "power",
            "powerfactor", "totalconsumption", "sessionconsumption", "maxpilotcurrent", "maxsystemcurrent",
            "phaseswitchstate", "phaseswitchsource");
    private static final Set<String> MODBUS_CHANNELS = Set.of("state", "cablestate", "errorcode", "I1", "I2", "I3",
            "power", "totalconsumption", "U1", "U2", "U3", "powerfactor", "maxpilotcurrent", "maxsystemcurrent",
            "fastchargingstatus", "sessionrfidtag", "sessionconsumption", "phaseswitchsource", "phaseswitchstate",
            "failsafecurrent", "failsafetimeout", "maxpresetcurrent", "setenergylimit", "unlockplug", "enableduser",
            "triggerphaseswitch", "failsafepersist", "activatefastcharging");
    private static final Set<String> UDP_CHANNELS = Set.of("backend", "timequality", "bootflag", "dipswitch1",
            "dipswitch2", "enabledsystem", "enableduser", "maxpresetcurrent", "maxpresetcurrentrange", "error1",
            "error2", "state", "wallbox", "vehicle", "locked", "maxpilotcurrent", "maxpilotcurrentdutycyle",
            "maxsystemcurrent", "failsafecurrent", "failsafetimeout", "currtimer", "currtimertimeout", "output",
            "input", "uptime", "authreq", "authon", "I1", "I2", "I3", "U1", "U2", "U3", "power", "powerfactor",
            "totalconsumption", "sessionconsumption", "sessionrfidtag", "sessionrfidclass", "sessionid", "display",
            "authenticate", "setenergylimit", "phaseswitchsource", "phaseswitchstate", "triggerphaseswitch");
    private static final Set<String> REST_CHANNELS = Set.of("reststate", "session", "error", "reserved", "temperature",
            "restinput", "sessionstart", "sessionduration", "externalmeter", "maxphases", "phaseconfiguration",
            "dipswitchsettings", "dipswitchinterpretation", "permanentlylocked", "start", "stop", "reboot", "unlock",
            "authon", "enableduser", "vehicle", "state", "I1", "I2", "I3", "U1", "U2", "U3", "power", "powerfactor",
            "totalconsumption", "sessionconsumption", "maxpilotcurrent", "maxsystemcurrent", "phaseswitchstate",
            "phaseswitchsource", "togglephaseswitch");

    private final ModbusManager modbusManager;
    private final KeContactTransceiver transceiver;
    private final @Nullable ThingTypeRegistry thingTypeRegistry;
    private volatile @Nullable Session session;

    private static final class Session {
        final KeContactCombinedConfiguration config;
        final AtomicBoolean active = new AtomicBoolean(true);
        final Object udpLock = new Object();
        volatile boolean modbusOnline;
        volatile boolean restOnline;
        volatile boolean udpOnline;
        volatile boolean modbusPending;
        volatile boolean restPending;
        volatile boolean udpPending;
        volatile boolean udpIdentified;
        volatile boolean udpCommands;
        volatile String product = "";
        long nextUdpProbe;
        long nextUdpPoll;
        long nextUdpSupplementalPoll;
        long nextRestRetry;
        @Nullable
        KeContactModbusHandler modbus;
        @Nullable
        KeContactRestHandler rest;
        @Nullable
        KeContactHandler udp;
        @Nullable
        ScheduledFuture<?> job;
        @Nullable
        ScheduledFuture<?> linkJob;

        Session(KeContactCombinedConfiguration config) {
            this.config = config;
            modbusPending = config.modbusEnabled;
            restPending = config.restEnabled;
            udpPending = config.udpEnabled;
        }
    }

    public KeContactCombinedHandler(Thing thing, ModbusManager modbusManager, KeContactTransceiver transceiver) {
        this(thing, modbusManager, transceiver, null);
    }

    public KeContactCombinedHandler(Thing thing, ModbusManager modbusManager, KeContactTransceiver transceiver,
            @Nullable ThingTypeRegistry thingTypeRegistry) {
        super(thing);
        this.modbusManager = modbusManager;
        this.transceiver = transceiver;
        this.thingTypeRegistry = thingTypeRegistry;
    }

    @Override
    public void initialize() {
        stopSession();
        KeContactCombinedConfiguration config = getConfigAs(KeContactCombinedConfiguration.class);
        boolean restEnabled = config.restEnabled;
        boolean hasProtocol = config.modbusEnabled || config.udpEnabled || restEnabled;
        boolean validModbus = !config.modbusEnabled || !config.getModbusAddress().isBlank() && config.port >= 1
                && config.port <= 65535 && config.unitId >= 0 && config.unitId <= 255;
        boolean validWallboxAddress = !(config.udpEnabled || restEnabled) || !config.ipAddress.isBlank();
        boolean validRestPort = !restEnabled || config.restPort >= 1 && config.restPort <= 65535;
        if (!validModbus || !validWallboxAddress || !validRestPort || config.refreshInterval < 10
                || config.refreshIntervalSlow < 10) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Configure enabled protocol endpoints and valid refresh intervals");
            return;
        }
        String password = config.password;
        if (restEnabled && (password == null || password.isBlank())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Configure REST credentials or disable REST");
            return;
        }
        if (restEnabled) {
            try {
                config.getRestBaseUrl();
            } catch (IllegalArgumentException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Invalid wallbox network address for REST");
                return;
            }
        }
        Session localSession = new Session(config);
        @Nullable
        String model = getThing().getProperties().get(PROPERTY_MODEL);
        if (model == null) {
            model = getThing().getProperties().get("modbusModel");
        }
        if (model != null) {
            localSession.product = model;
        }
        session = localSession;
        reconcileChannels(localSession);
        updateStatus(ThingStatus.UNKNOWN);
        updateProperties(Map.of("modbusAvailable", config.modbusEnabled ? "unknown" : "disabled", "udpAvailable",
                config.udpEnabled ? "unknown" : "disabled", "restAvailable", restEnabled ? "unknown" : "disabled"));
        if (config.modbusEnabled) {
            Configuration modbusConfig = new Configuration(Map.of("ipAddress", config.getModbusAddress(), "port",
                    config.port, "unitId", config.unitId, "refreshInterval", config.refreshInterval,
                    "refreshIntervalSlow", config.refreshIntervalSlow));
            KeContactModbusHandler modbus = new KeContactModbusHandler(protocolThing(), modbusManager, modbusConfig,
                    listener(localSession, Protocol.MODBUS));
            localSession.modbus = modbus;
            modbus.initialize();
        }
        if (config.udpEnabled) {
            KeContactHandler udp = new KeContactHandler(protocolThing(), transceiver, new Configuration(),
                    listener(localSession, Protocol.UDP));
            localSession.udp = udp;
        }
        if (restEnabled && password != null) {
            Configuration restConfig = new Configuration(Map.of("ipAddress", config.ipAddress, "restPort",
                    config.restPort, "restEnabled", true, "username", config.username, "password", password,
                    "refreshInterval", config.refreshInterval, "refreshIntervalSlow", config.refreshIntervalSlow,
                    "verifyCertificate", config.verifyCertificate));
            KeContactRestHandler rest = new KeContactRestHandler(protocolThing(), restConfig,
                    listener(localSession, Protocol.REST));
            localSession.rest = rest;
            rest.initialize();
        }
        if (hasProtocol) {
            localSession.job = scheduler.scheduleWithFixedDelay(() -> pollSupplemental(localSession), 0, 1,
                    TimeUnit.SECONDS);
        }
        updateCombinedStatus(localSession);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        initialize();
    }

    private void stopSession() {
        Session localSession = session;
        session = null;
        if (localSession == null) {
            return;
        }
        localSession.active.set(false);
        ScheduledFuture<?> job = localSession.job;
        if (job != null) {
            job.cancel(true);
        }
        ScheduledFuture<?> linkJob = localSession.linkJob;
        if (linkJob != null) {
            linkJob.cancel(false);
        }
        KeContactHandler udp = localSession.udp;
        if (udp != null) {
            udp.dispose();
        }
        KeContactRestHandler rest = localSession.rest;
        if (rest != null) {
            rest.dispose();
        }
        KeContactModbusHandler modbus = localSession.modbus;
        if (modbus != null) {
            modbus.dispose();
        }
    }

    private Thing protocolThing() {
        return ThingBuilder.create(getThing().getThingTypeUID(), getThing().getUID())
                .withChannels(getThing().getChannels()).withConfiguration(getThing().getConfiguration()).build();
    }

    private boolean current(Session localSession) {
        return localSession.active.get() && localSession.equals(session);
    }

    @Override
    protected void updateProperties(@Nullable Map<String, String> properties) {
        super.updateProperties(properties);
        if (properties == null) {
            return;
        }
        @Nullable
        String model = properties.get("modbusModel");
        if (model == null) {
            model = properties.get(PROPERTY_MODEL);
        }
        Session localSession = session;
        if (model != null && localSession != null
                && (localSession.product.isBlank() || model.length() >= localSession.product.length())) {
            localSession.product = model;
        }
        boolean p30 = model != null && model.toUpperCase(Locale.ROOT).contains("P30");
        if (localSession != null) {
            reconcileChannels(localSession);
        } else if (p30) {
            var removedChannels = getThing().getChannels().stream()
                    .filter(channel -> Set.of(CHANNEL_FAST_CHARGING_STATUS, CHANNEL_ACTIVATE_FAST_CHARGING)
                            .contains(channel.getUID().getIdWithoutGroup()))
                    .toList();
            if (!removedChannels.isEmpty()) {
                var builder = editThing();
                removedChannels.forEach(channel -> builder.withoutChannel(channel.getUID()));
                updateThing(builder.build());
            }
        }
    }

    private void reconcileChannels(Session localSession) {
        if (!current(localSession)) {
            return;
        }
        Map<String, org.openhab.core.thing.Channel> existing = new HashMap<>();
        getThing().getChannels().forEach(channel -> existing.put(channel.getUID().getIdWithoutGroup(), channel));
        List<org.openhab.core.thing.Channel> desired = new ArrayList<>();
        Set<String> added = new HashSet<>();
        @Nullable
        ThingTypeRegistry registry = thingTypeRegistry;
        @Nullable
        ThingType thingType = registry == null ? null : registry.getThingType(getThing().getThingTypeUID());
        boolean repaired = false;
        if (thingType != null) {
            @Nullable
            ThingHandlerCallback callback = getCallback();
            if (callback == null) {
                return;
            }
            for (ChannelDefinition definition : thingType.getChannelDefinitions()) {
                String id = definition.getId();
                if (!channelSupported(id, localSession)) {
                    continue;
                }
                @Nullable
                Channel channel = existing.get(id);
                ChannelUID channelUID = new ChannelUID(getThing().getUID(), id);
                ChannelBuilder builder = callback.createChannelBuilder(channelUID, definition.getChannelTypeUID());
                Channel canonical = builder.build();
                // XML candidates can be absent from the Thing after protocol-driven pruning.
                if (channel == null) {
                    builder.withProperties(definition.getProperties());
                    @Nullable
                    String label = definition.getLabel();
                    if (label != null) {
                        builder.withLabel(label);
                    }
                    @Nullable
                    String description = definition.getDescription();
                    if (description != null) {
                        builder.withDescription(description);
                    }
                    if (definition.getAutoUpdatePolicy() != null) {
                        builder.withAutoUpdatePolicy(definition.getAutoUpdatePolicy());
                    }
                    channel = builder.build();
                } else if (!definition.getChannelTypeUID().equals(channel.getChannelTypeUID())
                        || !Objects.equals(canonical.getAcceptedItemType(), channel.getAcceptedItemType())
                        || canonical.getKind() != channel.getKind()) {
                    channel = ChannelBuilder.create(channel).withType(definition.getChannelTypeUID())
                            .withAcceptedItemType(canonical.getAcceptedItemType()).withKind(canonical.getKind())
                            .build();
                    repaired = true;
                }
                desired.add(channel);
                added.add(id);
            }
        } else {
            for (org.openhab.core.thing.Channel channel : existing.values()) {
                String id = channel.getUID().getIdWithoutGroup();
                if (channelSupported(id, localSession)) {
                    desired.add(channel);
                    added.add(id);
                }
            }
        }
        if (repaired || desired.size() != existing.size() || !existing.keySet().equals(added)) {
            var builder = editThing().withChannels(desired);
            updateThing(builder.build());
        }
    }

    private boolean channelSupported(String channel, Session localSession) {
        String product = localSession.product.toUpperCase(Locale.ROOT);
        if (product.contains("P30")
                && Set.of(CHANNEL_FAST_CHARGING_STATUS, CHANNEL_ACTIVATE_FAST_CHARGING).contains(channel)) {
            return false;
        }
        boolean modbusSupported = localSession.config.modbusEnabled && !product.contains("P20");
        boolean udpSupported = localSession.config.udpEnabled && !product.contains("P40") && !product.contains("P0");
        boolean restSupported = localSession.config.restEnabled && !restUnsupportedForProduct(product);
        return modbusSupported && MODBUS_CHANNELS.contains(channel) || udpSupported && UDP_CHANNELS.contains(channel)
                || restSupported && REST_CHANNELS.contains(channel);
    }

    private KeContactProtocolHandler.Listener listener(Session localSession, Protocol protocol) {
        return new KeContactProtocolHandler.Listener() {
            @Override
            public boolean isPrimary() {
                return current(localSession) && switch (protocol) {
                    case MODBUS -> true;
                    case REST -> !localSession.modbusOnline;
                    case UDP -> !localSession.modbusOnline && !localSession.restOnline;
                };
            }

            @Override
            public void stateUpdated(String channel, State state) {
                if (current(localSession) && (protocol != Protocol.REST || localSession.rest != null)) {
                    publish(localSession, protocol, channel, state);
                }
            }

            @Override
            public void statusUpdated(ThingStatus status, ThingStatusDetail detail, @Nullable String description) {
                if (!current(localSession) || protocol == Protocol.UDP
                        || protocol == Protocol.REST && localSession.rest == null) {
                    return;
                }
                boolean online = status == ThingStatus.ONLINE;
                if (protocol == Protocol.MODBUS) {
                    localSession.modbusOnline = online;
                    localSession.modbusPending = status == ThingStatus.UNKNOWN || status == ThingStatus.INITIALIZING;
                } else {
                    localSession.restOnline = online;
                    localSession.restPending = status == ThingStatus.UNKNOWN || status == ThingStatus.INITIALIZING;
                }
                updateProperty(protocol == Protocol.MODBUS ? "modbusAvailable" : "restAvailable",
                        online ? "available" : status == ThingStatus.UNKNOWN ? "unknown" : "unavailable");
                updateCombinedStatus(localSession);
            }

            @Override
            public void propertiesUpdated(Map<String, String> properties) {
                if (!current(localSession) || protocol == Protocol.REST && localSession.rest == null) {
                    return;
                }
                Map<String, String> supplemental = new HashMap<>(properties);
                if (protocol != Protocol.MODBUS && localSession.modbusOnline) {
                    supplemental.remove(PROPERTY_SERIAL);
                    supplemental.remove(PROPERTY_FIRMWARE);
                }
                String model = properties.get(PROPERTY_MODEL);
                if (model != null
                        && (localSession.product.isBlank() || model.length() >= localSession.product.length())) {
                    localSession.product = model;
                } else {
                    supplemental.remove(PROPERTY_MODEL);
                }
                updateProperties(supplemental);
            }

            @Override
            public boolean isLinked(String channel) {
                if (!current(localSession)) {
                    return false;
                }
                if (protocol == Protocol.MODBUS && CHANNEL_CABLE_STATE.equals(channel)) {
                    return KeContactCombinedHandler.this.isLinked(channel)
                            || KeContactCombinedHandler.this.isLinked(CHANNEL_VEHICLE)
                            || KeContactCombinedHandler.this.isLinked(CHANNEL_WALLBOX)
                            || KeContactCombinedHandler.this.isLinked(CHANNEL_PLUG_LOCKED);
                }
                if (protocol == Protocol.REST && "phaseswitchsource".equals(channel) && localSession.modbusOnline) {
                    return false;
                }
                return KeContactCombinedHandler.this.isLinked(channel);
            }
        };
    }

    static Protocol sourceFor(String channel, boolean modbusOnline, boolean restOnline) {
        if (REST_ONLY.contains(channel)) {
            return Protocol.REST;
        }
        if ("vehicle".equals(channel) || "wallbox".equals(channel) || "locked".equals(channel)) {
            return modbusOnline ? Protocol.MODBUS
                    : restOnline && "vehicle".equals(channel) ? Protocol.REST : Protocol.UDP;
        }
        if ("authon".equals(channel) || "enableduser".equals(channel)) {
            return restOnline ? Protocol.REST : Protocol.UDP;
        }
        String modbusChannel = switch (channel) {
            case "maxsystemcurrent" -> CHANNEL_MAX_SUPPORTED_CURRENT;
            case "maxpilotcurrent" -> CHANNEL_MAX_CHARGING_CURRENT;
            case "failsafecurrent" -> CHANNEL_FAILSAFE_CURRENT_SETTING;
            case "failsafetimeout" -> CHANNEL_FAILSAFE_TIMEOUT_SETTING;
            default -> channel;
        };
        for (KebaModbusReadRegister register : KebaModbusReadRegister.values()) {
            if (register.getChannelId().equals(modbusChannel)) {
                return modbusOnline ? Protocol.MODBUS
                        : restOnline && REST_SHARED.contains(channel) ? Protocol.REST : Protocol.UDP;
            }
        }
        return Protocol.UDP;
    }

    static String channelGroup(String channel) {
        for (KebaModbusWriteRegister register : KebaModbusWriteRegister.values()) {
            if (register.getChannelId().equals(channel)) {
                return "modbus";
            }
        }
        return switch (sourceFor(channel, true, true)) {
            case MODBUS -> "modbus";
            case UDP -> "udp";
            case REST -> "rest";
        };
    }

    private String resolveChannelId(String channel) {
        String grouped = channelGroup(channel) + "#" + channel;
        return getThing().getChannel(grouped) != null ? grouped : channel;
    }

    @Override
    protected boolean isLinked(ChannelUID channelUID) {
        return super.isLinked(new ChannelUID(getThing().getUID(), resolveChannelId(channelUID.getIdWithoutGroup())));
    }

    @Override
    protected void updateState(String channel, State state) {
        super.updateState(resolveChannelId(channel), state);
    }

    private void publish(Session localSession, Protocol protocol, String originalChannel, State state) {
        if (protocol == Protocol.UDP && "dipswitch1".equals(originalChannel)) {
            try {
                localSession.udpCommands = (Long.decode(state.toString()) & 0x20) != 0;
            } catch (NumberFormatException e) {
                localSession.udpCommands = false;
            }
        }
        String channel = switch (protocol) {
            case UDP -> originalChannel;
            case REST -> switch (originalChannel) {
                case "state" -> "reststate";
                case "energy" -> CHANNEL_TOTAL_CONSUMPTION;
                case "current" -> CHANNEL_PILOT_CURRENT;
                case "maxsupportedcurrent" -> CHANNEL_MAX_SYSTEM_CURRENT;
                case "input" -> "restinput";
                default -> originalChannel;
            };
            case MODBUS -> switch (originalChannel) {
                case "maxsupportedcurrent" -> CHANNEL_MAX_SYSTEM_CURRENT;
                case "maxchargingcurrent" -> CHANNEL_PILOT_CURRENT;
                case "failsafecurrentsetting" -> CHANNEL_FAILSAFE_CURRENT;
                case "failsafetimeoutsetting" -> CHANNEL_FAILSAFE_TIMEOUT;
                default -> originalChannel;
            };
        };
        if (protocol == Protocol.REST && "reststate".equals(channel)
                && sourceFor(CHANNEL_STATE, localSession.modbusOnline, localSession.restOnline) == Protocol.REST) {
            updateState(CHANNEL_STATE, numericRestState(state.toString()));
        }
        if (sourceFor(channel, localSession.modbusOnline, localSession.restOnline) != protocol) {
            return;
        }
        if (protocol == Protocol.MODBUS && CHANNEL_CABLE_STATE.equals(channel) && state instanceof DecimalType cable) {
            int value = cable.intValue();
            boolean known = value == 0 || value == 1 || value == 3 || value == 5 || value == 7;
            updateState(CHANNEL_VEHICLE, known ? OnOffType.from(value == 5 || value == 7) : UnDefType.UNDEF);
            updateState(CHANNEL_WALLBOX,
                    known ? OnOffType.from(value == 1 || value == 3 || value == 5 || value == 7) : UnDefType.UNDEF);
            updateState(CHANNEL_PLUG_LOCKED, known ? OnOffType.from(value == 3 || value == 7) : UnDefType.UNDEF);
        }
        if (protocol == Protocol.UDP && ("authon".equals(channel) || "authreq".equals(channel))
                && state instanceof DecimalType value) {
            state = OnOffType.from(value.intValue() == 1);
        }
        if (getThing().getChannel(resolveChannelId(channel)) != null) {
            updateState(channel, state);
        }
    }

    static State numericRestState(String state) {
        return switch (state) {
            case "IDLE", "UNAVAILABLE" -> new DecimalType(1);
            case "READY_FOR_CHARGING" -> new DecimalType(2);
            case "CHARGING" -> new DecimalType(3);
            case "RECOVER_FROM_ERROR", "UNRECOVERABLE_ERROR", "DEGRADED" -> new DecimalType(4);
            case "SUSPENDED" -> new DecimalType(5);
            default -> UnDefType.UNDEF;
        };
    }

    private boolean needsReport(Session localSession, Set<String> channels) {
        if (channels.equals(UDP_REPORT_2) && !localSession.restOnline && getThing().getChannel("input") != null
                && isLinked("input")) {
            return true;
        }
        return channels.stream().anyMatch(channel -> isLinked(channel)
                && sourceFor(channel, localSession.modbusOnline, localSession.restOnline) == Protocol.UDP);
    }

    private void pollSupplemental(Session localSession) {
        if (!current(localSession)) {
            return;
        }
        long now = pollingTime();
        KeContactRestHandler rest = localSession.rest;
        if (rest != null && !localSession.restOnline && restUnsupportedForProduct(localSession.product)) {
            localSession.rest = null;
            localSession.restPending = false;
            rest.dispose();
            updateProperty("restAvailable", "unsupported");
            rest = null;
        }
        if (rest != null && !localSession.restOnline && now >= localSession.nextRestRetry) {
            localSession.nextRestRetry = now + TimeUnit.SECONDS.toNanos(300);
            rest.retryInitialization();
        }
        KeContactHandler udp = localSession.udp;
        if (!current(localSession) || udp == null || localSession.product.contains("P40")
                || localSession.product.contains("P0")) {
            if (current(localSession) && udp != null) {
                localSession.udp = null;
                localSession.udpPending = false;
                udp.dispose();
                updateProperty("udpAvailable", "unsupported");
                updateCombinedStatus(localSession);
            }
            return;
        }
        synchronized (localSession.udpLock) {
            if (!current(localSession)) {
                return;
            }
            if (!localSession.udpIdentified) {
                if (now < localSession.nextUdpProbe) {
                    return;
                }
                localSession.nextUdpProbe = now + TimeUnit.SECONDS.toNanos(300);
                localSession.udpPending = true;
                udp.initializeCommands(localSession.config.ipAddress);
                localSession.udpIdentified = udp.readReport(1);
                if (!current(localSession)) {
                    udp.dispose();
                    return;
                }
                updateProperty("udpAvailable", !localSession.udpIdentified ? "unavailable"
                        : localSession.udpCommands ? "available" : "identification-only");
                if (!localSession.udpIdentified) {
                    localSession.udpPending = false;
                    updateCombinedStatus(localSession);
                    return;
                }
            }
            boolean baseline = !localSession.modbusOnline && !localSession.restOnline;
            boolean operationalDue = baseline && now >= localSession.nextUdpPoll;
            boolean supplementalDue = now >= localSession.nextUdpSupplementalPoll;
            if (!operationalDue && !supplementalDue) {
                return;
            }
            if (operationalDue) {
                int interval = Math.min(localSession.config.refreshInterval, localSession.config.refreshIntervalSlow);
                localSession.nextUdpPoll = now + TimeUnit.SECONDS.toNanos(interval);
            }
            if (supplementalDue) {
                localSession.nextUdpSupplementalPoll = now
                        + TimeUnit.SECONDS.toNanos(localSession.config.refreshIntervalSlow);
            }
            boolean report2 = operationalDue || !baseline && supplementalDue && needsReport(localSession, UDP_REPORT_2);
            if (report2) {
                localSession.udpOnline = udp.readReport(2);
                localSession.udpCommands = localSession.udpOnline;
                if (!localSession.udpOnline) {
                    localSession.udpIdentified = false;
                    localSession.udpPending = false;
                    updateProperty("udpAvailable", "unavailable");
                    updateCombinedStatus(localSession);
                    return;
                }
                updateProperty("udpAvailable", "available");
            }
            if (supplementalDue && needsReport(localSession, UDP_REPORT_1)) {
                udp.readReport(1);
            }
            if (operationalDue && List.of("I1", "I2", "I3", "U1", "U2", "U3", "power", "powerfactor",
                    "totalconsumption", "sessionconsumption").stream().anyMatch(this::isLinked)) {
                udp.readReport(3);
            }
            if (supplementalDue && (needsReport(localSession, UDP_REPORT_100)
                    || !localSession.modbusOnline && isLinked("sessionrfidtag"))) {
                udp.readReport(100);
            }
            if (current(localSession)) {
                localSession.udpPending = false;
                updateCombinedStatus(localSession);
            }
        }
    }

    long pollingTime() {
        return System.nanoTime();
    }

    private void updateCombinedStatus(Session localSession) {
        if (!current(localSession)) {
            return;
        }
        if (localSession.modbusOnline || localSession.udpOnline || localSession.restOnline) {
            updateStatus(ThingStatus.ONLINE);
        } else if (localSession.modbusPending || localSession.udpPending || localSession.restPending) {
            updateStatus(ThingStatus.UNKNOWN);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "No wallbox protocol is responding; enable Modbus/UDP or configure REST credentials");
        }
    }

    static boolean restUnsupportedForProduct(String product) {
        return product.contains("P20") || product.contains("P30") && product.length() > 13
                && KebaSeries.C.matchesSeries(Character.toUpperCase(product.charAt(13)));
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        refreshModbusPolls();
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        refreshModbusPolls();
    }

    private void refreshModbusPolls() {
        Session localSession = session;
        if (localSession == null || !current(localSession)) {
            return;
        }
        ScheduledFuture<?> task = localSession.linkJob;
        if (task != null) {
            task.cancel(false);
        }
        localSession.linkJob = scheduler.schedule(() -> {
            KeContactModbusHandler modbus = localSession.modbus;
            if (current(localSession) && modbus != null) {
                modbus.refreshLinkedPolls();
            }
        }, 1, TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Session localSession = session;
        if (localSession == null || !current(localSession) || command instanceof RefreshType) {
            return;
        }
        String channel = channelUID.getIdWithoutGroup();
        if ("display".equals(channel) && command instanceof StringType text) {
            setDisplay(text.toString(), -1, -1);
            return;
        }
        scheduler.execute(() -> {
            if (!current(localSession)) {
                return;
            }
            KeContactModbusHandler modbus = localSession.modbus;
            KeContactRestHandler rest = localSession.rest;
            KeContactHandler udp = localSession.udp;
            String modbusChannel = "maxpresetcurrent".equals(channel) ? CHANNEL_SET_CHARGING_CURRENT : channel;
            boolean modbusCommand = List.of(KebaModbusWriteRegister.values()).stream()
                    .anyMatch(register -> register.getChannelId().equals(modbusChannel));
            if (modbusCommand && localSession.modbusOnline && modbus != null) {
                modbus.handleCommand(new ChannelUID(getThing().getUID(), modbusChannel), command);
            } else if ((REST_ONLY.contains(channel) || "enableduser".equals(channel)
                    || "phaseswitchsource".equals(channel)) && localSession.restOnline && rest != null) {
                rest.handleCommand(new ChannelUID(getThing().getUID(), channel), command);
            } else if (udp != null && (UDP_COMMANDS.contains(channel)
                    || Set.of("setenergylimit", "enableduser", "phaseswitchsource", "triggerphaseswitch")
                            .contains(channel))) {
                synchronized (localSession.udpLock) {
                    if (current(localSession) && ensureUdpCommands(localSession, udp)) {
                        String udpChannel = switch (channel) {
                            case "triggerphaseswitch" -> "phaseswitchstate";
                            default -> channel;
                        };
                        if ("triggerphaseswitch".equals(channel) && (!(command instanceof DecimalType value)
                                || value.toBigDecimal().compareTo(BigDecimal.ONE) != 0
                                        && value.toBigDecimal().compareTo(BigDecimal.valueOf(3)) != 0)) {
                            return;
                        }
                        udp.handleCommand(new ChannelUID(getThing().getUID(), udpChannel), command);
                    }
                }
            }
        });
    }

    private boolean ensureUdpCommands(Session localSession, KeContactHandler udp) {
        if (!localSession.udpIdentified) {
            return false;
        }
        if (!localSession.udpCommands) {
            localSession.udpCommands = udp.readReport(2);
            if (current(localSession)) {
                updateProperty("udpAvailable", localSession.udpCommands ? "available" : "identification-only");
            }
        }
        return current(localSession) && localSession.udpCommands;
    }

    public void setDisplay(@Nullable String text, int durationMin, int durationMax) {
        Session localSession = session;
        if (localSession == null) {
            return;
        }
        scheduler.execute(() -> {
            KeContactHandler udp = localSession.udp;
            if (udp != null && current(localSession)) {
                synchronized (localSession.udpLock) {
                    if (current(localSession) && ensureUdpCommands(localSession, udp)) {
                        udp.setDisplay(text, durationMin, durationMax);
                    }
                }
            }
        });
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(KeContactActions.class);
    }

    @Override
    public void dispose() {
        stopSession();
        super.dispose();
    }
}
