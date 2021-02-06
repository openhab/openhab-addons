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
package org.openhab.binding.yamahamusiccast.internal;

import static org.openhab.binding.yamahamusiccast.internal.YamahaMusiccastBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.yamahamusiccast.internal.dto.DeviceInfo;
import org.openhab.binding.yamahamusiccast.internal.dto.DistributionInfo;
import org.openhab.binding.yamahamusiccast.internal.dto.Features;
import org.openhab.binding.yamahamusiccast.internal.dto.PlayInfo;
import org.openhab.binding.yamahamusiccast.internal.dto.PresetInfo;
import org.openhab.binding.yamahamusiccast.internal.dto.RecentInfo;
import org.openhab.binding.yamahamusiccast.internal.dto.Response;
import org.openhab.binding.yamahamusiccast.internal.dto.Status;
import org.openhab.binding.yamahamusiccast.internal.dto.UdpMessage;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link YamahaMusiccastHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Lennert Coopman - Initial contribution
 */
@NonNullByDefault
public class YamahaMusiccastHandler extends BaseThingHandler {
    private Gson gson = new Gson();
    private Logger logger = LoggerFactory.getLogger(YamahaMusiccastHandler.class);
    private @Nullable ScheduledFuture<?> keepUdpEventsAliveTask;
    private @Nullable ScheduledFuture<?> generalHousekeepingTask;
    private @Nullable YamahaMusiccastConfiguration config;
    private @Nullable String httpResponse;

    private @Nullable JsonParser parser = new JsonParser();
    @Nullable
    String tmpString = "";
    int tmpInteger = 0;
    int volumePercent = 0;
    int volumeAbsValue = 0;
    int connectionTimeout = 5000;
    int longConnectionTimeout = 60000;
    @Nullable
    String responseCode = "";
    @Nullable
    String powerState = "";
    @Nullable
    String muteState = "";
    int volumeState = 0;
    int maxVolumeState = 0;
    @Nullable
    String inputState = "";
    int presetNumber = 0;
    @Nullable
    String soundProgramState = "";
    int sleepState = 0;
    @Nullable
    String playbackState = "";
    @Nullable
    String artistState = "";
    @Nullable
    String trackState = "";
    @Nullable
    String albumState = "";
    @Nullable
    String albumArtUrlState = "";
    @Nullable
    String repeatState = "";
    @Nullable
    String shuffleState = "";
    int playTimeState = 0;
    int totalTimeState = 0;
    @Nullable
    String topicAVR = "";
    @Nullable
    String zone = "main";
    String channelWithoutGroup = "";
    @Nullable
    String thingLabel = "";
    @Nullable
    String mclinkSetupServer = "";
    @Nullable
    String mclinkSetupZone = "";
    String url = "";
    String json = "";
    String action = "";

    int zoneNum = 0;
    @Nullable
    String groupId = "";
    @Nullable
    String role = "";
    @Nullable
    String host;
    @Nullable
    public String deviceId = "";

    private YamahaMusiccastStateDescriptionProvider stateDescriptionProvider;

    public YamahaMusiccastHandler(Thing thing, YamahaMusiccastStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command != RefreshType.REFRESH) {
            logger.debug("Handling command {} for channel {}", command, channelUID);
            channelWithoutGroup = channelUID.getIdWithoutGroup();
            zone = channelUID.getGroupId();
            DistributionInfo distributioninfo = new DistributionInfo();
            Response response = new Response();
            switch (channelWithoutGroup) {
                case CHANNEL_POWER:
                    if (command == OnOffType.ON) {
                        httpResponse = setPower("on", zone);
                        response = gson.fromJson(httpResponse, Response.class);
                        tmpString = response.getResponseCode();
                        if (!tmpString.equals("0")) {
                            updateState(channelUID, OnOffType.OFF);
                        }
                    } else if (command == OnOffType.OFF) {
                        httpResponse = setPower("standby", zone);
                        response = gson.fromJson(httpResponse, Response.class);
                        tmpString = response.getResponseCode();
                        if (!tmpString.equals("0")) {
                            updateState(channelUID, OnOffType.ON);
                        }
                    }
                    break;
                case CHANNEL_MUTE:
                    if (command == OnOffType.ON) {
                        httpResponse = setMute("true", zone);
                        response = gson.fromJson(httpResponse, Response.class);
                        tmpString = response.getResponseCode();
                        if (!tmpString.equals("0")) {
                            updateState(channelUID, OnOffType.OFF);
                        }
                    } else if (command == OnOffType.OFF) {
                        httpResponse = setMute("false", zone);
                        response = gson.fromJson(httpResponse, Response.class);
                        tmpString = response.getResponseCode();
                        if (!tmpString.equals("0")) {
                            updateState(channelUID, OnOffType.ON);
                        }
                    }
                    break;
                case CHANNEL_VOLUME:
                    volumePercent = Integer.parseInt(command.toString().replace(".0", ""));
                    volumeAbsValue = (maxVolumeState * volumePercent) / 100;
                    setVolume(volumeAbsValue, zone, this.host);
                    if (config.syncVolume) {
                        tmpString = getDistributionInfo(this.host);
                        distributioninfo = gson.fromJson(tmpString, DistributionInfo.class);
                        role = distributioninfo.getRole();
                        if (role.equals("server")) {
                            for (JsonElement ip : distributioninfo.getClientList()) {
                                JsonObject clientObject = ip.getAsJsonObject();
                                setVolumeLinkedDevice(volumePercent, zone,
                                        clientObject.get("ip_address").getAsString());
                            }
                        }
                    } // END config.syncVolume
                    break;
                case CHANNEL_VOLUMEABS:
                    volumeAbsValue = Integer.parseInt(command.toString().replace(".0", ""));
                    volumePercent = (volumeAbsValue / maxVolumeState) * 100;
                    setVolume(volumeAbsValue, zone, this.host);
                    if (config.syncVolume) {
                        tmpString = getDistributionInfo(this.host);
                        distributioninfo = gson.fromJson(tmpString, DistributionInfo.class);
                        role = distributioninfo.getRole();
                        if (role.equals("server")) {
                            for (JsonElement ip : distributioninfo.getClientList()) {
                                JsonObject clientObject = ip.getAsJsonObject();
                                setVolumeLinkedDevice(volumePercent, zone,
                                        clientObject.get("ip_address").getAsString());
                            }
                        }
                    }
                    break;
                case CHANNEL_INPUT:
                    // if it is a client, disconnect it first.
                    tmpString = getDistributionInfo(this.host);
                    distributioninfo = gson.fromJson(tmpString, DistributionInfo.class);
                    role = distributioninfo.getRole();
                    if (role.equals("client")) {
                        json = "{\"group_id\":\"\"}";
                        httpResponse = setClientInfo(this.host, json);
                    }
                    setInput(command.toString(), zone);
                    break;
                case CHANNEL_SOUNDPROGRAM:
                    setSoundProgram(command.toString(), zone);
                    break;
                case CHANNEL_SELECTPRESET:
                    setPreset(command.toString(), zone);
                    break;
                case CHANNEL_PLAYER:
                    if (command.equals(PlayPauseType.PLAY)) {
                        setPlayback("play");
                    } else if (command.equals(PlayPauseType.PAUSE)) {
                        setPlayback("pause");
                    } else if (command.equals(NextPreviousType.NEXT)) {
                        setPlayback("next");
                    } else if (command.equals(NextPreviousType.PREVIOUS)) {
                        setPlayback("previous");
                    } else if (command.equals(RewindFastforwardType.REWIND)) {
                        setPlayback("fast_reverse_start");
                    } else if (command.equals(RewindFastforwardType.FASTFORWARD)) {
                        setPlayback("fast_forward_end");
                    }
                    break;
                case CHANNEL_SLEEP:
                    setSleep(command.toString(), zone);
                    break;
                case CHANNEL_MCSERVER:
                    action = "";
                    json = "";
                    if (command.toString().equals("")) {
                        action = "unlink";
                        role = "";
                        groupId = "";
                    } else {
                        action = "link";
                        String[] parts = command.toString().split("\\*\\*\\*");
                        if (parts.length > 1) {
                            mclinkSetupServer = parts[0];
                            mclinkSetupZone = parts[1];
                            tmpString = getDistributionInfo(mclinkSetupServer);
                            distributioninfo = gson.fromJson(tmpString, DistributionInfo.class);
                            responseCode = distributioninfo.getResponseCode();

                            role = distributioninfo.getRole();
                            if (role.equals("server")) {
                                groupId = distributioninfo.getGroupId();
                            } else if (role.equals("client")) {
                                groupId = "";
                            } else if (role.equals("none")) {
                                groupId = generateGroupId();
                            }
                        }
                    }

                    if (action.equals("unlink")) {
                        json = "{\"group_id\":\"\"}";
                        httpResponse = setClientInfo(this.host, json);
                    } else if (action.equals("link")) {
                        json = "{\"group_id\":\"" + groupId + "\", \"zone\":\"" + mclinkSetupZone
                                + "\", \"type\":\"add\", \"client_list\":[\"" + this.host + "\"]}";
                        logger.debug("setServerInfo json: {}", json);
                        httpResponse = setServerInfo(mclinkSetupServer, json);
                        // All zones of Model are required for MC Link
                        tmpString = "";
                        for (int i = 1; i <= zoneNum; i++) {
                            switch (i) {
                                case 1:
                                    tmpString = "\"main\"";
                                    break;
                                case 2:
                                    tmpString = tmpString + ", \"zone2\"";
                                    break;
                                case 3:
                                    tmpString = tmpString + ", \"zone3\"";
                                    break;
                                case 4:
                                    tmpString = tmpString + ", \"zone4\"";
                                    break;
                            }
                        }
                        json = "{\"group_id\":\"" + groupId + "\", \"zone\":[" + tmpString + "]}";
                        logger.debug("setClientInfo json: {}", json);
                        httpResponse = setClientInfo(this.host, json);
                        httpResponse = startDistribution(mclinkSetupServer);
                    }
                    break;
                case CHANNEL_UNLINKMCSERVER:
                    if (command.equals(OnOffType.ON)) {
                        json = "{\"group_id\":\"\"}";
                        httpResponse = setServerInfo(this.host, json);
                        updateState(channelUID, OnOffType.OFF);
                    }
                    break;
                case CHANNEL_RECALLSCENE:
                    recallScene(command.toString(), zone);
                    break;
                case CHANNEL_REPEAT:
                    setRepeat(command.toString());
                    break;
                case CHANNEL_SHUFFLE:
                    setShuffle(command.toString());
                    break;
            } // END Switch Channel
        }
    }

    @Override
    public void initialize() {
        thingLabel = thing.getLabel();
        logger.debug("Start initializing! - {}", thingLabel);
        this.config = getConfigAs(YamahaMusiccastConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        if (config.host != null) {
            this.host = config.host;
        }
        if (!this.host.equals("")) {
            zoneNum = getNumberOfZones(this.host);
            logger.debug("Zones found: {} - {}", zoneNum, thingLabel);

            if (zoneNum > 0) {
                refreshOnStartup();
                generalHousekeepingTask = scheduler.scheduleWithFixedDelay(this::generalHousekeeping, 5, 300,
                        TimeUnit.SECONDS);
                logger.debug("Start Keep Alive UDP events (5 minutes - {}) ", thingLabel);

                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
                logger.debug("No host found");
            }
        }
    }

    private void generalHousekeeping() {
        keepUdpEventsAlive();
        fillOptionsForMCLink();
        updateMCLinkStatus();
    }

    private void refreshOnStartup() {
        for (int i = 1; i <= zoneNum; i++) {
            switch (i) {
                case 1:
                    updateStatusZone("main");
                    break;
                case 2:
                    updateStatusZone("zone2");
                    break;
                case 3:
                    updateStatusZone("zone3");
                    break;
                case 4:
                    updateStatusZone("zone4");
                    break;
            }
        }
        updatePresets(0);
        updateNetUSBPlayer();
        fillOptionsForMCLink();
        updateMCLinkStatus();
    }

    @Override
    public void dispose() {
        if (generalHousekeepingTask != null) {
            generalHousekeepingTask.cancel(true);
        }
    }

    // Various functions
    public void processUDPEvent(String json, String trackingID) {
        logger.debug("UDP package: {} (Tracking: {})", json, trackingID);
        @Nullable
        UdpMessage targetObject = gson.fromJson(json, UdpMessage.class);
        String jsonMain;
        String jsonZone2;
        String jsonZone3;
        String jsonZone4;
        String netUsb;
        String dist;

        if (Objects.nonNull(targetObject.getMain())) {
            updateStateFromUDPEvent("main", targetObject);
        }
        if (Objects.nonNull(targetObject.getZone2())) {
            updateStateFromUDPEvent("zone2", targetObject);
        }
        if (Objects.nonNull(targetObject.getZone3())) {
            updateStateFromUDPEvent("zone3", targetObject);
        }
        if (Objects.nonNull(targetObject.getZone4())) {
            updateStateFromUDPEvent("zone4", targetObject);
        }
        if (Objects.nonNull(targetObject.getNetUSB())) {
            updateStateFromUDPEvent("netusb", targetObject);
        }
        if (Objects.nonNull(targetObject.getDist())) {
            updateStateFromUDPEvent("dist", targetObject);
        }
    }

    private void updateStateFromUDPEvent(String zoneToUpdate, UdpMessage targetObject) {
        ChannelUID channel;
        String playInfoUpdated = "";
        String statusUpdated = "";
        String powerState = "";
        String muteState = "";
        String inputState = "";
        int volumeState = 0;
        int presetNumber = 0;
        int playTime = 0;
        String distInfoUpdated = "";
        logger.debug("Handling UDP for {}", zoneToUpdate);
        switch (zoneToUpdate) {
            case "main":
                powerState = targetObject.getMain().getPower();
                muteState = targetObject.getMain().getMute();
                inputState = targetObject.getMain().getInput();
                volumeState = targetObject.getMain().getVolume();
                statusUpdated = targetObject.getMain().getstatusUpdated();
                break;
            case "zone2":
                powerState = targetObject.getZone2().getPower();
                muteState = targetObject.getZone2().getMute();
                inputState = targetObject.getZone2().getInput();
                volumeState = targetObject.getZone2().getVolume();
                statusUpdated = targetObject.getZone2().getstatusUpdated();
                break;
            case "zone3":
                powerState = targetObject.getZone3().getPower();
                muteState = targetObject.getZone3().getMute();
                inputState = targetObject.getZone3().getInput();
                volumeState = targetObject.getZone3().getVolume();
                statusUpdated = targetObject.getZone3().getstatusUpdated();
                break;
            case "zone4":
                powerState = targetObject.getZone4().getPower();
                muteState = targetObject.getZone4().getMute();
                inputState = targetObject.getZone4().getInput();
                volumeState = targetObject.getZone4().getVolume();
                statusUpdated = targetObject.getZone4().getstatusUpdated();
                break;
            case "netusb":
                if (Objects.isNull(targetObject.getNetUSB().getPresetControl())) {
                    presetNumber = 0;
                } else {
                    presetNumber = targetObject.getNetUSB().getPresetControl().getNum();
                }
                playInfoUpdated = targetObject.getNetUSB().getPlayInfoUpdated();
                playTime = targetObject.getNetUSB().getPlayTime();
                // totalTime is not in UDP event
                break;
            case "dist":
                distInfoUpdated = targetObject.getDist().getDistInfoUpdated();
                break;
        }

        if (!powerState.isEmpty()) {
            channel = new ChannelUID(getThing().getUID(), zoneToUpdate, CHANNEL_POWER);
            if (isLinked(channel)) {
                if (powerState.equals("on")) {
                    updateState(channel, OnOffType.ON);
                } else if (powerState.equals("standby")) {
                    updateState(channel, OnOffType.OFF);
                }
            }
        }

        if (!muteState.isEmpty()) {
            channel = new ChannelUID(getThing().getUID(), zoneToUpdate, CHANNEL_MUTE);
            if (isLinked(channel)) {
                if (muteState.equals("true")) {
                    updateState(channel, OnOffType.ON);
                } else if (muteState.equals("false")) {
                    updateState(channel, OnOffType.OFF);
                }
            }
        }

        if (!inputState.isEmpty()) {
            channel = new ChannelUID(getThing().getUID(), zoneToUpdate, CHANNEL_INPUT);
            if (isLinked(channel)) {
                updateState(channel, StringType.valueOf(inputState));
            }
        }

        if (volumeState != 0) {
            channel = new ChannelUID(getThing().getUID(), zoneToUpdate, CHANNEL_VOLUME);
            if (isLinked(channel)) {
                updateState(channel, new PercentType((volumeState * 100) / maxVolumeState));
            }
            channel = new ChannelUID(getThing().getUID(), zoneToUpdate, CHANNEL_VOLUMEABS);
            if (isLinked(channel)) {
                updateState(channel, new DecimalType(volumeState));
            }
        }

        if (presetNumber != 0) {
            logger.debug("Preset detected: {}", presetNumber);
            updatePresets(presetNumber);
        }

        if (playInfoUpdated.equals("true")) {
            updateNetUSBPlayer();
        }

        if (!statusUpdated.isEmpty()) {
            updateStatusZone(zoneToUpdate);
        }
        if (playTime != 0) {
            channel = new ChannelUID(getThing().getUID(), "playerControls", CHANNEL_PLAYTIME);
            if (isLinked(channel)) {
                updateState(channel, StringType.valueOf(String.valueOf(playTime)));
            }
        }
        if (distInfoUpdated.equals("true")) {
            updateMCLinkStatus();
        }
    }

    private void updateStatusZone(String zoneToUpdate) {
        tmpString = getStatus(this.host, zoneToUpdate);
        @Nullable
        Status targetObject = gson.fromJson(tmpString, Status.class);
        responseCode = targetObject.getResponseCode();
        powerState = targetObject.getPower();
        muteState = targetObject.getMute();
        volumeState = targetObject.getVolume();
        maxVolumeState = targetObject.getMaxVolume();
        inputState = targetObject.getInput();
        soundProgramState = targetObject.getSoundProgram();
        sleepState = targetObject.getSleep();

        logger.debug("{} - Response: {}", zoneToUpdate, responseCode);
        logger.debug("{} - Power: {}", zoneToUpdate, powerState);
        logger.debug("{} - Mute: {}", zoneToUpdate, muteState);
        logger.debug("{} - Volume: {}", zoneToUpdate, volumeState);
        logger.debug("{} - Max Volume: {}", zoneToUpdate, maxVolumeState);
        logger.debug("{} - Input: {}", zoneToUpdate, inputState);
        logger.debug("{} - Soundprogram: {}", zoneToUpdate, soundProgramState);
        logger.debug("{} - Sleep: {}", zoneToUpdate, sleepState);

        switch (responseCode) {
            case "0":
                for (Channel channel : getThing().getChannels()) {
                    ChannelUID channelUID = channel.getUID();
                    channelWithoutGroup = channelUID.getIdWithoutGroup();
                    zone = channelUID.getGroupId();
                    if (isLinked(channelUID)) {
                        switch (channelWithoutGroup) {
                            case CHANNEL_POWER:
                                if (powerState.equals("on")) {
                                    if (zone.equals(zoneToUpdate)) {
                                        updateState(channelUID, OnOffType.ON);
                                    }
                                } else if (powerState.equals("standby")) {
                                    if (zone.equals(zoneToUpdate)) {
                                        updateState(channelUID, OnOffType.OFF);
                                    }
                                }
                                break;
                            case CHANNEL_MUTE:
                                if (muteState.equals("true")) {
                                    if (zone.equals(zoneToUpdate)) {
                                        updateState(channelUID, OnOffType.ON);
                                    }
                                } else if (muteState.equals("false")) {
                                    if (zone.equals(zoneToUpdate)) {
                                        updateState(channelUID, OnOffType.OFF);
                                    }
                                }
                                break;
                            case CHANNEL_VOLUME:
                                if (zone.equals(zoneToUpdate)) {
                                    updateState(channelUID, new PercentType((volumeState * 100) / maxVolumeState));
                                }
                                break;
                            case CHANNEL_VOLUMEABS:
                                if (zone.equals(zoneToUpdate)) {
                                    updateState(channelUID, new DecimalType(volumeState));
                                }
                                break;
                            case CHANNEL_INPUT:
                                if (zone.equals(zoneToUpdate)) {
                                    updateState(channelUID, StringType.valueOf(inputState));
                                }
                                break;
                            case CHANNEL_SOUNDPROGRAM:
                                if (zone.equals(zoneToUpdate)) {
                                    updateState(channelUID, StringType.valueOf(soundProgramState));
                                }
                                break;
                            case CHANNEL_SLEEP:
                                if (zone.equals(zoneToUpdate)) {
                                    updateState(channelUID, new DecimalType(sleepState));
                                }
                                break;
                        } // END switch (channelWithoutGroup)
                    } // END IsLinked
                }
                break;
            case "999":
                logger.info("Nothing to do! - {} ({})", thingLabel, zoneToUpdate);
                break;
        }
    }

    private void updatePresets(int value) {
        String inputText = "";
        int presetCounter = 0;
        int currentPreset = 0;
        tmpString = getPresetInfo(); // Without zone

        PresetInfo presetinfo = gson.fromJson(tmpString, PresetInfo.class);
        responseCode = presetinfo.getResponseCode();
        if (responseCode.equals("0")) {

            List<StateOption> optionsPresets = new ArrayList<>();
            inputText = getLastInput(); // Without zone
            for (JsonElement pr : presetinfo.getPresetInfo()) {
                presetCounter = presetCounter + 1;
                JsonObject presetObject = pr.getAsJsonObject();
                String text = presetObject.get("text").getAsString();
                if (!text.equals("")) {
                    optionsPresets.add(new StateOption(String.valueOf(presetCounter),
                            "#" + String.valueOf(presetCounter) + " " + text));
                    if (inputText.equals(text)) {
                        currentPreset = presetCounter;
                    }
                }
            }
            if (value != 0) {
                currentPreset = value;
            }
            for (Channel channel : getThing().getChannels()) {
                ChannelUID channelUID = channel.getUID();
                channelWithoutGroup = channelUID.getIdWithoutGroup();
                if (isLinked(channelUID)) {
                    switch (channelWithoutGroup) {
                        case CHANNEL_SELECTPRESET:
                            stateDescriptionProvider.setStateOptions(channelUID, optionsPresets);
                            updateState(channelUID, StringType.valueOf(String.valueOf(currentPreset)));
                            break;
                    }
                }
            }
        }
    }

    private void updateNetUSBPlayer() {
        tmpString = getPlayInfo();
        try {
            @Nullable
            PlayInfo targetObject = gson.fromJson(tmpString, PlayInfo.class);
            responseCode = targetObject.getResponseCode();
            playbackState = targetObject.getPlayback();
            artistState = targetObject.getArtist();
            trackState = targetObject.getTrack();
            albumState = targetObject.getAlbum();
            albumArtUrlState = targetObject.getAlbumArtUrl();
            repeatState = targetObject.getRepeat();
            shuffleState = targetObject.getShuffle();
            playTimeState = targetObject.getPlayTime();
            totalTimeState = targetObject.getTotalTime();
        } catch (Exception e) {
            responseCode = "999";
        }

        if (responseCode.equals("0")) {
            ChannelUID testchannel = new ChannelUID(getThing().getUID(), "playerControls", CHANNEL_PLAYER);
            switch (playbackState) {
                case "play":
                    updateState(testchannel, PlayPauseType.PLAY);
                    break;
                case "stop":
                    updateState(testchannel, PlayPauseType.PAUSE);
                    break;
                case "pause":
                    updateState(testchannel, PlayPauseType.PAUSE);
                    break;
                case "fast_reverse":
                    updateState(testchannel, RewindFastforwardType.REWIND);
                    break;
                case "fast_forward":
                    updateState(testchannel, RewindFastforwardType.FASTFORWARD);
                    break;
            }
            testchannel = new ChannelUID(getThing().getUID(), "playerControls", CHANNEL_ARTIST);
            if (isLinked(testchannel)) {
                updateState(testchannel, StringType.valueOf(artistState));
            }
            testchannel = new ChannelUID(getThing().getUID(), "playerControls", CHANNEL_TRACK);
            if (isLinked(testchannel)) {
                updateState(testchannel, StringType.valueOf(trackState));
            }
            testchannel = new ChannelUID(getThing().getUID(), "playerControls", CHANNEL_ALBUM);
            if (isLinked(testchannel)) {
                updateState(testchannel, StringType.valueOf(albumState));
            }
            testchannel = new ChannelUID(getThing().getUID(), "playerControls", CHANNEL_ALBUMART);
            if (isLinked(testchannel)) {
                if (!albumArtUrlState.equals("")) {
                    albumArtUrlState = "http://" + this.host + albumArtUrlState;
                }
                updateState(testchannel, StringType.valueOf(albumArtUrlState));
            }
            testchannel = new ChannelUID(getThing().getUID(), "playerControls", CHANNEL_REPEAT);
            if (isLinked(testchannel)) {
                updateState(testchannel, StringType.valueOf(repeatState));
            }
            testchannel = new ChannelUID(getThing().getUID(), "playerControls", CHANNEL_SHUFFLE);
            if (isLinked(testchannel)) {
                updateState(testchannel, StringType.valueOf(shuffleState));
            }
            testchannel = new ChannelUID(getThing().getUID(), "playerControls", CHANNEL_PLAYTIME);
            if (isLinked(testchannel)) {
                updateState(testchannel, StringType.valueOf(String.valueOf(playTimeState)));
            }
            testchannel = new ChannelUID(getThing().getUID(), "playerControls", CHANNEL_TOTALTIME);
            if (isLinked(testchannel)) {
                updateState(testchannel, StringType.valueOf(String.valueOf(totalTimeState)));
            }
        }
    }

    private @Nullable String getLastInput() {
        String text = "";
        tmpString = getRecentInfo();
        RecentInfo recentinfo = gson.fromJson(tmpString, RecentInfo.class);
        responseCode = recentinfo.getResponseCode();
        if (responseCode.equals("0")) {
            for (JsonElement ri : recentinfo.getRecentInfo()) {
                JsonObject recentObject = ri.getAsJsonObject();
                text = recentObject.get("text").getAsString();
                break;
            }
        }
        return text;
    }

    private void fillOptionsForMCLink() {
        Bridge bridge = getBridge();
        String host = "";
        String label = "";
        int zonesPerHost = 1;
        List<StateOption> options = new ArrayList<>();
        // first add 3 options for Mc Link
        options.add(new StateOption("", "Standalone"));
        options.add(new StateOption("server", "Server"));
        options.add(new StateOption("client", "Client"));

        for (Thing thing : bridge.getThings()) {
            label = thing.getLabel();
            host = thing.getConfiguration().get("host").toString();
            if (host == null) {
                host = "";
                zonesPerHost = 0;
            } else {
                logger.debug("Thing found on Bridge: {} - {}", label, host);
                zonesPerHost = getNumberOfZones(host);
                for (int i = 1; i <= zonesPerHost; i++) {
                    switch (i) {
                        case 1:
                            options.add(new StateOption(host + "***main", label + " - main (" + host + ")"));
                            break;
                        case 2:
                            options.add(new StateOption(host + "***zone2", label + " - zone2 (" + host + ")"));
                            break;
                        case 3:
                            options.add(new StateOption(host + "***zone3", label + " - zone3 (" + host + ")"));
                            break;
                        case 4:
                            options.add(new StateOption(host + "***zone4", label + " - zone4 (" + host + ")"));
                            break;
                    }
                }
            }
        }

        // for each zone of the device, set all the possible combinations
        for (int i = 1; i <= zoneNum; i++) {
            switch (i) {
                case 1:
                    ChannelUID testchannel = new ChannelUID(getThing().getUID(), "main", CHANNEL_MCSERVER);
                    if (isLinked(testchannel)) {
                        stateDescriptionProvider.setStateOptions(testchannel, options);
                    }
                    break;
                case 2:
                    testchannel = new ChannelUID(getThing().getUID(), "zone2", CHANNEL_MCSERVER);
                    if (isLinked(testchannel)) {
                        stateDescriptionProvider.setStateOptions(testchannel, options);
                    }
                    break;
                case 3:
                    testchannel = new ChannelUID(getThing().getUID(), "zone3", CHANNEL_MCSERVER);
                    if (isLinked(testchannel)) {
                        stateDescriptionProvider.setStateOptions(testchannel, options);
                    }
                    break;
                case 4:
                    testchannel = new ChannelUID(getThing().getUID(), "zone4", CHANNEL_MCSERVER);
                    if (isLinked(testchannel)) {
                        stateDescriptionProvider.setStateOptions(testchannel, options);
                    }
                    break;
            }
        }
    }

    private String generateGroupId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }

    private int getNumberOfZones(@Nullable String host) {
        int numberOfZones = 0;
        try {
            tmpString = getFeatures(host);
            @Nullable
            Features targetObject = gson.fromJson(tmpString, Features.class);
            numberOfZones = targetObject.getSystem().getZoneNum();
            return numberOfZones;
        } catch (Exception e) {
            logger.warn("Error fetching zones");
            return numberOfZones;
        }
    }

    public @Nullable String getDeviceId() {
        try {
            tmpString = getDeviceInfo();
            @Nullable
            DeviceInfo targetObject = gson.fromJson(tmpString, DeviceInfo.class);
            return targetObject.getDeviceId();
        } catch (Exception e) {
            logger.warn("Error fetching Device Id");
            return "";
        }
    }

    private void setVolumeLinkedDevice(int value, @Nullable String zone, String host) {
        logger.info("setVolumeLinkedDevice: {}", host);
        int zoneNumLinkedDevice = getNumberOfZones(host);
        int maxVolumeLinkedDevice = 0;
        @Nullable
        Status targetObject = new Status();
        int newVolume = 0;
        for (int i = 1; i <= zoneNumLinkedDevice; i++) {
            switch (i) {
                case 1:
                    tmpString = getStatus(host, "main");
                    targetObject = gson.fromJson(tmpString, Status.class);
                    responseCode = targetObject.getResponseCode();
                    maxVolumeLinkedDevice = targetObject.getMaxVolume();
                    newVolume = maxVolumeLinkedDevice * value / 100;
                    setVolume(newVolume, "main", host);
                    break;
                case 2:
                    tmpString = getStatus(host, "zone2");
                    targetObject = gson.fromJson(tmpString, Status.class);
                    responseCode = targetObject.getResponseCode();
                    maxVolumeLinkedDevice = targetObject.getMaxVolume();
                    newVolume = maxVolumeLinkedDevice * value / 100;
                    setVolume(newVolume, "zone2", host);
                    break;
                case 3:
                    tmpString = getStatus(host, "zone3");
                    targetObject = gson.fromJson(tmpString, Status.class);
                    responseCode = targetObject.getResponseCode();
                    maxVolumeLinkedDevice = targetObject.getMaxVolume();
                    newVolume = maxVolumeLinkedDevice * value / 100;
                    setVolume(newVolume, "zone3", host);
                    break;
                case 4:
                    tmpString = getStatus(host, "zone4");
                    targetObject = gson.fromJson(tmpString, Status.class);
                    responseCode = targetObject.getResponseCode();
                    maxVolumeLinkedDevice = targetObject.getMaxVolume();
                    newVolume = maxVolumeLinkedDevice * value / 100;
                    setVolume(newVolume, "zone4", host);
                    break;
            }
        }
    }

    private void updateMCLinkStatus() {
        tmpString = getDistributionInfo(this.host);
        @Nullable
        DistributionInfo targetObject = gson.fromJson(tmpString, DistributionInfo.class);
        role = targetObject.getRole();

        switch (role) {
            case "none":
                setMCLinkToStandalone();
                break;
            case "server":
                setMCLinkToServer();
                break;
            case "client":
                setMCLinkToClient();
                break;
        }
    }

    private void setMCLinkToStandalone() {
        ChannelUID testchannel;
        for (int i = 1; i <= zoneNum; i++) {
            switch (i) {
                case 1:
                    testchannel = new ChannelUID(getThing().getUID(), "main", CHANNEL_MCSERVER);
                    if (isLinked(testchannel)) {
                        updateState(testchannel, StringType.valueOf(""));
                    }
                    break;
                case 2:
                    testchannel = new ChannelUID(getThing().getUID(), "zone2", CHANNEL_MCSERVER);
                    if (isLinked(testchannel)) {
                        updateState(testchannel, StringType.valueOf(""));
                    }
                    break;
                case 3:
                    testchannel = new ChannelUID(getThing().getUID(), "zone3", CHANNEL_MCSERVER);
                    if (isLinked(testchannel)) {
                        updateState(testchannel, StringType.valueOf(""));
                    }
                    break;
                case 4:
                    testchannel = new ChannelUID(getThing().getUID(), "zone4", CHANNEL_MCSERVER);
                    if (isLinked(testchannel)) {
                        updateState(testchannel, StringType.valueOf(""));
                    }
                    break;
            }
        }
    }

    private void setMCLinkToClient() {
        ChannelUID testchannel;
        for (int i = 1; i <= zoneNum; i++) {
            switch (i) {
                case 1:
                    testchannel = new ChannelUID(getThing().getUID(), "main", CHANNEL_MCSERVER);
                    if (isLinked(testchannel)) {
                        updateState(testchannel, StringType.valueOf("client"));
                    }
                    break;
                case 2:
                    testchannel = new ChannelUID(getThing().getUID(), "zone2", CHANNEL_MCSERVER);
                    if (isLinked(testchannel)) {
                        updateState(testchannel, StringType.valueOf("client"));
                    }
                    break;
                case 3:
                    testchannel = new ChannelUID(getThing().getUID(), "zone3", CHANNEL_MCSERVER);
                    if (isLinked(testchannel)) {
                        updateState(testchannel, StringType.valueOf("client"));
                    }
                    break;
                case 4:
                    testchannel = new ChannelUID(getThing().getUID(), "zone4", CHANNEL_MCSERVER);
                    if (isLinked(testchannel)) {
                        updateState(testchannel, StringType.valueOf("client"));
                    }
                    break;
            }
        }
    }

    private void setMCLinkToServer() {
        ChannelUID testchannel;
        for (int i = 1; i <= zoneNum; i++) {
            switch (i) {
                case 1:
                    testchannel = new ChannelUID(getThing().getUID(), "main", CHANNEL_MCSERVER);
                    if (isLinked(testchannel)) {
                        updateState(testchannel, StringType.valueOf("server"));
                    }
                    break;
                case 2:
                    testchannel = new ChannelUID(getThing().getUID(), "zone2", CHANNEL_MCSERVER);
                    if (isLinked(testchannel)) {
                        updateState(testchannel, StringType.valueOf("server"));
                    }
                    break;
                case 3:
                    testchannel = new ChannelUID(getThing().getUID(), "zone3", CHANNEL_MCSERVER);
                    if (isLinked(testchannel)) {
                        updateState(testchannel, StringType.valueOf("server"));
                    }
                    break;
                case 4:
                    testchannel = new ChannelUID(getThing().getUID(), "zone4", CHANNEL_MCSERVER);
                    if (isLinked(testchannel)) {
                        updateState(testchannel, StringType.valueOf("server"));
                    }
                    break;
            }
        }
    }

    private String makeRequest(@Nullable String topicAVR, String url) {
        String response = "";
        try {
            response = HttpUtil.executeUrl("GET", url, connectionTimeout);
            logger.debug("{} - {}", topicAVR, response);
            return response;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.getMessage());
            return "{\"response_code\":\"999\"}";
        }
    }
    // End Various functions

    // API calls to AVR

    // Start Zone Related

    private @Nullable String getStatus(@Nullable String host, String zone) {
        topicAVR = "Status";
        url = "http://" + host + "/YamahaExtendedControl/v1/" + zone + "/getStatus";
        return makeRequest(topicAVR, url);
    }

    private @Nullable String setPower(String value, @Nullable String zone) {
        topicAVR = "Power";
        url = "http://" + this.host + "/YamahaExtendedControl/v1/" + zone + "/setPower?power=" + value;
        return makeRequest(topicAVR, url);
    }

    private @Nullable String setMute(String value, @Nullable String zone) {
        topicAVR = "Mute";
        url = "http://" + this.host + "/YamahaExtendedControl/v1/" + zone + "/setMute?enable=" + value;
        return makeRequest(topicAVR, url);
    }

    private @Nullable String setVolume(int value, @Nullable String zone, @Nullable String host) {
        topicAVR = "Volume";
        url = "http://" + host + "/YamahaExtendedControl/v1/" + zone + "/setVolume?volume=" + value;
        return makeRequest(topicAVR, url);
    }

    private @Nullable String setInput(String value, @Nullable String zone) {
        topicAVR = "setInput";
        url = "http://" + this.host + "/YamahaExtendedControl/v1/" + zone + "/setInput?input=" + value;
        return makeRequest(topicAVR, url);
    }

    private @Nullable String setSoundProgram(String value, @Nullable String zone) {
        topicAVR = "setSoundProgram";
        url = "http://" + this.host + "/YamahaExtendedControl/v1/" + zone + "/setSoundProgram?program=" + value;
        return makeRequest(topicAVR, url);
    }

    private @Nullable String setPreset(String value, @Nullable String zone) {
        topicAVR = "setPreset";
        url = "http://" + this.host + "/YamahaExtendedControl/v1/netusb/recallPreset?zone=" + zone + "&num=" + value;
        return makeRequest(topicAVR, url);
    }

    private @Nullable String setSleep(String value, @Nullable String zone) {
        topicAVR = "setSleep";
        url = "http://" + this.host + "/YamahaExtendedControl/v1/" + zone + "/setSleep?sleep=" + value;
        return makeRequest(topicAVR, url);
    }

    private @Nullable String recallScene(String value, @Nullable String zone) {
        topicAVR = "recallScene";
        url = "http://" + this.host + "/YamahaExtendedControl/v1/" + zone + "/recallScene?num=" + value;
        return makeRequest(topicAVR, url);
    }
    // End Zone Related

    // Start Net Radio/USB Related

    private @Nullable String getPresetInfo() {
        topicAVR = "PresetInfo";
        url = "http://" + this.host + "/YamahaExtendedControl/v2/netusb/getPresetInfo";
        return makeRequest(topicAVR, url);
    }

    private @Nullable String getRecentInfo() {
        topicAVR = "RecentInfo";
        url = "http://" + this.host + "/YamahaExtendedControl/v1/netusb/getRecentInfo";
        return makeRequest(topicAVR, url);
    }

    private @Nullable String getPlayInfo() {
        topicAVR = "PlayInfo";
        url = "http://" + this.host + "/YamahaExtendedControl/v1/netusb/getPlayInfo";
        return makeRequest(topicAVR, url);
    }

    private @Nullable String setPlayback(String value) {
        topicAVR = "Playback";
        url = "http://" + this.host + "/YamahaExtendedControl/v1/netusb/setPlayback?playback=" + value;
        return makeRequest(topicAVR, url);
    }

    private @Nullable String setRepeat(String value) {
        topicAVR = "Repeat";
        url = "http://" + this.host + "/YamahaExtendedControl/v1/netusb/setRepeat?mode=" + value;
        return makeRequest(topicAVR, url);
    }

    private @Nullable String setShuffle(String value) {
        topicAVR = "Shuffle";
        url = "http://" + this.host + "/YamahaExtendedControl/v1/netusb/setShuffle?mode=" + value;
        return makeRequest(topicAVR, url);
    }

    // End Net Radio/USB Related

    // Start Music Cast API calls
    private @Nullable String getDistributionInfo(@Nullable String host) {
        topicAVR = "DistributionInfo";
        url = "http://" + host + "/YamahaExtendedControl/v1/dist/getDistributionInfo";
        return makeRequest(topicAVR, url);
    }

    private @Nullable String setServerInfo(@Nullable String host, String json) {
        InputStream is = new ByteArrayInputStream(json.getBytes());
        topicAVR = "SetServerInfo";
        url = "";
        try {
            url = "http://" + host + "/YamahaExtendedControl/v1/dist/setServerInfo";
            httpResponse = HttpUtil.executeUrl("POST", url, is, "", longConnectionTimeout);
            logger.debug("MC Link/Unlink Server {}", httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.getMessage());
            return "{\"response_code\":\"999\"}";
        }
    }

    private @Nullable String setClientInfo(@Nullable String host, String json) {
        InputStream is = new ByteArrayInputStream(json.getBytes());
        topicAVR = "SetClientInfo";
        url = "";
        try {
            url = "http://" + host + "/YamahaExtendedControl/v1/dist/setClientInfo";
            httpResponse = HttpUtil.executeUrl("POST", url, is, "", longConnectionTimeout);
            logger.debug("MC Link/Unlink Client {}", httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.getMessage());
            return "{\"response_code\":\"999\"}";
        }
    }

    private @Nullable String startDistribution(@Nullable String host) {
        topicAVR = "StartDistribution";
        url = "http://" + host + "/YamahaExtendedControl/v1/dist/startDistribution?num=1";
        return makeRequest(topicAVR, url);
    }

    // End Music Cast API calls

    // Start General/System API calls

    private @Nullable String getFeatures(@Nullable String host) {
        topicAVR = "Features";
        url = "";
        // return makeRequest(topicAVR, url);
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + host + "/YamahaExtendedControl/v1/system/getFeatures",
                    longConnectionTimeout);
            logger.debug("{}", httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.getMessage());
            return "{\"response_code\":\"999\"}";
        }
    }

    private @Nullable String getDeviceInfo() {
        topicAVR = "DeviceInfo";
        url = "";
        // return makeRequest(topicAVR, url);
        try {
            httpResponse = HttpUtil.executeUrl("GET",
                    "http://" + this.host + "/YamahaExtendedControl/v1/system/getDeviceInfo", connectionTimeout);
            logger.debug("{}", httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.getMessage());
            return "{\"response_code\":\"999\"}";
        }
    }

    private void keepUdpEventsAlive() {
        Properties appProps = new Properties();
        appProps.setProperty("X-AppName", "MusicCast/1");
        appProps.setProperty("X-AppPort", "41100");
        try {
            httpResponse = HttpUtil.executeUrl("GET",
                    "http://" + this.host + "/YamahaExtendedControl/v1/netusb/getPlayInfo", appProps, null, "",
                    connectionTimeout);
            logger.debug("{}", httpResponse);
        } catch (IOException e) {
            logger.warn("UDP refresh failed - {}", e.getMessage());
        }
    }
    // End General/System API calls
}
