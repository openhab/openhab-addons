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
package org.openhab.binding.yamahamusiccast.internal;

import static org.openhab.binding.yamahamusiccast.internal.YamahaMusiccastBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
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
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
    private @Nullable ScheduledFuture<?> generalHousekeepingTask;
    private @Nullable String httpResponse;
    private @Nullable String tmpString = "";
    private int volumePercent = 0;
    private int volumeAbsValue = 0;
    private @Nullable String responseCode = "";
    private int volumeState = 0;
    private int maxVolumeState = 0;
    private @Nullable String inputState = "";
    private @Nullable String soundProgramState = "";
    private int sleepState = 0;
    private @Nullable String artistState = "";
    private @Nullable String trackState = "";
    private @Nullable String albumState = "";
    private @Nullable String repeatState = "";
    private @Nullable String shuffleState = "";
    private int playTimeState = 0;
    private int totalTimeState = 0;
    private @Nullable String zone = "main";
    private String channelWithoutGroup = "";
    private @Nullable String thingLabel = "";
    private @Nullable String mclinkSetupServer = "";
    private @Nullable String mclinkSetupZone = "";
    private String url = "";
    private String json = "";
    private String action = "";
    private int zoneNum = 0;
    private @Nullable String groupId = "";
    private @Nullable String host;
    public @Nullable String deviceId = "";

    private YamahaMusiccastStateDescriptionProvider stateDescriptionProvider;

    public YamahaMusiccastHandler(Thing thing, YamahaMusiccastStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String localValueToCheck = "";
        String localRole = "";
        boolean localSyncVolume;
        String localDefaultAfterMCLink = "";
        String localRoleSelectedThing = "";
        if (command != RefreshType.REFRESH) {
            logger.trace("Handling command {} for channel {}", command, channelUID);
            channelWithoutGroup = channelUID.getIdWithoutGroup();
            zone = channelUID.getGroupId();
            DistributionInfo distributioninfo = new DistributionInfo();
            Response response = new Response();
            switch (channelWithoutGroup) {
                case CHANNEL_POWER:
                    if (command == OnOffType.ON) {
                        httpResponse = setPower("on", zone, this.host);
                        response = gson.fromJson(httpResponse, Response.class);
                        if (response != null) {
                            localValueToCheck = response.getResponseCode();
                            if (!"0".equals(localValueToCheck)) {
                                updateState(channelUID, OnOffType.OFF);
                            }
                        }
                        // check on scheduler task for UDP events
                        ScheduledFuture<?> localGeneralHousekeepingTask = generalHousekeepingTask;
                        if (localGeneralHousekeepingTask == null) {
                            logger.trace("YXC - No scheduler task found!");
                            generalHousekeepingTask = scheduler.scheduleWithFixedDelay(this::generalHousekeeping, 5,
                                    300, TimeUnit.SECONDS);

                        } else {
                            logger.trace("Scheduler task found!");
                        }

                    } else if (command == OnOffType.OFF) {
                        httpResponse = setPower("standby", zone, this.host);
                        response = gson.fromJson(httpResponse, Response.class);
                        powerOffCleanup();
                        if (response != null) {
                            localValueToCheck = response.getResponseCode();
                            if (!"0".equals(localValueToCheck)) {
                                updateState(channelUID, OnOffType.ON);
                            }
                        }
                    }
                    break;
                case CHANNEL_MUTE:
                    if (command == OnOffType.ON) {
                        httpResponse = setMute("true", zone, this.host);
                        response = gson.fromJson(httpResponse, Response.class);
                        if (response != null) {
                            localValueToCheck = response.getResponseCode();
                            if (!"0".equals(localValueToCheck)) {
                                updateState(channelUID, OnOffType.OFF);
                            }
                        }
                    } else if (command == OnOffType.OFF) {
                        httpResponse = setMute("false", zone, this.host);
                        response = gson.fromJson(httpResponse, Response.class);
                        if (response != null) {
                            localValueToCheck = response.getResponseCode();
                            if (!"0".equals(localValueToCheck)) {
                                updateState(channelUID, OnOffType.ON);
                            }
                        }
                    }
                    break;
                case CHANNEL_VOLUME:
                    volumePercent = Integer.parseInt(command.toString().replace(".0", ""));
                    volumeAbsValue = (maxVolumeState * volumePercent) / 100;
                    setVolume(volumeAbsValue, zone, this.host);
                    localSyncVolume = Boolean.parseBoolean(getThing().getConfiguration().get("syncVolume").toString());
                    if (localSyncVolume == Boolean.TRUE) {
                        tmpString = getDistributionInfo(this.host);
                        distributioninfo = gson.fromJson(tmpString, DistributionInfo.class);
                        if (distributioninfo != null) {
                            localRole = distributioninfo.getRole();
                            if ("server".equals(localRole)) {
                                for (JsonElement ip : distributioninfo.getClientList()) {
                                    JsonObject clientObject = ip.getAsJsonObject();
                                    setVolumeLinkedDevice(volumePercent, zone,
                                            clientObject.get("ip_address").getAsString());
                                }
                            }
                        }
                    } // END config.syncVolume
                    break;
                case CHANNEL_VOLUMEABS:
                    volumeAbsValue = Integer.parseInt(command.toString().replace(".0", ""));
                    volumePercent = (volumeAbsValue / maxVolumeState) * 100;
                    setVolume(volumeAbsValue, zone, this.host);
                    localSyncVolume = Boolean.parseBoolean(getThing().getConfiguration().get("syncVolume").toString());
                    if (localSyncVolume == Boolean.TRUE) {
                        tmpString = getDistributionInfo(this.host);
                        distributioninfo = gson.fromJson(tmpString, DistributionInfo.class);
                        if (distributioninfo != null) {
                            localRole = distributioninfo.getRole();
                            if ("server".equals(localRole)) {
                                for (JsonElement ip : distributioninfo.getClientList()) {
                                    JsonObject clientObject = ip.getAsJsonObject();
                                    setVolumeLinkedDevice(volumePercent, zone,
                                            clientObject.get("ip_address").getAsString());
                                }
                            }
                        }
                    }
                    break;
                case CHANNEL_INPUT:
                    // if it is a client, disconnect it first.
                    tmpString = getDistributionInfo(this.host);
                    distributioninfo = gson.fromJson(tmpString, DistributionInfo.class);
                    if (distributioninfo != null) {
                        localRole = distributioninfo.getRole();
                        if ("client".equals(localRole)) {
                            json = "{\"group_id\":\"\"}";
                            httpResponse = setClientServerInfo(this.host, json, "setClientInfo");
                        }
                    }
                    setInput(command.toString(), zone, this.host);
                    break;
                case CHANNEL_SOUNDPROGRAM:
                    setSoundProgram(command.toString(), zone, this.host);
                    break;
                case CHANNEL_SELECTPRESET:
                    setPreset(command.toString(), zone, this.host);
                    break;
                case CHANNEL_PLAYER:
                    if (command.equals(PlayPauseType.PLAY)) {
                        setPlayback("play", this.host);
                    } else if (command.equals(PlayPauseType.PAUSE)) {
                        setPlayback("pause", this.host);
                    } else if (command.equals(NextPreviousType.NEXT)) {
                        setPlayback("next", this.host);
                    } else if (command.equals(NextPreviousType.PREVIOUS)) {
                        setPlayback("previous", this.host);
                    } else if (command.equals(RewindFastforwardType.REWIND)) {
                        setPlayback("fast_reverse_start", this.host);
                    } else if (command.equals(RewindFastforwardType.FASTFORWARD)) {
                        setPlayback("fast_forward_end", this.host);
                    }
                    break;
                case CHANNEL_SLEEP:
                    setSleep(command.toString(), zone, this.host);
                    break;
                case CHANNEL_MCLINKSTATUS:
                    action = "";
                    json = "";
                    tmpString = getDistributionInfo(this.host);
                    distributioninfo = gson.fromJson(tmpString, DistributionInfo.class);
                    if (distributioninfo != null) {
                        responseCode = distributioninfo.getResponseCode();
                        localRole = distributioninfo.getRole();
                        if (command.toString().equals("")) {
                            action = "unlink";
                            groupId = distributioninfo.getGroupId();
                        } else if (command.toString().contains("***")) {
                            action = "link";
                            String[] parts = command.toString().split("\\*\\*\\*");
                            if (parts.length > 1) {
                                mclinkSetupServer = parts[0];
                                mclinkSetupZone = parts[1];
                                tmpString = getDistributionInfo(mclinkSetupServer);
                                distributioninfo = gson.fromJson(tmpString, DistributionInfo.class);
                                if (distributioninfo != null) {
                                    responseCode = distributioninfo.getResponseCode();
                                    localRoleSelectedThing = distributioninfo.getRole();
                                    groupId = distributioninfo.getGroupId();
                                    if (localRoleSelectedThing != null) {
                                        if ("server".equals(localRoleSelectedThing)) {
                                            groupId = distributioninfo.getGroupId();
                                        } else if ("client".equals(localRoleSelectedThing)) {
                                            groupId = "";
                                        } else if ("none".equals(localRoleSelectedThing)) {
                                            groupId = generateGroupId();
                                        }
                                    }
                                }
                            }
                        }

                        if ("unlink".equals(action)) {
                            json = "{\"group_id\":\"\"}";
                            if (localRole != null) {
                                if ("server".equals(localRole)) {
                                    httpResponse = setClientServerInfo(this.host, json, "setServerInfo");
                                    // Set GroupId = "" for linked clients
                                    if (distributioninfo != null) {
                                        for (JsonElement ip : distributioninfo.getClientList()) {
                                            JsonObject clientObject = ip.getAsJsonObject();
                                            setClientServerInfo(clientObject.get("ip_address").getAsString(), json,
                                                    "setClientInfo");
                                        }
                                    }
                                } else if ("client".equals(localRole)) {
                                    mclinkSetupServer = connectedServer();
                                    // Step 1. empty group on client
                                    httpResponse = setClientServerInfo(this.host, json, "setClientInfo");
                                    // empty zone to respect defaults
                                    if (!"".equals(mclinkSetupServer)) {
                                        // Step 2. remove client from server
                                        json = "{\"group_id\":\"" + groupId
                                                + "\", \"type\":\"remove\", \"client_list\":[\"" + this.host + "\"]}";
                                        httpResponse = setClientServerInfo(mclinkSetupServer, json, "setServerInfo");
                                        // Step 3. reflect changes to master
                                        httpResponse = startDistribution(mclinkSetupServer);
                                        localDefaultAfterMCLink = getThing().getConfiguration()
                                                .get("defaultAfterMCLink").toString();
                                        httpResponse = setInput(localDefaultAfterMCLink.toString(), zone, this.host);
                                    } else if ("".equals(mclinkSetupServer)) {
                                        // fallback in case client is removed from group by ending group on server side
                                        localDefaultAfterMCLink = getThing().getConfiguration()
                                                .get("defaultAfterMCLink").toString();
                                        httpResponse = setInput(localDefaultAfterMCLink.toString(), zone, this.host);
                                    }
                                }
                            }
                        } else if ("link".equals(action)) {
                            if (localRole != null) {
                                if ("none".equals(localRole)) {
                                    json = "{\"group_id\":\"" + groupId + "\", \"zone\":\"" + mclinkSetupZone
                                            + "\", \"type\":\"add\", \"client_list\":[\"" + this.host + "\"]}";
                                    logger.trace("setServerInfo json: {}", json);
                                    httpResponse = setClientServerInfo(mclinkSetupServer, json, "setServerInfo");
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
                                    logger.trace("setClientInfo json: {}", json);
                                    httpResponse = setClientServerInfo(this.host, json, "setClientInfo");
                                    httpResponse = startDistribution(mclinkSetupServer);
                                }
                            }
                        }
                    }
                    updateMCLinkStatus();
                    break;
                case CHANNEL_RECALLSCENE:
                    recallScene(command.toString(), zone, this.host);
                    break;
                case CHANNEL_REPEAT:
                    setRepeat(command.toString(), this.host);
                    break;
                case CHANNEL_SHUFFLE:
                    setShuffle(command.toString(), this.host);
                    break;
            } // END Switch Channel
        }
    }

    @Override
    public void initialize() {
        String localHost = "";
        thingLabel = thing.getLabel();
        updateStatus(ThingStatus.UNKNOWN);
        localHost = getThing().getConfiguration().get("host").toString();
        this.host = localHost;
        if (!"".equals(this.host)) {
            zoneNum = getNumberOfZones(this.host);
            logger.trace("Zones found: {} - {}", zoneNum, thingLabel);

            if (zoneNum > 0) {
                refreshOnStartup();
                generalHousekeepingTask = scheduler.scheduleWithFixedDelay(this::generalHousekeeping, 5, 300,
                        TimeUnit.SECONDS);
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "No host found");
            }
        }
    }

    private void generalHousekeeping() {
        thingLabel = thing.getLabel();
        logger.trace("YXC - Start Keep Alive UDP events (5 minutes - {}) ", thingLabel);
        keepUdpEventsAlive(this.host);
        fillOptionsForMCLink();
        updateMCLinkStatus();
    }

    private void refreshOnStartup() {
        for (int i = 1; i <= zoneNum; i++) {
            switch (i) {
                case 1:
                    createChannels("main");
                    updateStatusZone("main");
                    break;
                case 2:
                    createChannels("zone2");
                    updateStatusZone("zone2");
                    break;
                case 3:
                    createChannels("zone3");
                    updateStatusZone("zone3");
                    break;
                case 4:
                    createChannels("zone4");
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
        ScheduledFuture<?> localGeneralHousekeepingTask = generalHousekeepingTask;
        if (localGeneralHousekeepingTask != null) {
            localGeneralHousekeepingTask.cancel(true);
        }
    }

    // Various functions

    private void createChannels(String zone) {
        createChannel(zone, CHANNEL_POWER, CHANNEL_TYPE_UID_POWER, "Switch");
        createChannel(zone, CHANNEL_MUTE, CHANNEL_TYPE_UID_MUTE, "Switch");
        createChannel(zone, CHANNEL_VOLUME, CHANNEL_TYPE_UID_VOLUME, "Dimmer");
        createChannel(zone, CHANNEL_VOLUMEABS, CHANNEL_TYPE_UID_VOLUMEABS, "Number");
        createChannel(zone, CHANNEL_INPUT, CHANNEL_TYPE_UID_INPUT, "String");
        createChannel(zone, CHANNEL_SOUNDPROGRAM, CHANNEL_TYPE_UID_SOUNDPROGRAM, "String");
        createChannel(zone, CHANNEL_SLEEP, CHANNEL_TYPE_UID_SLEEP, "Number");
        createChannel(zone, CHANNEL_SELECTPRESET, CHANNEL_TYPE_UID_SELECTPRESET, "String");
        createChannel(zone, CHANNEL_RECALLSCENE, CHANNEL_TYPE_UID_RECALLSCENE, "Number");
        createChannel(zone, CHANNEL_MCLINKSTATUS, CHANNEL_TYPE_UID_MCLINKSTATUS, "String");
    }

    private void createChannel(String zone, String channel, ChannelTypeUID channelTypeUID, String itemType) {
        ChannelUID channelToCheck = new ChannelUID(thing.getUID(), zone, channel);
        if (thing.getChannel(channelToCheck) == null) {
            ThingBuilder thingBuilder = editThing();
            Channel testchannel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), zone, channel), itemType)
                    .withType(channelTypeUID).build();
            thingBuilder.withChannel(testchannel);
            updateThing(thingBuilder.build());
        }
    }

    private void powerOffCleanup() {
        ChannelUID channel;
        channel = new ChannelUID(getThing().getUID(), "playerControls", CHANNEL_ARTIST);
        updateState(channel, StringType.valueOf("-"));
        channel = new ChannelUID(getThing().getUID(), "playerControls", CHANNEL_TRACK);
        updateState(channel, StringType.valueOf("-"));
        channel = new ChannelUID(getThing().getUID(), "playerControls", CHANNEL_ALBUM);
        updateState(channel, StringType.valueOf("-"));
    }

    public void processUDPEvent(String json, String trackingID) {
        logger.trace("UDP package: {} (Tracking: {})", json, trackingID);
        @Nullable
        UdpMessage targetObject = gson.fromJson(json, UdpMessage.class);
        if (targetObject != null) {
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
        logger.trace("Handling UDP for {}", zoneToUpdate);
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
            if ("on".equals(powerState)) {
                updateState(channel, OnOffType.ON);
            } else if ("standby".equals(powerState)) {
                updateState(channel, OnOffType.OFF);
                powerOffCleanup();
            }
        }

        if (!muteState.isEmpty()) {
            channel = new ChannelUID(getThing().getUID(), zoneToUpdate, CHANNEL_MUTE);
            if ("true".equals(muteState)) {
                updateState(channel, OnOffType.ON);
            } else if ("false".equals(muteState)) {
                updateState(channel, OnOffType.OFF);
            }
        }

        if (!inputState.isEmpty()) {
            channel = new ChannelUID(getThing().getUID(), zoneToUpdate, CHANNEL_INPUT);
            updateState(channel, StringType.valueOf(inputState));
        }

        if (volumeState != 0) {
            channel = new ChannelUID(getThing().getUID(), zoneToUpdate, CHANNEL_VOLUME);
            updateState(channel, new PercentType((volumeState * 100) / maxVolumeState));
            channel = new ChannelUID(getThing().getUID(), zoneToUpdate, CHANNEL_VOLUMEABS);
            updateState(channel, new DecimalType(volumeState));
        }

        if (presetNumber != 0) {
            logger.trace("Preset detected: {}", presetNumber);
            updatePresets(presetNumber);
        }

        if ("true".equals(playInfoUpdated)) {
            updateNetUSBPlayer();
        }

        if (!statusUpdated.isEmpty()) {
            updateStatusZone(zoneToUpdate);
        }
        if (playTime != 0) {
            channel = new ChannelUID(getThing().getUID(), "playerControls", CHANNEL_PLAYTIME);
            updateState(channel, StringType.valueOf(String.valueOf(playTime)));
        }
        if ("true".equals(distInfoUpdated)) {
            updateMCLinkStatus();
        }
    }

    private void updateStatusZone(String zoneToUpdate) {
        String localZone = "";
        tmpString = getStatus(this.host, zoneToUpdate);
        @Nullable
        Status targetObject = gson.fromJson(tmpString, Status.class);
        if (targetObject != null) {
            String responseCode = targetObject.getResponseCode();
            String powerState = targetObject.getPower();
            String muteState = targetObject.getMute();
            volumeState = targetObject.getVolume();
            maxVolumeState = targetObject.getMaxVolume();
            inputState = targetObject.getInput();
            soundProgramState = targetObject.getSoundProgram();
            sleepState = targetObject.getSleep();

            logger.trace("{} - Response: {}", zoneToUpdate, responseCode);
            logger.trace("{} - Power: {}", zoneToUpdate, powerState);
            logger.trace("{} - Mute: {}", zoneToUpdate, muteState);
            logger.trace("{} - Volume: {}", zoneToUpdate, volumeState);
            logger.trace("{} - Max Volume: {}", zoneToUpdate, maxVolumeState);
            logger.trace("{} - Input: {}", zoneToUpdate, inputState);
            logger.trace("{} - Soundprogram: {}", zoneToUpdate, soundProgramState);
            logger.trace("{} - Sleep: {}", zoneToUpdate, sleepState);

            switch (responseCode) {
                case "0":
                    for (Channel channel : getThing().getChannels()) {
                        ChannelUID channelUID = channel.getUID();
                        channelWithoutGroup = channelUID.getIdWithoutGroup();
                        localZone = channelUID.getGroupId();
                        if (localZone != null) {
                            if (isLinked(channelUID)) {
                                switch (channelWithoutGroup) {
                                    case CHANNEL_POWER:
                                        if ("on".equals(powerState)) {
                                            if (localZone.equals(zoneToUpdate)) {
                                                updateState(channelUID, OnOffType.ON);
                                            }
                                        } else if ("standby".equals(powerState)) {
                                            if (localZone.equals(zoneToUpdate)) {
                                                updateState(channelUID, OnOffType.OFF);
                                            }
                                        }
                                        break;
                                    case CHANNEL_MUTE:
                                        if ("true".equals(muteState)) {
                                            if (localZone.equals(zoneToUpdate)) {
                                                updateState(channelUID, OnOffType.ON);
                                            }
                                        } else if ("false".equals(muteState)) {
                                            if (localZone.equals(zoneToUpdate)) {
                                                updateState(channelUID, OnOffType.OFF);
                                            }
                                        }
                                        break;
                                    case CHANNEL_VOLUME:
                                        if (localZone.equals(zoneToUpdate)) {
                                            updateState(channelUID,
                                                    new PercentType((volumeState * 100) / maxVolumeState));
                                        }
                                        break;
                                    case CHANNEL_VOLUMEABS:
                                        if (localZone.equals(zoneToUpdate)) {
                                            updateState(channelUID, new DecimalType(volumeState));
                                        }
                                        break;
                                    case CHANNEL_INPUT:
                                        if (localZone.equals(zoneToUpdate)) {
                                            updateState(channelUID, StringType.valueOf(inputState));
                                        }
                                        break;
                                    case CHANNEL_SOUNDPROGRAM:
                                        if (localZone.equals(zoneToUpdate)) {
                                            updateState(channelUID, StringType.valueOf(soundProgramState));
                                        }
                                        break;
                                    case CHANNEL_SLEEP:
                                        if (localZone.equals(zoneToUpdate)) {
                                            updateState(channelUID, new DecimalType(sleepState));
                                        }
                                        break;
                                } // END switch (channelWithoutGroup)
                            } // END IsLinked
                        }
                    }
                    break;
                case "999":
                    logger.trace("Nothing to do! - {} ({})", thingLabel, zoneToUpdate);
                    break;
            }
        }
    }

    private void updatePresets(int value) {
        String inputText = "";
        int presetCounter = 0;
        int currentPreset = 0;
        tmpString = getPresetInfo(this.host);

        PresetInfo presetinfo = gson.fromJson(tmpString, PresetInfo.class);
        if (presetinfo != null) {
            String responseCode = presetinfo.getResponseCode();
            if ("0".equals(responseCode)) {
                List<StateOption> optionsPresets = new ArrayList<>();
                inputText = getLastInput();
                if (inputText != null) {
                    for (JsonElement pr : presetinfo.getPresetInfo()) {
                        presetCounter = presetCounter + 1;
                        JsonObject presetObject = pr.getAsJsonObject();
                        String text = presetObject.get("text").getAsString();
                        if (!"".equals(text)) {
                            optionsPresets.add(new StateOption(String.valueOf(presetCounter),
                                    "#" + String.valueOf(presetCounter) + " " + text));
                            if (inputText.equals(text)) {
                                currentPreset = presetCounter;
                            }
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
    }

    private void updateNetUSBPlayer() {
        tmpString = getPlayInfo(this.host);

        @Nullable
        PlayInfo targetObject = gson.fromJson(tmpString, PlayInfo.class);
        if (targetObject != null) {
            String responseCode = targetObject.getResponseCode();
            String playbackState = targetObject.getPlayback();
            artistState = targetObject.getArtist();
            trackState = targetObject.getTrack();
            albumState = targetObject.getAlbum();
            String albumArtUrlState = targetObject.getAlbumArtUrl();
            repeatState = targetObject.getRepeat();
            shuffleState = targetObject.getShuffle();
            playTimeState = targetObject.getPlayTime();
            totalTimeState = targetObject.getTotalTime();

            if ("0".equals(responseCode)) {
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
                updateState(testchannel, StringType.valueOf(artistState));
                testchannel = new ChannelUID(getThing().getUID(), "playerControls", CHANNEL_TRACK);
                updateState(testchannel, StringType.valueOf(trackState));
                testchannel = new ChannelUID(getThing().getUID(), "playerControls", CHANNEL_ALBUM);
                updateState(testchannel, StringType.valueOf(albumState));
                testchannel = new ChannelUID(getThing().getUID(), "playerControls", CHANNEL_ALBUMART);
                if (!"".equals(albumArtUrlState)) {
                    albumArtUrlState = HTTP + this.host + albumArtUrlState;
                }
                updateState(testchannel, StringType.valueOf(albumArtUrlState));
                testchannel = new ChannelUID(getThing().getUID(), "playerControls", CHANNEL_REPEAT);
                updateState(testchannel, StringType.valueOf(repeatState));
                testchannel = new ChannelUID(getThing().getUID(), "playerControls", CHANNEL_SHUFFLE);
                updateState(testchannel, StringType.valueOf(shuffleState));
                testchannel = new ChannelUID(getThing().getUID(), "playerControls", CHANNEL_PLAYTIME);
                updateState(testchannel, StringType.valueOf(String.valueOf(playTimeState)));
                testchannel = new ChannelUID(getThing().getUID(), "playerControls", CHANNEL_TOTALTIME);
                updateState(testchannel, StringType.valueOf(String.valueOf(totalTimeState)));
            }
        }
    }

    private @Nullable String getLastInput() {
        String text = "";
        tmpString = getRecentInfo(this.host);
        RecentInfo recentinfo = gson.fromJson(tmpString, RecentInfo.class);
        if (recentinfo != null) {
            String responseCode = recentinfo.getResponseCode();
            if ("0".equals(responseCode)) {
                for (JsonElement ri : recentinfo.getRecentInfo()) {
                    JsonObject recentObject = ri.getAsJsonObject();
                    text = recentObject.get("text").getAsString();
                    break;
                }
            }
        }
        return text;
    }

    private String connectedServer() {
        DistributionInfo distributioninfo = new DistributionInfo();
        Bridge bridge = getBridge();
        String remotehost = "";
        String result = "";
        String localHost = "";
        if (bridge != null) {
            for (Thing thing : bridge.getThings()) {
                remotehost = thing.getConfiguration().get("host").toString();
                tmpString = getDistributionInfo(remotehost);
                distributioninfo = gson.fromJson(tmpString, DistributionInfo.class);
                if (distributioninfo != null) {
                    String localRole = distributioninfo.getRole();
                    if ("server".equals(localRole)) {
                        for (JsonElement ip : distributioninfo.getClientList()) {
                            JsonObject clientObject = ip.getAsJsonObject();
                            localHost = getThing().getConfiguration().get("host").toString();
                            if (localHost.equals(clientObject.get("ip_address").getAsString())) {
                                result = remotehost;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private void fillOptionsForMCLink() {
        Bridge bridge = getBridge();
        String host = "";
        String label = "";
        int zonesPerHost = 1;
        int clients = 0;
        tmpString = getDistributionInfo(this.host);
        DistributionInfo targetObject = gson.fromJson(tmpString, DistributionInfo.class);
        if (targetObject != null) {
            clients = targetObject.getClientList().size();
        }

        List<StateOption> options = new ArrayList<>();
        // first add 3 options for MC Link
        options.add(new StateOption("", "Standalone"));
        options.add(new StateOption("server", "Server: " + clients + " clients"));
        options.add(new StateOption("client", "Client"));

        if (bridge != null) {
            for (Thing thing : bridge.getThings()) {
                label = thing.getLabel();
                host = thing.getConfiguration().get("host").toString();
                logger.trace("Thing found on Bridge: {} - {}", label, host);
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
        ChannelUID testchannel;
        for (int i = 1; i <= zoneNum; i++) {
            switch (i) {
                case 1:
                    testchannel = new ChannelUID(getThing().getUID(), "main", CHANNEL_MCLINKSTATUS);
                    if (isLinked(testchannel)) {
                        stateDescriptionProvider.setStateOptions(testchannel, options);
                    }
                    break;
                case 2:
                    testchannel = new ChannelUID(getThing().getUID(), "zone2", CHANNEL_MCLINKSTATUS);
                    if (isLinked(testchannel)) {
                        stateDescriptionProvider.setStateOptions(testchannel, options);
                    }
                    break;
                case 3:
                    testchannel = new ChannelUID(getThing().getUID(), "zone3", CHANNEL_MCLINKSTATUS);
                    if (isLinked(testchannel)) {
                        stateDescriptionProvider.setStateOptions(testchannel, options);
                    }
                    break;
                case 4:
                    testchannel = new ChannelUID(getThing().getUID(), "zone4", CHANNEL_MCLINKSTATUS);
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
        tmpString = getFeatures(host);
        @Nullable
        Features targetObject = gson.fromJson(tmpString, Features.class);
        if (targetObject != null) {
            responseCode = targetObject.getResponseCode();
            if ("0".equals(responseCode)) {
                numberOfZones = targetObject.getSystem().getZoneNum();
            }
        }
        return numberOfZones;
    }

    public @Nullable String getDeviceId() {
        tmpString = getDeviceInfo(this.host);
        String localValueToCheck = "";
        @Nullable
        DeviceInfo targetObject = gson.fromJson(tmpString, DeviceInfo.class);
        if (targetObject != null) {
            localValueToCheck = targetObject.getDeviceId();
        }
        return localValueToCheck;
    }

    private void setVolumeLinkedDevice(int value, @Nullable String zone, String host) {
        logger.trace("setVolumeLinkedDevice: {}", host);
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
                    if (targetObject != null) {
                        responseCode = targetObject.getResponseCode();
                        maxVolumeLinkedDevice = targetObject.getMaxVolume();
                        newVolume = maxVolumeLinkedDevice * value / 100;
                        setVolume(newVolume, "main", host);
                    }
                    break;
                case 2:
                    tmpString = getStatus(host, "zone2");
                    targetObject = gson.fromJson(tmpString, Status.class);
                    if (targetObject != null) {
                        responseCode = targetObject.getResponseCode();
                        maxVolumeLinkedDevice = targetObject.getMaxVolume();
                        newVolume = maxVolumeLinkedDevice * value / 100;
                        setVolume(newVolume, "zone2", host);
                    }
                    break;
                case 3:
                    tmpString = getStatus(host, "zone3");
                    targetObject = gson.fromJson(tmpString, Status.class);
                    if (targetObject != null) {
                        responseCode = targetObject.getResponseCode();
                        maxVolumeLinkedDevice = targetObject.getMaxVolume();
                        newVolume = maxVolumeLinkedDevice * value / 100;
                        setVolume(newVolume, "zone3", host);
                    }
                    break;
                case 4:
                    tmpString = getStatus(host, "zone4");
                    targetObject = gson.fromJson(tmpString, Status.class);
                    if (targetObject != null) {
                        responseCode = targetObject.getResponseCode();
                        maxVolumeLinkedDevice = targetObject.getMaxVolume();
                        newVolume = maxVolumeLinkedDevice * value / 100;
                        setVolume(newVolume, "zone4", host);
                    }
                    break;
            }
        }
    }

    public void updateMCLinkStatus() {
        tmpString = getDistributionInfo(this.host);
        @Nullable
        DistributionInfo targetObject = gson.fromJson(tmpString, DistributionInfo.class);
        if (targetObject != null) {
            String localRole = targetObject.getRole();
            groupId = targetObject.getGroupId();
            switch (localRole) {
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
    }

    private void setMCLinkToStandalone() {
        ChannelUID testchannel;
        for (int i = 1; i <= zoneNum; i++) {
            switch (i) {
                case 1:
                    testchannel = new ChannelUID(getThing().getUID(), "main", CHANNEL_MCLINKSTATUS);
                    updateState(testchannel, StringType.valueOf(""));
                    break;
                case 2:
                    testchannel = new ChannelUID(getThing().getUID(), "zone2", CHANNEL_MCLINKSTATUS);
                    updateState(testchannel, StringType.valueOf(""));
                    break;
                case 3:
                    testchannel = new ChannelUID(getThing().getUID(), "zone3", CHANNEL_MCLINKSTATUS);
                    updateState(testchannel, StringType.valueOf(""));
                    break;
                case 4:
                    testchannel = new ChannelUID(getThing().getUID(), "zone4", CHANNEL_MCLINKSTATUS);
                    updateState(testchannel, StringType.valueOf(""));
                    break;
            }
        }
    }

    private void setMCLinkToClient() {
        ChannelUID testchannel;
        for (int i = 1; i <= zoneNum; i++) {
            switch (i) {
                case 1:
                    testchannel = new ChannelUID(getThing().getUID(), "main", CHANNEL_MCLINKSTATUS);
                    updateState(testchannel, StringType.valueOf("client"));
                    break;
                case 2:
                    testchannel = new ChannelUID(getThing().getUID(), "zone2", CHANNEL_MCLINKSTATUS);
                    updateState(testchannel, StringType.valueOf("client"));
                    break;
                case 3:
                    testchannel = new ChannelUID(getThing().getUID(), "zone3", CHANNEL_MCLINKSTATUS);
                    updateState(testchannel, StringType.valueOf("client"));
                    break;
                case 4:
                    testchannel = new ChannelUID(getThing().getUID(), "zone4", CHANNEL_MCLINKSTATUS);
                    updateState(testchannel, StringType.valueOf("client"));
                    break;
            }
        }
    }

    private void setMCLinkToServer() {
        ChannelUID testchannel;
        for (int i = 1; i <= zoneNum; i++) {
            switch (i) {
                case 1:
                    testchannel = new ChannelUID(getThing().getUID(), "main", CHANNEL_MCLINKSTATUS);
                    updateState(testchannel, StringType.valueOf("server"));
                    break;
                case 2:
                    testchannel = new ChannelUID(getThing().getUID(), "zone2", CHANNEL_MCLINKSTATUS);
                    updateState(testchannel, StringType.valueOf("server"));
                    break;
                case 3:
                    testchannel = new ChannelUID(getThing().getUID(), "zone3", CHANNEL_MCLINKSTATUS);
                    updateState(testchannel, StringType.valueOf("server"));
                    break;
                case 4:
                    testchannel = new ChannelUID(getThing().getUID(), "zone4", CHANNEL_MCLINKSTATUS);
                    updateState(testchannel, StringType.valueOf("server"));
                    break;
            }
        }
    }

    private String makeRequest(@Nullable String topicAVR, String url) {
        String response = "";
        try {
            response = HttpUtil.executeUrl("GET", HTTP + url, LONG_CONNECTION_TIMEOUT_MILLISEC);
            logger.trace("{} - {}", topicAVR, response);
            return response;
        } catch (IOException e) {
            logger.trace("IO Exception - {} - {}", topicAVR, e.getMessage());
            return "{\"response_code\":\"999\"}";
        }
    }
    // End Various functions

    // API calls to AVR

    // Start Zone Related

    private @Nullable String getStatus(@Nullable String host, String zone) {
        return makeRequest("Status", host + YAMAHA_EXTENDED_CONTROL + zone + "/getStatus");
    }

    private @Nullable String setPower(String value, @Nullable String zone, @Nullable String host) {
        return makeRequest("Power", host + YAMAHA_EXTENDED_CONTROL + zone + "/setPower?power=" + value);
    }

    private @Nullable String setMute(String value, @Nullable String zone, @Nullable String host) {
        return makeRequest("Mute", host + YAMAHA_EXTENDED_CONTROL + zone + "/setMute?enable=" + value);
    }

    private @Nullable String setVolume(int value, @Nullable String zone, @Nullable String host) {
        return makeRequest("Volume", host + YAMAHA_EXTENDED_CONTROL + zone + "/setVolume?volume=" + value);
    }

    private @Nullable String setInput(String value, @Nullable String zone, @Nullable String host) {
        return makeRequest("setInput", host + YAMAHA_EXTENDED_CONTROL + zone + "/setInput?input=" + value);
    }

    private @Nullable String setSoundProgram(String value, @Nullable String zone, @Nullable String host) {
        return makeRequest("setSoundProgram",
                host + YAMAHA_EXTENDED_CONTROL + zone + "/setSoundProgram?program=" + value);
    }

    private @Nullable String setPreset(String value, @Nullable String zone, @Nullable String host) {
        return makeRequest("setPreset",
                host + YAMAHA_EXTENDED_CONTROL + "netusb/recallPreset?zone=" + zone + "&num=" + value);
    }

    private @Nullable String setSleep(String value, @Nullable String zone, @Nullable String host) {
        return makeRequest("setSleep", host + YAMAHA_EXTENDED_CONTROL + zone + "/setSleep?sleep=" + value);
    }

    private @Nullable String recallScene(String value, @Nullable String zone, @Nullable String host) {
        return makeRequest("recallScene", host + YAMAHA_EXTENDED_CONTROL + zone + "/recallScene?num=" + value);
    }
    // End Zone Related

    // Start Net Radio/USB Related

    private @Nullable String getPresetInfo(@Nullable String host) {
        return makeRequest("PresetInfo", host + YAMAHA_EXTENDED_CONTROL + "netusb/getPresetInfo");
    }

    private @Nullable String getRecentInfo(@Nullable String host) {
        return makeRequest("RecentInfo", host + YAMAHA_EXTENDED_CONTROL + "netusb/getRecentInfo");
    }

    private @Nullable String getPlayInfo(@Nullable String host) {
        return makeRequest("PlayInfo", host + YAMAHA_EXTENDED_CONTROL + "netusb/getPlayInfo");
    }

    private @Nullable String setPlayback(String value, @Nullable String host) {
        return makeRequest("Playback", host + YAMAHA_EXTENDED_CONTROL + "netusb/setPlayback?playback=" + value);
    }

    private @Nullable String setRepeat(String value, @Nullable String host) {
        return makeRequest("Repeat", host + YAMAHA_EXTENDED_CONTROL + "netusb/setRepeat?mode=" + value);
    }

    private @Nullable String setShuffle(String value, @Nullable String host) {
        return makeRequest("Shuffle", host + YAMAHA_EXTENDED_CONTROL + "netusb/setShuffle?mode=" + value);
    }

    // End Net Radio/USB Related

    // Start Music Cast API calls
    private @Nullable String getDistributionInfo(@Nullable String host) {
        return makeRequest("DistributionInfo", host + YAMAHA_EXTENDED_CONTROL + "dist/getDistributionInfo");
    }

    private @Nullable String setClientServerInfo(@Nullable String host, String json, String type) {
        InputStream is = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        try {
            url = "http://" + host + YAMAHA_EXTENDED_CONTROL + "dist/" + type;
            httpResponse = HttpUtil.executeUrl("POST", url, is, "", LONG_CONNECTION_TIMEOUT_MILLISEC);
            logger.trace("MC Link/Unlink Client {}", httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.trace("IO Exception - {} - {}", type, e.getMessage());
            return "{\"response_code\":\"999\"}";
        }
    }

    private @Nullable String startDistribution(@Nullable String host) {
        Random ran = new Random();
        int nxt = ran.nextInt(200000);
        return makeRequest("StartDistribution", host + YAMAHA_EXTENDED_CONTROL + "dist/startDistribution?num=" + nxt);
    }

    // End Music Cast API calls

    // Start General/System API calls

    private @Nullable String getFeatures(@Nullable String host) {
        return makeRequest("Features", host + YAMAHA_EXTENDED_CONTROL + "system/getFeatures");
    }

    private @Nullable String getDeviceInfo(@Nullable String host) {
        return makeRequest("DeviceInfo", host + YAMAHA_EXTENDED_CONTROL + "system/getDeviceInfo");
    }

    private void keepUdpEventsAlive(@Nullable String host) {
        Properties appProps = new Properties();
        appProps.setProperty("X-AppName", "MusicCast/1");
        appProps.setProperty("X-AppPort", "41100");
        try {
            httpResponse = HttpUtil.executeUrl("GET", HTTP + host + YAMAHA_EXTENDED_CONTROL + "netusb/getPlayInfo",
                    appProps, null, "", LONG_CONNECTION_TIMEOUT_MILLISEC);
            // logger.trace("{}", httpResponse);
            logger.trace("{} - {}", "UDP task", httpResponse);
        } catch (IOException e) {
            logger.trace("UDP refresh failed - {}", e.getMessage());
        }
    }
    // End General/System API calls
}
