/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.nuvo.internal.handler;

import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.openhab.binding.nuvo.internal.NuvoBindingConstants.*;

import java.io.StringReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.Unit;
import javax.measure.quantity.Time;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.nuvo.internal.NuvoException;
import org.openhab.binding.nuvo.internal.NuvoStateDescriptionOptionProvider;
import org.openhab.binding.nuvo.internal.NuvoThingActions;
import org.openhab.binding.nuvo.internal.communication.NuvoCommand;
import org.openhab.binding.nuvo.internal.communication.NuvoConnector;
import org.openhab.binding.nuvo.internal.communication.NuvoEnum;
import org.openhab.binding.nuvo.internal.communication.NuvoImageResizer;
import org.openhab.binding.nuvo.internal.communication.NuvoIpConnector;
import org.openhab.binding.nuvo.internal.communication.NuvoMessageEvent;
import org.openhab.binding.nuvo.internal.communication.NuvoMessageEventListener;
import org.openhab.binding.nuvo.internal.communication.NuvoSerialConnector;
import org.openhab.binding.nuvo.internal.communication.NuvoStatusCodes;
import org.openhab.binding.nuvo.internal.configuration.NuvoBindingConfiguration;
import org.openhab.binding.nuvo.internal.configuration.NuvoThingConfiguration;
import org.openhab.binding.nuvo.internal.dto.JAXBUtils;
import org.openhab.binding.nuvo.internal.dto.NuvoMenu;
import org.openhab.binding.nuvo.internal.dto.NuvoMenu.Source.TopMenu;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NuvoHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * Based on the Rotel binding by Laurent Garnier
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class NuvoHandler extends BaseThingHandler implements NuvoMessageEventListener {
    private static final long RECON_POLLING_INTERVAL_SEC = 60;
    private static final long POLLING_INTERVAL_SEC = 30;
    private static final long CLOCK_SYNC_INTERVAL_SEC = 3600;
    private static final long INITIAL_POLLING_DELAY_SEC = 30;
    private static final long INITIAL_CLOCK_SYNC_DELAY_SEC = 10;
    private static final long PING_TIMEOUT_SEC = 60;
    // spec says wait 50ms, min is 100
    private static final long SLEEP_BETWEEN_CMD_MS = 100;
    private static final Unit<Time> API_SECOND_UNIT = Units.SECOND;

    private static final String ZONE = "ZONE";
    private static final String SOURCE = "SOURCE";
    private static final String CHANNEL_DELIMIT = "#";
    private static final String UNDEF = "UNDEF";
    private static final String GC_STR = "NV-I8G";

    private static final int MAX_ZONES = 20;
    private static final int MAX_SRC = 6;
    private static final int MAX_FAV = 12;
    private static final int MIN_VOLUME = 0;
    private static final int MAX_VOLUME = 79;
    private static final int MIN_EQ = -18;
    private static final int MAX_EQ = 18;

    private static final int MPS4_PORT = 5006;

    private static final byte[] NO_ART = { 0 };

    private static final Pattern ZONE_PATTERN = Pattern
            .compile("^ON,SRC(\\d{1}),(MUTE|VOL\\d{1,2}),DND([0-1]),LOCK([0-1])$");
    private static final Pattern DISP_PATTERN = Pattern.compile("^DISPLINE(\\d{1}),\"(.*)\"$");
    private static final Pattern DISP_INFO_PATTERN = Pattern
            .compile("^DISPINFO,DUR(\\d{1,6}),POS(\\d{1,6}),STATUS(\\d{1,2})$");
    private static final Pattern ZONE_CFG_EQ_PATTERN = Pattern.compile("^BASS(.*),TREB(.*),BAL(.*),LOUDCMP([0-1])$");
    private static final Pattern ZONE_CFG_PATTERN = Pattern.compile(
            "^ENABLE1,NAME\"(.*)\",SLAVETO(.*),GROUP([0-4]),SOURCES(.*),XSRC(.*),IR(.*),DND(.*),LOCKED(.*),SLAVEEQ(.*)$");
    private static final Pattern MCS_INSTANCE_PATTERN = Pattern.compile("MCSInstance\",\"value\":\"(.*?)\"");
    private static final Pattern ART_GUID_PATTERN = Pattern.compile("NowPlayingGuid\",\"value\":\"\\{(.*?)\\}\"\\}");

    private final Logger logger = LoggerFactory.getLogger(NuvoHandler.class);
    private final NuvoBindingConfiguration bindingConf;
    private final NuvoStateDescriptionOptionProvider stateDescriptionProvider;
    private final SerialPortManager serialPortManager;
    private final HttpClient httpClient;

    private @Nullable ScheduledFuture<?> reconnectJob;
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ScheduledFuture<?> clockSyncJob;
    private @Nullable ScheduledFuture<?> pingJob;

    private NuvoConnector connector = new NuvoIpConnector();
    private long lastEventReceived = System.currentTimeMillis();
    private int numZones = 1;
    private String versionString = BLANK;
    private boolean isGConcerto = false;
    private Object sequenceLock = new Object();

    private boolean isAnyOhNuvoNet = false;
    private NuvoMenu nuvoMenus = new NuvoMenu();
    private HashMap<String, Set<NuvoEnum>> nuvoGroupMap = new HashMap<>();
    private HashMap<NuvoEnum, Integer> nuvoNetSrcMap = new HashMap<>();
    private HashMap<NuvoEnum, String> favPrefixMap = new HashMap<>();
    private HashMap<NuvoEnum, String[]> favoriteMap = new HashMap<>();
    private HashMap<NuvoEnum, NuvoEnum> sourceZoneMap = new HashMap<>();
    private HashMap<NuvoEnum, String> sourceInstanceMap = new HashMap<>();

    private HashMap<NuvoEnum, byte[]> albumArtMap = new HashMap<>();
    private HashMap<NuvoEnum, Integer> albumArtIds = new HashMap<>();
    private HashMap<NuvoEnum, String> dispInfoCache = new HashMap<>();
    private HashMap<NuvoEnum, String> mps4ArtGuids = new HashMap<>();

    Set<Integer> activeZones = new HashSet<>(1);

    // A tree map that maps the source ids to source labels
    TreeMap<String, String> sourceLabels = new TreeMap<>();

    // Indicates if there is a need to poll status because of a disconnection used for MPS4 systems
    boolean pollStatusNeeded = true;
    boolean isMps4 = false;
    String mps4Host = BLANK;

    /**
     * Constructor
     */
    public NuvoHandler(Thing thing, NuvoStateDescriptionOptionProvider stateDescriptionProvider,
            SerialPortManager serialPortManager, HttpClient httpClient, NuvoBindingConfiguration bindingConf) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.serialPortManager = serialPortManager;
        this.httpClient = httpClient;
        this.bindingConf = bindingConf;
    }

    @Override
    public void initialize() {
        final String uid = this.getThing().getUID().getAsString();
        NuvoThingConfiguration config = getConfigAs(NuvoThingConfiguration.class);
        final String serialPort = config.serialPort;
        final String host = config.host;
        final Integer port = config.port;
        final Integer numZones = config.numZones;

        // Check configuration settings
        String configError = null;
        if ((serialPort == null || serialPort.isEmpty()) && (host == null || host.isEmpty())) {
            configError = "undefined serialPort and host configuration settings; please set one of them";
        } else if (serialPort != null && (host == null || host.isEmpty())) {
            if (serialPort.toLowerCase().startsWith("rfc2217")) {
                configError = "use host and port configuration settings for a serial over IP connection";
            }
        } else {
            if (port == null) {
                configError = "undefined port configuration setting";
            } else if (port <= 0) {
                configError = "invalid port configuration setting";
            }
        }

        if (configError != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, configError);
            return;
        }

        if (serialPort != null && !serialPort.isEmpty()) {
            connector = new NuvoSerialConnector(serialPortManager, serialPort, uid);
        } else if (host != null && port != null) {
            connector = new NuvoIpConnector(host, port, uid);
            this.isMps4 = (port.intValue() == MPS4_PORT);
            mps4Host = host;
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Either Serial port or Host & Port must be specifed");
            return;
        }

        nuvoNetSrcMap.put(NuvoEnum.SOURCE1, config.nuvoNetSrc1);
        nuvoNetSrcMap.put(NuvoEnum.SOURCE2, config.nuvoNetSrc2);
        nuvoNetSrcMap.put(NuvoEnum.SOURCE3, config.nuvoNetSrc3);
        nuvoNetSrcMap.put(NuvoEnum.SOURCE4, config.nuvoNetSrc4);
        nuvoNetSrcMap.put(NuvoEnum.SOURCE5, config.nuvoNetSrc5);
        nuvoNetSrcMap.put(NuvoEnum.SOURCE6, config.nuvoNetSrc6);

        IntStream.range(1, 5).forEach(i -> nuvoGroupMap.put(String.valueOf(i), new HashSet<>()));

        if (this.isMps4) {
            logger.debug("Port set to {} configuring binding for MPS4 compatability", MPS4_PORT);

            this.isAnyOhNuvoNet = (config.nuvoNetSrc1.equals(2) || config.nuvoNetSrc2.equals(2)
                    || config.nuvoNetSrc3.equals(2) || config.nuvoNetSrc4.equals(2) || config.nuvoNetSrc5.equals(2)
                    || config.nuvoNetSrc6.equals(2));

            mps4ArtGuids.put(NuvoEnum.SOURCE1, BLANK);
            mps4ArtGuids.put(NuvoEnum.SOURCE2, BLANK);
            mps4ArtGuids.put(NuvoEnum.SOURCE3, BLANK);
            mps4ArtGuids.put(NuvoEnum.SOURCE4, BLANK);
            mps4ArtGuids.put(NuvoEnum.SOURCE5, BLANK);
            mps4ArtGuids.put(NuvoEnum.SOURCE6, BLANK);

            if (this.isAnyOhNuvoNet) {
                logger.debug("At least one source is configured as an openHAB NuvoNet source");
                connector.setAnyOhNuvoNet(true);
                loadMenuConfiguration(config);

                favoriteMap.put(NuvoEnum.SOURCE1,
                        !config.favoritesSrc1.isEmpty() ? config.favoritesSrc1.split(COMMA) : new String[0]);
                favoriteMap.put(NuvoEnum.SOURCE2,
                        !config.favoritesSrc2.isEmpty() ? config.favoritesSrc2.split(COMMA) : new String[0]);
                favoriteMap.put(NuvoEnum.SOURCE3,
                        !config.favoritesSrc3.isEmpty() ? config.favoritesSrc3.split(COMMA) : new String[0]);
                favoriteMap.put(NuvoEnum.SOURCE4,
                        !config.favoritesSrc4.isEmpty() ? config.favoritesSrc4.split(COMMA) : new String[0]);
                favoriteMap.put(NuvoEnum.SOURCE5,
                        !config.favoritesSrc5.isEmpty() ? config.favoritesSrc5.split(COMMA) : new String[0]);
                favoriteMap.put(NuvoEnum.SOURCE6,
                        !config.favoritesSrc6.isEmpty() ? config.favoritesSrc6.split(COMMA) : new String[0]);

                favPrefixMap.put(NuvoEnum.SOURCE1, config.favPrefix1);
                favPrefixMap.put(NuvoEnum.SOURCE2, config.favPrefix2);
                favPrefixMap.put(NuvoEnum.SOURCE3, config.favPrefix3);
                favPrefixMap.put(NuvoEnum.SOURCE4, config.favPrefix4);
                favPrefixMap.put(NuvoEnum.SOURCE5, config.favPrefix5);
                favPrefixMap.put(NuvoEnum.SOURCE6, config.favPrefix6);

                albumArtIds.put(NuvoEnum.SOURCE1, 0);
                albumArtIds.put(NuvoEnum.SOURCE2, 0);
                albumArtIds.put(NuvoEnum.SOURCE3, 0);
                albumArtIds.put(NuvoEnum.SOURCE4, 0);
                albumArtIds.put(NuvoEnum.SOURCE5, 0);
                albumArtIds.put(NuvoEnum.SOURCE6, 0);

                albumArtMap.put(NuvoEnum.SOURCE1, NO_ART);
                albumArtMap.put(NuvoEnum.SOURCE2, NO_ART);
                albumArtMap.put(NuvoEnum.SOURCE3, NO_ART);
                albumArtMap.put(NuvoEnum.SOURCE4, NO_ART);
                albumArtMap.put(NuvoEnum.SOURCE5, NO_ART);
                albumArtMap.put(NuvoEnum.SOURCE6, NO_ART);
            }
        }

        if (numZones != null) {
            this.numZones = numZones;
        }

        activeZones = IntStream.range(1, this.numZones + 1).boxed().collect(Collectors.toSet());

        // remove the channels for the zones we are not using
        if (this.numZones < MAX_ZONES) {
            List<Channel> channels = new ArrayList<>(this.getThing().getChannels());

            List<Integer> zonesToRemove = IntStream.range(this.numZones + 1, MAX_ZONES + 1).boxed()
                    .collect(Collectors.toList());

            zonesToRemove.forEach(zone -> channels.removeIf(c -> (c.getUID().getId().contains("zone" + zone))));
            updateThing(editThing().withChannels(channels).build());
        }

        // Build a list of State options for the global favorites using user config values (if supplied)
        String[] favoritesArr = !config.favoriteLabels.isEmpty() ? config.favoriteLabels.split(COMMA) : new String[0];
        List<StateOption> favoriteLabelsStateOptions = new ArrayList<>();
        IntStream.range(1, MAX_FAV + 1).forEach(i -> {
            if (favoritesArr.length >= i) {
                favoriteLabelsStateOptions.add(new StateOption(String.valueOf(i), favoritesArr[i - 1]));
            } else if (favoritesArr.length == 0) {
                favoriteLabelsStateOptions.add(new StateOption(String.valueOf(i), "Favorite " + (i)));
            }
        });

        // Also add any openHAB NuvoNet source favorites to the list
        IntStream.range(1, MAX_SRC + 1).forEach(src -> {
            NuvoEnum source = NuvoEnum.valueOf(SOURCE + src);
            String[] favorites = favoriteMap.get(source);
            if (favorites != null) {
                IntStream.range(0, favorites.length).forEach(fav -> {
                    favoriteLabelsStateOptions.add(new StateOption(String.valueOf(src * 100 + fav),
                            favPrefixMap.get(source) + favorites[fav]));
                });
            }
        });

        // Put the global favorites labels on all active zones
        activeZones.forEach(zoneNum -> {
            stateDescriptionProvider.setStateOptions(
                    new ChannelUID(getThing().getUID(),
                            ZONE.toLowerCase() + zoneNum + CHANNEL_DELIMIT + CHANNEL_TYPE_FAVORITE),
                    favoriteLabelsStateOptions);
        });

        if (config.clockSync) {
            scheduleClockSyncJob();
        }

        scheduleReconnectJob();
        schedulePollingJob();
        schedulePingTimeoutJob();
        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void dispose() {
        if (this.isAnyOhNuvoNet) {
            try {
                // disable NuvoNet for each source that was configured as an openHAB NuvoNet source
                nuvoNetSrcMap.forEach((source, val) -> {
                    if (val.equals(2)) {
                        try {
                            connector.sendCommand(source.getId() + "DISPINFOTWO0,0,0,0,0,0,0");
                            Thread.sleep(SLEEP_BETWEEN_CMD_MS);
                            connector.sendCommand(
                                    source.getId() + "DISPLINES0,0,0,\"Source Unavailable\",\"\",\"\",\"\"");
                            Thread.sleep(SLEEP_BETWEEN_CMD_MS);
                            connector.sendCommand(source.getConfigId() + "NUVONET0");
                            Thread.sleep(SLEEP_BETWEEN_CMD_MS);
                        } catch (NuvoException | InterruptedException e) {
                            logger.debug("Error sending command to disable NuvoNet source: {}", source.getNum());
                        }
                    }
                });

                // need '1' flag for sources configured as an MPS4 NuvoNet source, but disable openHAB NuvoNet sources
                connector.sendCommand(
                        "SNUMBERS" + (nuvoNetSrcMap.getOrDefault(NuvoEnum.SOURCE1, 0).equals(1) ? ONE : ZERO) + COMMA
                                + (nuvoNetSrcMap.getOrDefault(NuvoEnum.SOURCE2, 0).equals(1) ? ONE : ZERO) + COMMA
                                + (nuvoNetSrcMap.getOrDefault(NuvoEnum.SOURCE3, 0).equals(1) ? ONE : ZERO) + COMMA
                                + (nuvoNetSrcMap.getOrDefault(NuvoEnum.SOURCE4, 0).equals(1) ? ONE : ZERO) + COMMA
                                + (nuvoNetSrcMap.getOrDefault(NuvoEnum.SOURCE5, 0).equals(1) ? ONE : ZERO) + COMMA
                                + (nuvoNetSrcMap.getOrDefault(NuvoEnum.SOURCE6, 0).equals(1) ? ONE : ZERO));
            } catch (NuvoException e) {
                logger.debug("Error sending SNUMBERS command to disable NuvoNet sources");
            }
        }

        cancelReconnectJob();
        cancelPollingJob();
        cancelClockSyncJob();
        cancelPingTimeoutJob();
        closeConnection();
        super.dispose();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(NuvoThingActions.class);
    }

    public void handleRawCommand(String command) {
        synchronized (sequenceLock) {
            try {
                connector.sendCommand(command);
            } catch (NuvoException e) {
                logger.warn("Nuvo Command: {} failed", command);
            }
        }
    }

    /**
     * Handle a command from the UI
     *
     * @param channelUID the channel sending the command
     * @param command the command received
     *
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channel = channelUID.getId();
        String[] channelSplit = channel.split(CHANNEL_DELIMIT);
        NuvoEnum target = NuvoEnum.valueOf(channelSplit[0].toUpperCase());

        String channelType = channelSplit[1];

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Thing is not ONLINE; command {} from channel {} is ignored", command, channel);
            return;
        }

        synchronized (sequenceLock) {
            if (!connector.isConnected()) {
                logger.warn("Command {} from channel {} is ignored: connection not established", command, channel);
                return;
            }

            try {
                switch (channelType) {
                    case CHANNEL_TYPE_POWER:
                        if (command instanceof OnOffType) {
                            connector.sendCommand(target, command == OnOffType.ON ? NuvoCommand.ON : NuvoCommand.OFF);
                        }
                        break;
                    case CHANNEL_TYPE_SOURCE:
                        if (command instanceof DecimalType decimalCommand) {
                            int value = decimalCommand.intValue();
                            if (value >= 1 && value <= MAX_SRC) {
                                logger.debug("Got source command {} zone {}", value, target);
                                connector.sendCommand(target, NuvoCommand.SOURCE, String.valueOf(value));

                                // update the other group member's selected source
                                updateSrcForZoneGroup(target, String.valueOf(value));
                                sourceZoneMap.put(NuvoEnum.valueOf(SOURCE + value), target);
                            }
                        }
                        break;
                    case CHANNEL_TYPE_FAVORITE:
                        if (command instanceof DecimalType decimalCommand) {
                            int value = decimalCommand.intValue();
                            if (value >= 1 && value <= MAX_FAV) {
                                logger.debug("Got favorite command {} zone {}", value, target);
                                connector.sendCommand(target, NuvoCommand.FAVORITE, String.valueOf(value));
                            } else if (value >= 100 && value <= 650) {
                                String sourceNum = String.valueOf(value / 100);
                                NuvoEnum source = NuvoEnum.valueOf(SOURCE + sourceNum);
                                updateChannelState(source, CHANNEL_BUTTON_PRESS,
                                        PLAY_MUSIC_PRESET + getFavorite(source, value % 100));
                                connector.sendCommand(target, NuvoCommand.SOURCE, sourceNum);

                                // if this zone is in a group, update the other group member's selected source
                                updateSrcForZoneGroup(target, sourceNum);
                            }
                        }
                        break;
                    case CHANNEL_TYPE_VOLUME:
                        if (command instanceof PercentType percentCommand) {
                            int value = (MAX_VOLUME
                                    - (int) Math.round(percentCommand.doubleValue() / 100.0 * (MAX_VOLUME - MIN_VOLUME))
                                    + MIN_VOLUME);
                            logger.debug("Got volume command {} zone {}", value, target);
                            connector.sendCommand(target, NuvoCommand.VOLUME, String.valueOf(value));
                        }
                        break;
                    case CHANNEL_TYPE_MUTE:
                        if (command instanceof OnOffType) {
                            connector.sendCommand(target,
                                    command == OnOffType.ON ? NuvoCommand.MUTE_ON : NuvoCommand.MUTE_OFF);
                        }
                        break;
                    case CHANNEL_TYPE_TREBLE:
                        if (command instanceof DecimalType decimalCommand) {
                            int value = decimalCommand.intValue();
                            if (value >= MIN_EQ && value <= MAX_EQ) {
                                // device can only accept even values
                                if (value % 2 == 1) {
                                    value++;
                                }
                                logger.debug("Got treble command {} zone {}", value, target);
                                connector.sendCfgCommand(target, NuvoCommand.TREBLE, String.valueOf(value));
                            }
                        }
                        break;
                    case CHANNEL_TYPE_BASS:
                        if (command instanceof DecimalType decimalCommand) {
                            int value = decimalCommand.intValue();
                            if (value >= MIN_EQ && value <= MAX_EQ) {
                                if (value % 2 == 1) {
                                    value++;
                                }
                                logger.debug("Got bass command {} zone {}", value, target);
                                connector.sendCfgCommand(target, NuvoCommand.BASS, String.valueOf(value));
                            }
                        }
                        break;
                    case CHANNEL_TYPE_BALANCE:
                        if (command instanceof DecimalType decimalCommand) {
                            int value = decimalCommand.intValue();
                            if (value >= MIN_EQ && value <= MAX_EQ) {
                                if (value % 2 == 1) {
                                    value++;
                                }
                                logger.debug("Got balance command {} zone {}", value, target);
                                connector.sendCfgCommand(target, NuvoCommand.BALANCE,
                                        NuvoStatusCodes.getBalanceFromInt(value));
                            }
                        }
                        break;
                    case CHANNEL_TYPE_LOUDNESS:
                        if (command instanceof OnOffType) {
                            connector.sendCfgCommand(target, NuvoCommand.LOUDNESS,
                                    command == OnOffType.ON ? ONE : ZERO);
                        }
                        break;
                    case CHANNEL_TYPE_CONTROL:
                        handleControlCommand(target, command);
                        break;
                    case CHANNEL_TYPE_DND:
                        if (command instanceof OnOffType) {
                            connector.sendCommand(target,
                                    command == OnOffType.ON ? NuvoCommand.DND_ON : NuvoCommand.DND_OFF);
                        }
                        break;
                    case CHANNEL_TYPE_PARTY:
                        if (command instanceof OnOffType) {
                            connector.sendCommand(target,
                                    command == OnOffType.ON ? NuvoCommand.PARTY_ON : NuvoCommand.PARTY_OFF);
                        }
                        break;
                    case CHANNEL_DISPLAY_LINE1:
                        if (command instanceof StringType) {
                            connector.sendCommand(target, NuvoCommand.DISPLINE1, "\"" + command + "\"");
                        }
                        break;
                    case CHANNEL_DISPLAY_LINE2:
                        if (command instanceof StringType) {
                            connector.sendCommand(target, NuvoCommand.DISPLINE2, "\"" + command + "\"");
                        }
                        break;
                    case CHANNEL_DISPLAY_LINE3:
                        if (command instanceof StringType) {
                            connector.sendCommand(target, NuvoCommand.DISPLINE3, "\"" + command + "\"");
                        }
                        break;
                    case CHANNEL_DISPLAY_LINE4:
                        if (command instanceof StringType) {
                            connector.sendCommand(target, NuvoCommand.DISPLINE4, "\"" + command + "\"");
                        }
                        break;
                    case CHANNEL_TYPE_ALLOFF:
                        if (command instanceof OnOffType) {
                            connector.sendCommand(NuvoCommand.ALLOFF);
                        }
                        break;
                    case CHANNEL_TYPE_ALLMUTE:
                        if (command instanceof OnOffType) {
                            connector.sendCommand(
                                    command == OnOffType.ON ? NuvoCommand.ALLMUTE_ON : NuvoCommand.ALLMUTE_OFF);
                        }
                        break;
                    case CHANNEL_TYPE_PAGE:
                        if (command instanceof OnOffType) {
                            connector.sendCommand(command == OnOffType.ON ? NuvoCommand.PAGE_ON : NuvoCommand.PAGE_OFF);
                        }
                        break;
                    case CHANNEL_TYPE_SENDCMD:
                        if (command instanceof StringType) {
                            String commandStr = command.toString();
                            if (commandStr.contains(DISP_INFO_TWO)) {
                                NuvoEnum source = NuvoEnum
                                        .valueOf(commandStr.split(DISP_INFO_TWO)[0].replace("S", SOURCE));
                                dispInfoCache.put(source, commandStr);

                                // if 'albumartid' is present, substitute it with the albumArtId hex string
                                connector.sendCommand(commandStr.replace(ALBUM_ART_ID,
                                        (OFFSET_ZERO + Integer.toHexString(albumArtIds.getOrDefault(source, 0)))));
                            } else {
                                connector.sendCommand(commandStr);
                            }
                        }
                        break;
                    case CHANNEL_ART_URL:
                        if (command instanceof StringType) {
                            String url = command.toString();
                            if (url.startsWith(HTTP) || url.startsWith(HTTPS)) {
                                try {
                                    ContentResponse contentResponse = httpClient.newRequest(url).method(GET)
                                            .timeout(10, TimeUnit.SECONDS).send();
                                    int httpStatus = contentResponse.getStatus();
                                    if (httpStatus == OK_200) {
                                        albumArtMap.put(target,
                                                NuvoImageResizer.resizeImage(contentResponse.getContent(), 80, 80));

                                        updateChannelState(target, CHANNEL_ALBUM_ART, BLANK,
                                                contentResponse.getContent());
                                    } else {
                                        albumArtMap.put(target, NO_ART);
                                        albumArtIds.put(target, 0);
                                        updateChannelState(target, CHANNEL_ALBUM_ART, UNDEF);
                                        return;
                                    }
                                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                                    albumArtMap.put(target, NO_ART);
                                    albumArtIds.put(target, 0);
                                    updateChannelState(target, CHANNEL_ALBUM_ART, UNDEF);
                                    return;
                                }
                                albumArtIds.put(target, Math.abs(url.hashCode()));

                                // re-send the cached DISPINFOTWO message, substituting in the new albumArtId
                                if (dispInfoCache.get(target) != null) {
                                    connector.sendCommand(dispInfoCache.getOrDefault(target, BLANK).replace(
                                            ALBUM_ART_ID,
                                            (OFFSET_ZERO + Integer.toHexString(albumArtIds.getOrDefault(target, 0)))));
                                }
                            } else {
                                albumArtMap.put(target, NO_ART);
                                albumArtIds.put(target, 0);
                                updateChannelState(target, CHANNEL_ALBUM_ART, UNDEF);
                            }
                        }
                        break;
                    case CHANNEL_SOURCE_MENU:
                        if (command instanceof StringType) {
                            updateChannelState(target, CHANNEL_BUTTON_PRESS, command.toString());
                        }
                }
            } catch (NuvoException e) {
                logger.warn("Command {} from channel {} failed: {}", command, channel, e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Sending command failed");
                closeConnection();
                scheduleReconnectJob();
            }
        }
    }

    /**
     * Open the connection with the Nuvo device
     *
     * @return true if the connection is opened successfully or false if not
     */
    private synchronized boolean openConnection() {
        connector.addEventListener(this);
        try {
            connector.open();
        } catch (NuvoException e) {
            logger.debug("openConnection() failed: {}", e.getMessage());
        }
        logger.debug("openConnection(): {}", connector.isConnected() ? "connected" : "disconnected");
        return connector.isConnected();
    }

    /**
     * Close the connection with the Nuvo device
     */
    private synchronized void closeConnection() {
        if (connector.isConnected()) {
            connector.close();
            connector.removeEventListener(this);
            pollStatusNeeded = true;
            logger.debug("closeConnection(): disconnected");
        }
    }

    /**
     * Handle an event received from the Nuvo device
     *
     * @param evt the event to process
     */
    @Override
    public void onNewMessageEvent(NuvoMessageEvent evt) {
        logger.debug("onNewMessageEvent: zone {}, source {}, value {}", evt.getZone(), evt.getSrc(), evt.getValue());
        lastEventReceived = System.currentTimeMillis();

        final NuvoEnum zone = !evt.getZone().isEmpty() ? NuvoEnum.valueOf(ZONE + evt.getZone()) : NuvoEnum.SYSTEM;
        final NuvoEnum source = !evt.getSrc().isEmpty() ? NuvoEnum.valueOf(SOURCE + evt.getSrc()) : NuvoEnum.SYSTEM;
        final String sourceZone = source.getId() + zone.getId();
        final String updateData = evt.getValue().trim();

        if (this.getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, this.versionString);
        }

        switch (evt.getType()) {
            case TYPE_VERSION:
                this.versionString = updateData;
                // Determine if we are a Grand Concerto or not
                if (this.versionString.contains(GC_STR)) {
                    logger.debug("Grand Concerto detected");
                    this.isGConcerto = true;
                    connector.setEssentia(false);
                } else {
                    logger.debug("Grand Concerto not detected");
                }
                break;
            case TYPE_RESTART:
                logger.debug("Restart message received; re-sending initialization messages");
                enableNuvonet(false);
                return;
            case TYPE_PING:
                logger.debug("Ping message received- rescheduling ping timeout");
                schedulePingTimeoutJob();
                // Return here because receiving a ping does not indicate that one can poll
                return;
            case TYPE_ALLOFF:
                activeZones.forEach(zoneNum -> {
                    updateChannelState(NuvoEnum.valueOf(ZONE + zoneNum), CHANNEL_TYPE_POWER, OFF);
                });

                // Publish the ALLOFF event to all button channels for awareness in source rules
                updateChannelState(NuvoEnum.SYSTEM, CHANNEL_TYPE_BUTTONPRESS, ZERO + COMMA + ALLOFF);
                NuvoEnum.VALID_SOURCES.forEach(src -> {
                    updateChannelState(src, CHANNEL_BUTTON_PRESS, ALLOFF);
                });

                break;
            case TYPE_ALLMUTE:
                updateChannelState(NuvoEnum.SYSTEM, CHANNEL_TYPE_ALLMUTE, ONE.equals(updateData) ? ON : OFF);
                activeZones.forEach(zoneNum -> {
                    updateChannelState(NuvoEnum.valueOf(ZONE + zoneNum), CHANNEL_TYPE_MUTE,
                            ONE.equals(updateData) ? ON : OFF);
                });
                break;
            case TYPE_PAGE:
                updateChannelState(NuvoEnum.SYSTEM, CHANNEL_TYPE_PAGE, ONE.equals(updateData) ? ON : OFF);
                break;
            case TYPE_SOURCE_UPDATE:
                logger.debug("Source update: Source: {} - Value: {}", source.getNum(), updateData);

                if (updateData.contains(DISPLINE)) {
                    // example: DISPLINE2,"Play My Song (Featuring Dee Ajayi)"
                    Matcher matcher = DISP_PATTERN.matcher(updateData);
                    if (matcher.find()) {
                        updateChannelState(source, CHANNEL_DISPLAY_LINE + matcher.group(1), matcher.group(2));
                    } else {
                        logger.debug("no match on message: {}", updateData);
                    }
                } else if (updateData.contains(DISPINFO)) {
                    // example: DISPINFO,DUR0,POS70,STATUS2 (DUR and POS are expressed in tenths of a second)
                    // 6 places(tenths of a second)-> max 999,999 /10/60/60/24 = 1.15 days
                    Matcher matcher = DISP_INFO_PATTERN.matcher(updateData);
                    if (matcher.find()) {
                        updateChannelState(source, CHANNEL_TRACK_LENGTH, matcher.group(1));
                        updateChannelState(source, CHANNEL_TRACK_POSITION, matcher.group(2));
                        updateChannelState(source, CHANNEL_PLAY_MODE, matcher.group(3));

                        // if this is an MPS4 source, the following retrieves album art when the source is playing
                        if (nuvoNetSrcMap.get(source) == 1
                                && isLinked(source.name().toLowerCase() + CHANNEL_DELIMIT + CHANNEL_ALBUM_ART)) {
                            if (MPS4_PLAYING_MODES.contains(matcher.group(3))) {
                                logger.debug("DISPINFO update, trying to get album art");
                                getMps4AlbumArt(source);
                            } else if (MPS4_IDLE_MODES.contains(matcher.group(3)) && ZERO.equals(matcher.group(1))) {
                                // clear album art channel for this source
                                logger.debug("DISPINFO update- not playing; clearing art, mode: {}", matcher.group(3));
                                updateChannelState(source, CHANNEL_ALBUM_ART, UNDEF);
                                mps4ArtGuids.put(source, BLANK);
                            }
                        }
                    } else {
                        logger.debug("no match on message: {}", updateData);
                    }
                } else if (updateData.contains(NAME_QUOTE)) {
                    // example: NAME"Ipod"
                    String name = updateData.split("\"")[1];
                    sourceLabels.put(String.valueOf(source.getNum()), name);
                }
                break;
            case TYPE_ZONE_UPDATE:
                logger.debug("Zone update: Zone: {} - Value: {}", zone.getNum(), updateData);
                // example : OFF
                // or: ON,SRC3,VOL63,DND0,LOCK0
                // or: ON,SRC3,MUTE,DND0,LOCK0

                if (OFF.equals(updateData)) {
                    updateChannelState(zone, CHANNEL_TYPE_POWER, OFF);
                    updateChannelState(zone, CHANNEL_TYPE_SOURCE, UNDEF);
                } else {
                    Matcher matcher = ZONE_PATTERN.matcher(updateData);
                    if (matcher.find()) {
                        updateChannelState(zone, CHANNEL_TYPE_POWER, ON);
                        updateChannelState(zone, CHANNEL_TYPE_SOURCE, matcher.group(1));
                        sourceZoneMap.put(NuvoEnum.valueOf(SOURCE + matcher.group(1)), zone);

                        // update the other group member's selected source
                        updateSrcForZoneGroup(zone, matcher.group(1));

                        if (MUTE.equals(matcher.group(2))) {
                            updateChannelState(zone, CHANNEL_TYPE_MUTE, ON);
                        } else {
                            updateChannelState(zone, CHANNEL_TYPE_MUTE, NuvoCommand.OFF.getValue());
                            updateChannelState(zone, CHANNEL_TYPE_VOLUME, matcher.group(2).replace(VOL, BLANK));
                        }

                        updateChannelState(zone, CHANNEL_TYPE_DND, ONE.equals(matcher.group(3)) ? ON : OFF);
                        updateChannelState(zone, CHANNEL_TYPE_LOCK, ONE.equals(matcher.group(4)) ? ON : OFF);
                    } else {
                        logger.debug("no match on message: {}", updateData);
                    }
                }
                break;
            case TYPE_ZONE_SOURCE_BUTTON:
                logger.debug("Source Button pressed: Source: {} - Button: {}", source.getNum(), updateData);
                updateChannelState(source, CHANNEL_BUTTON_PRESS, updateData);
                updateChannelState(NuvoEnum.SYSTEM, CHANNEL_TYPE_BUTTONPRESS, zone.getNum() + COMMA + updateData);
                break;
            case TYPE_NN_BUTTON:
                String buttonAction = NuvoStatusCodes.BUTTON_CODE.get(updateData);

                if (buttonAction != null) {
                    logger.debug("NuvoNet Source Button pressed: Source: {} - Button: {}", source.getNum(),
                            buttonAction);
                    updateChannelState(source, CHANNEL_BUTTON_PRESS, buttonAction);
                    updateChannelState(NuvoEnum.SYSTEM, CHANNEL_TYPE_BUTTONPRESS, zone.getNum() + COMMA + buttonAction);
                } else {
                    logger.debug("NuvoNet Source Button pressed: Source: {} - Unknown button code: {}", source.getNum(),
                            updateData);
                    updateChannelState(source, CHANNEL_BUTTON_PRESS, updateData);
                    updateChannelState(NuvoEnum.SYSTEM, CHANNEL_TYPE_BUTTONPRESS, zone.getNum() + COMMA + updateData);
                }
                break;
            case TYPE_NN_MENU_ITEM_SELECTED:
                // ignore this update unless openHAB is handling this source
                if (nuvoNetSrcMap.getOrDefault(source, 0).equals(2)) {
                    String[] updateDataSplit = updateData.split(COMMA);
                    String menuId = updateDataSplit[0];
                    int menuItemIdx = Integer.parseInt(updateDataSplit[1]) - 1;

                    boolean exitMenu = false;
                    if ("0xFFFFFFFF".equals(menuId)) {
                        TopMenu topMenuItem = nuvoMenus.getSource().get(source.getNum() - 1).getTopMenu()
                                .get(menuItemIdx);
                        logger.debug("Top Menu item selected: Source: {} - Menu Item: {}", source.getNum(),
                                topMenuItem.getText());
                        updateChannelState(source, CHANNEL_BUTTON_PRESS, topMenuItem.getText());
                        updateChannelState(NuvoEnum.SYSTEM, CHANNEL_TYPE_BUTTONPRESS,
                                zone.getNum() + COMMA + topMenuItem.getText());

                        List<String> subMenuItems = topMenuItem.getItems();

                        if (subMenuItems.isEmpty()) {
                            exitMenu = true;
                        } else {
                            // send submenu (maximum of 20 items)
                            int subMenuSize = subMenuItems.size() < 20 ? subMenuItems.size() : 20;
                            try {
                                connector.sendCommand(sourceZone + "MENU" + (menuItemIdx + 11) + ",0,0," + subMenuSize
                                        + ",0,0," + subMenuSize + ",\"" + topMenuItem.getText() + "\"");
                                Thread.sleep(SLEEP_BETWEEN_CMD_MS);

                                for (int i = 0; i < subMenuSize; i++) {
                                    connector.sendCommand(
                                            sourceZone + "MENUITEM" + (i + 1) + ",0,0,\"" + subMenuItems.get(i) + "\"");
                                }
                            } catch (NuvoException | InterruptedException e) {
                                logger.debug("Error sending sub menu to {}", sourceZone);
                            }
                        }
                    } else {
                        // a sub menu item was selected
                        TopMenu topMenuItem = nuvoMenus.getSource().get(source.getNum() - 1).getTopMenu()
                                .get(Integer.decode(menuId) - 11);
                        String subMenuItem = topMenuItem.getItems().get(menuItemIdx);

                        logger.debug("Sub Menu item selected: Source: {} - Menu Item: {}", source.getNum(),
                                topMenuItem.getText() + "|" + subMenuItem);
                        updateChannelState(source, CHANNEL_BUTTON_PRESS, topMenuItem.getText() + "|" + subMenuItem);
                        updateChannelState(NuvoEnum.SYSTEM, CHANNEL_TYPE_BUTTONPRESS,
                                zone.getNum() + COMMA + topMenuItem.getText() + "|" + subMenuItem);
                        exitMenu = true;
                    }

                    if (exitMenu) {
                        try {
                            // tell the zone to exit the menu
                            connector.sendCommand(sourceZone + "MENU0,0,0,0,0,0,0,\"\"");
                        } catch (NuvoException e) {
                            logger.debug("Error sending exit menu command to {}", sourceZone);
                        }
                    }
                }
                break;
            case TYPE_NN_MENUREQ:
                // ignore this update unless openHAB is handling this source
                if (nuvoNetSrcMap.getOrDefault(source, 0).equals(2)) {
                    logger.debug("Menu Request: Source: {} - Value: {}", source.getNum(), updateData);
                    // For now we only support one level deep menus. If second field is '1', indicates go back to main
                    // menu.
                    String[] menuDataSplit = updateData.split(COMMA);
                    if (menuDataSplit.length > 2 && ONE.equals(menuDataSplit[1])) {
                        try {
                            connector.sendCommand(sourceZone + "MENU0xFFFFFFFF,0,0,0,0,0,0,\"\"");
                        } catch (NuvoException e) {
                            logger.debug("Error sending main menu command to {}", sourceZone);
                        }
                    }
                }
                break;
            case TYPE_ZONE_CONFIG:
                logger.debug("Zone Configuration: Zone: {} - Value: {}", zone.getNum(), updateData);
                // example: BASS1,TREB-2,BALR2,LOUDCMP1
                Matcher matcher = ZONE_CFG_EQ_PATTERN.matcher(updateData);
                if (matcher.find()) {
                    updateChannelState(zone, CHANNEL_TYPE_BASS, matcher.group(1));
                    updateChannelState(zone, CHANNEL_TYPE_TREBLE, matcher.group(2));
                    updateChannelState(zone, CHANNEL_TYPE_BALANCE, NuvoStatusCodes.getBalanceFromStr(matcher.group(3)));
                    updateChannelState(zone, CHANNEL_TYPE_LOUDNESS, ONE.equals(matcher.group(4)) ? ON : OFF);
                } else {
                    matcher = ZONE_CFG_PATTERN.matcher(updateData);
                    // example: ENABLE1,NAME"Great Room",SLAVETO0,GROUP1,SOURCES63,XSRC0,IR1,DND0,LOCKED0,SLAVEEQ0
                    if (matcher.find()) {
                        // TODO: utilize other info such as zone name, available sources bitmask, etc.

                        // if this zone is a member of a group (1-4), add the zone's enum to the appropriate group map
                        if (!ZERO.equals(matcher.group(3))) {
                            nuvoGroupMap.getOrDefault(matcher.group(3), new HashSet<>()).add(zone);
                        }
                    } else {
                        logger.debug("no match on message: {}", updateData);
                    }
                }
                break;
            case TYPE_NN_ALBUM_ART_REQ:
                // ignore this update unless openHAB is handling this source
                if (nuvoNetSrcMap.getOrDefault(source, 0).equals(2)) {
                    logger.debug("Album Art Request for Source: {} - Data: {}", source.getNum(), updateData);
                    // 0x620FD879,80,80,2,0x00C0C0C0,0,0,0,0,1
                    String[] albumArtReq = updateData.split(COMMA);
                    albumArtIds.put(source, Integer.decode(albumArtReq[0]));

                    try {
                        if (albumArtMap.getOrDefault(source, NO_ART).length > 1) {
                            connector.sendCommand(
                                    source.getId() + ALBUM_ART_AVAILABLE + albumArtIds.getOrDefault(source, 0) + COMMA
                                            + albumArtMap.getOrDefault(source, NO_ART).length);
                        } else {
                            connector.sendCommand(source.getId() + ALBUM_ART_AVAILABLE + ZERO_COMMA);
                        }
                    } catch (NuvoException e) {
                        logger.debug("Error sending ALBUMARTAVAILABLE command for source: {}", source.getNum());
                    }
                }
                break;
            case TYPE_NN_ALBUM_ART_FRAG_REQ:
                // ignore this update unless openHAB is handling this source
                if (nuvoNetSrcMap.getOrDefault(source, 0).equals(2)) {
                    logger.debug("Album Art Fragment Request for Source: {} - Data: {}", source.getNum(), updateData);
                    // 0x620FD879,0,750 (id, requested offset from start of image, byte length requested)
                    String[] albumArtFragReq = updateData.split(COMMA);
                    int requestedId = Integer.decode(albumArtFragReq[0]);
                    int offset = Integer.parseInt(albumArtFragReq[1]);
                    int length = Integer.parseInt(albumArtFragReq[2]);

                    if (requestedId == albumArtIds.get(source)) {
                        byte[] chunk = new byte[length];
                        byte[] albumArtBytes = albumArtMap.get(source);

                        if (albumArtBytes != null) {
                            System.arraycopy(albumArtBytes, offset, chunk, 0, length);
                            final String frag = Base64.getEncoder().encodeToString(chunk);
                            try {
                                connector.sendCommand(source.getId() + ALBUM_ART_FRAG + requestedId + COMMA + offset
                                        + COMMA + frag.length() + COMMA + frag);
                            } catch (NuvoException e) {
                                logger.debug("Error sending ALBUMARTFRAG command for source: {}, artId: {}",
                                        source.getNum(), requestedId);
                            }
                        }
                    }
                }
                break;
            case TYPE_NN_FAVORITE_REQ:
                // ignore this update unless openHAB is handling this source
                if (nuvoNetSrcMap.getOrDefault(source, 0).equals(2)) {
                    logger.debug("Favorite request for source: {} - favoriteId: {}", source.getNum(), updateData);
                    try {
                        int playlistIdx = Integer.parseInt(updateData, 16) - 1000;
                        updateChannelState(source, CHANNEL_BUTTON_PRESS,
                                PLAY_MUSIC_PRESET + getFavorite(source, playlistIdx));
                    } catch (NumberFormatException nfe) {
                        logger.debug("Unable to parse favoriteId: {}", updateData);
                    }
                }
                break;
            default:
                logger.debug("onNewMessageEvent: unhandled event type {}", evt.getType());
                // Return here because receiving an unknown message does not indicate that one can poll
                return;
        }

        if (isMps4 && pollStatusNeeded) {
            pollStatus();
        }
    }

    private void loadMenuConfiguration(NuvoThingConfiguration config) {
        StringBuilder menuXml = new StringBuilder("<menu>");

        if (!config.menuXmlSrc1.isEmpty()) {
            menuXml.append("<source>" + config.menuXmlSrc1 + "</source>");
        } else {
            menuXml.append("<source/>");
        }
        if (!config.menuXmlSrc2.isEmpty()) {
            menuXml.append("<source>" + config.menuXmlSrc2 + "</source>");
        } else {
            menuXml.append("<source/>");
        }
        if (!config.menuXmlSrc3.isEmpty()) {
            menuXml.append("<source>" + config.menuXmlSrc3 + "</source>");
        } else {
            menuXml.append("<source/>");
        }
        if (!config.menuXmlSrc4.isEmpty()) {
            menuXml.append("<source>" + config.menuXmlSrc4 + "</source>");
        } else {
            menuXml.append("<source/>");
        }
        if (!config.menuXmlSrc5.isEmpty()) {
            menuXml.append("<source>" + config.menuXmlSrc5 + "</source>");
        } else {
            menuXml.append("<source/>");
        }
        if (!config.menuXmlSrc6.isEmpty()) {
            menuXml.append("<source>" + config.menuXmlSrc6 + "</source>");
        } else {
            menuXml.append("<source/>");
        }
        menuXml.append("</menu>");

        try {
            JAXBContext ctx = JAXBUtils.JAXBCONTEXT_NUVO_MENU;
            if (ctx != null) {
                Unmarshaller unmarshaller = ctx.createUnmarshaller();
                if (unmarshaller != null) {
                    XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY
                            .createXMLStreamReader(new StringReader(menuXml.toString()));
                    NuvoMenu menu = (NuvoMenu) unmarshaller.unmarshal(xsr);
                    if (menu != null) {
                        nuvoMenus = menu;
                        return;
                    }
                }
            }
            logger.debug("No JAXBContext available to parse Nuvo Menu XML");
        } catch (JAXBException | XMLStreamException e) {
            logger.warn("Error processing Nuvo Menu XML: {}", e.getLocalizedMessage());
        }
    }

    private void enableNuvonet(boolean showReady) {
        if (!this.isAnyOhNuvoNet) {
            return;
        }

        // enable NuvoNet for each source configured as an openHAB NuvoNet source
        nuvoNetSrcMap.forEach((source, val) -> {
            if (val.equals(2)) {
                try {
                    connector.sendCommand(source.getConfigId() + "NUVONET1");
                    Thread.sleep(SLEEP_BETWEEN_CMD_MS);
                } catch (NuvoException | InterruptedException e) {
                    logger.debug("Error sending SCFG command for source: {}", source.getNum());
                }
            }
        });

        try {
            // set '1' flag for each source configured as an MPS4 NuvoNet source or openHAB NuvoNet source
            connector.sendCommand("SNUMBERS" + nuvoNetSrcMap.getOrDefault(NuvoEnum.SOURCE1, 0).compareTo(0) + COMMA
                    + nuvoNetSrcMap.getOrDefault(NuvoEnum.SOURCE2, 0).compareTo(0) + COMMA
                    + nuvoNetSrcMap.getOrDefault(NuvoEnum.SOURCE3, 0).compareTo(0) + COMMA
                    + nuvoNetSrcMap.getOrDefault(NuvoEnum.SOURCE4, 0).compareTo(0) + COMMA
                    + nuvoNetSrcMap.getOrDefault(NuvoEnum.SOURCE5, 0).compareTo(0) + COMMA
                    + nuvoNetSrcMap.getOrDefault(NuvoEnum.SOURCE6, 0).compareTo(0));
            Thread.sleep(SLEEP_BETWEEN_CMD_MS);
        } catch (NuvoException | InterruptedException e) {
            logger.debug("Error sending SNUMBERS command");
        }

        // go though each source and if is openHAB NuvoNet then configure menu, favorites, etc.
        nuvoNetSrcMap.forEach((source, val) -> {
            if (val.equals(2)) {
                try {
                    List<TopMenu> topMenuItems = nuvoMenus.getSource().get(source.getNum() - 1).getTopMenu();

                    if (!topMenuItems.isEmpty()) {
                        connector.sendCommand(
                                source.getId() + "MENU," + (topMenuItems.size() < 10 ? topMenuItems.size() : 10));
                        Thread.sleep(SLEEP_BETWEEN_CMD_MS);

                        for (int i = 0; i < (topMenuItems.size() < 10 ? topMenuItems.size() : 10); i++) {
                            connector.sendCommand(source.getId() + "MENUITEM" + (i + 1) + ","
                                    + (topMenuItems.get(i).getItems().isEmpty() ? ZERO : ONE) + ",0,\""
                                    + topMenuItems.get(i).getText() + "\"");
                            Thread.sleep(SLEEP_BETWEEN_CMD_MS);
                        }

                        // Build a State options selection that represents this source's custom menu
                        List<StateOption> sourceMenuStateOptions = new ArrayList<>();
                        topMenuItems.forEach(topItem -> {
                            sourceMenuStateOptions.add(new StateOption(topItem.getText(), topItem.getText()));
                            topItem.getItems().forEach(subItem -> sourceMenuStateOptions
                                    .add(new StateOption(topItem.getText() + "|" + subItem, "-> " + subItem)));
                        });
                        stateDescriptionProvider.setStateOptions(
                                new ChannelUID(getThing().getUID(),
                                        source.name().toLowerCase() + CHANNEL_DELIMIT + CHANNEL_SOURCE_MENU),
                                sourceMenuStateOptions);
                    }

                    String[] favorites = favoriteMap.get(source);
                    if (favorites != null) {
                        connector.sendCommand(source.getId() + "FAVORITES"
                                + (favorites.length < 20 ? favorites.length : 20) + COMMA
                                + (source.getNum() == 1 ? ONE : ZERO) + COMMA + (source.getNum() == 2 ? ONE : ZERO)
                                + COMMA + (source.getNum() == 3 ? ONE : ZERO) + COMMA
                                + (source.getNum() == 4 ? ONE : ZERO) + COMMA + (source.getNum() == 5 ? ONE : ZERO)
                                + COMMA + (source.getNum() == 6 ? ONE : ZERO));
                        Thread.sleep(SLEEP_BETWEEN_CMD_MS);

                        for (int i = 0; i < (favorites.length < 20 ? favorites.length : 20); i++) {
                            connector.sendCommand(source.getId() + "FAVORITESITEM" + (i + 1000) + ",0,0,\""
                                    + favPrefixMap.get(source) + favorites[i] + "\"");
                            Thread.sleep(SLEEP_BETWEEN_CMD_MS);
                        }
                    }

                    if (showReady) {
                        connector.sendCommand(source.getId() + "DISPINFOTWO0,0,0,0,0,0,0");
                        Thread.sleep(SLEEP_BETWEEN_CMD_MS);
                        connector.sendCommand(source.getId() + "DISPLINES0,0,0,\"Ready\",\"\",\"\",\"\"");
                        Thread.sleep(SLEEP_BETWEEN_CMD_MS);
                    }

                } catch (NuvoException | InterruptedException e) {
                    logger.debug("Error configuring NuvoNet for source: {}", source.getNum());
                }
            }
        });
    }

    /**
     * Schedule the reconnection job
     */
    private void scheduleReconnectJob() {
        logger.debug("Schedule reconnect job");
        cancelReconnectJob();
        reconnectJob = scheduler.scheduleWithFixedDelay(() -> {
            if (!connector.isConnected()) {
                logger.debug("Trying to reconnect...");
                closeConnection();
                if (openConnection()) {
                    logger.debug("Reconnected");
                    // Polling status will disconnect from MPS4 on reconnect
                    if (!isMps4) {
                        pollStatus();
                    }
                    enableNuvonet(true);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Reconnection failed");
                    closeConnection();
                }
            }
        }, 1, RECON_POLLING_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    /**
     * If a ping is not received within ping interval the connection is closed and a reconnect job is scheduled
     */
    private void schedulePingTimeoutJob() {
        if (isMps4) {
            logger.debug("Schedule Ping Timeout job");
            cancelPingTimeoutJob();
            pingJob = scheduler.schedule(() -> {
                closeConnection();
                scheduleReconnectJob();
            }, PING_TIMEOUT_SEC, TimeUnit.SECONDS);
        } else {
            logger.debug("Ping Timeout job not valid for serial connections");
        }
    }

    /**
     * Cancel the ping timeout job
     */
    private void cancelPingTimeoutJob() {
        ScheduledFuture<?> pingJob = this.pingJob;
        if (pingJob != null) {
            pingJob.cancel(true);
            this.pingJob = null;
        }
    }

    private void pollStatus() {
        pollStatusNeeded = false;
        scheduler.submit(() -> {
            synchronized (sequenceLock) {
                try {
                    connector.sendCommand(NuvoCommand.GET_CONTROLLER_VERSION);

                    NuvoEnum.VALID_SOURCES.forEach(source -> {
                        try {
                            connector.sendQuery(source, NuvoCommand.NAME);
                            Thread.sleep(SLEEP_BETWEEN_CMD_MS);
                            connector.sendQuery(source, NuvoCommand.DISPINFO);
                            Thread.sleep(SLEEP_BETWEEN_CMD_MS);
                            connector.sendQuery(source, NuvoCommand.DISPLINE);
                            Thread.sleep(SLEEP_BETWEEN_CMD_MS);
                        } catch (NuvoException | InterruptedException e) {
                            logger.debug("Error Querying Source data: {}", e.getMessage());
                        }
                    });

                    // Query all active zones to get their current status and eq configuration
                    activeZones.forEach(zoneNum -> {
                        try {
                            connector.sendQuery(NuvoEnum.valueOf(ZONE + zoneNum), NuvoCommand.STATUS);
                            Thread.sleep(SLEEP_BETWEEN_CMD_MS);
                            connector.sendCfgCommand(NuvoEnum.valueOf(ZONE + zoneNum), NuvoCommand.STATUS_QUERY, BLANK);
                            Thread.sleep(SLEEP_BETWEEN_CMD_MS);
                            connector.sendCfgCommand(NuvoEnum.valueOf(ZONE + zoneNum), NuvoCommand.EQ_QUERY, BLANK);
                            Thread.sleep(SLEEP_BETWEEN_CMD_MS);
                        } catch (NuvoException | InterruptedException e) {
                            logger.debug("Error Querying Zone data: {}", e.getMessage());
                        }
                    });

                    List<StateOption> sourceStateOptions = new ArrayList<>();
                    sourceLabels.keySet().forEach(key -> {
                        sourceStateOptions.add(new StateOption(key, sourceLabels.get(key)));
                    });

                    // Put the source labels on all active zones
                    activeZones.forEach(zoneNum -> {
                        stateDescriptionProvider.setStateOptions(
                                new ChannelUID(getThing().getUID(),
                                        ZONE.toLowerCase() + zoneNum + CHANNEL_DELIMIT + CHANNEL_TYPE_SOURCE),
                                sourceStateOptions);
                    });
                } catch (NuvoException e) {
                    logger.debug("Error polling status from Nuvo: {}", e.getMessage());
                }
            }
        });
    }

    /**
     * Cancel the reconnection job
     */
    private void cancelReconnectJob() {
        ScheduledFuture<?> reconnectJob = this.reconnectJob;
        if (reconnectJob != null) {
            reconnectJob.cancel(true);
            this.reconnectJob = null;
        }
    }

    /**
     * Schedule the polling job
     */
    private void schedulePollingJob() {
        cancelPollingJob();

        if (isMps4) {
            logger.debug("MPS4 doesn't support polling");
            return;
        } else {
            logger.debug("Schedule polling job");
        }

        // when the Nuvo amp is off, this will keep the connection (esp Serial over IP) alive and detect if the
        // connection goes down
        pollingJob = scheduler.scheduleWithFixedDelay(() -> {
            if (connector.isConnected()) {
                logger.debug("Polling the component for updated status...");

                synchronized (sequenceLock) {
                    try {
                        connector.sendCommand(NuvoCommand.GET_CONTROLLER_VERSION);
                    } catch (NuvoException e) {
                        logger.debug("Polling error: {}", e.getMessage());
                    }

                    // if the last event received was more than 1.25 intervals ago,
                    // the component is not responding even though the connection is still good
                    if ((System.currentTimeMillis() - lastEventReceived) > (POLLING_INTERVAL_SEC * 1.25 * 1000)) {
                        logger.debug("Component not responding to status requests");
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Component not responding to status requests");
                        closeConnection();
                        scheduleReconnectJob();
                    }
                }
            }
        }, INITIAL_POLLING_DELAY_SEC, POLLING_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    /**
     * Cancel the polling job
     */
    private void cancelPollingJob() {
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
            this.pollingJob = null;
        }
    }

    /**
     * Schedule the clock sync job
     */
    private void scheduleClockSyncJob() {
        logger.debug("Schedule clock sync job");
        cancelClockSyncJob();
        clockSyncJob = scheduler.scheduleWithFixedDelay(() -> {
            if (this.isGConcerto) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy,MM,dd,HH,mm");
                    connector.sendCommand(NuvoCommand.CFGTIME.getValue() + dateFormat.format(new Date()));
                } catch (NuvoException e) {
                    logger.debug("Error syncing clock: {}", e.getMessage());
                }
            } else {
                this.cancelClockSyncJob();
            }
        }, INITIAL_CLOCK_SYNC_DELAY_SEC, CLOCK_SYNC_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    /**
     * Cancel the clock sync job
     */
    private void cancelClockSyncJob() {
        ScheduledFuture<?> clockSyncJob = this.clockSyncJob;
        if (clockSyncJob != null) {
            clockSyncJob.cancel(true);
            this.clockSyncJob = null;
        }
    }

    /**
     * Update the state of a channel (original method signature)
     *
     * @param target the channel group
     * @param channelType the channel group item
     * @param value the value to be updated
     */
    private void updateChannelState(NuvoEnum target, String channelType, String value) {
        updateChannelState(target, channelType, value, NO_ART);
    }

    /**
     * Update the state of a channel (overloaded method to handle album_art channel)
     *
     * @param target the channel group
     * @param channelType the channel group item
     * @param value the value to be updated
     * @param bytes the byte[] to load into the Image channel
     */
    private void updateChannelState(NuvoEnum target, String channelType, String value, byte[] bytes) {
        String channel = target.name().toLowerCase() + CHANNEL_DELIMIT + channelType;

        if (!isLinked(channel)) {
            return;
        }

        State state = UnDefType.UNDEF;

        if (UNDEF.equals(value)) {
            updateState(channel, state);
            return;
        }

        switch (channelType) {
            case CHANNEL_TYPE_POWER:
            case CHANNEL_TYPE_MUTE:
            case CHANNEL_TYPE_DND:
            case CHANNEL_TYPE_PARTY:
            case CHANNEL_TYPE_ALLMUTE:
            case CHANNEL_TYPE_PAGE:
            case CHANNEL_TYPE_LOUDNESS:
                state = OnOffType.from(ON.equals(value));
                break;
            case CHANNEL_TYPE_LOCK:
                state = ON.equals(value) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                break;
            case CHANNEL_TYPE_SOURCE:
            case CHANNEL_TYPE_TREBLE:
            case CHANNEL_TYPE_BASS:
            case CHANNEL_TYPE_BALANCE:
                state = new DecimalType(value);
                break;
            case CHANNEL_TYPE_VOLUME:
                int volume = Integer.parseInt(value);
                long volumePct = Math
                        .round((double) (MAX_VOLUME - volume) / (double) (MAX_VOLUME - MIN_VOLUME) * 100.0);
                state = new PercentType(BigDecimal.valueOf(volumePct));
                break;
            case CHANNEL_TYPE_BUTTONPRESS:
            case CHANNEL_DISPLAY_LINE1:
            case CHANNEL_DISPLAY_LINE2:
            case CHANNEL_DISPLAY_LINE3:
            case CHANNEL_DISPLAY_LINE4:
            case CHANNEL_BUTTON_PRESS:
                state = new StringType(value);
                break;
            case CHANNEL_PLAY_MODE:
                state = new StringType(NuvoStatusCodes.PLAY_MODE.get(value));
                break;
            case CHANNEL_TRACK_LENGTH:
            case CHANNEL_TRACK_POSITION:
                state = new QuantityType<>(Integer.parseInt(value) / 10, NuvoHandler.API_SECOND_UNIT);
                break;
            case CHANNEL_ALBUM_ART:
                state = new RawType(bytes, "image/jpeg");
                break;
            default:
                break;
        }
        updateState(channel, state);
    }

    /**
     * For grouped zones, update the source channel for all group members
     *
     * @param zoneEnum the zone where the source was changed
     * @param srcId the new source number that was selected
     */
    private void updateSrcForZoneGroup(NuvoEnum zoneEnum, String srcId) {
        // check if this zone is in a group, if so update the other group member's selected source
        nuvoGroupMap.forEach((groupId, groupZones) -> {
            if (groupZones.contains(zoneEnum)) {
                groupZones.forEach(z -> {
                    if (!zoneEnum.equals(z)) {
                        updateChannelState(z, CHANNEL_TYPE_SOURCE, srcId);
                    }
                });
            }
        });
    }

    /**
     * Handle a button press from a UI Player item
     *
     * @param target the nuvo zone to receive the command
     * @param command the button press command to send to the zone
     */
    private void handleControlCommand(NuvoEnum target, Command command) throws NuvoException {
        if (command instanceof PlayPauseType) {
            connector.sendCommand(target, NuvoCommand.PLAYPAUSE);
        } else if (command instanceof NextPreviousType) {
            if (command == NextPreviousType.NEXT) {
                connector.sendCommand(target, NuvoCommand.NEXT);
            } else if (command == NextPreviousType.PREVIOUS) {
                connector.sendCommand(target, NuvoCommand.PREV);
            }
        } else {
            logger.warn("Unknown control command: {}", command);
        }
    }

    /**
     * Scrapes the MPS4's json api to retrieve the currently playing media's album art
     *
     * @param source the source that should be queried to load the current album art
     */
    private void getMps4AlbumArt(NuvoEnum source) {
        final String clientId = UUID.randomUUID().toString();

        // try to get cached source instance
        String instance = sourceInstanceMap.get(source);

        // if not found, need to retrieve from the api, once found these calls will be skipped
        if (instance == null) {
            // find which zone is using this source
            NuvoEnum zone = sourceZoneMap.get(source);

            if (zone == null) {
                logger.debug("Unable to determine zone that is using source {}", source);
                return;
            } else {
                try {
                    final String json = getMcsJson(String.format(GET_MCS_INSTANCE, mps4Host, zone.getNum(), clientId),
                            clientId);

                    Matcher matcher = MCS_INSTANCE_PATTERN.matcher(json);
                    if (matcher.find()) {
                        instance = matcher.group(1);
                        sourceInstanceMap.put(source, instance);
                        logger.debug("Found instance '{}' for source {}", instance, source);
                    } else {
                        logger.debug("No instance match found for json: {}", json);
                        return;
                    }
                } catch (TimeoutException | ExecutionException e) {
                    logger.debug("Failed getting instance name", e);
                    return;
                } catch (InterruptedException e) {
                    logger.debug("InterruptedException getting instance name", e);
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        try {
            logger.debug("Using MCS instance '{}' for source {}", instance, source);
            final String json = getMcsJson(String.format(GET_MCS_STATUS, mps4Host, instance, clientId), clientId);

            if (json.contains("\"name\":\"PlayState\",\"value\":1}")
                    || json.contains("\"name\":\"PlayState\",\"value\":3}")) {
                Matcher matcher = ART_GUID_PATTERN.matcher(json);
                if (matcher.find()) {
                    final String nowPlayingGuid = matcher.group(1);

                    // If streaming (not local mp3 or flac) always retrieve because the same NowPlayingGuid can
                    // get a different image written to it by Gracenote when the track changes
                    if (!mps4ArtGuids.getOrDefault(source, BLANK).equals(nowPlayingGuid)
                            || !(json.contains("NowPlayingSrce\",\"value\":\"WMP\"")
                                    || json.contains("NowPlayingSrce\",\"value\":\"Flac\""))) {
                        ContentResponse artResponse = httpClient
                                .newRequest(String.format(GET_MCS_ART, mps4Host, nowPlayingGuid, instance,
                                        bindingConf.imageHeight, bindingConf.imageWidth))
                                .method(GET).timeout(10, TimeUnit.SECONDS).send();

                        if (artResponse.getStatus() == OK_200) {
                            logger.debug("Retrieved album art for guid: {}", nowPlayingGuid);
                            updateChannelState(source, CHANNEL_ALBUM_ART, BLANK, artResponse.getContent());
                            mps4ArtGuids.put(source, nowPlayingGuid);
                        }
                    } else {
                        logger.debug("Album art has not changed, guid: {}", nowPlayingGuid);
                    }
                } else {
                    logger.debug("NowPlayingGuid not found");
                }
            } else {
                logger.debug("PlayState not valid");
            }
        } catch (TimeoutException | ExecutionException e) {
            logger.debug("Failed getting album art", e);
            updateChannelState(source, CHANNEL_ALBUM_ART, UNDEF);
            mps4ArtGuids.put(source, BLANK);
        } catch (InterruptedException e) {
            logger.debug("InterruptedException getting album art", e);
            updateChannelState(source, CHANNEL_ALBUM_ART, UNDEF);
            mps4ArtGuids.put(source, BLANK);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Used by getMps4AlbumArt to abstract retrieval of status json from MCS
     *
     * @param commandUrl the url with the embedded commands to send to MCS
     * @param clientId the current clientId
     * @return string json result from the command executed
     *
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     */
    private String getMcsJson(String commandUrl, String clientId)
            throws InterruptedException, TimeoutException, ExecutionException {
        ContentResponse commandResp = httpClient.newRequest(commandUrl).method(GET).timeout(10, TimeUnit.SECONDS)
                .send();

        if (commandResp.getStatus() == OK_200) {
            Thread.sleep(SLEEP_BETWEEN_CMD_MS);
            ContentResponse jsonResp = httpClient.newRequest(String.format(GET_MCS_JSON, mps4Host, clientId))
                    .method(GET).timeout(10, TimeUnit.SECONDS).send();
            if (jsonResp.getStatus() == OK_200) {
                return jsonResp.getContentAsString();
            } else {
                logger.debug("Got error response {} when getting json from MCS", commandResp.getStatus());
                return BLANK;
            }
        }
        logger.debug("Got error response {} when sending json command url: {}", commandResp.getStatus(), commandUrl);
        return BLANK;
    }

    private String getFavorite(NuvoEnum source, int playlistIdx) {
        final String[] favoritesArr = favoriteMap.get(source);
        return favoritesArr != null ? favoritesArr[playlistIdx] : BLANK;
    }
}
