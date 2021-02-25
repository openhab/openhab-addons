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
package org.openhab.binding.irobot.internal.handler;

import static org.openhab.binding.irobot.internal.IRobotBindingConstants.*;

import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.irobot.internal.RawMQTT;
import org.openhab.binding.irobot.internal.RoombaConfiguration;
import org.openhab.binding.irobot.internal.dto.IdentProtocol;
import org.openhab.binding.irobot.internal.dto.IdentProtocol.IdentData;
import org.openhab.binding.irobot.internal.dto.MQTTProtocol;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionObserver;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.openhab.core.io.transport.mqtt.reconnect.PeriodicReconnectStrategy;
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
 */
@NonNullByDefault
public class RoombaHandler extends BaseThingHandler implements MqttConnectionObserver, MqttMessageSubscriber {
    private final Logger logger = LoggerFactory.getLogger(RoombaHandler.class);
    private final Gson gson = new Gson();
    private static final int RECONNECT_DELAY_SEC = 5; // In seconds
    private @Nullable Future<?> reconnectReq;
    // Dummy RoombaConfiguration object in order to shut up Eclipse warnings
    // The real one is set in initialize()
    private RoombaConfiguration config = new RoombaConfiguration();
    private @Nullable String blid = null;
    private @Nullable MqttBrokerConnection connection;
    private Hashtable<String, State> lastState = new Hashtable<>();
    private MQTTProtocol.@Nullable Schedule lastSchedule = null;
    private boolean autoPasses = true;
    private @Nullable Boolean twoPasses = null;
    private boolean carpetBoost = true;
    private @Nullable Boolean vacHigh = null;
    private boolean isPaused = false;

    public RoombaHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(RoombaConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::connect);
    }

    @Override
    public void dispose() {
        scheduler.execute(this::disconnect);
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
                        String[] regionIds = params[1].split(",");

                        sendRequest(new MQTTProtocol.CleanRoomsRequest("start", mapId, regionIds));
                    } else {
                        logger.warn("Invalid request: {}", cmd);
                        logger.warn("Correct format: cleanRegions:<pmid>;<region_id1>,<region_id2>,...>");
                    }
                } else {
                    sendRequest(new MQTTProtocol.CommandRequest(cmd));
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
        sendRequest(new MQTTProtocol.DeltaRequest(state));
    }

    private void sendRequest(MQTTProtocol.Request request) {
        MqttBrokerConnection conn = connection;

        if (conn != null) {
            String json = gson.toJson(request);
            logger.trace("Sending {}: {}", request.getTopic(), json);
            // 1 here actually corresponds to MQTT qos 0 (AT_MOST_ONCE). Only this value is accepted
            // by Roomba, others just cause it to reject the command and drop the connection.
            conn.publish(request.getTopic(), json.getBytes(), 1, false);
        }
    }

    // In order not to mess up our connection state we need to make sure
    // that connect() and disconnect() are never running concurrently, so
    // they are synchronized
    private synchronized void connect() {
        logger.debug("Connecting to {}", config.ipaddress);

        try {
            InetAddress host = InetAddress.getByName(config.ipaddress);
            String blid = this.blid;

            if (blid == null) {
                DatagramSocket identSocket = IdentProtocol.sendRequest(host);
                DatagramPacket identPacket = IdentProtocol.receiveResponse(identSocket);
                IdentProtocol.IdentData ident;

                identSocket.close();

                try {
                    ident = IdentProtocol.decodeResponse(identPacket);
                } catch (JsonParseException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Malformed IDENT response");
                    return;
                }

                if (ident.ver < IdentData.MIN_SUPPORTED_VERSION) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Unsupported version " + ident.ver);
                    return;
                }

                if (!ident.product.equals(IdentData.PRODUCT_ROOMBA)) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Not a Roomba: " + ident.product);
                    return;
                }

                blid = ident.blid;
                this.blid = blid;
            }

            logger.debug("BLID is: {}", blid);

            if (config.password.isEmpty()) {
                RawMQTT mqtt;

                try {
                    mqtt = new RawMQTT(host, 8883);
                } catch (KeyManagementException | NoSuchAlgorithmException e1) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e1.toString());
                    return; // This is internal system error, no retry
                }

                mqtt.requestPassword();
                RawMQTT.Packet response = mqtt.readPacket();
                mqtt.close();

                if (response != null && response.isValidPasswdPacket()) {
                    RawMQTT.PasswdPacket passwdPacket = new RawMQTT.PasswdPacket(response);
                    String password = passwdPacket.getPassword();

                    if (password != null) {
                        config.password = password;

                        Configuration configuration = editConfiguration();

                        configuration.put("password", password);
                        updateConfiguration(configuration);

                        logger.debug("Password successfully retrieved");
                    }
                }
            }

            if (config.password.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                        "Authentication on the robot is required");
                scheduleReconnect();
                return;
            }

            // BLID is used as both client ID and username. The name of BLID also came from Roomba980-python
            MqttBrokerConnection connection = new MqttBrokerConnection(config.ipaddress, RawMQTT.ROOMBA_MQTT_PORT, true,
                    blid);

            this.connection = connection;

            // Disable sending UNSUBSCRIBE request before disconnecting becuase Roomba doesn't like it.
            // It just swallows the request and never sends any response, so stop() method never completes.
            connection.setUnsubscribeOnStop(false);
            connection.setCredentials(blid, config.password);
            connection.setTrustManagers(RawMQTT.getTrustManagers());
            // 1 here actually corresponds to MQTT qos 0 (AT_MOST_ONCE). Only this value is accepted
            // by Roomba, others just cause it to reject the command and drop the connection.
            connection.setQos(1);
            // MQTT connection reconnects itself, so we don't have to call scheduleReconnect()
            // when it breaks. Just set the period in ms.
            connection.setReconnectStrategy(
                    new PeriodicReconnectStrategy(RECONNECT_DELAY_SEC * 1000, RECONNECT_DELAY_SEC * 1000));
            connection.start().exceptionally(e -> {
                connectionStateChanged(MqttConnectionState.DISCONNECTED, e);
                return false;
            }).thenAccept(v -> {
                if (!v) {
                    connectionStateChanged(MqttConnectionState.DISCONNECTED, new TimeoutException("Timeout"));
                } else {
                    connectionStateChanged(MqttConnectionState.CONNECTED, null);
                }
            });
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            scheduleReconnect();
        }
    }

    private synchronized void disconnect() {
        Future<?> reconnectReq = this.reconnectReq;
        MqttBrokerConnection connection = this.connection;

        if (reconnectReq != null) {
            reconnectReq.cancel(false);
            this.reconnectReq = null;
        }

        if (connection != null) {
            connection.stop();
            logger.trace("Closed connection to {}", config.ipaddress);
            this.connection = null;
        }
    }

    private void scheduleReconnect() {
        reconnectReq = scheduler.schedule(this::connect, RECONNECT_DELAY_SEC, TimeUnit.SECONDS);
    }

    public void onConnected() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void processMessage(String topic, byte[] payload) {
        String jsonStr = new String(payload);
        MQTTProtocol.StateMessage msg;

        logger.trace("Got topic {} data {}", topic, jsonStr);

        try {
            // We are not consuming all the fields, so we have to create the reader explicitly
            // If we use fromJson(String) or fromJson(java.util.reader), it will throw
            // "JSON not fully consumed" exception, because not all the reader's content has been
            // used up. We want to avoid that also for compatibility reasons because newer iRobot
            // versions may add fields.
            JsonReader jsonReader = new JsonReader(new StringReader(jsonStr));
            msg = gson.fromJson(jsonReader, MQTTProtocol.StateMessage.class);
        } catch (JsonParseException e) {
            logger.warn("Failed to parse JSON message from {}: {}", config.ipaddress, e.toString());
            logger.warn("Raw contents: {}", payload);
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

            if (cycle.equals("none")) {
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

    @Override
    public void connectionStateChanged(MqttConnectionState state, @Nullable Throwable error) {
        if (state == MqttConnectionState.CONNECTED) {
            MqttBrokerConnection connection = this.connection;

            if (connection == null) {
                // This would be very strange, but Eclipse forces us to do the check
                logger.warn("Established connection without broker pointer");
                return;
            }

            updateStatus(ThingStatus.ONLINE);

            // Roomba sends us two topics:
            // "wifistat" - reports singnal strength and current robot position
            // "$aws/things/<BLID>/shadow/update" - the rest of messages
            // Subscribe to everything since we're interested in both
            connection.subscribe("#", this).exceptionally(e -> {
                logger.warn("MQTT subscription failed: {}", e.getMessage());
                return false;
            }).thenAccept(v -> {
                if (!v) {
                    logger.warn("Subscription timeout");
                } else {
                    logger.trace("Subscription done");
                }
            });

        } else {
            String message;

            if (error != null) {
                message = error.getMessage();
                logger.warn("MQTT connection failed: {}", message);
            } else {
                message = null;
                logger.warn("MQTT connection failed for unspecified reason");
            }

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
        }
    }
}
