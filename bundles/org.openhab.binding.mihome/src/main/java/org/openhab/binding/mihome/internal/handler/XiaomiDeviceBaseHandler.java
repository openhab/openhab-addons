/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mihome.internal.handler;

import static org.openhab.binding.mihome.internal.XiaomiGatewayBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.openhab.binding.mihome.internal.XiaomiItemUpdateListener;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link XiaomiDeviceBaseHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Patrick Boos - Initial contribution
 * @author Kuba Wolanin - Added voltage and low battery report
 * @author Dieter Schmidt - Added cube rotation, heartbeat and voltage handling, configurable window and motion delay,
 *         Aqara
 *         switches
 * @author Daniel Walters - Added Aqara Door/Window sensor and Aqara temperature, humidity and pressure sensor
 */
public class XiaomiDeviceBaseHandler extends BaseThingHandler implements XiaomiItemUpdateListener {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(THING_TYPE_GATEWAY,
            THING_TYPE_SENSOR_HT, THING_TYPE_SENSOR_AQARA_WEATHER_V1, THING_TYPE_SENSOR_MOTION,
            THING_TYPE_SENSOR_AQARA_MOTION, THING_TYPE_SENSOR_SWITCH, THING_TYPE_SENSOR_AQARA_SWITCH,
            THING_TYPE_SENSOR_MAGNET, THING_TYPE_SENSOR_AQARA_LOCK, THING_TYPE_SENSOR_AQARA_MAGNET,
            THING_TYPE_SENSOR_CUBE, THING_TYPE_SENSOR_AQARA_VIBRATION, THING_TYPE_SENSOR_AQARA1,
            THING_TYPE_SENSOR_AQARA2, THING_TYPE_SENSOR_GAS, THING_TYPE_SENSOR_SMOKE, THING_TYPE_SENSOR_WATER,
            THING_TYPE_ACTOR_AQARA1, THING_TYPE_ACTOR_AQARA2, THING_TYPE_ACTOR_PLUG, THING_TYPE_ACTOR_AQARA_ZERO1,
            THING_TYPE_ACTOR_AQARA_ZERO2, THING_TYPE_ACTOR_CURTAIN, THING_TYPE_BASIC));

    protected static final Unit<Temperature> TEMPERATURE_UNIT = SIUnits.CELSIUS;
    protected static final Unit<Pressure> PRESSURE_UNIT = KILO(SIUnits.PASCAL);
    protected static final Unit<Dimensionless> PERCENT_UNIT = Units.PERCENT;
    protected static final Unit<Angle> ANGLE_UNIT = Units.DEGREE_ANGLE;
    protected static final Unit<Time> TIME_UNIT = MILLI(Units.SECOND);

    private static final String REMOVE_DEVICE = "remove_device";
    private static final long ONLINE_TIMEOUT_MILLIS = TimeUnit.HOURS.toMillis(2);
    private ScheduledFuture<?> onlineCheckTask;

    private XiaomiBridgeHandler bridgeHandler;

    private String itemId;

    private final void setItemId(String itemId) {
        this.itemId = itemId;
    }

    private final Logger logger = LoggerFactory.getLogger(XiaomiDeviceBaseHandler.class);

    public XiaomiDeviceBaseHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        setItemId((String) getConfig().get(ITEM_ID));
        onlineCheckTask = scheduler.scheduleWithFixedDelay(this::updateThingStatus, 0, ONLINE_TIMEOUT_MILLIS / 2,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposes. Unregistering listener");
        if (getItemId() != null) {
            if (bridgeHandler != null) {
                bridgeHandler.unregisterItemListener(this);
                bridgeHandler = null;
            }
            setItemId(null);
        }
        if (!onlineCheckTask.isDone()) {
            onlineCheckTask.cancel(false);
        }
    }

    @Override
    public void handleRemoval() {
        getXiaomiBridgeHandler().writeToBridge(new String[] { REMOVE_DEVICE }, new Object[] { itemId });
        super.handleRemoval();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Device {} on channel {} received command {}", getItemId(), channelUID, command);
        if (command instanceof RefreshType) {
            JsonObject message = getXiaomiBridgeHandler().getDeferredMessage(getItemId());
            if (message != null) {
                String cmd = message.get("cmd").getAsString();
                logger.debug("Update Item {} with retented message", getItemId());
                onItemUpdate(getItemId(), cmd, message);
            }
            return;
        }
        execute(channelUID, command);
    }

    @Override
    public void onItemUpdate(String sid, String command, JsonObject message) {
        if (getItemId() != null && getItemId().equals(sid)) {
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
            logger.debug("Item got update: {}", message);
            try {
                JsonObject data = JsonParser.parseString(message.get("data").getAsString()).getAsJsonObject();
                parseCommand(command, data);
                if (THING_TYPE_BASIC.equals(getThing().getThingTypeUID())) {
                    parseDefault(message);
                }
            } catch (JsonSyntaxException e) {
                logger.warn("Unable to parse message as valid JSON: {}", message);
            }
        }
    }

    @Override
    public String getItemId() {
        return itemId;
    }

    void parseCommand(String command, JsonObject data) {
        switch (command) {
            case "report":
                parseReport(data);
                break;
            case "heartbeat":
                parseHeartbeat(data);
                break;
            case "read_ack":
                parseReadAck(data);
                break;
            case "write_ack":
                parseWriteAck(data);
                break;
            default:
                logger.debug("Device {} got unknown command {}", getItemId(), command);
        }
    }

    void parseReport(JsonObject data) {
        updateState(CHANNEL_REPORT_MSG, StringType.valueOf(data.toString()));
    }

    void parseHeartbeat(JsonObject data) {
        updateState(CHANNEL_HEARTBEAT_MSG, StringType.valueOf(data.toString()));
    }

    void parseReadAck(JsonObject data) {
        updateState(CHANNEL_READ_ACK_MSG, StringType.valueOf(data.toString()));
    }

    void parseWriteAck(JsonObject data) {
        updateState(CHANNEL_WRITE_ACK_MSG, StringType.valueOf(data.toString()));
    }

    void parseDefault(JsonObject data) {
        updateState(CHANNEL_LAST_MSG, StringType.valueOf(data.toString()));
    }

    void execute(ChannelUID channelUID, Command command) {
        if (CHANNEL_WRITE_MSG.equals(channelUID.getId())) {
            if (command instanceof StringType str) {
                getXiaomiBridgeHandler().writeToDevice(itemId, str.toFullString());
            } else {
                logger.debug("Command \"{}\" has to be of StringType", command);
            }
        } else {
            logger.debug("Received command on read-only channel, thus ignoring it.");
        }
    }

    private void updateThingStatus() {
        if (getItemId() != null) {
            // note: this call implicitly registers our handler as a listener on the bridge, if it's not already
            if (getXiaomiBridgeHandler() != null) {
                Bridge bridge = getBridge();
                ThingStatus bridgeStatus = (bridge == null) ? null : bridge.getStatus();
                if (bridgeStatus == ThingStatus.ONLINE) {
                    ThingStatus itemStatus = getThing().getStatus();
                    boolean hasItemActivity = getXiaomiBridgeHandler().hasItemActivity(getItemId(),
                            ONLINE_TIMEOUT_MILLIS);
                    ThingStatus newStatus = hasItemActivity ? ThingStatus.ONLINE : ThingStatus.OFFLINE;

                    if (!newStatus.equals(itemStatus)) {
                        updateStatus(newStatus);
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
    }

    synchronized XiaomiBridgeHandler getXiaomiBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof XiaomiBridgeHandler xiaomiBridgeHandler) {
                this.bridgeHandler = xiaomiBridgeHandler;
                this.bridgeHandler.registerItemListener(this);
            } else {
                return null;
            }
        }
        return this.bridgeHandler;
    }
}
