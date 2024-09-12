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
package org.openhab.binding.sonos.internal.handler;

import static org.openhab.binding.sonos.internal.SonosBindingConstants.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonos.internal.SonosAlarm;
import org.openhab.binding.sonos.internal.SonosBindingConstants;
import org.openhab.binding.sonos.internal.SonosEntry;
import org.openhab.binding.sonos.internal.SonosMetaData;
import org.openhab.binding.sonos.internal.SonosMusicService;
import org.openhab.binding.sonos.internal.SonosResourceMetaData;
import org.openhab.binding.sonos.internal.SonosStateDescriptionOptionProvider;
import org.openhab.binding.sonos.internal.SonosXMLParser;
import org.openhab.binding.sonos.internal.SonosZoneGroup;
import org.openhab.binding.sonos.internal.SonosZonePlayerState;
import org.openhab.binding.sonos.internal.config.ZonePlayerConfiguration;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.io.transport.upnp.UpnpIOParticipant;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZonePlayerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class ZonePlayerHandler extends BaseThingHandler implements UpnpIOParticipant {

    private static final String ANALOG_LINE_IN_URI = "x-rincon-stream:";
    private static final String OPTICAL_LINE_IN_URI = "x-sonos-htastream:";
    private static final String VIRTUAL_LINE_IN_URI = "x-sonos-vli:";
    private static final String QUEUE_URI = "x-rincon-queue:";
    private static final String GROUP_URI = "x-rincon:";
    private static final String STREAM_URI = "x-sonosapi-stream:";
    private static final String RADIO_URI = "x-sonosapi-radio:";
    private static final String RADIO_MP3_URI = "x-rincon-mp3radio:";
    private static final String RADIOAPP_URI = "x-sonosapi-hls:radioapp_";
    private static final String OPML_TUNE = "://opml.radiotime.com/Tune.ashx";
    private static final String FILE_URI = "x-file-cifs:";
    private static final String SPDIF = ":spdif";
    private static final String TUNEIN_URI = "x-sonosapi-stream:s%s?sid=%s&flags=32";

    private static final String STATE_PLAYING = "PLAYING";
    private static final String STATE_PAUSED_PLAYBACK = "PAUSED_PLAYBACK";
    private static final String STATE_STOPPED = "STOPPED";
    private static final String STATE_TRANSITIONING = "TRANSITIONING";

    private static final String LINEINCONNECTED = "LineInConnected";
    private static final String TOSLINEINCONNECTED = "TOSLinkConnected";

    private static final String SERVICE_DEVICE_PROPERTIES = "DeviceProperties";
    private static final String SERVICE_AV_TRANSPORT = "AVTransport";
    private static final String SERVICE_RENDERING_CONTROL = "RenderingControl";
    private static final String SERVICE_ZONE_GROUP_TOPOLOGY = "ZoneGroupTopology";
    private static final String SERVICE_GROUP_MANAGEMENT = "GroupManagement";
    private static final String SERVICE_AUDIO_IN = "AudioIn";
    private static final String SERVICE_HT_CONTROL = "HTControl";
    private static final String SERVICE_CONTENT_DIRECTORY = "ContentDirectory";
    private static final String SERVICE_ALARM_CLOCK = "AlarmClock";

    private static final Collection<String> SERVICE_SUBSCRIPTIONS = Arrays.asList(SERVICE_DEVICE_PROPERTIES,
            SERVICE_AV_TRANSPORT, SERVICE_ZONE_GROUP_TOPOLOGY, SERVICE_GROUP_MANAGEMENT, SERVICE_RENDERING_CONTROL,
            SERVICE_AUDIO_IN, SERVICE_HT_CONTROL, SERVICE_CONTENT_DIRECTORY);
    protected static final int SUBSCRIPTION_DURATION = 1800;

    private static final String ACTION_GET_ZONE_ATTRIBUTES = "GetZoneAttributes";
    private static final String ACTION_GET_ZONE_INFO = "GetZoneInfo";
    private static final String ACTION_GET_LED_STATE = "GetLEDState";
    private static final String ACTION_SET_LED_STATE = "SetLEDState";

    private static final String ACTION_GET_POSITION_INFO = "GetPositionInfo";
    private static final String ACTION_SET_AV_TRANSPORT_URI = "SetAVTransportURI";
    private static final String ACTION_SEEK = "Seek";
    private static final String ACTION_PLAY = "Play";
    private static final String ACTION_STOP = "Stop";
    private static final String ACTION_PAUSE = "Pause";
    private static final String ACTION_PREVIOUS = "Previous";
    private static final String ACTION_NEXT = "Next";
    private static final String ACTION_ADD_URI_TO_QUEUE = "AddURIToQueue";
    private static final String ACTION_REMOVE_TRACK_RANGE_FROM_QUEUE = "RemoveTrackRangeFromQueue";
    private static final String ACTION_REMOVE_ALL_TRACKS_FROM_QUEUE = "RemoveAllTracksFromQueue";
    private static final String ACTION_SAVE_QUEUE = "SaveQueue";
    private static final String ACTION_SET_PLAY_MODE = "SetPlayMode";
    private static final String ACTION_BECOME_COORDINATOR_OF_STANDALONE_GROUP = "BecomeCoordinatorOfStandaloneGroup";
    private static final String ACTION_GET_RUNNING_ALARM_PROPERTIES = "GetRunningAlarmProperties";
    private static final String ACTION_SNOOZE_ALARM = "SnoozeAlarm";
    private static final String ACTION_GET_REMAINING_SLEEP_TIMER_DURATION = "GetRemainingSleepTimerDuration";
    private static final String ACTION_CONFIGURE_SLEEP_TIMER = "ConfigureSleepTimer";

    private static final String ACTION_SET_VOLUME = "SetVolume";
    private static final String ACTION_SET_MUTE = "SetMute";
    private static final String ACTION_SET_BASS = "SetBass";
    private static final String ACTION_SET_TREBLE = "SetTreble";
    private static final String ACTION_SET_LOUDNESS = "SetLoudness";
    private static final String ACTION_SET_EQ = "SetEQ";

    private static final int TUNEIN_DEFAULT_SERVICE_TYPE = 65031;

    private static final int MIN_BASS = -10;
    private static final int MAX_BASS = 10;
    private static final int MIN_TREBLE = -10;
    private static final int MAX_TREBLE = 10;
    private static final int MIN_SUBWOOFER_GAIN = -15;
    private static final int MAX_SUBWOOFER_GAIN = 15;
    private static final int MIN_SURROUND_LEVEL = -15;
    private static final int MAX_SURROUND_LEVEL = 15;
    private static final int MIN_HEIGHT_LEVEL = -10;
    private static final int MAX_HEIGHT_LEVEL = 10;

    private static final int HTTP_TIMEOUT = 5000;

    private final Logger logger = LoggerFactory.getLogger(ZonePlayerHandler.class);

    private final ThingRegistry localThingRegistry;
    private final UpnpIOService service;
    private final @Nullable String opmlUrl;
    private final SonosStateDescriptionOptionProvider stateDescriptionProvider;

    private ZonePlayerConfiguration configuration = new ZonePlayerConfiguration();

    /**
     * Intrinsic lock used to synchronize the execution of notification sounds
     */
    private final Object notificationLock = new Object();
    private final Object upnpLock = new Object();
    private final Object stateLock = new Object();
    private final Object jobLock = new Object();

    private final Map<String, String> stateMap = Collections.synchronizedMap(new HashMap<>());

    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable SonosZonePlayerState savedState;

    private Map<String, Boolean> subscriptionState = new HashMap<>();

    /**
     * Thing handler instance of the coordinator speaker used for control delegation
     */
    private @Nullable ZonePlayerHandler coordinatorHandler;

    private @Nullable List<SonosMusicService> musicServices;

    private enum LineInType {
        ANALOG,
        DIGITAL,
        ANY
    }

    public ZonePlayerHandler(ThingRegistry thingRegistry, Thing thing, UpnpIOService upnpIOService,
            @Nullable String opmlUrl, SonosStateDescriptionOptionProvider stateDescriptionProvider) {
        super(thing);
        this.localThingRegistry = thingRegistry;
        this.opmlUrl = opmlUrl;
        logger.debug("Creating a ZonePlayerHandler for thing '{}'", getThing().getUID());
        this.service = upnpIOService;
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed for thing {}", getThing().getUID());

        ScheduledFuture<?> job = this.pollingJob;
        if (job != null) {
            job.cancel(true);
        }
        this.pollingJob = null;

        removeSubscription();
        service.unregisterParticipant(this);
    }

    @Override
    public void initialize() {
        logger.debug("initializing handler for thing {}", getThing().getUID());

        if (migrateThingType()) {
            // we change the type, so we might need a different handler -> let's finish
            return;
        }

        configuration = getConfigAs(ZonePlayerConfiguration.class);
        String udn = configuration.udn;
        if (udn != null && !udn.isEmpty()) {
            service.registerParticipant(this);
            pollingJob = scheduler.scheduleWithFixedDelay(this::poll, 0, configuration.refresh, TimeUnit.SECONDS);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-udn");
            logger.debug("Cannot initalize the zoneplayer. UDN not set.");
        }
    }

    private void poll() {
        synchronized (jobLock) {
            if (pollingJob == null) {
                return;
            }
            try {
                logger.debug("Polling job");

                // First check if the Sonos zone is set in the UPnP service registry
                // If not, set the thing state to OFFLINE and wait for the next poll
                if (!isUpnpDeviceRegistered()) {
                    logger.debug("UPnP device {} not yet registered", getUDN());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.upnp-device-not-registered [\"" + getUDN() + "\"]");
                    synchronized (upnpLock) {
                        subscriptionState = new HashMap<>();
                    }
                    return;
                }

                // Check if the Sonos zone can be joined
                // If not, set the thing state to OFFLINE and do nothing else
                updatePlayerState();
                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    return;
                }

                addSubscription();

                if (isLinked(ZONENAME)) {
                    updateCurrentZoneName();
                }
                if (isLinked(LED)) {
                    updateLed();
                }
                // Action GetRemainingSleepTimerDuration is failing for a group slave member (error code 500)
                if (isLinked(SLEEPTIMER) && isCoordinator()) {
                    updateSleepTimerDuration();
                }
            } catch (Exception e) {
                logger.debug("Exception during poll: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            updateChannel(channelUID.getId());
        } else {
            switch (channelUID.getId()) {
                case LED:
                    setLed(command);
                    break;
                case MUTE:
                    setMute(command);
                    break;
                case NOTIFICATIONSOUND:
                    scheduleNotificationSound(command);
                    break;
                case STOP:
                    stopPlaying(command);
                    break;
                case VOLUME:
                    setVolumeForGroup(command);
                    break;
                case BASS:
                    setBass(command);
                    break;
                case TREBLE:
                    setTreble(command);
                    break;
                case LOUDNESS:
                    setLoudness(command);
                    break;
                case SUBWOOFER:
                    setSubwoofer(command);
                    break;
                case SUBWOOFERGAIN:
                    setSubwooferGain(command);
                    break;
                case SURROUND:
                    setSurround(command);
                    break;
                case SURROUNDMUSICMODE:
                    setSurroundMusicMode(command);
                    break;
                case SURROUNDMUSICLEVEL:
                    setSurroundMusicLevel(command);
                    break;
                case SURROUNDTVLEVEL:
                    setSurroundTvLevel(command);
                    break;
                case HEIGHTLEVEL:
                    setHeightLevel(command);
                    break;
                case ADD:
                    addMember(command);
                    break;
                case REMOVE:
                    removeMember(command);
                    break;
                case STANDALONE:
                    becomeStandAlonePlayer();
                    break;
                case PUBLICADDRESS:
                    publicAddress(LineInType.ANY);
                    break;
                case PUBLICANALOGADDRESS:
                    publicAddress(LineInType.ANALOG);
                    break;
                case PUBLICDIGITALADDRESS:
                    publicAddress(LineInType.DIGITAL);
                    break;
                case RADIO:
                    playRadio(command);
                    break;
                case TUNEINSTATIONID:
                    playTuneinStation(command);
                    break;
                case FAVORITE:
                    playFavorite(command);
                    break;
                case ALARM:
                    setAlarm(command);
                    break;
                case SNOOZE:
                    snoozeAlarm(command);
                    break;
                case SAVEALL:
                    saveAllPlayerState();
                    break;
                case RESTOREALL:
                    restoreAllPlayerState();
                    break;
                case SAVE:
                    saveState();
                    break;
                case RESTORE:
                    restoreState();
                    break;
                case PLAYLIST:
                    playPlayList(command);
                    break;
                case CLEARQUEUE:
                    clearQueue();
                    break;
                case PLAYQUEUE:
                    playQueue();
                    break;
                case PLAYTRACK:
                    playTrack(command);
                    break;
                case PLAYURI:
                    playURI(command);
                    break;
                case PLAYLINEIN:
                    playLineIn(command);
                    break;
                case CONTROL:
                    try {
                        if (command instanceof PlayPauseType) {
                            if (command == PlayPauseType.PLAY) {
                                getCoordinatorHandler().play();
                            } else if (command == PlayPauseType.PAUSE) {
                                getCoordinatorHandler().pause();
                            }
                        }
                        if (command instanceof NextPreviousType) {
                            if (command == NextPreviousType.NEXT) {
                                getCoordinatorHandler().next();
                            } else if (command == NextPreviousType.PREVIOUS) {
                                getCoordinatorHandler().previous();
                            }
                        }
                        // Rewind and Fast Forward are currently not implemented by the binding
                    } catch (IllegalStateException e) {
                        logger.debug("Cannot handle control command ({})", e.getMessage());
                    }
                    break;
                case SLEEPTIMER:
                    setSleepTimer(command);
                    break;
                case SHUFFLE:
                    setShuffle(command);
                    break;
                case REPEAT:
                    setRepeat(command);
                    break;
                case NIGHTMODE:
                    setNightMode(command);
                    break;
                case SPEECHENHANCEMENT:
                    setSpeechEnhancement(command);
                    break;
                default:
                    break;
            }
        }
    }

    private void restoreAllPlayerState() {
        for (Thing aThing : localThingRegistry.getAll()) {
            if (SonosBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(aThing.getThingTypeUID())) {
                ZonePlayerHandler handler = (ZonePlayerHandler) aThing.getHandler();
                if (handler != null) {
                    handler.restoreState();
                }
            }
        }
    }

    private void saveAllPlayerState() {
        for (Thing aThing : localThingRegistry.getAll()) {
            if (SonosBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(aThing.getThingTypeUID())) {
                ZonePlayerHandler handler = (ZonePlayerHandler) aThing.getHandler();
                if (handler != null) {
                    handler.saveState();
                }
            }
        }
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        if (variable == null || value == null || service == null) {
            return;
        }

        if (getThing().getStatus() == ThingStatus.ONLINE) {
            logger.trace("Received pair '{}':'{}' (service '{}') for thing '{}'",
                    new Object[] { variable, value, service, this.getThing().getUID() });

            String oldValue = this.stateMap.get(variable);
            if (shouldIgnoreVariableUpdate(variable, value, oldValue)) {
                return;
            }

            this.stateMap.put(variable, value);

            // pre-process some variables, eg XML processing
            if (SERVICE_AV_TRANSPORT.equals(service) && "LastChange".equals(variable)) {
                Map<String, String> parsedValues = SonosXMLParser.getAVTransportFromXML(value);
                parsedValues.forEach((variable1, value1) -> {
                    // Update the transport state after the update of the media information
                    // to not break the notification mechanism
                    if (!"TransportState".equals(variable1)) {
                        onValueReceived(variable1, value1, service);
                    }
                    // Translate AVTransportURI/AVTransportURIMetaData to CurrentURI/CurrentURIMetaData
                    // for a compatibility with the result of the action GetMediaInfo
                    if ("AVTransportURI".equals(variable1)) {
                        onValueReceived("CurrentURI", value1, service);
                    } else if ("AVTransportURIMetaData".equals(variable1)) {
                        onValueReceived("CurrentURIMetaData", value1, service);
                    }
                });
                updateMediaInformation();
                if (parsedValues.get("TransportState") != null) {
                    onValueReceived("TransportState", parsedValues.get("TransportState"), service);
                }
            }

            if (SERVICE_RENDERING_CONTROL.equals(service) && "LastChange".equals(variable)) {
                Map<String, String> parsedValues = SonosXMLParser.getRenderingControlFromXML(value);
                parsedValues.forEach((variable1, value1) -> {
                    onValueReceived(variable1, value1, service);
                });
            }

            List<StateOption> options = new ArrayList<>();

            // update the appropriate channel
            switch (variable) {
                case "TransportState":
                    updateChannel(STATE);
                    updateChannel(CONTROL);
                    updateChannel(STOP);
                    dispatchOnAllGroupMembers(variable, value, service);
                    break;
                case "CurrentPlayMode":
                    updateChannel(SHUFFLE);
                    updateChannel(REPEAT);
                    dispatchOnAllGroupMembers(variable, value, service);
                    break;
                case "CurrentLEDState":
                    updateChannel(LED);
                    break;
                case "ZoneName":
                    updateState(ZONENAME, new StringType(value));
                    break;
                case "CurrentZoneName":
                    updateChannel(ZONENAME);
                    break;
                case "ZoneGroupState":
                    updateChannel(COORDINATOR);
                    // Update coordinator after a change is made to the grouping of Sonos players
                    updateGroupCoordinator();
                    updateMediaInformation();
                    // Update state and control channels for the group members with the coordinator values
                    String transportState = getTransportState();
                    if (transportState != null) {
                        dispatchOnAllGroupMembers("TransportState", transportState, SERVICE_AV_TRANSPORT);
                    }
                    // Update shuffle and repeat channels for the group members with the coordinator values
                    String playMode = getPlayMode();
                    if (playMode != null) {
                        dispatchOnAllGroupMembers("CurrentPlayMode", playMode, SERVICE_AV_TRANSPORT);
                    }
                    break;
                case "LocalGroupUUID":
                    updateChannel(ZONEGROUPID);
                    break;
                case "GroupCoordinatorIsLocal":
                    updateChannel(LOCALCOORDINATOR);
                    break;
                case "VolumeMaster":
                    updateChannel(VOLUME);
                    break;
                case "MuteMaster":
                    updateChannel(MUTE);
                    break;
                case "Bass":
                    updateChannel(BASS);
                    break;
                case "Treble":
                    updateChannel(TREBLE);
                    break;
                case "LoudnessMaster":
                    updateChannel(LOUDNESS);
                    break;
                case "OutputFixed":
                    updateChannel(BASS);
                    updateChannel(TREBLE);
                    updateChannel(LOUDNESS);
                    break;
                case "SubEnabled":
                    updateChannel(SUBWOOFER);
                    break;
                case "SubGain":
                    updateChannel(SUBWOOFERGAIN);
                    break;
                case "SurroundEnabled":
                    updateChannel(SURROUND);
                    break;
                case "SurroundMode":
                    updateChannel(SURROUNDMUSICMODE);
                    break;
                case "SurroundLevel":
                    updateChannel(SURROUNDTVLEVEL);
                    break;
                case "HTAudioIn":
                    updateChannel(CODEC);
                    break;
                case "MusicSurroundLevel":
                    updateChannel(SURROUNDMUSICLEVEL);
                    break;
                case "HeightChannelLevel":
                    updateChannel(HEIGHTLEVEL);
                    break;
                case "NightMode":
                    updateChannel(NIGHTMODE);
                    break;
                case "DialogLevel":
                    updateChannel(SPEECHENHANCEMENT);
                    break;
                case LINEINCONNECTED:
                    if (SonosBindingConstants.WITH_LINEIN_THING_TYPES_UIDS.contains(getThing().getThingTypeUID())) {
                        updateChannel(LINEIN);
                    }
                    if (SonosBindingConstants.WITH_ANALOG_LINEIN_THING_TYPES_UIDS
                            .contains(getThing().getThingTypeUID())) {
                        updateChannel(ANALOGLINEIN);
                    }
                    break;
                case TOSLINEINCONNECTED:
                    if (SonosBindingConstants.WITH_LINEIN_THING_TYPES_UIDS.contains(getThing().getThingTypeUID())) {
                        updateChannel(LINEIN);
                    }
                    if (SonosBindingConstants.WITH_DIGITAL_LINEIN_THING_TYPES_UIDS
                            .contains(getThing().getThingTypeUID())) {
                        updateChannel(DIGITALLINEIN);
                    }
                    break;
                case "AlarmRunning":
                    updateChannel(ALARMRUNNING);
                    updateRunningAlarmProperties();
                    break;
                case "RunningAlarmProperties":
                    updateChannel(ALARMPROPERTIES);
                    break;
                case "CurrentURIFormatted":
                    updateChannel(CURRENTTRACK);
                    break;
                case "CurrentTitle":
                    updateChannel(CURRENTTITLE);
                    break;
                case "CurrentArtist":
                    updateChannel(CURRENTARTIST);
                    break;
                case "CurrentAlbum":
                    updateChannel(CURRENTALBUM);
                    break;
                case "CurrentURI":
                    updateChannel(CURRENTTRANSPORTURI);
                    break;
                case "CurrentTrackURI":
                    updateChannel(CURRENTTRACKURI);
                    break;
                case "CurrentAlbumArtURI":
                    updateChannel(CURRENTALBUMARTURL);
                    break;
                case "CurrentSleepTimerGeneration":
                    if ("0".equals(value)) {
                        updateState(SLEEPTIMER, new DecimalType(0));
                    }
                    break;
                case "SleepTimerGeneration":
                    if ("0".equals(value)) {
                        updateState(SLEEPTIMER, new DecimalType(0));
                    } else {
                        updateSleepTimerDuration();
                    }
                    break;
                case "RemainingSleepTimerDuration":
                    updateState(SLEEPTIMER, new DecimalType(sleepStrTimeToSeconds(value)));
                    break;
                case "CurrentTuneInStationId":
                    updateChannel(TUNEINSTATIONID);
                    break;
                case "SavedQueuesUpdateID": // service ContentDirectoy
                    for (SonosEntry entry : getPlayLists()) {
                        options.add(new StateOption(entry.getTitle(), entry.getTitle()));
                    }
                    stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), PLAYLIST), options);
                    break;
                case "FavoritesUpdateID": // service ContentDirectoy
                    for (SonosEntry entry : getFavorites()) {
                        options.add(new StateOption(entry.getTitle(), entry.getTitle()));
                    }
                    stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), FAVORITE), options);
                    break;
                // For favorite radios, we should have checked the state variable named RadioFavoritesUpdateID
                // Due to a bug in the data type definition of this state variable, it is not set.
                // As a workaround, we check the state variable named ContainerUpdateIDs.
                case "ContainerUpdateIDs": // service ContentDirectoy
                    if (value.startsWith("R:0,") || stateDescriptionProvider
                            .getStateOptions(new ChannelUID(getThing().getUID(), RADIO)) == null) {
                        for (SonosEntry entry : getFavoriteRadios()) {
                            options.add(new StateOption(entry.getTitle(), entry.getTitle()));
                        }
                        stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), RADIO), options);
                    }
                    break;
                case "MoreInfo":
                    updateChannel(BATTERYCHARGING);
                    updateChannel(BATTERYLEVEL);
                    break;
                case "MicEnabled":
                    updateChannel(MICROPHONE);
                    break;
                default:
                    break;
            }
        }
    }

    private void dispatchOnAllGroupMembers(String variable, String value, String service) {
        if (isCoordinator()) {
            for (String member : getOtherZoneGroupMembers()) {
                try {
                    ZonePlayerHandler memberHandler = getHandlerByName(member);
                    if (ThingStatus.ONLINE.equals(memberHandler.getThing().getStatus())) {
                        memberHandler.onValueReceived(variable, value, service);
                    }
                } catch (IllegalStateException e) {
                    logger.debug("Cannot update channel for group member ({})", e.getMessage());
                }
            }
        }
    }

    private @Nullable String getAlbumArtUrl() {
        String url = null;
        String albumArtURI = stateMap.get("CurrentAlbumArtURI");
        if (albumArtURI != null) {
            if (albumArtURI.startsWith("http")) {
                url = albumArtURI;
            } else if (albumArtURI.startsWith("/")) {
                try {
                    URL serviceDescrUrl = service.getDescriptorURL(this);
                    if (serviceDescrUrl != null) {
                        url = new URL(serviceDescrUrl.getProtocol(), serviceDescrUrl.getHost(),
                                serviceDescrUrl.getPort(), albumArtURI).toExternalForm();
                    }
                } catch (MalformedURLException e) {
                    logger.debug("Failed to build a valid album art URL from {}: {}", albumArtURI, e.getMessage());
                }
            }
        }
        return url;
    }

    protected void updateChannel(String channelId) {
        if (!isLinked(channelId)) {
            return;
        }

        String url;

        State newState = UnDefType.UNDEF;
        String value;
        switch (channelId) {
            case STATE:
                value = getTransportState();
                if (value != null) {
                    // Ignoring state TRANSITIONING
                    newState = STATE_TRANSITIONING.equals(value) ? null : new StringType(value);
                }
                break;
            case CONTROL:
                value = getTransportState();
                if (STATE_PLAYING.equals(value)) {
                    newState = PlayPauseType.PLAY;
                } else if (STATE_STOPPED.equals(value)) {
                    newState = PlayPauseType.PAUSE;
                } else if (STATE_PAUSED_PLAYBACK.equals(value)) {
                    newState = PlayPauseType.PAUSE;
                } else if (STATE_TRANSITIONING.equals(value)) {
                    // Ignoring state TRANSITIONING
                    newState = null;
                }
                break;
            case STOP:
                value = getTransportState();
                if (value != null) {
                    newState = STATE_TRANSITIONING.equals(value) ? null : OnOffType.from(STATE_STOPPED.equals(value));
                }
                break;
            case SHUFFLE:
                if (getPlayMode() != null) {
                    newState = OnOffType.from(isShuffleActive());
                }
                break;
            case REPEAT:
                if (getPlayMode() != null) {
                    newState = new StringType(getRepeatMode());
                }
                break;
            case LED:
                value = getLed();
                if (value != null) {
                    newState = OnOffType.from(value);
                }
                break;
            case ZONENAME:
                value = getCurrentZoneName();
                if (value != null) {
                    newState = new StringType(value);
                }
                break;
            case ZONEGROUPID:
                value = getZoneGroupID();
                if (value != null) {
                    newState = new StringType(value);
                }
                break;
            case COORDINATOR:
                newState = new StringType(getCoordinator());
                break;
            case LOCALCOORDINATOR:
                if (getGroupCoordinatorIsLocal() != null) {
                    newState = OnOffType.from(isGroupCoordinator());
                }
                break;
            case VOLUME:
                value = getVolume();
                if (value != null) {
                    newState = new PercentType(value);
                }
                break;
            case BASS:
                value = getBass();
                if (value != null && !isOutputLevelFixed()) {
                    newState = new DecimalType(value);
                }
                break;
            case TREBLE:
                value = getTreble();
                if (value != null && !isOutputLevelFixed()) {
                    newState = new DecimalType(value);
                }
                break;
            case LOUDNESS:
                value = getLoudness();
                if (value != null && !isOutputLevelFixed()) {
                    newState = OnOffType.from(value);
                }
                break;
            case MUTE:
                value = getMute();
                if (value != null) {
                    newState = OnOffType.from(value);
                }
                break;
            case SUBWOOFER:
                value = getSubwooferEnabled();
                if (value != null) {
                    newState = OnOffType.from(value);
                }
                break;
            case SUBWOOFERGAIN:
                value = getSubwooferGain();
                if (value != null) {
                    newState = new DecimalType(value);
                }
                break;
            case SURROUND:
                value = getSurroundEnabled();
                if (value != null) {
                    newState = OnOffType.from(value);
                }
                break;
            case SURROUNDMUSICMODE:
                value = getSurroundMusicMode();
                if (value != null) {
                    newState = new StringType(value);
                }
                break;
            case SURROUNDMUSICLEVEL:
                value = getSurroundMusicLevel();
                if (value != null) {
                    newState = new DecimalType(value);
                }
                break;
            case SURROUNDTVLEVEL:
                value = getSurroundTvLevel();
                if (value != null) {
                    newState = new DecimalType(value);
                }
                break;
            case CODEC:
                value = getCodec();
                if (value != null) {
                    newState = new StringType(value);
                }
                break;
            case HEIGHTLEVEL:
                value = getHeightLevel();
                if (value != null) {
                    newState = new DecimalType(value);
                }
                break;
            case NIGHTMODE:
                value = getNightMode();
                if (value != null) {
                    newState = OnOffType.from(value);
                }
                break;
            case SPEECHENHANCEMENT:
                value = getDialogLevel();
                if (value != null) {
                    newState = OnOffType.from(value);
                }
                break;
            case LINEIN:
                if (getAnalogLineInConnected() != null) {
                    newState = OnOffType.from(isAnalogLineInConnected());
                } else if (getOpticalLineInConnected() != null) {
                    newState = OnOffType.from(isOpticalLineInConnected());
                }
                break;
            case ANALOGLINEIN:
                if (getAnalogLineInConnected() != null) {
                    newState = OnOffType.from(isAnalogLineInConnected());
                }
                break;
            case DIGITALLINEIN:
                if (getOpticalLineInConnected() != null) {
                    newState = OnOffType.from(isOpticalLineInConnected());
                }
                break;
            case ALARMRUNNING:
                if (getAlarmRunning() != null) {
                    newState = OnOffType.from(isAlarmRunning());
                }
                break;
            case ALARMPROPERTIES:
                value = getRunningAlarmProperties();
                if (value != null) {
                    newState = new StringType(value);
                }
                break;
            case CURRENTTRACK:
                value = stateMap.get("CurrentURIFormatted");
                if (value != null) {
                    newState = new StringType(value);
                }
                break;
            case CURRENTTITLE:
                value = getCurrentTitle();
                if (value != null) {
                    newState = new StringType(value);
                }
                break;
            case CURRENTARTIST:
                value = getCurrentArtist();
                if (value != null) {
                    newState = new StringType(value);
                }
                break;
            case CURRENTALBUM:
                value = getCurrentAlbum();
                if (value != null) {
                    newState = new StringType(value);
                }
                break;
            case CURRENTALBUMART:
                newState = null;
                updateAlbumArtChannel(false);
                break;
            case CURRENTALBUMARTURL:
                url = getAlbumArtUrl();
                if (url != null) {
                    newState = new StringType(url);
                }
                break;
            case CURRENTTRANSPORTURI:
                value = getCurrentURI();
                if (value != null) {
                    newState = new StringType(value);
                }
                break;
            case CURRENTTRACKURI:
                value = stateMap.get("CurrentTrackURI");
                if (value != null) {
                    newState = new StringType(value);
                }
                break;
            case TUNEINSTATIONID:
                value = stateMap.get("CurrentTuneInStationId");
                if (value != null) {
                    newState = new StringType(value);
                }
                break;
            case BATTERYCHARGING:
                value = extractInfoFromMoreInfo("BattChg");
                if (value != null) {
                    newState = OnOffType.from("CHARGING".equalsIgnoreCase(value));
                }
                break;
            case BATTERYLEVEL:
                value = extractInfoFromMoreInfo("BattPct");
                if (value != null) {
                    newState = new DecimalType(value);
                }
                break;
            case MICROPHONE:
                value = getMicEnabled();
                if (value != null) {
                    newState = OnOffType.from(value);
                }
                break;
            default:
                newState = null;
                break;
        }
        if (newState != null) {
            updateState(channelId, newState);
        }
    }

    private void updateAlbumArtChannel(boolean allGroup) {
        String url = getAlbumArtUrl();
        if (url != null) {
            // We download the cover art in a different thread to not delay the other operations
            scheduler.submit(() -> {
                RawType image = HttpUtil.downloadImage(url, true, 500000);
                updateChannel(CURRENTALBUMART, image != null ? image : UnDefType.UNDEF, allGroup);
            });
        } else {
            updateChannel(CURRENTALBUMART, UnDefType.UNDEF, allGroup);
        }
    }

    private void updateChannel(String channeldD, State state, boolean allGroup) {
        if (allGroup) {
            for (String member : getZoneGroupMembers()) {
                try {
                    ZonePlayerHandler memberHandler = getHandlerByName(member);
                    if (ThingStatus.ONLINE.equals(memberHandler.getThing().getStatus())
                            && memberHandler.isLinked(channeldD)) {
                        memberHandler.updateState(channeldD, state);
                    }
                } catch (IllegalStateException e) {
                    logger.debug("Cannot update channel for group member ({})", e.getMessage());
                }
            }
        } else if (ThingStatus.ONLINE.equals(getThing().getStatus()) && isLinked(channeldD)) {
            updateState(channeldD, state);
        }
    }

    /**
     * CurrentURI will not change, but will trigger change of CurrentURIFormated
     * CurrentTrackMetaData will not change, but will trigger change of Title, Artist, Album
     */
    private boolean shouldIgnoreVariableUpdate(String variable, String value, @Nullable String oldValue) {
        return !hasValueChanged(value, oldValue) && !isQueueEvent(variable);
    }

    private boolean hasValueChanged(@Nullable String value, @Nullable String oldValue) {
        return oldValue != null ? !oldValue.equals(value) : value != null;
    }

    /**
     * Similar to the AVTransport eventing, the Queue events its state variables
     * as sub values within a synthesized LastChange state variable.
     */
    private boolean isQueueEvent(String variable) {
        return "LastChange".equals(variable);
    }

    private void updateGroupCoordinator() {
        try {
            coordinatorHandler = getHandlerByName(getCoordinator());
        } catch (IllegalStateException e) {
            logger.debug("Cannot update the group coordinator ({})", e.getMessage());
            coordinatorHandler = null;
        }
    }

    private boolean isUpnpDeviceRegistered() {
        return service.isRegistered(this);
    }

    private void addSubscription() {
        synchronized (upnpLock) {
            // Set up GENA Subscriptions
            if (service.isRegistered(this)) {
                for (String subscription : SERVICE_SUBSCRIPTIONS) {
                    Boolean state = subscriptionState.get(subscription);
                    if (state == null || !state) {
                        logger.debug("{}: Subscribing to service {}...", getUDN(), subscription);
                        service.addSubscription(this, subscription, SUBSCRIPTION_DURATION);
                        subscriptionState.put(subscription, true);
                    }
                }
            }
        }
    }

    private void removeSubscription() {
        synchronized (upnpLock) {
            // Set up GENA Subscriptions
            if (service.isRegistered(this)) {
                for (String subscription : SERVICE_SUBSCRIPTIONS) {
                    Boolean state = subscriptionState.get(subscription);
                    if (state != null && state) {
                        logger.debug("{}: Unsubscribing from service {}...", getUDN(), subscription);
                        service.removeSubscription(this, subscription);
                    }
                }
            }
            subscriptionState = new HashMap<>();
        }
    }

    @Override
    public void onServiceSubscribed(@Nullable String service, boolean succeeded) {
        if (service == null) {
            return;
        }
        synchronized (upnpLock) {
            logger.debug("{}: Subscription to service {} {}", getUDN(), service, succeeded ? "succeeded" : "failed");
            subscriptionState.put(service, succeeded);
        }
    }

    private Map<String, String> executeAction(String serviceId, String actionId, @Nullable Map<String, String> inputs) {
        Map<String, String> result = service.invokeAction(this, serviceId, actionId, inputs);
        result.forEach((variable, value) -> {
            this.onValueReceived(variable, value, serviceId);
        });
        return result;
    }

    private void updatePlayerState() {
        if (!updateZoneInfo()) {
            if (!ThingStatus.OFFLINE.equals(getThing().getStatus())) {
                logger.debug("Sonos player {} is not available in local network", getUDN());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.not-available-on-network [\"" + getUDN() + "\"]");
                synchronized (upnpLock) {
                    subscriptionState = new HashMap<>();
                }
            }
        } else if (!ThingStatus.ONLINE.equals(getThing().getStatus())) {
            logger.debug("Sonos player {} has been found in local network", getUDN());
            updateStatus(ThingStatus.ONLINE);
        }
    }

    protected void updateCurrentZoneName() {
        executeAction(SERVICE_DEVICE_PROPERTIES, ACTION_GET_ZONE_ATTRIBUTES, null);
    }

    protected void updateLed() {
        executeAction(SERVICE_DEVICE_PROPERTIES, ACTION_GET_LED_STATE, null);
    }

    protected void updateTime() {
        executeAction(SERVICE_ALARM_CLOCK, "GetTimeNow", null);
    }

    protected void updatePosition() {
        executeAction(SERVICE_AV_TRANSPORT, ACTION_GET_POSITION_INFO, null);
    }

    protected void updateRunningAlarmProperties() {
        Map<String, String> result = service.invokeAction(this, SERVICE_AV_TRANSPORT,
                ACTION_GET_RUNNING_ALARM_PROPERTIES, null);

        String alarmID = result.get("AlarmID");
        String loggedStartTime = result.get("LoggedStartTime");
        String newStringValue = null;
        if (alarmID != null && loggedStartTime != null) {
            newStringValue = alarmID + " - " + loggedStartTime;
        } else {
            newStringValue = "No running alarm";
        }
        result.put("RunningAlarmProperties", newStringValue);

        result.forEach((variable, value) -> {
            this.onValueReceived(variable, value, SERVICE_AV_TRANSPORT);
        });
    }

    protected boolean updateZoneInfo() {
        Map<String, String> result = executeAction(SERVICE_DEVICE_PROPERTIES, ACTION_GET_ZONE_INFO, null);

        Map<String, String> properties = editProperties();
        String value = stateMap.get("HardwareVersion");
        if (value != null && !value.isEmpty()) {
            properties.put(Thing.PROPERTY_HARDWARE_VERSION, value);
        }
        value = stateMap.get("DisplaySoftwareVersion");
        if (value != null && !value.isEmpty()) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, value);
        }
        value = stateMap.get("SerialNumber");
        if (value != null && !value.isEmpty()) {
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, value);
        }
        value = stateMap.get("MACAddress");
        if (value != null && !value.isEmpty()) {
            properties.put(MAC_ADDRESS, value);
        }
        value = stateMap.get("IPAddress");
        if (value != null && !value.isEmpty()) {
            properties.put(IP_ADDRESS, value);
        }
        updateProperties(properties);

        return !result.isEmpty();
    }

    public String getCoordinator() {
        for (SonosZoneGroup zg : getZoneGroups()) {
            if (zg.getMembers().contains(getUDN())) {
                return zg.getCoordinator();
            }
        }
        return getUDN();
    }

    public boolean isCoordinator() {
        return getUDN().equals(getCoordinator());
    }

    protected void updateMediaInformation() {
        String currentURI = getCurrentURI();
        SonosMetaData currentTrack = getTrackMetadata();
        SonosMetaData currentUriMetaData = getCurrentURIMetadata();

        String stationID = null;
        SonosMediaInformation mediaInfo = new SonosMediaInformation();

        // if currentURI == null, we do nothing
        if (currentURI != null) {
            if (currentURI.isEmpty()) {
                // Reset data
                mediaInfo = new SonosMediaInformation(true);
            }

            // if (currentURI.contains(GROUP_URI)) we do nothing, because
            // The Sonos is a slave member of a group
            // The media information will be updated by the coordinator
            // Notification of group change occurs later, so we just check the URI

            else if (isPlayingStream(currentURI) || isPlayingRadioStartedByAmazonEcho(currentURI)) {
                // Radio stream (tune-in)
                stationID = extractStationId(currentURI);
                mediaInfo = SonosMediaInformation.parseTuneInMediaInfo(getOpmlData(stationID),
                        currentUriMetaData != null ? currentUriMetaData.getTitle() : null, currentTrack);
            }

            else if (isPlayingRadioApp(currentURI)) {
                mediaInfo = SonosMediaInformation.parseRadioAppMediaInfo(
                        currentUriMetaData != null ? currentUriMetaData.getTitle() : null, currentTrack);
            }

            else if (isPlayingLineIn(currentURI)) {
                mediaInfo = SonosMediaInformation.parseTrackTitle(currentTrack);
            }

            else if (isPlayingRadio(currentURI)
                    || (!currentURI.contains("x-rincon-mp3") && !currentURI.contains("x-sonosapi"))) {
                mediaInfo = SonosMediaInformation.parseTrack(currentTrack);
            }
        }

        String albumArtURI = (currentTrack != null && !currentTrack.getAlbumArtUri().isEmpty())
                ? currentTrack.getAlbumArtUri()
                : "";

        ZonePlayerHandler handlerForImageUpdate = null;
        for (String member : getZoneGroupMembers()) {
            try {
                ZonePlayerHandler memberHandler = getHandlerByName(member);
                if (ThingStatus.ONLINE.equals(memberHandler.getThing().getStatus())) {
                    if (memberHandler.isLinked(CURRENTALBUMART)
                            && hasValueChanged(albumArtURI, memberHandler.stateMap.get("CurrentAlbumArtURI"))) {
                        handlerForImageUpdate = memberHandler;
                    }
                    memberHandler.onValueReceived("CurrentTuneInStationId", (stationID != null) ? stationID : "",
                            SERVICE_AV_TRANSPORT);
                    if (mediaInfo.needsUpdate()) {
                        String artist = mediaInfo.getArtist();
                        String album = mediaInfo.getAlbum();
                        String title = mediaInfo.getTitle();
                        String combinedInfo = mediaInfo.getCombinedInfo();
                        memberHandler.onValueReceived("CurrentArtist", (artist != null) ? artist : "",
                                SERVICE_AV_TRANSPORT);
                        memberHandler.onValueReceived("CurrentAlbum", (album != null) ? album : "",
                                SERVICE_AV_TRANSPORT);
                        memberHandler.onValueReceived("CurrentTitle", (title != null) ? title : "",
                                SERVICE_AV_TRANSPORT);
                        memberHandler.onValueReceived("CurrentURIFormatted", (combinedInfo != null) ? combinedInfo : "",
                                SERVICE_AV_TRANSPORT);
                        memberHandler.onValueReceived("CurrentAlbumArtURI", albumArtURI, SERVICE_AV_TRANSPORT);
                    }
                }
            } catch (IllegalStateException e) {
                logger.debug("Cannot update media data for group member ({})", e.getMessage());
            }
        }
        if (mediaInfo.needsUpdate() && handlerForImageUpdate != null) {
            handlerForImageUpdate.updateAlbumArtChannel(true);
        }
    }

    private @Nullable String getOpmlData(@Nullable String stationId) {
        String url = opmlUrl;
        if (url != null && stationId != null && !stationId.isEmpty()) {
            String mac = getMACAddress();
            if (mac != null && !mac.isEmpty()) {
                url = url.replace("%id", stationId);
                url = url.replace("%serial", mac);
                String response = null;
                try {
                    response = HttpUtil.executeUrl("GET", url, HTTP_TIMEOUT);
                } catch (IOException e) {
                    logger.debug("OPML request failed ({})", url, e);
                }
                logger.trace("OPML response = {}", response);
                return response;
            }
        }
        return null;
    }

    private @Nullable String extractStationId(String uri) {
        String stationID = null;
        if (isPlayingStream(uri)) {
            stationID = substringBetween(uri, ":s", "?sid");
        } else if (isPlayingRadioStartedByAmazonEcho(uri)) {
            stationID = substringBetween(uri, "sid=s", "&");
        }
        return stationID;
    }

    private @Nullable String substringBetween(String str, String open, String close) {
        String result = null;
        int idx1 = str.indexOf(open);
        if (idx1 >= 0) {
            idx1 += open.length();
            int idx2 = str.indexOf(close, idx1);
            if (idx2 >= 0) {
                result = str.substring(idx1, idx2);
            }
        }
        return result;
    }

    public @Nullable String getGroupCoordinatorIsLocal() {
        return stateMap.get("GroupCoordinatorIsLocal");
    }

    public boolean isGroupCoordinator() {
        return "true".equals(getGroupCoordinatorIsLocal());
    }

    @Override
    public String getUDN() {
        String udn = configuration.udn;
        return udn != null && !udn.isEmpty() ? udn : "undefined";
    }

    public @Nullable String getCurrentURI() {
        return stateMap.get("CurrentURI");
    }

    public @Nullable String getCurrentURIMetadataAsString() {
        return stateMap.get("CurrentURIMetaData");
    }

    public @Nullable SonosMetaData getCurrentURIMetadata() {
        String metaData = getCurrentURIMetadataAsString();
        return metaData != null && !metaData.isEmpty() ? SonosXMLParser.getMetaDataFromXML(metaData) : null;
    }

    public @Nullable SonosMetaData getTrackMetadata() {
        String metaData = stateMap.get("CurrentTrackMetaData");
        return metaData != null && !metaData.isEmpty() ? SonosXMLParser.getMetaDataFromXML(metaData) : null;
    }

    public @Nullable SonosMetaData getEnqueuedTransportURIMetaData() {
        String metaData = stateMap.get("EnqueuedTransportURIMetaData");
        return metaData != null && !metaData.isEmpty() ? SonosXMLParser.getMetaDataFromXML(metaData) : null;
    }

    public @Nullable String getMACAddress() {
        String mac = stateMap.get("MACAddress");
        if (mac == null || mac.isEmpty()) {
            updateZoneInfo();
        }
        return stateMap.get("MACAddress");
    }

    public @Nullable String getRefreshedPosition() {
        updatePosition();
        return stateMap.get("RelTime");
    }

    public long getRefreshedCurrenTrackNr() {
        updatePosition();
        String value = stateMap.get("Track");
        if (value != null) {
            return Long.valueOf(value);
        } else {
            return -1;
        }
    }

    public @Nullable String getVolume() {
        return stateMap.get("VolumeMaster");
    }

    public boolean isOutputLevelFixed() {
        return "1".equals(stateMap.get("OutputFixed"));
    }

    public @Nullable String getBass() {
        return stateMap.get("Bass");
    }

    public @Nullable String getTreble() {
        return stateMap.get("Treble");
    }

    public @Nullable String getLoudness() {
        return stateMap.get("LoudnessMaster");
    }

    public @Nullable String getSurroundEnabled() {
        return stateMap.get("SurroundEnabled");
    }

    public @Nullable String getSurroundMusicMode() {
        return stateMap.get("SurroundMode");
    }

    public @Nullable String getSurroundTvLevel() {
        return stateMap.get("SurroundLevel");
    }

    public @Nullable String getSurroundMusicLevel() {
        return stateMap.get("MusicSurroundLevel");
    }

    public @Nullable String getCodec() {
        String codec = stateMap.get("HTAudioIn");
        if (codec != null) {
            switch (codec) {
                case "0":
                case "21":
                    codec = "noSignal";
                    break;
                case "22":
                case "33554454":
                    codec = "silence";
                    break;
                case "32":
                    codec = "DTS";
                    break;
                case "59":
                case "61":
                case "63":
                    codec = "Atmos";
                    break;
                case "33554434":
                case "33554488":
                    codec = "DD20";
                    break;
                case "33554490":
                    codec = "DDPlus20";
                    break;
                case "33554494":
                    codec = "PCM20";
                    break;
                case "84934713":
                    codec = "DD51";
                    break;
                case "84934714":
                    codec = "DDPlus51";
                    break;
                case "84934716":
                    codec = "TrueHD51";
                    break;
                case "84934718":
                    codec = "PCM51";
                    break;
                case "84934721":
                    codec = "DTS51";
                    break;
                case "118489148":
                    codec = "TrueHD71";
                    break;
                default:
                    codec = "Unknown - " + codec;
            }
        }
        return codec;
    }

    public @Nullable String getSubwooferEnabled() {
        return stateMap.get("SubEnabled");
    }

    public @Nullable String getSubwooferGain() {
        return stateMap.get("SubGain");
    }

    public @Nullable String getHeightLevel() {
        return stateMap.get("HeightChannelLevel");
    }

    public @Nullable String getTransportState() {
        return stateMap.get("TransportState");
    }

    public @Nullable String getCurrentTitle() {
        return stateMap.get("CurrentTitle");
    }

    public @Nullable String getCurrentArtist() {
        return stateMap.get("CurrentArtist");
    }

    public @Nullable String getCurrentAlbum() {
        return stateMap.get("CurrentAlbum");
    }

    public List<SonosEntry> getArtists(String filter) {
        return getEntries("A:", filter);
    }

    public List<SonosEntry> getArtists() {
        return getEntries("A:", "dc:title,res,dc:creator,upnp:artist,upnp:album");
    }

    public List<SonosEntry> getAlbums(String filter) {
        return getEntries("A:ALBUM", filter);
    }

    public List<SonosEntry> getAlbums() {
        return getEntries("A:ALBUM", "dc:title,res,dc:creator,upnp:artist,upnp:album");
    }

    public List<SonosEntry> getTracks(String filter) {
        return getEntries("A:TRACKS", filter);
    }

    public List<SonosEntry> getTracks() {
        return getEntries("A:TRACKS", "dc:title,res,dc:creator,upnp:artist,upnp:album");
    }

    public List<SonosEntry> getQueue(String filter) {
        return getEntries("Q:0", filter);
    }

    public List<SonosEntry> getQueue() {
        return getEntries("Q:0", "dc:title,res,dc:creator,upnp:artist,upnp:album");
    }

    public long getQueueSize() {
        return getNbEntries("Q:0");
    }

    public List<SonosEntry> getPlayLists(String filter) {
        return getEntries("SQ:", filter);
    }

    public List<SonosEntry> getPlayLists() {
        return getEntries("SQ:", "dc:title,res,dc:creator,upnp:artist,upnp:album");
    }

    public List<SonosEntry> getFavoriteRadios(String filter) {
        return getEntries("R:0/0", filter);
    }

    public List<SonosEntry> getFavoriteRadios() {
        return getEntries("R:0/0", "dc:title,res,dc:creator,upnp:artist,upnp:album");
    }

    /**
     * Searches for entries in the 'favorites' list on a sonos account
     *
     * @return
     */
    public List<SonosEntry> getFavorites() {
        return getEntries("FV:2", "dc:title,res,dc:creator,upnp:artist,upnp:album");
    }

    protected List<SonosEntry> getEntries(String type, String filter) {
        long startAt = 0;

        Map<String, String> inputs = new HashMap<>();
        inputs.put("ObjectID", type);
        inputs.put("BrowseFlag", "BrowseDirectChildren");
        inputs.put("Filter", filter);
        inputs.put("StartingIndex", Long.toString(startAt));
        inputs.put("RequestedCount", Integer.toString(200));
        inputs.put("SortCriteria", "");

        Map<String, String> result = service.invokeAction(this, SERVICE_CONTENT_DIRECTORY, "Browse", inputs);

        String initialResult = result.get("Result");
        if (initialResult == null) {
            return Collections.emptyList();
        }

        long totalMatches = getResultEntry(result, "TotalMatches", type, filter);
        long initialNumberReturned = getResultEntry(result, "NumberReturned", type, filter);

        List<SonosEntry> resultList = SonosXMLParser.getEntriesFromString(initialResult);
        startAt = startAt + initialNumberReturned;

        while (startAt < totalMatches) {
            inputs.put("StartingIndex", Long.toString(startAt));
            result = service.invokeAction(this, SERVICE_CONTENT_DIRECTORY, "Browse", inputs);

            // Execute this action synchronously
            String nextResult = result.get("Result");
            if (nextResult == null) {
                break;
            }

            long numberReturned = getResultEntry(result, "NumberReturned", type, filter);

            resultList.addAll(SonosXMLParser.getEntriesFromString(nextResult));

            startAt = startAt + numberReturned;
        }

        return resultList;
    }

    protected long getNbEntries(String type) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("ObjectID", type);
        inputs.put("BrowseFlag", "BrowseDirectChildren");
        inputs.put("Filter", "dc:title");
        inputs.put("StartingIndex", "0");
        inputs.put("RequestedCount", "1");
        inputs.put("SortCriteria", "");

        Map<String, String> result = service.invokeAction(this, SERVICE_CONTENT_DIRECTORY, "Browse", inputs);

        return getResultEntry(result, "TotalMatches", type, "dc:title");
    }

    /**
     * Handles value searching in a SONOS result map (called by {@link #getEntries(String, String)})
     *
     * @param resultInput - the map to be examined for the requestedKey
     * @param requestedKey - the key to be sought in the resultInput map
     * @param entriesType - the 'type' argument of {@link #getEntries(String, String)} method used for logging
     * @param entriesFilter - the 'filter' argument of {@link #getEntries(String, String)} method used for logging
     *
     * @return 0 as long or the value corresponding to the requiredKey if found
     */
    private Long getResultEntry(Map<String, String> resultInput, String requestedKey, String entriesType,
            String entriesFilter) {
        long result = 0;

        if (resultInput.isEmpty()) {
            return result;
        }

        try {
            String resultString = resultInput.get(requestedKey);
            if (resultString == null) {
                throw new NumberFormatException("Requested key is null.");
            }
            result = Long.valueOf(resultString);
        } catch (NumberFormatException ex) {
            logger.debug("Could not fetch {} result for type: {} and filter: {}. Using default value '0': {}",
                    requestedKey, entriesType, entriesFilter, ex.getMessage(), ex);
        }

        return result;
    }

    /**
     * Save the state (track, position etc) of the Sonos Zone player.
     */
    protected void saveState() {
        synchronized (stateLock) {
            SonosZonePlayerState savedState = new SonosZonePlayerState();
            savedState.transportState = getTransportState();
            savedState.volume = getVolume();
            this.savedState = savedState;

            String currentURI = getCurrentURI();

            if (currentURI != null) {
                if (isPlayingStreamOrRadio(currentURI)) {
                    // we are streaming music, like tune-in radio or Google Play Music radio
                    SonosMetaData track = getTrackMetadata();
                    SonosMetaData current = getCurrentURIMetadata();
                    if (track != null && current != null) {
                        savedState.entry = new SonosEntry("", current.getTitle(), "", "", track.getAlbumArtUri(), "",
                                current.getUpnpClass(), currentURI);
                    }
                } else if (currentURI.contains(GROUP_URI)) {
                    // we are a slave to some coordinator
                    savedState.entry = new SonosEntry("", "", "", "", "", "", "", currentURI);
                } else if (isPlayingLineIn(currentURI)) {
                    // we are streaming from the Line In connection
                    savedState.entry = new SonosEntry("", "", "", "", "", "", "", currentURI);
                } else if (isPlayingQueue(currentURI)) {
                    // we are playing something that sits in the queue
                    SonosMetaData queued = getEnqueuedTransportURIMetaData();
                    if (queued != null) {
                        savedState.track = getRefreshedCurrenTrackNr();

                        if (queued.getUpnpClass().contains("object.container.playlistContainer")) {
                            // we are playing a real 'saved' playlist
                            List<SonosEntry> playLists = getPlayLists();
                            for (SonosEntry someList : playLists) {
                                if (someList.getTitle().equals(queued.getTitle())) {
                                    savedState.entry = new SonosEntry(someList.getId(), someList.getTitle(),
                                            someList.getParentId(), "", "", "", someList.getUpnpClass(),
                                            someList.getRes());
                                    break;
                                }
                            }
                        } else if (queued.getUpnpClass().contains("object.container")) {
                            // we are playing some other sort of
                            // 'container' - we will save that to a
                            // playlist for our convenience
                            logger.debug("Save State for a container of type {}", queued.getUpnpClass());

                            // save the playlist
                            String existingList = "";
                            List<SonosEntry> playLists = getPlayLists();
                            for (SonosEntry someList : playLists) {
                                if (someList.getTitle().equals(TITLE_PREFIX + getUDN())) {
                                    existingList = someList.getId();
                                    break;
                                }
                            }

                            saveQueue(TITLE_PREFIX + getUDN(), existingList);

                            // get all the playlists and a ref to our
                            // saved list
                            playLists = getPlayLists();
                            for (SonosEntry someList : playLists) {
                                if (someList.getTitle().equals(TITLE_PREFIX + getUDN())) {
                                    savedState.entry = new SonosEntry(someList.getId(), someList.getTitle(),
                                            someList.getParentId(), "", "", "", someList.getUpnpClass(),
                                            someList.getRes());
                                    break;
                                }
                            }
                        }
                    } else {
                        savedState.entry = new SonosEntry("", "", "", "", "", "", "", QUEUE_URI + getUDN() + "#0");
                    }
                }

                savedState.relTime = getRefreshedPosition();
            } else {
                savedState.entry = null;
            }
        }
    }

    /**
     * Restore the state (track, position etc) of the Sonos Zone player.
     */
    protected void restoreState() {
        synchronized (stateLock) {
            SonosZonePlayerState state = savedState;
            if (state != null) {
                // put settings back
                String volume = state.volume;
                if (volume != null) {
                    setVolume(DecimalType.valueOf(volume));
                }

                if (isCoordinator()) {
                    SonosEntry entry = state.entry;
                    if (entry != null) {
                        // check if we have a playlist to deal with
                        if (entry.getUpnpClass().contains("object.container.playlistContainer")) {
                            addURIToQueue(entry.getRes(), SonosXMLParser.compileMetadataString(entry), 0, true);
                            entry = new SonosEntry("", "", "", "", "", "", "", QUEUE_URI + getUDN() + "#0");
                            setCurrentURI(entry);
                            setPositionTrack(state.track);
                        } else {
                            setCurrentURI(entry);
                            setPosition(state.relTime);
                        }
                    }

                    String transportState = state.transportState;
                    if (STATE_PLAYING.equals(transportState)) {
                        play();
                    } else if (STATE_STOPPED.equals(transportState)) {
                        stop();
                    } else if (STATE_PAUSED_PLAYBACK.equals(transportState)) {
                        pause();
                    }
                }
            }
        }
    }

    public void saveQueue(String name, String queueID) {
        executeAction(SERVICE_AV_TRANSPORT, ACTION_SAVE_QUEUE, Map.of("Title", name, "ObjectID", queueID));
    }

    public void setVolume(Command command) {
        if (command instanceof OnOffType || command instanceof IncreaseDecreaseType || command instanceof DecimalType
                || command instanceof PercentType) {
            String newValue = null;
            String currentVolume = getVolume();
            if (command == IncreaseDecreaseType.INCREASE && currentVolume != null) {
                int i = Integer.valueOf(currentVolume);
                newValue = String.valueOf(Math.min(100, i + 1));
            } else if (command == IncreaseDecreaseType.DECREASE && currentVolume != null) {
                int i = Integer.valueOf(currentVolume);
                newValue = String.valueOf(Math.max(0, i - 1));
            } else if (command == OnOffType.ON) {
                newValue = "100";
            } else if (command == OnOffType.OFF) {
                newValue = "0";
            } else if (command instanceof DecimalType decimalCommand) {
                newValue = String.valueOf(decimalCommand.intValue());
            } else {
                return;
            }
            executeAction(SERVICE_RENDERING_CONTROL, ACTION_SET_VOLUME,
                    Map.of("Channel", "Master", "DesiredVolume", newValue));
        }
    }

    /**
     * Set the VOLUME command specific to the current grouping according to the Sonos behaviour.
     * AdHoc groups handles the volume specifically for each player.
     * Bonded groups delegate the volume to the coordinator which applies the same level to all group members.
     */
    public void setVolumeForGroup(Command command) {
        if (isAdHocGroup() || isStandalonePlayer()) {
            setVolume(command);
        } else {
            try {
                getCoordinatorHandler().setVolume(command);
            } catch (IllegalStateException e) {
                logger.debug("Cannot set group volume ({})", e.getMessage());
            }
        }
    }

    public void setBass(Command command) {
        if (!isOutputLevelFixed()) {
            String newValue = getNewNumericValue(command, getBass(), MIN_BASS, MAX_BASS);
            if (newValue != null) {
                executeAction(SERVICE_RENDERING_CONTROL, ACTION_SET_BASS,
                        Map.of("InstanceID", "0", "DesiredBass", newValue));
            }
        }
    }

    public void setTreble(Command command) {
        if (!isOutputLevelFixed()) {
            String newValue = getNewNumericValue(command, getTreble(), MIN_TREBLE, MAX_TREBLE);
            if (newValue != null) {
                executeAction(SERVICE_RENDERING_CONTROL, ACTION_SET_TREBLE,
                        Map.of("InstanceID", "0", "DesiredTreble", newValue));
            }
        }
    }

    private @Nullable String getNewNumericValue(Command command, @Nullable String currentValue, int minValue,
            int maxValue) {
        String newValue = null;
        if (command instanceof IncreaseDecreaseType || command instanceof DecimalType) {
            if (command == IncreaseDecreaseType.INCREASE && currentValue != null) {
                int i = Integer.valueOf(currentValue);
                newValue = String.valueOf(Math.min(maxValue, i + 1));
            } else if (command == IncreaseDecreaseType.DECREASE && currentValue != null) {
                int i = Integer.valueOf(currentValue);
                newValue = String.valueOf(Math.max(minValue, i - 1));
            } else if (command instanceof DecimalType decimalCommand) {
                newValue = String.valueOf(decimalCommand.intValue());
            }
        }
        return newValue;
    }

    public void setLoudness(Command command) {
        if (!isOutputLevelFixed() && (command instanceof OnOffType || command instanceof OpenClosedType
                || command instanceof UpDownType)) {
            String value = (command.equals(OnOffType.ON) || command.equals(UpDownType.UP)
                    || command.equals(OpenClosedType.OPEN)) ? "True" : "False";
            executeAction(SERVICE_RENDERING_CONTROL, ACTION_SET_LOUDNESS,
                    Map.of("InstanceID", "0", "Channel", "Master", "DesiredLoudness", value));
        }
    }

    /**
     * Checks if the player receiving the command is part of a group that
     * consists of randomly added players or contains bonded players
     *
     * @return boolean
     */
    private boolean isAdHocGroup() {
        SonosZoneGroup currentZoneGroup = getCurrentZoneGroup();
        if (currentZoneGroup != null) {
            List<String> zoneGroupMemberNames = currentZoneGroup.getMemberZoneNames();

            for (String zoneName : zoneGroupMemberNames) {
                if (!zoneName.equals(zoneGroupMemberNames.get(0))) {
                    // At least one "ZoneName" differs so we have an AdHoc group
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the player receiving the command is a standalone player
     *
     * @return boolean
     */
    private boolean isStandalonePlayer() {
        SonosZoneGroup zoneGroup = getCurrentZoneGroup();
        return zoneGroup == null || zoneGroup.getMembers().size() == 1;
    }

    private Collection<SonosZoneGroup> getZoneGroups() {
        String zoneGroupState = stateMap.get("ZoneGroupState");
        return zoneGroupState == null ? Collections.emptyList() : SonosXMLParser.getZoneGroupFromXML(zoneGroupState);
    }

    /**
     * Returns the current zone group
     * (of which the player receiving the command is part)
     *
     * @return {@link SonosZoneGroup}
     */
    private @Nullable SonosZoneGroup getCurrentZoneGroup() {
        for (SonosZoneGroup zoneGroup : getZoneGroups()) {
            if (zoneGroup.getMembers().contains(getUDN())) {
                return zoneGroup;
            }
        }
        logger.debug("Could not fetch Sonos group state information");
        return null;
    }

    /**
     * Sets the volume level for a notification sound
     *
     * @param notificationSoundVolume
     */
    public void setNotificationSoundVolume(@Nullable PercentType notificationSoundVolume) {
        if (notificationSoundVolume != null) {
            setVolumeForGroup(notificationSoundVolume);
        }
    }

    /**
     * Gets the volume level for a notification sound
     */
    public @Nullable PercentType getNotificationSoundVolume() {
        Integer notificationSoundVolume = getConfigAs(ZonePlayerConfiguration.class).notificationVolume;
        if (notificationSoundVolume == null) {
            // if no value is set we use the current volume instead
            String volume = getVolume();
            return volume != null ? new PercentType(volume) : null;
        }
        return new PercentType(notificationSoundVolume);
    }

    public void addURIToQueue(String URI, String meta, long desiredFirstTrack, boolean enqueueAsNext) {
        Map<String, String> inputs = new HashMap<>();

        try {
            inputs.put("InstanceID", "0");
            inputs.put("EnqueuedURI", URI);
            inputs.put("EnqueuedURIMetaData", meta);
            inputs.put("DesiredFirstTrackNumberEnqueued", Long.toString(desiredFirstTrack));
            inputs.put("EnqueueAsNext", Boolean.toString(enqueueAsNext));
        } catch (NumberFormatException ex) {
            logger.debug("Action Invalid Value Format Exception {}", ex.getMessage());
        }

        executeAction(SERVICE_AV_TRANSPORT, ACTION_ADD_URI_TO_QUEUE, inputs);
    }

    public void setCurrentURI(SonosEntry newEntry) {
        setCurrentURI(newEntry.getRes(), SonosXMLParser.compileMetadataString(newEntry));
    }

    public void setCurrentURI(@Nullable String URI, @Nullable String URIMetaData) {
        if (URI != null && URIMetaData != null) {
            logger.debug("setCurrentURI URI {} URIMetaData {}", URI, URIMetaData);
            executeAction(SERVICE_AV_TRANSPORT, ACTION_SET_AV_TRANSPORT_URI,
                    Map.of("InstanceID", "0", "CurrentURI", URI, "CurrentURIMetaData", URIMetaData));
        }
    }

    public void setPosition(@Nullable String relTime) {
        seek("REL_TIME", relTime);
    }

    public void setPositionTrack(long tracknr) {
        seek("TRACK_NR", Long.toString(tracknr));
    }

    public void setPositionTrack(String tracknr) {
        seek("TRACK_NR", tracknr);
    }

    protected void seek(String unit, @Nullable String target) {
        if (target != null) {
            executeAction(SERVICE_AV_TRANSPORT, ACTION_SEEK, Map.of("InstanceID", "0", "Unit", unit, "Target", target));
        }
    }

    public void play() {
        executeAction(SERVICE_AV_TRANSPORT, ACTION_PLAY, Map.of("Speed", "1"));
    }

    public void stop() {
        executeAction(SERVICE_AV_TRANSPORT, ACTION_STOP, null);
    }

    public void pause() {
        executeAction(SERVICE_AV_TRANSPORT, ACTION_PAUSE, null);
    }

    public void setShuffle(Command command) {
        if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {
            try {
                ZonePlayerHandler coordinator = getCoordinatorHandler();

                if (command.equals(OnOffType.ON) || command.equals(UpDownType.UP)
                        || command.equals(OpenClosedType.OPEN)) {
                    switch (coordinator.getRepeatMode()) {
                        case "ALL":
                            coordinator.updatePlayMode("SHUFFLE");
                            break;
                        case "ONE":
                            coordinator.updatePlayMode("SHUFFLE_REPEAT_ONE");
                            break;
                        case "OFF":
                            coordinator.updatePlayMode("SHUFFLE_NOREPEAT");
                            break;
                    }
                } else if (command.equals(OnOffType.OFF) || command.equals(UpDownType.DOWN)
                        || command.equals(OpenClosedType.CLOSED)) {
                    switch (coordinator.getRepeatMode()) {
                        case "ALL":
                            coordinator.updatePlayMode("REPEAT_ALL");
                            break;
                        case "ONE":
                            coordinator.updatePlayMode("REPEAT_ONE");
                            break;
                        case "OFF":
                            coordinator.updatePlayMode("NORMAL");
                            break;
                    }
                }
            } catch (IllegalStateException e) {
                logger.debug("Cannot handle shuffle command ({})", e.getMessage());
            }
        }
    }

    public void setRepeat(Command command) {
        if (command instanceof StringType) {
            try {
                ZonePlayerHandler coordinator = getCoordinatorHandler();

                switch (command.toString()) {
                    case "ALL":
                        coordinator.updatePlayMode(coordinator.isShuffleActive() ? "SHUFFLE" : "REPEAT_ALL");
                        break;
                    case "ONE":
                        coordinator.updatePlayMode(coordinator.isShuffleActive() ? "SHUFFLE_REPEAT_ONE" : "REPEAT_ONE");
                        break;
                    case "OFF":
                        coordinator.updatePlayMode(coordinator.isShuffleActive() ? "SHUFFLE_NOREPEAT" : "NORMAL");
                        break;
                    default:
                        logger.debug("{}: unexpected repeat command; accepted values are ALL, ONE and OFF",
                                command.toString());
                        break;
                }
            } catch (IllegalStateException e) {
                logger.debug("Cannot handle repeat command ({})", e.getMessage());
            }
        }
    }

    public void setSubwoofer(Command command) {
        setEqualizerBooleanSetting(command, "SubEnable");
    }

    public void setSubwooferGain(Command command) {
        setEqualizerNumericSetting(command, "SubGain", getSubwooferGain(), MIN_SUBWOOFER_GAIN, MAX_SUBWOOFER_GAIN);
    }

    public void setSurround(Command command) {
        setEqualizerBooleanSetting(command, "SurroundEnable");
    }

    public void setSurroundMusicMode(Command command) {
        if (command instanceof StringType) {
            setEQ("SurroundMode", command.toString());
        }
    }

    public void setSurroundMusicLevel(Command command) {
        setEqualizerNumericSetting(command, "MusicSurroundLevel", getSurroundMusicLevel(), MIN_SURROUND_LEVEL,
                MAX_SURROUND_LEVEL);
    }

    public void setSurroundTvLevel(Command command) {
        setEqualizerNumericSetting(command, "SurroundLevel", getSurroundTvLevel(), MIN_SURROUND_LEVEL,
                MAX_SURROUND_LEVEL);
    }

    public void setHeightLevel(Command command) {
        setEqualizerNumericSetting(command, "HeightChannelLevel", getHeightLevel(), MIN_HEIGHT_LEVEL, MAX_HEIGHT_LEVEL);
    }

    public void setNightMode(Command command) {
        setEqualizerBooleanSetting(command, "NightMode");
    }

    public void setSpeechEnhancement(Command command) {
        setEqualizerBooleanSetting(command, "DialogLevel");
    }

    private void setEqualizerBooleanSetting(Command command, String eqType) {
        if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {
            setEQ(eqType, (command.equals(OnOffType.ON) || command.equals(UpDownType.UP)
                    || command.equals(OpenClosedType.OPEN)) ? "1" : "0");
        }
    }

    private void setEqualizerNumericSetting(Command command, String eqType, @Nullable String currentValue, int minValue,
            int maxValue) {
        String newValue = getNewNumericValue(command, currentValue, minValue, maxValue);
        if (newValue != null) {
            setEQ(eqType, newValue);
        }
    }

    private void setEQ(String eqType, String value) {
        try {
            executeAction(SERVICE_RENDERING_CONTROL, ACTION_SET_EQ,
                    Map.of("InstanceID", "0", "EQType", eqType, "DesiredValue", value));
        } catch (IllegalStateException e) {
            logger.debug("Cannot handle {} command ({})", eqType, e.getMessage());
        }
    }

    public @Nullable String getNightMode() {
        return stateMap.get("NightMode");
    }

    public @Nullable String getDialogLevel() {
        return stateMap.get("DialogLevel");
    }

    public @Nullable String getPlayMode() {
        return stateMap.get("CurrentPlayMode");
    }

    public Boolean isShuffleActive() {
        String playMode = getPlayMode();
        return (playMode != null && playMode.startsWith("SHUFFLE"));
    }

    public String getRepeatMode() {
        String mode = "OFF";
        String playMode = getPlayMode();
        if (playMode != null) {
            switch (playMode) {
                case "REPEAT_ALL":
                case "SHUFFLE":
                    mode = "ALL";
                    break;
                case "REPEAT_ONE":
                case "SHUFFLE_REPEAT_ONE":
                    mode = "ONE";
                    break;
                case "NORMAL":
                case "SHUFFLE_NOREPEAT":
                default:
                    mode = "OFF";
                    break;
            }
        }
        return mode;
    }

    public @Nullable String getMicEnabled() {
        return stateMap.get("MicEnabled");
    }

    protected void updatePlayMode(String playMode) {
        executeAction(SERVICE_AV_TRANSPORT, ACTION_SET_PLAY_MODE, Map.of("InstanceID", "0", "NewPlayMode", playMode));
    }

    /**
     * Clear all scheduled music from the current queue.
     *
     */
    public void removeAllTracksFromQueue() {
        executeAction(SERVICE_AV_TRANSPORT, ACTION_REMOVE_ALL_TRACKS_FROM_QUEUE, Map.of("InstanceID", "0"));
    }

    /**
     * Play music from the line-in of the given Player referenced by the given UDN or name
     *
     * @param command udn or name
     */
    public void playLineIn(Command command) {
        if (command instanceof StringType) {
            try {
                LineInType lineInType = LineInType.ANY;
                String remotePlayerName = command.toString();
                if (remotePlayerName.toUpperCase().startsWith("ANALOG,")) {
                    lineInType = LineInType.ANALOG;
                    remotePlayerName = remotePlayerName.substring(7);
                } else if (remotePlayerName.toUpperCase().startsWith("DIGITAL,")) {
                    lineInType = LineInType.DIGITAL;
                    remotePlayerName = remotePlayerName.substring(8);
                }
                ZonePlayerHandler coordinatorHandler = getCoordinatorHandler();
                ZonePlayerHandler remoteHandler = getHandlerByName(remotePlayerName);

                // check if player has a line-in connected
                if ((lineInType != LineInType.DIGITAL && remoteHandler.isAnalogLineInConnected())
                        || (lineInType != LineInType.ANALOG && remoteHandler.isOpticalLineInConnected())) {
                    // stop whatever is currently playing
                    coordinatorHandler.stop();

                    // set the URI
                    if (lineInType != LineInType.DIGITAL && remoteHandler.isAnalogLineInConnected()) {
                        coordinatorHandler.setCurrentURI(ANALOG_LINE_IN_URI + remoteHandler.getUDN(), "");
                    } else {
                        coordinatorHandler.setCurrentURI(OPTICAL_LINE_IN_URI + remoteHandler.getUDN() + SPDIF, "");
                    }

                    // take the system off mute
                    coordinatorHandler.setMute(OnOffType.OFF);

                    // start jammin'
                    coordinatorHandler.play();
                } else {
                    logger.debug("Line-in of {} is not connected", remoteHandler.getUDN());
                }
            } catch (IllegalStateException e) {
                logger.debug("Cannot play line-in ({})", e.getMessage());
            }
        }
    }

    private ZonePlayerHandler getCoordinatorHandler() throws IllegalStateException {
        ZonePlayerHandler handler = coordinatorHandler;
        if (handler != null) {
            return handler;
        }
        try {
            handler = getHandlerByName(getCoordinator());
            coordinatorHandler = handler;
            return handler;
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Missing group coordinator " + getCoordinator());
        }
    }

    /**
     * Returns a list of all zone group members this particular player is member of
     * Or empty list if the players is not assigned to any group
     *
     * @return a list of Strings containing the UDNs of other group members
     */
    protected List<String> getZoneGroupMembers() {
        List<String> result = new ArrayList<>();

        Collection<SonosZoneGroup> zoneGroups = getZoneGroups();
        if (!zoneGroups.isEmpty()) {
            for (SonosZoneGroup zg : zoneGroups) {
                if (zg.getMembers().contains(getUDN())) {
                    result.addAll(zg.getMembers());
                    break;
                }
            }
        } else {
            // If the group topology was not yet received, return at least the current Sonos zone
            result.add(getUDN());
        }
        return result;
    }

    /**
     * Returns a list of other zone group members this particular player is member of
     * Or empty list if the players is not assigned to any group
     *
     * @return a list of Strings containing the UDNs of other group members
     */
    protected List<String> getOtherZoneGroupMembers() {
        List<String> zoneGroupMembers = getZoneGroupMembers();
        zoneGroupMembers.remove(getUDN());
        return zoneGroupMembers;
    }

    protected ZonePlayerHandler getHandlerByName(String remotePlayerName) throws IllegalStateException {
        for (ThingTypeUID supportedThingType : SonosBindingConstants.SUPPORTED_THING_TYPES_UIDS) {
            Thing thing = localThingRegistry.get(new ThingUID(supportedThingType, remotePlayerName));
            if (thing != null) {
                ThingHandler handler = thing.getHandler();
                if (handler instanceof ZonePlayerHandler zonePlayerHandler) {
                    return zonePlayerHandler;
                }
            }
        }
        for (Thing aThing : localThingRegistry.getAll()) {
            if (SonosBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(aThing.getThingTypeUID())
                    && aThing.getConfiguration().get(ZonePlayerConfiguration.UDN).equals(remotePlayerName)) {
                ThingHandler handler = aThing.getHandler();
                if (handler instanceof ZonePlayerHandler zonePlayerHandler) {
                    return zonePlayerHandler;
                }
            }
        }
        throw new IllegalStateException("Could not find handler for " + remotePlayerName);
    }

    public void setMute(Command command) {
        if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {
            String value = (command.equals(OnOffType.ON) || command.equals(UpDownType.UP)
                    || command.equals(OpenClosedType.OPEN)) ? "True" : "False";
            executeAction(SERVICE_RENDERING_CONTROL, ACTION_SET_MUTE,
                    Map.of("Channel", "Master", "DesiredMute", value));
        }
    }

    public List<SonosAlarm> getCurrentAlarmList() {
        Map<String, String> result = executeAction(SERVICE_ALARM_CLOCK, "ListAlarms", null);
        String alarmList = result.get("CurrentAlarmList");
        return alarmList == null ? Collections.emptyList() : SonosXMLParser.getAlarmsFromStringResult(alarmList);
    }

    public void updateAlarm(SonosAlarm alarm) {
        Map<String, String> inputs = new HashMap<>();

        try {
            inputs.put("ID", Integer.toString(alarm.getId()));
            inputs.put("StartLocalTime", alarm.getStartTime());
            inputs.put("Duration", alarm.getDuration());
            inputs.put("Recurrence", alarm.getRecurrence());
            inputs.put("RoomUUID", alarm.getRoomUUID());
            inputs.put("ProgramURI", alarm.getProgramURI());
            inputs.put("ProgramMetaData", alarm.getProgramMetaData());
            inputs.put("PlayMode", alarm.getPlayMode());
            inputs.put("Volume", Integer.toString(alarm.getVolume()));
            if (alarm.getIncludeLinkedZones()) {
                inputs.put("IncludeLinkedZones", "1");
            } else {
                inputs.put("IncludeLinkedZones", "0");
            }

            if (alarm.getEnabled()) {
                inputs.put("Enabled", "1");
            } else {
                inputs.put("Enabled", "0");
            }
        } catch (NumberFormatException ex) {
            logger.debug("Action Invalid Value Format Exception {}", ex.getMessage());
        }

        executeAction(SERVICE_ALARM_CLOCK, "UpdateAlarm", inputs);
    }

    public void setAlarm(Command command) {
        if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {
            if (command.equals(OnOffType.ON) || command.equals(UpDownType.UP) || command.equals(OpenClosedType.OPEN)) {
                setAlarm(true);
            } else if (command.equals(OnOffType.OFF) || command.equals(UpDownType.DOWN)
                    || command.equals(OpenClosedType.CLOSED)) {
                setAlarm(false);
            }
        }
    }

    public void setAlarm(boolean alarmSwitch) {
        List<SonosAlarm> sonosAlarms = getCurrentAlarmList();

        // find the nearest alarm - take the current time from the Sonos system,
        // not the system where we are running
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));

        String currentLocalTime = getTime();
        Date currentDateTime = null;
        try {
            currentDateTime = fmt.parse(currentLocalTime);
        } catch (ParseException e) {
            logger.debug("An exception occurred while formatting a date", e);
        }

        if (currentDateTime != null) {
            Calendar currentDateTimeCalendar = Calendar.getInstance();
            currentDateTimeCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
            currentDateTimeCalendar.setTime(currentDateTime);
            currentDateTimeCalendar.add(Calendar.DAY_OF_YEAR, 10);
            long shortestDuration = currentDateTimeCalendar.getTimeInMillis() - currentDateTime.getTime();

            SonosAlarm firstAlarm = null;

            for (SonosAlarm anAlarm : sonosAlarms) {
                SimpleDateFormat durationFormat = new SimpleDateFormat("HH:mm:ss");
                durationFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date durationDate;
                try {
                    durationDate = durationFormat.parse(anAlarm.getDuration());
                } catch (ParseException e) {
                    logger.debug("An exception occurred while parsing a date : '{}'", e.getMessage());
                    continue;
                }

                long duration = durationDate.getTime();

                if (duration < shortestDuration && anAlarm.getRoomUUID().equals(getUDN())) {
                    shortestDuration = duration;
                    firstAlarm = anAlarm;
                }
            }

            // Set the Alarm
            if (firstAlarm != null) {
                if (alarmSwitch) {
                    firstAlarm.setEnabled(true);
                } else {
                    firstAlarm.setEnabled(false);
                }

                updateAlarm(firstAlarm);
            }
        }
    }

    public @Nullable String getTime() {
        updateTime();
        return stateMap.get("CurrentLocalTime");
    }

    public @Nullable String getAlarmRunning() {
        return stateMap.get("AlarmRunning");
    }

    public boolean isAlarmRunning() {
        return "1".equals(getAlarmRunning());
    }

    public void snoozeAlarm(Command command) {
        if (isAlarmRunning() && command instanceof DecimalType decimalCommand) {
            int minutes = decimalCommand.intValue();

            Map<String, String> inputs = new HashMap<>();

            Calendar snoozePeriod = Calendar.getInstance();
            snoozePeriod.setTimeZone(TimeZone.getTimeZone("GMT"));
            snoozePeriod.setTimeInMillis(0);
            snoozePeriod.add(Calendar.MINUTE, minutes);
            SimpleDateFormat pFormatter = new SimpleDateFormat("HH:mm:ss");
            pFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));

            try {
                inputs.put("Duration", pFormatter.format(snoozePeriod.getTime()));
            } catch (NumberFormatException ex) {
                logger.debug("Action Invalid Value Format Exception {}", ex.getMessage());
            }

            executeAction(SERVICE_AV_TRANSPORT, ACTION_SNOOZE_ALARM, inputs);
        } else {
            logger.debug("There is no alarm running on {}", getUDN());
        }
    }

    public @Nullable String getAnalogLineInConnected() {
        return stateMap.get(LINEINCONNECTED);
    }

    public boolean isAnalogLineInConnected() {
        return "true".equals(getAnalogLineInConnected());
    }

    public @Nullable String getOpticalLineInConnected() {
        return stateMap.get(TOSLINEINCONNECTED);
    }

    public boolean isOpticalLineInConnected() {
        return "true".equals(getOpticalLineInConnected());
    }

    public void becomeStandAlonePlayer() {
        executeAction(SERVICE_AV_TRANSPORT, ACTION_BECOME_COORDINATOR_OF_STANDALONE_GROUP, null);
    }

    public void addMember(Command command) {
        if (command instanceof StringType) {
            SonosEntry entry = new SonosEntry("", "", "", "", "", "", "", GROUP_URI + getUDN());
            try {
                getHandlerByName(command.toString()).setCurrentURI(entry);
            } catch (IllegalStateException e) {
                logger.debug("Cannot add group member ({})", e.getMessage());
            }
        }
    }

    public boolean publicAddress(LineInType lineInType) {
        // check if sourcePlayer has a line-in connected
        if ((lineInType != LineInType.DIGITAL && isAnalogLineInConnected())
                || (lineInType != LineInType.ANALOG && isOpticalLineInConnected())) {
            // first remove this player from its own group if any
            becomeStandAlonePlayer();

            // add all other players to this new group
            for (SonosZoneGroup group : getZoneGroups()) {
                for (String player : group.getMembers()) {
                    try {
                        ZonePlayerHandler somePlayer = getHandlerByName(player);
                        if (!somePlayer.equals(this)) {
                            somePlayer.becomeStandAlonePlayer();
                            somePlayer.stop();
                            addMember(StringType.valueOf(somePlayer.getUDN()));
                        }
                    } catch (IllegalStateException e) {
                        logger.debug("Cannot add to group ({})", e.getMessage());
                    }
                }
            }

            try {
                ZonePlayerHandler coordinator = getCoordinatorHandler();
                // set the URI of the group to the line-in
                SonosEntry entry = new SonosEntry("", "", "", "", "", "", "", ANALOG_LINE_IN_URI + getUDN());
                if (lineInType != LineInType.ANALOG && isOpticalLineInConnected()) {
                    entry = new SonosEntry("", "", "", "", "", "", "", OPTICAL_LINE_IN_URI + getUDN() + SPDIF);
                }
                coordinator.setCurrentURI(entry);
                coordinator.play();

                return true;
            } catch (IllegalStateException e) {
                logger.debug("Cannot handle command ({})", e.getMessage());
                return false;
            }
        } else {
            logger.debug("Line-in of {} is not connected", getUDN());
            return false;
        }
    }

    /**
     * Play a given url to music in one of the music libraries.
     *
     * @param command
     *            in the format of //host/folder/filename.mp3
     */
    public void playURI(Command command) {
        if (command instanceof StringType) {
            try {
                String url = command.toString();

                ZonePlayerHandler coordinator = getCoordinatorHandler();

                // stop whatever is currently playing
                coordinator.stop();
                coordinator.waitForNotTransportState(STATE_PLAYING);

                // clear any tracks which are pending in the queue
                coordinator.removeAllTracksFromQueue();

                // add the new track we want to play to the queue
                // The url will be prefixed with x-file-cifs if it is NOT a http URL
                if (!url.startsWith("x-") && (!url.startsWith("http"))) {
                    // default to file based url
                    url = FILE_URI + url;
                }
                coordinator.addURIToQueue(url, "", 0, true);

                // set the current playlist to our new queue
                coordinator.setCurrentURI(QUEUE_URI + coordinator.getUDN() + "#0", "");

                // take the system off mute
                coordinator.setMute(OnOffType.OFF);

                // start jammin'
                coordinator.play();
            } catch (IllegalStateException e) {
                logger.debug("Cannot play URI ({})", e.getMessage());
            } catch (InterruptedException e) {
                logger.debug("Play URI interrupted ({})", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    private void scheduleNotificationSound(final Command command) {
        scheduler.submit(() -> {
            synchronized (notificationLock) {
                playNotificationSoundURI(command);
            }
        });
    }

    /**
     * Play a given notification sound
     *
     * @param notificationURL in the format of //host/folder/filename.mp3
     */
    public void playNotificationSoundURI(Command notificationURL) {
        if (notificationURL instanceof StringType) {
            try {
                ZonePlayerHandler coordinator = getCoordinatorHandler();

                String currentURI = coordinator.getCurrentURI();
                logger.debug("playNotificationSoundURI: currentURI {} metadata {}", currentURI,
                        coordinator.getCurrentURIMetadataAsString());

                if (isPlayingStreamOrRadio(currentURI)) {
                    handleNotifForRadioStream(currentURI, notificationURL, coordinator);
                } else if (isPlayingLineIn(currentURI)) {
                    handleNotifForLineIn(currentURI, notificationURL, coordinator);
                } else if (isPlayingVirtualLineIn(currentURI)) {
                    handleNotifForVirtualLineIn(currentURI, notificationURL, coordinator);
                } else if (isPlayingQueue(currentURI)) {
                    handleNotifForSharedQueue(currentURI, notificationURL, coordinator);
                } else if (isPlaylistEmpty(coordinator)) {
                    handleNotifForEmptyQueue(notificationURL, coordinator);
                } else {
                    logger.debug("Notification feature not yet implemented while the current media is being played");
                }
                synchronized (notificationLock) {
                    notificationLock.notify();
                }
            } catch (IllegalStateException e) {
                logger.debug("Cannot play notification sound ({})", e.getMessage());
            } catch (InterruptedException e) {
                logger.debug("Play notification sound interrupted ({})", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean isPlaylistEmpty(ZonePlayerHandler coordinator) {
        return coordinator.getQueueSize() == 0;
    }

    private boolean isPlayingQueue(@Nullable String currentURI) {
        return currentURI != null && currentURI.contains(QUEUE_URI);
    }

    private boolean isPlayingStream(@Nullable String currentURI) {
        return currentURI != null && currentURI.contains(STREAM_URI);
    }

    private boolean isPlayingRadio(@Nullable String currentURI) {
        // Google Play Music radio or Apple Music radio
        return currentURI != null && currentURI.contains(RADIO_URI);
    }

    private boolean isPlayingRadioApp(@Nullable String currentURI) {
        // RadioApp music service
        return currentURI != null && currentURI.contains(RADIOAPP_URI);
    }

    private boolean isPlayingRadioStartedByAmazonEcho(@Nullable String currentURI) {
        return currentURI != null && currentURI.contains(RADIO_MP3_URI) && currentURI.contains(OPML_TUNE);
    }

    private boolean isPlayingStreamOrRadio(@Nullable String currentURI) {
        return isPlayingStream(currentURI) || isPlayingRadioStartedByAmazonEcho(currentURI)
                || isPlayingRadio(currentURI) || isPlayingRadioApp(currentURI);
    }

    private boolean isPlayingLineIn(@Nullable String currentURI) {
        return currentURI != null && (isPlayingAnalogLineIn(currentURI) || isPlayingOpticalLineIn(currentURI));
    }

    private boolean isPlayingAnalogLineIn(@Nullable String currentURI) {
        return currentURI != null && currentURI.contains(ANALOG_LINE_IN_URI);
    }

    private boolean isPlayingOpticalLineIn(@Nullable String currentURI) {
        return currentURI != null && currentURI.startsWith(OPTICAL_LINE_IN_URI) && currentURI.endsWith(SPDIF);
    }

    private boolean isPlayingVirtualLineIn(@Nullable String currentURI) {
        return currentURI != null && currentURI.startsWith(VIRTUAL_LINE_IN_URI);
    }

    /**
     * Does a chain of predefined actions when a Notification sound is played by
     * {@link ZonePlayerHandler#playNotificationSoundURI(Command)} in case
     * radio streaming is currently loaded
     *
     * @param currentStreamURI - the currently loaded stream's URI
     * @param notificationURL - the notification url in the format of //host/folder/filename.mp3
     * @param coordinator - {@link ZonePlayerHandler} coordinator for the SONOS device(s)
     * @throws InterruptedException
     */
    private void handleNotifForRadioStream(@Nullable String currentStreamURI, Command notificationURL,
            ZonePlayerHandler coordinator) throws InterruptedException {
        String nextAction = coordinator.getTransportState();
        SonosMetaData track = coordinator.getTrackMetadata();
        SonosMetaData currentUriMetaData = coordinator.getCurrentURIMetadata();

        handleNotificationSound(notificationURL, coordinator);
        if (currentStreamURI != null && track != null && currentUriMetaData != null) {
            coordinator.setCurrentURI(new SonosEntry("", currentUriMetaData.getTitle(), "", "", track.getAlbumArtUri(),
                    "", currentUriMetaData.getUpnpClass(), currentStreamURI));
            restoreLastTransportState(coordinator, nextAction);
        }
    }

    /**
     * Does a chain of predefined actions when a Notification sound is played by
     * {@link ZonePlayerHandler#playNotificationSoundURI(Command)} in case
     * line in is currently loaded
     *
     * @param currentLineInURI - the currently loaded line-in URI
     * @param notificationURL - the notification url in the format of //host/folder/filename.mp3
     * @param coordinator - {@link ZonePlayerHandler} coordinator for the SONOS device(s)
     * @throws InterruptedException
     */
    private void handleNotifForLineIn(@Nullable String currentLineInURI, Command notificationURL,
            ZonePlayerHandler coordinator) throws InterruptedException {
        logger.debug("Handling notification while sound from line-in was being played");
        String nextAction = coordinator.getTransportState();

        handleNotificationSound(notificationURL, coordinator);
        if (currentLineInURI != null) {
            logger.debug("Restoring sound from line-in using URI {}", currentLineInURI);
            coordinator.setCurrentURI(currentLineInURI, "");
            restoreLastTransportState(coordinator, nextAction);
        }
    }

    /**
     * Does a chain of predefined actions when a Notification sound is played by
     * {@link ZonePlayerHandler#playNotificationSoundURI(Command)} in case
     * virtual line in is currently loaded
     *
     * @param currentVirtualLineInURI - the currently loaded virtual line-in URI
     * @param notificationURL - the notification url in the format of //host/folder/filename.mp3
     * @param coordinator - {@link ZonePlayerHandler} coordinator for the SONOS device(s)
     * @throws InterruptedException
     */
    private void handleNotifForVirtualLineIn(@Nullable String currentVirtualLineInURI, Command notificationURL,
            ZonePlayerHandler coordinator) throws InterruptedException {
        logger.debug("Handling notification while sound from virtual line-in was being played");
        String nextAction = coordinator.getTransportState();
        String currentUriMetaData = coordinator.getCurrentURIMetadataAsString();

        handleNotificationSound(notificationURL, coordinator);
        if (currentVirtualLineInURI != null && currentUriMetaData != null) {
            logger.debug("Restoring sound from virtual line-in using URI {} and metadata {}", currentVirtualLineInURI,
                    currentUriMetaData);
            coordinator.setCurrentURI(currentVirtualLineInURI, currentUriMetaData);
            restoreLastTransportState(coordinator, nextAction);
        }
    }

    /**
     * Does a chain of predefined actions when a Notification sound is played by
     * {@link ZonePlayerHandler#playNotificationSoundURI(Command)} in case
     * shared queue is currently loaded
     *
     * @param currentQueueURI - the currently loaded queue URI
     * @param notificationURL - the notification url in the format of //host/folder/filename.mp3
     * @param coordinator - {@link ZonePlayerHandler} coordinator for the SONOS device(s)
     * @throws InterruptedException
     */
    private void handleNotifForSharedQueue(@Nullable String currentQueueURI, Command notificationURL,
            ZonePlayerHandler coordinator) throws InterruptedException {
        String nextAction = coordinator.getTransportState();
        String trackPosition = coordinator.getRefreshedPosition();
        long currentTrackNumber = coordinator.getRefreshedCurrenTrackNr();
        logger.debug(
                "Handling notification while playing queue: currentQueueURI {} trackPosition {} currentTrackNumber {}",
                currentQueueURI, trackPosition, currentTrackNumber);

        handleNotificationSound(notificationURL, coordinator);
        String queueUri = QUEUE_URI + coordinator.getUDN() + "#0";
        if (queueUri.equals(currentQueueURI)) {
            coordinator.setPositionTrack(currentTrackNumber);
            coordinator.setPosition(trackPosition);
            restoreLastTransportState(coordinator, nextAction);
        }
    }

    /**
     * Handle the execution of the notification sound by sequentially executing the required steps.
     *
     * @param notificationURL - the notification url in the format of //host/folder/filename.mp3
     * @param coordinator - {@link ZonePlayerHandler} coordinator for the SONOS device(s)
     * @throws InterruptedException
     */
    private void handleNotificationSound(Command notificationURL, ZonePlayerHandler coordinator)
            throws InterruptedException {
        boolean sourceStoppable = !isPlayingOpticalLineIn(coordinator.getCurrentURI());
        String originalVolume = (isAdHocGroup() || isStandalonePlayer()) ? getVolume() : coordinator.getVolume();
        if (sourceStoppable) {
            coordinator.stop();
            coordinator.waitForNotTransportState(STATE_PLAYING);
            applyNotificationSoundVolume();
        }
        long notificationPosition = coordinator.getQueueSize() + 1;
        coordinator.addURIToQueue(notificationURL.toString(), "", notificationPosition, false);
        coordinator.setCurrentURI(QUEUE_URI + coordinator.getUDN() + "#0", "");
        coordinator.setPositionTrack(notificationPosition);
        if (!sourceStoppable) {
            coordinator.stop();
            coordinator.waitForNotTransportState(STATE_PLAYING);
            applyNotificationSoundVolume();
        }
        coordinator.play();
        coordinator.waitForFinishedNotification();
        if (originalVolume != null) {
            setVolumeForGroup(DecimalType.valueOf(originalVolume));
        }
        coordinator.removeRangeOfTracksFromQueue(new StringType(Long.toString(notificationPosition) + ",1"));
    }

    private void restoreLastTransportState(ZonePlayerHandler coordinator, @Nullable String nextAction)
            throws InterruptedException {
        if (nextAction != null) {
            switch (nextAction) {
                case STATE_PLAYING:
                    coordinator.play();
                    coordinator.waitForTransportState(STATE_PLAYING);
                    break;
                case STATE_PAUSED_PLAYBACK:
                    coordinator.pause();
                    break;
            }
        }
    }

    /**
     * Does a chain of predefined actions when a Notification sound is played by
     * {@link ZonePlayerHandler#playNotificationSoundURI(Command)} in case
     * empty queue is currently loaded
     *
     * @param notificationURL - the notification url in the format of //host/folder/filename.mp3
     * @param coordinator - {@link ZonePlayerHandler} coordinator for the SONOS device(s)
     * @throws InterruptedException
     */
    private void handleNotifForEmptyQueue(Command notificationURL, ZonePlayerHandler coordinator)
            throws InterruptedException {
        String originalVolume = coordinator.getVolume();
        coordinator.applyNotificationSoundVolume();
        coordinator.playURI(notificationURL);
        coordinator.waitForFinishedNotification();
        coordinator.removeAllTracksFromQueue();
        if (originalVolume != null) {
            coordinator.setVolume(DecimalType.valueOf(originalVolume));
        }
    }

    /**
     * Applies the notification sound volume level to the group (if not null)
     *
     * @param coordinator - {@link ZonePlayerHandler} coordinator for the SONOS device(s)
     */
    private void applyNotificationSoundVolume() {
        setNotificationSoundVolume(getNotificationSoundVolume());
    }

    private void waitForFinishedNotification() throws InterruptedException {
        waitForTransportState(STATE_PLAYING);

        // check Sonos state events to determine the end of the notification sound
        String notificationTitle = getCurrentTitle();
        long playstart = System.currentTimeMillis();
        while (System.currentTimeMillis() - playstart < (long) configuration.notificationTimeout * 1000) {
            Thread.sleep(50);
            String currentTitle = getCurrentTitle();
            if ((notificationTitle == null && currentTitle != null)
                    || (notificationTitle != null && !notificationTitle.equals(currentTitle))
                    || !STATE_PLAYING.equals(getTransportState())) {
                break;
            }
        }
    }

    private void waitForTransportState(String state) throws InterruptedException {
        if (getTransportState() != null) {
            long start = System.currentTimeMillis();
            while (!state.equals(getTransportState())) {
                Thread.sleep(50);
                if (System.currentTimeMillis() - start > (long) configuration.notificationTimeout * 1000) {
                    break;
                }
            }
        }
    }

    private void waitForNotTransportState(String state) throws InterruptedException {
        if (getTransportState() != null) {
            long start = System.currentTimeMillis();
            while (state.equals(getTransportState())) {
                Thread.sleep(50);
                if (System.currentTimeMillis() - start > (long) configuration.notificationTimeout * 1000) {
                    break;
                }
            }
        }
    }

    /**
     * Removes a range of tracks from the queue.
     * ({@code <x,y>} will remove y songs started by the song number x)
     *
     * @param command - must be in the format {@code <startIndex, numberOfSongs>}
     */
    public void removeRangeOfTracksFromQueue(Command command) {
        if (command instanceof StringType) {
            String[] rangeInputSplit = command.toString().split(",");
            // If range input is incorrect, remove the first song by default
            String startIndex = rangeInputSplit[0] != null ? rangeInputSplit[0] : "1";
            String numberOfTracks = rangeInputSplit[1] != null ? rangeInputSplit[1] : "1";
            executeAction(SERVICE_AV_TRANSPORT, ACTION_REMOVE_TRACK_RANGE_FROM_QUEUE,
                    Map.of("InstanceID", "0", "StartingIndex", startIndex, "NumberOfTracks", numberOfTracks));
        }
    }

    public void clearQueue() {
        try {
            ZonePlayerHandler coordinator = getCoordinatorHandler();

            coordinator.removeAllTracksFromQueue();
        } catch (IllegalStateException e) {
            logger.debug("Cannot clear queue ({})", e.getMessage());
        }
    }

    public void playQueue() {
        try {
            ZonePlayerHandler coordinator = getCoordinatorHandler();

            // set the current playlist to our new queue
            coordinator.setCurrentURI(QUEUE_URI + coordinator.getUDN() + "#0", "");

            // take the system off mute
            coordinator.setMute(OnOffType.OFF);

            // start jammin'
            coordinator.play();
        } catch (IllegalStateException e) {
            logger.debug("Cannot play queue ({})", e.getMessage());
        }
    }

    public void setLed(Command command) {
        if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {
            String value = (command.equals(OnOffType.ON) || command.equals(UpDownType.UP)
                    || command.equals(OpenClosedType.OPEN)) ? "On" : "Off";
            executeAction(SERVICE_DEVICE_PROPERTIES, ACTION_SET_LED_STATE, Map.of("DesiredLEDState", value));
            executeAction(SERVICE_DEVICE_PROPERTIES, ACTION_GET_LED_STATE, null);
        }
    }

    public void removeMember(Command command) {
        if (command instanceof StringType) {
            try {
                ZonePlayerHandler oldmemberHandler = getHandlerByName(command.toString());

                oldmemberHandler.becomeStandAlonePlayer();
                SonosEntry entry = new SonosEntry("", "", "", "", "", "", "",
                        QUEUE_URI + oldmemberHandler.getUDN() + "#0");
                oldmemberHandler.setCurrentURI(entry);
            } catch (IllegalStateException e) {
                logger.debug("Cannot remove group member ({})", e.getMessage());
            }
        }
    }

    public void previous() {
        executeAction(SERVICE_AV_TRANSPORT, ACTION_PREVIOUS, null);
    }

    public void next() {
        executeAction(SERVICE_AV_TRANSPORT, ACTION_NEXT, null);
    }

    public void stopPlaying(Command command) {
        if (command instanceof OnOffType) {
            try {
                getCoordinatorHandler().stop();
            } catch (IllegalStateException e) {
                logger.debug("Cannot handle stop command ({})", e.getMessage(), e);
            }
        }
    }

    public void playRadio(Command command) {
        if (command instanceof StringType) {
            String station = command.toString();
            List<SonosEntry> stations = getFavoriteRadios();

            SonosEntry theEntry = null;
            // search for the appropriate radio based on its name (title)
            for (SonosEntry someStation : stations) {
                if (someStation.getTitle().equals(station)) {
                    theEntry = someStation;
                    break;
                }
            }

            // set the URI of the group coordinator
            if (theEntry != null) {
                try {
                    ZonePlayerHandler coordinator = getCoordinatorHandler();
                    coordinator.setCurrentURI(theEntry);
                    coordinator.play();
                } catch (IllegalStateException e) {
                    logger.debug("Cannot play radio ({})", e.getMessage());
                }
            } else {
                logger.debug("Radio station '{}' not found", station);
            }
        }
    }

    public void playTuneinStation(Command command) {
        if (command instanceof StringType) {
            String stationId = command.toString();
            List<SonosMusicService> allServices = getAvailableMusicServices();

            SonosMusicService tuneinService = null;
            // search for the TuneIn music service based on its name
            if (allServices != null) {
                for (SonosMusicService service : allServices) {
                    if ("TuneIn".equals(service.getName())) {
                        tuneinService = service;
                        break;
                    }
                }
            }

            // set the URI of the group coordinator
            if (tuneinService != null) {
                try {
                    ZonePlayerHandler coordinator = getCoordinatorHandler();
                    SonosEntry entry = new SonosEntry("", "TuneIn station", "", "", "", "",
                            "object.item.audioItem.audioBroadcast",
                            String.format(TUNEIN_URI, stationId, tuneinService.getId()));
                    Integer tuneinServiceType = tuneinService.getType();
                    int serviceTypeNum = tuneinServiceType == null ? TUNEIN_DEFAULT_SERVICE_TYPE : tuneinServiceType;
                    entry.setDesc("SA_RINCON" + Integer.toString(serviceTypeNum) + "_");
                    coordinator.setCurrentURI(entry);
                    coordinator.play();
                } catch (IllegalStateException e) {
                    logger.debug("Cannot play TuneIn station {} ({})", stationId, e.getMessage());
                }
            } else {
                logger.debug("TuneIn service not found");
            }
        }
    }

    private @Nullable List<SonosMusicService> getAvailableMusicServices() {
        if (musicServices == null) {
            Map<String, String> result = service.invokeAction(this, "MusicServices", "ListAvailableServices", null);

            String serviceList = result.get("AvailableServiceDescriptorList");
            if (serviceList != null) {
                List<SonosMusicService> services = SonosXMLParser.getMusicServicesFromXML(serviceList);
                musicServices = services;

                String[] servicesTypes = new String[0];
                String serviceTypeList = result.get("AvailableServiceTypeList");
                if (serviceTypeList != null) {
                    // It is a comma separated list of service types (integers) in the same order as the services
                    // declaration in "AvailableServiceDescriptorList" except that there is no service type for the
                    // TuneIn service
                    servicesTypes = serviceTypeList.split(",");
                }

                int idx = 0;
                for (SonosMusicService service : services) {
                    if (!"TuneIn".equals(service.getName())) {
                        // Add the service type integer value from "AvailableServiceTypeList" to each service
                        // except TuneIn
                        if (idx < servicesTypes.length) {
                            try {
                                Integer serviceType = Integer.parseInt(servicesTypes[idx]);
                                service.setType(serviceType);
                            } catch (NumberFormatException e) {
                            }
                            idx++;
                        }
                    } else {
                        service.setType(TUNEIN_DEFAULT_SERVICE_TYPE);
                    }
                    logger.debug("Service name {} => id {} type {}", service.getName(), service.getId(),
                            service.getType());
                }
            }
        }
        return musicServices;
    }

    /**
     * This will attempt to match the station string with an entry in the
     * favorites list, this supports both single entries and playlists
     *
     * @param command favorite to match
     */
    public void playFavorite(Command command) {
        if (command instanceof StringType) {
            String favorite = command.toString();
            List<SonosEntry> favorites = getFavorites();

            SonosEntry theEntry = null;
            // search for the appropriate favorite based on its name (title)
            for (SonosEntry entry : favorites) {
                if (entry.getTitle().equals(favorite)) {
                    theEntry = entry;
                    break;
                }
            }

            // set the URI of the group coordinator
            if (theEntry != null) {
                try {
                    ZonePlayerHandler coordinator = getCoordinatorHandler();

                    /**
                     * If this is a playlist we need to treat it as such
                     */
                    SonosResourceMetaData resourceMetaData = theEntry.getResourceMetaData();
                    if (resourceMetaData != null && resourceMetaData.getUpnpClass().startsWith("object.container")) {
                        coordinator.removeAllTracksFromQueue();
                        coordinator.addURIToQueue(theEntry);
                        coordinator.setCurrentURI(QUEUE_URI + coordinator.getUDN() + "#0", "");
                        String firstTrackNumberEnqueued = stateMap.get("FirstTrackNumberEnqueued");
                        coordinator.seek("TRACK_NR", firstTrackNumberEnqueued);
                    } else {
                        coordinator.setCurrentURI(theEntry);
                    }
                    coordinator.play();
                } catch (IllegalStateException e) {
                    logger.debug("Cannot paly favorite ({})", e.getMessage());
                }
            } else {
                logger.debug("Favorite '{}' not found", favorite);
            }
        }
    }

    public void playTrack(Command command) {
        if (command instanceof DecimalType decimalCommand) {
            try {
                ZonePlayerHandler coordinator = getCoordinatorHandler();

                String trackNumber = String.valueOf(decimalCommand.intValue());

                coordinator.setCurrentURI(QUEUE_URI + coordinator.getUDN() + "#0", "");

                // seek the track - warning, we do not check if the tracknumber falls in the boundary of the queue
                coordinator.setPositionTrack(trackNumber);

                // take the system off mute
                coordinator.setMute(OnOffType.OFF);

                // start jammin'
                coordinator.play();
            } catch (IllegalStateException e) {
                logger.debug("Cannot play track ({})", e.getMessage());
            }
        }
    }

    public void playPlayList(Command command) {
        if (command instanceof StringType) {
            String playlist = command.toString();
            List<SonosEntry> playlists = getPlayLists();

            SonosEntry theEntry = null;
            // search for the appropriate play list based on its name (title)
            for (SonosEntry somePlaylist : playlists) {
                if (somePlaylist.getTitle().equals(playlist)) {
                    theEntry = somePlaylist;
                    break;
                }
            }

            // set the URI of the group coordinator
            if (theEntry != null) {
                try {
                    ZonePlayerHandler coordinator = getCoordinatorHandler();

                    coordinator.addURIToQueue(theEntry);

                    coordinator.setCurrentURI(QUEUE_URI + coordinator.getUDN() + "#0", "");

                    String firstTrackNumberEnqueued = stateMap.get("FirstTrackNumberEnqueued");
                    coordinator.seek("TRACK_NR", firstTrackNumberEnqueued);

                    coordinator.play();
                } catch (IllegalStateException e) {
                    logger.debug("Cannot play playlist ({})", e.getMessage());
                }
            } else {
                logger.debug("Playlist '{}' not found", playlist);
            }
        }
    }

    public void addURIToQueue(SonosEntry newEntry) {
        addURIToQueue(newEntry.getRes(), SonosXMLParser.compileMetadataString(newEntry), 1, true);
    }

    public @Nullable String getZoneName() {
        return stateMap.get("ZoneName");
    }

    public @Nullable String getZoneGroupID() {
        return stateMap.get("LocalGroupUUID");
    }

    public @Nullable String getRunningAlarmProperties() {
        return stateMap.get("RunningAlarmProperties");
    }

    public @Nullable String getRefreshedRunningAlarmProperties() {
        updateRunningAlarmProperties();
        return getRunningAlarmProperties();
    }

    public @Nullable String getMute() {
        return stateMap.get("MuteMaster");
    }

    public @Nullable String getLed() {
        return stateMap.get("CurrentLEDState");
    }

    public @Nullable String getCurrentZoneName() {
        return stateMap.get("CurrentZoneName");
    }

    public @Nullable String getRefreshedCurrentZoneName() {
        updateCurrentZoneName();
        return getCurrentZoneName();
    }

    @Override
    public void onStatusChanged(boolean status) {
        if (status) {
            logger.info("UPnP device {} is present (thing {})", getUDN(), getThing().getUID());
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
                scheduler.execute(this::poll);
            }
        } else {
            logger.info("UPnP device {} is absent (thing {})", getUDN(), getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR);
        }
    }

    private @Nullable String getModelNameFromDescriptor() {
        URL descriptor = service.getDescriptorURL(this);
        if (descriptor != null) {
            String sonosModelDescription = SonosXMLParser.parseModelDescription(descriptor);
            return sonosModelDescription == null ? null
                    : SonosXMLParser.buildThingTypeIdFromModelName(sonosModelDescription);
        } else {
            return null;
        }
    }

    private boolean migrateThingType() {
        if (getThing().getThingTypeUID().equals(ZONEPLAYER_THING_TYPE_UID)) {
            String modelName = getModelNameFromDescriptor();
            if (modelName != null && isSupportedModel(modelName)) {
                updateSonosThingType(modelName);
                return true;
            }
        }
        return false;
    }

    private boolean isSupportedModel(String modelName) {
        for (ThingTypeUID thingTypeUID : SUPPORTED_KNOWN_THING_TYPES_UIDS) {
            if (thingTypeUID.getId().equalsIgnoreCase(modelName)) {
                return true;
            }
        }
        return false;
    }

    private void updateSonosThingType(String newThingTypeID) {
        changeThingType(new ThingTypeUID(SonosBindingConstants.BINDING_ID, newThingTypeID), getConfig());
    }

    /*
     * Set the sleeptimer duration
     * Use String command of format "HH:MM:SS" to set the timer to the desired duration
     * Use empty String "" to switch the sleep timer off
     */
    public void setSleepTimer(Command command) {
        if (command instanceof DecimalType decimalCommand) {
            this.service.invokeAction(this, SERVICE_AV_TRANSPORT, ACTION_CONFIGURE_SLEEP_TIMER, Map.of("InstanceID",
                    "0", "NewSleepTimerDuration", sleepSecondsToTimeStr(decimalCommand.longValue())));
        }
    }

    protected void updateSleepTimerDuration() {
        executeAction(SERVICE_AV_TRANSPORT, ACTION_GET_REMAINING_SLEEP_TIMER_DURATION, null);
    }

    private String sleepSecondsToTimeStr(long sleepSeconds) {
        if (sleepSeconds == 0) {
            return "";
        } else if (sleepSeconds < 68400) {
            long remainingSeconds = sleepSeconds;
            long hours = TimeUnit.SECONDS.toHours(remainingSeconds);
            remainingSeconds -= TimeUnit.HOURS.toSeconds(hours);
            long minutes = TimeUnit.SECONDS.toMinutes(remainingSeconds);
            remainingSeconds -= TimeUnit.MINUTES.toSeconds(minutes);
            long seconds = TimeUnit.SECONDS.toSeconds(remainingSeconds);
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            logger.debug("Sonos SleepTimer: Invalid sleep time set. sleep time must be >=0 and < 68400s (24h)");
            return "ERR";
        }
    }

    private long sleepStrTimeToSeconds(String sleepTime) {
        String[] units = sleepTime.split(":");
        int hours = Integer.parseInt(units[0]);
        int minutes = Integer.parseInt(units[1]);
        int seconds = Integer.parseInt(units[2]);
        return 3600 * hours + 60 * minutes + seconds;
    }

    private @Nullable String extractInfoFromMoreInfo(String searchedInfo) {
        String value = stateMap.get("MoreInfo");
        if (value != null) {
            String[] fields = value.split(",");
            for (int i = 0; i < fields.length; i++) {
                String[] pair = fields[i].trim().split(":");
                if (pair.length == 2 && searchedInfo.equalsIgnoreCase(pair[0].trim())) {
                    return pair[1].trim();
                }
            }
        }
        return null;
    }
}
