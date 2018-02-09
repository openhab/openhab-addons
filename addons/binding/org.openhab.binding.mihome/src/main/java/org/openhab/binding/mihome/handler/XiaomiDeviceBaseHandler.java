/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.handler;

import static org.openhab.binding.mihome.XiaomiGatewayBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.mihome.internal.XiaomiItemUpdateListener;
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
public abstract class XiaomiDeviceBaseHandler extends BaseThingHandler implements XiaomiItemUpdateListener {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(THING_TYPE_GATEWAY,
            THING_TYPE_SENSOR_HT, THING_TYPE_SENSOR_AQARA_WEATHER_V1, THING_TYPE_SENSOR_MOTION,
            THING_TYPE_SENSOR_AQARA_MOTION, THING_TYPE_SENSOR_SWITCH, THING_TYPE_SENSOR_AQARA_SWITCH,
            THING_TYPE_SENSOR_MAGNET, THING_TYPE_SENSOR_AQARA_MAGNET, THING_TYPE_SENSOR_CUBE, THING_TYPE_SENSOR_AQARA1,
            THING_TYPE_SENSOR_AQARA2, THING_TYPE_SENSOR_GAS, THING_TYPE_SENSOR_SMOKE, THING_TYPE_SENSOR_WATER,
            THING_TYPE_ACTOR_AQARA1, THING_TYPE_ACTOR_AQARA2, THING_TYPE_ACTOR_PLUG, THING_TYPE_ACTOR_AQARA_ZERO1,
            THING_TYPE_ACTOR_AQARA_ZERO2, THING_TYPE_ACTOR_CURTAIN));

    private static final long ONLINE_TIMEOUT_MILLIS = 2 * 60 * 60 * 1000; // 2 hours

    private JsonParser parser = new JsonParser();

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
        updateThingStatus();
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
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Device {} on channel {} received command {}", getItemId(), channelUID, command);
        if (command instanceof RefreshType) {
            JsonObject message = getXiaomiBridgeHandler().getRetentedMessage(getItemId());
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
            updateThingStatus();
            logger.debug("Item got update: {}", message);
            try {
                JsonObject data = parser.parse(message.get("data").getAsString()).getAsJsonObject();
                parseCommand(command, data);
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
        logger.debug("The binding does not parse this message yet, contact authors if you want it to");
    }

    void parseHeartbeat(JsonObject data) {
        logger.debug("The binding does not parse this message yet, contact authors if you want it to");
    }

    void parseReadAck(JsonObject data) {
        logger.debug("The binding does not parse this message yet, contact authors if you want it to");
    }

    void parseWriteAck(JsonObject data) {
        logger.debug("The binding does not parse this message yet, contact authors if you want it to");
    }

    abstract void parseDefault(JsonObject data);

    abstract void execute(ChannelUID channelUID, Command command);

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
            if (handler instanceof XiaomiBridgeHandler) {
                this.bridgeHandler = (XiaomiBridgeHandler) handler;
                this.bridgeHandler.registerItemListener(this);
            } else {
                return null;
            }
        }
        return this.bridgeHandler;
    }
}
