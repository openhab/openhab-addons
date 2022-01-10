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
package org.openhab.binding.irobot.internal.handler;

import static org.openhab.binding.irobot.internal.IRobotBindingConstants.BIN_FULL;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.BIN_OK;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.BIN_REMOVED;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.BOOST_AUTO;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.BOOST_ECO;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.BOOST_PERFORMANCE;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.CHANNEL_ALWAYS_FINISH;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.CHANNEL_BATTERY;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.CHANNEL_BIN;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.CHANNEL_CLEAN_PASSES;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.CHANNEL_COMMAND;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.CHANNEL_CYCLE;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.CHANNEL_EDGE_CLEAN;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.CHANNEL_ERROR;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.CHANNEL_LAST_COMMAND;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.CHANNEL_MAP_UPLOAD;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.CHANNEL_PHASE;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.CHANNEL_POWER_BOOST;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.CHANNEL_RSSI;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.CHANNEL_SCHEDULE;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.CHANNEL_SCHED_SWITCH;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.CHANNEL_SCHED_SWITCH_PREFIX;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.CHANNEL_SNR;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.CMD_CLEAN;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.CMD_CLEAN_REGIONS;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.CMD_DOCK;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.CMD_PAUSE;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.CMD_STOP;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.PASSES_1;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.PASSES_2;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.PASSES_AUTO;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.ROBOT_BLID;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.ROBOT_PASSWORD;
import static org.openhab.binding.irobot.internal.IRobotBindingConstants.UNKNOWN;
import static org.openhab.core.thing.ThingStatus.INITIALIZING;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.UNINITIALIZED;

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.irobot.internal.config.IRobotConfiguration;
import org.openhab.binding.irobot.internal.dto.MQTTProtocol;
import org.openhab.binding.irobot.internal.utils.LoginRequester;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

/**
 * The {@link RoombaHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author hkuhn42 - Initial contribution
 * @author Pavel Fedin - Rewrite for 900 series
 * @author Florian Binder - added cleanRegions command and lastCommand channel
 * @author Alexander Falkenstern - Add support for I7 series
 */
@NonNullByDefault
public class RoombaHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(RoombaHandler.class);

    private final Gson gson = new Gson();

    private Hashtable<String, State> lastState = new Hashtable<>();
    private MQTTProtocol.@Nullable Schedule lastSchedule = null;
    private boolean autoPasses = true;
    private @Nullable Boolean twoPasses = null;
    private boolean carpetBoost = true;
    private @Nullable Boolean vacHigh = null;
    private boolean isPaused = false;

    private @Nullable Future<?> credentialRequester;
    protected IRobotConnectionHandler connection = new IRobotConnectionHandler() {
        @Override
        public void receive(final String topic, final String json) {
            RoombaHandler.this.receive(topic, json);
        }

        @Override
        public void connectionStateChanged(MqttConnectionState state, @Nullable Throwable error) {
            super.connectionStateChanged(state, error);
            if (state == MqttConnectionState.CONNECTED) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                String message = (error != null) ? error.getMessage() : "Unknown reason";
                updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
            }
        }
    };

    public RoombaHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        IRobotConfiguration config = getConfigAs(IRobotConfiguration.class);

        if (UNKNOWN.equals(config.getPassword()) || UNKNOWN.equals(config.getBlid())) {
            final String message = "Robot authentication is required";
            updateStatus(OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
            scheduler.execute(this::getCredentials);
        } else {
            scheduler.execute(this::connect);
        }
    }

    @Override
    public void dispose() {
        Future<?> requester = credentialRequester;
        if (requester != null) {
            requester.cancel(false);
            credentialRequester = null;
        }

        scheduler.execute(connection::disconnect);
    }

    // lastState.get() can return null if the key is not found according
    // to the documentation
    @SuppressWarnings("null")
    private void handleRefresh(String ch) {
        State value = lastState.get(ch);

        if (value != null) {
            updateState(ch, value);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String ch = channelUID.getId();
        if (command instanceof RefreshType) {
            handleRefresh(ch);
            return;
        }

        if (ch.equals(CHANNEL_COMMAND)) {
            if (command instanceof StringType) {
                String cmd = command.toString();

                if (cmd.equals(CMD_CLEAN)) {
                    cmd = isPaused ? "resume" : "start";
                }

                if (cmd.startsWith(CMD_CLEAN_REGIONS)) {
                    // format: cleanRegions:<pmid>;<region_id1>,<region_id2>,...
                    if (Pattern.matches("cleanRegions:[^:;,]+;.+(,[^:;,]+)*", cmd)) {
                        String[] cmds = cmd.split(":");
                        String[] params = cmds[1].split(";");

                        String mapId = params[0];
                        String userPmapvId;
                        if (params.length >= 3) {
                            userPmapvId = params[2];
                        } else {
                            userPmapvId = null;
                        }

                        String[] regions = params[1].split(",");
                        String regionIds[] = new String[regions.length];
                        String regionTypes[] = new String[regions.length];

                        for (int i = 0; i < regions.length; i++) {
                            String[] regionDetails = regions[i].split("=");

                            if (regionDetails.length >= 2) {
                                if (regionDetails[0].equals("r")) {
                                    regionIds[i] = regionDetails[1];
                                    regionTypes[i] = "rid";
                                } else if (regionDetails[0].equals("z")) {
                                    regionIds[i] = regionDetails[1];
                                    regionTypes[i] = "zid";
                                } else {
                                    regionIds[i] = regionDetails[0];
                                    regionTypes[i] = "rid";
                                }
                            } else {
                                regionIds[i] = regionDetails[0];
                                regionTypes[i] = "rid";
                            }
                        }
                        MQTTProtocol.Request request = new MQTTProtocol.CleanRoomsRequest("start", mapId, regionIds,
                                regionTypes, userPmapvId);
                        connection.send(request.getTopic(), gson.toJson(request));
                    } else {
                        logger.warn("Invalid request: {}", cmd);
                        logger.warn("Correct format: cleanRegions:<pmid>;<region_id1>,<region_id2>,...>");
                    }
                } else {
                    MQTTProtocol.Request request = new MQTTProtocol.CommandRequest(cmd);
                    connection.send(request.getTopic(), gson.toJson(request));
                }
            }
        } else if (ch.startsWith(CHANNEL_SCHED_SWITCH_PREFIX)) {
            MQTTProtocol.Schedule schedule = lastSchedule;

            // Schedule can only be updated in a bulk, so we have to store current
            // schedule and modify components.
            if (command instanceof OnOffType && schedule != null && schedule.cycle != null) {
                for (int i = 0; i < CHANNEL_SCHED_SWITCH.length; i++) {
                    if (ch.equals(CHANNEL_SCHED_SWITCH[i])) {
                        MQTTProtocol.Schedule newSchedule = new MQTTProtocol.Schedule(schedule.cycle);

                        newSchedule.enableCycle(i, command.equals(OnOffType.ON));
                        sendSchedule(newSchedule);
                        break;
                    }
                }
            }
        } else if (ch.equals(CHANNEL_SCHEDULE)) {
            if (command instanceof DecimalType) {
                int bitmask = ((DecimalType) command).intValue();
                JsonArray cycle = new JsonArray();

                for (int i = 0; i < CHANNEL_SCHED_SWITCH.length; i++) {
                    enableCycle(cycle, i, (bitmask & (1 << i)) != 0);
                }

                sendSchedule(new MQTTProtocol.Schedule(bitmask));
            }
        } else if (ch.equals(CHANNEL_EDGE_CLEAN)) {
            if (command instanceof OnOffType) {
                sendDelta(new MQTTProtocol.OpenOnly(command.equals(OnOffType.OFF)));
            }
        } else if (ch.equals(CHANNEL_ALWAYS_FINISH)) {
            if (command instanceof OnOffType) {
                sendDelta(new MQTTProtocol.BinPause(command.equals(OnOffType.OFF)));
            }
        } else if (ch.equals(CHANNEL_POWER_BOOST)) {
            sendDelta(new MQTTProtocol.PowerBoost(command.equals(BOOST_AUTO), command.equals(BOOST_PERFORMANCE)));
        } else if (ch.equals(CHANNEL_CLEAN_PASSES)) {
            sendDelta(new MQTTProtocol.CleanPasses(!command.equals(PASSES_AUTO), command.equals(PASSES_2)));
        } else if (ch.equals(CHANNEL_MAP_UPLOAD)) {
            if (command instanceof OnOffType) {
                sendDelta(new MQTTProtocol.MapUploadAllowed(command.equals(OnOffType.ON)));
            }
        }
    }

    private void enableCycle(JsonArray cycle, int i, boolean enable) {
        JsonPrimitive value = new JsonPrimitive(enable ? "start" : "none");
        cycle.set(i, value);
    }

    private void sendSchedule(MQTTProtocol.Schedule schedule) {
        sendDelta(new MQTTProtocol.CleanSchedule(schedule));
    }

    private void sendDelta(MQTTProtocol.StateValue state) {
        MQTTProtocol.Request request = new MQTTProtocol.DeltaRequest(state);
        connection.send(request.getTopic(), gson.toJson(request));
    }

    private synchronized void getCredentials() {
        ThingStatus status = thing.getStatusInfo().getStatus();
        IRobotConfiguration config = getConfigAs(IRobotConfiguration.class);
        if (UNINITIALIZED.equals(status) || INITIALIZING.equals(status) || OFFLINE.equals(status)) {
            if (UNKNOWN.equals(config.getBlid())) {
                @Nullable
                String blid = null;
                try {
                    blid = LoginRequester.getBlid(config.getIpAddress());
                } catch (IOException exception) {
                    updateStatus(OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, exception.toString());
                }

                if (blid != null) {
                    org.openhab.core.config.core.Configuration configuration = editConfiguration();
                    configuration.put(ROBOT_BLID, blid);
                    updateConfiguration(configuration);
                }
            }

            if (UNKNOWN.equals(config.getPassword())) {
                @Nullable
                String password = null;
                try {
                    password = LoginRequester.getPassword(config.getIpAddress());
                } catch (KeyManagementException | NoSuchAlgorithmException exception) {
                    updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, exception.toString());
                    return; // This is internal system error, no retry
                } catch (IOException exception) {
                    updateStatus(OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, exception.toString());
                }

                if (password != null) {
                    org.openhab.core.config.core.Configuration configuration = editConfiguration();
                    configuration.put(ROBOT_PASSWORD, password.trim());
                    updateConfiguration(configuration);
                }
            }
        }

        credentialRequester = null;
        if (UNKNOWN.equals(config.getBlid()) || UNKNOWN.equals(config.getPassword())) {
            credentialRequester = scheduler.schedule(this::getCredentials, 10000, TimeUnit.MILLISECONDS);
        } else {
            scheduler.execute(this::connect);
        }
    }

    // In order not to mess up our connection state we need to make sure that connect()
    // and disconnect() are never running concurrently, so they are synchronized
    private synchronized void connect() {
        IRobotConfiguration config = getConfigAs(IRobotConfiguration.class);
        final String address = config.getIpAddress();
        logger.debug("Connecting to {}", address);

        final String blid = config.getBlid();
        final String password = config.getPassword();
        if (UNKNOWN.equals(blid) || UNKNOWN.equals(password)) {
            final String message = "Robot authentication is required";
            updateStatus(OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
            scheduler.execute(this::getCredentials);
        } else {
            final String message = "Robot authentication is successful";
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING, message);
            connection.connect(address, blid, password);
        }
    }

    public void receive(final String topic, final String json) {
        MQTTProtocol.StateMessage msg;

        logger.trace("Got topic {} data {}", topic, json);

        try {
            // We are not consuming all the fields, so we have to create the reader explicitly
            // If we use fromJson(String) or fromJson(java.util.reader), it will throw
            // "JSON not fully consumed" exception, because not all the reader's content has been
            // used up. We want to avoid that also for compatibility reasons because newer iRobot
            // versions may add fields.
            JsonReader jsonReader = new JsonReader(new StringReader(json));
            msg = gson.fromJson(jsonReader, MQTTProtocol.StateMessage.class);
        } catch (JsonParseException exception) {
            logger.warn("Failed to parse JSON message for {}: {}", thing.getLabel(), exception.toString());
            logger.warn("Raw contents: {}", json);
            return;
        }

        // Since all the fields are in fact optional, and a single message never
        // contains all of them, we have to check presence of each individually
        if (msg.state == null || msg.state.reported == null) {
            return;
        }

        MQTTProtocol.GenericState reported = msg.state.reported;

        if (reported.cleanMissionStatus != null) {
            String cycle = reported.cleanMissionStatus.cycle;
            String phase = reported.cleanMissionStatus.phase;
            String command;

            if ("none".equals(cycle)) {
                command = CMD_STOP;
            } else {
                switch (phase) {
                    case "stop":
                    case "stuck": // CHECKME: could also be equivalent to "stop" command
                    case "pause": // Never observed in Roomba 930
                        command = CMD_PAUSE;
                        break;
                    case "hmUsrDock":
                    case "dock": // Never observed in Roomba 930
                        command = CMD_DOCK;
                        break;
                    default:
                        command = cycle; // "clean" or "spot"
                        break;
                }
            }

            isPaused = command.equals(CMD_PAUSE);

            reportString(CHANNEL_CYCLE, cycle);
            reportString(CHANNEL_PHASE, phase);
            reportString(CHANNEL_COMMAND, command);
            reportString(CHANNEL_ERROR, String.valueOf(reported.cleanMissionStatus.error));
        }

        if (reported.batPct != null) {
            reportInt(CHANNEL_BATTERY, reported.batPct);
        }

        if (reported.bin != null) {
            String binStatus;

            // The bin cannot be both full and removed simultaneously, so let's
            // encode it as a single value
            if (!reported.bin.present) {
                binStatus = BIN_REMOVED;
            } else if (reported.bin.full) {
                binStatus = BIN_FULL;
            } else {
                binStatus = BIN_OK;
            }

            reportString(CHANNEL_BIN, binStatus);
        }

        if (reported.signal != null) {
            reportInt(CHANNEL_RSSI, reported.signal.rssi);
            reportInt(CHANNEL_SNR, reported.signal.snr);
        }

        if (reported.cleanSchedule != null) {
            MQTTProtocol.Schedule schedule = reported.cleanSchedule;

            if (schedule.cycle != null) {
                int binary = 0;

                for (int i = 0; i < CHANNEL_SCHED_SWITCH.length; i++) {
                    boolean on = schedule.cycleEnabled(i);

                    reportSwitch(CHANNEL_SCHED_SWITCH[i], on);
                    if (on) {
                        binary |= (1 << i);
                    }
                }

                reportInt(CHANNEL_SCHEDULE, binary);
            }

            lastSchedule = schedule;
        }

        if (reported.openOnly != null) {
            reportSwitch(CHANNEL_EDGE_CLEAN, !reported.openOnly);
        }

        if (reported.binPause != null) {
            reportSwitch(CHANNEL_ALWAYS_FINISH, !reported.binPause);
        }

        // To make the life more interesting, paired values may not appear together in the
        // same message, so we have to keep track of current values.
        if (reported.carpetBoost != null) {
            carpetBoost = reported.carpetBoost;
            if (reported.carpetBoost) {
                // When set to true, overrides vacHigh
                reportString(CHANNEL_POWER_BOOST, BOOST_AUTO);
            } else if (vacHigh != null) {
                reportVacHigh();
            }
        }

        if (reported.vacHigh != null) {
            vacHigh = reported.vacHigh;
            if (!carpetBoost) {
                // Can be overridden by "carpetBoost":true
                reportVacHigh();
            }
        }

        if (reported.noAutoPasses != null) {
            autoPasses = !reported.noAutoPasses;
            if (!reported.noAutoPasses) {
                // When set to false, overrides twoPass
                reportString(CHANNEL_CLEAN_PASSES, PASSES_AUTO);
            } else if (twoPasses != null) {
                reportTwoPasses();
            }
        }

        if (reported.twoPass != null) {
            twoPasses = reported.twoPass;
            if (!autoPasses) {
                // Can be overridden by "noAutoPasses":false
                reportTwoPasses();
            }
        }

        if (reported.lastCommand != null) {
            reportString(CHANNEL_LAST_COMMAND, reported.lastCommand.toString());
        }

        if (reported.mapUploadAllowed != null) {
            reportSwitch(CHANNEL_MAP_UPLOAD, reported.mapUploadAllowed);
        }

        reportProperty(Thing.PROPERTY_FIRMWARE_VERSION, reported.softwareVer);
        reportProperty("navSwVer", reported.navSwVer);
        reportProperty("wifiSwVer", reported.wifiSwVer);
        reportProperty("mobilityVer", reported.mobilityVer);
        reportProperty("bootloaderVer", reported.bootloaderVer);
        reportProperty("umiVer", reported.umiVer);
        reportProperty("sku", reported.sku);
        reportProperty("batteryType", reported.batteryType);

        if (reported.subModSwVer != null) {
            // This is used by i7 model. It has more capabilities, perhaps a dedicated
            // handler should be written by someone who owns it.
            reportProperty("subModSwVer.nav", reported.subModSwVer.nav);
            reportProperty("subModSwVer.mob", reported.subModSwVer.mob);
            reportProperty("subModSwVer.pwr", reported.subModSwVer.pwr);
            reportProperty("subModSwVer.sft", reported.subModSwVer.sft);
            reportProperty("subModSwVer.mobBtl", reported.subModSwVer.mobBtl);
            reportProperty("subModSwVer.linux", reported.subModSwVer.linux);
            reportProperty("subModSwVer.con", reported.subModSwVer.con);
        }
    }

    private void reportVacHigh() {
        reportString(CHANNEL_POWER_BOOST, vacHigh ? BOOST_PERFORMANCE : BOOST_ECO);
    }

    private void reportTwoPasses() {
        reportString(CHANNEL_CLEAN_PASSES, twoPasses ? PASSES_2 : PASSES_1);
    }

    private void reportString(String channel, String str) {
        reportState(channel, StringType.valueOf(str));
    }

    private void reportInt(String channel, int n) {
        reportState(channel, new DecimalType(n));
    }

    private void reportSwitch(String channel, boolean s) {
        reportState(channel, OnOffType.from(s));
    }

    private void reportState(String channel, State value) {
        lastState.put(channel, value);
        updateState(channel, value);
    }

    private void reportProperty(String property, @Nullable String value) {
        if (value != null) {
            updateProperty(property, value);
        }
    }
}
