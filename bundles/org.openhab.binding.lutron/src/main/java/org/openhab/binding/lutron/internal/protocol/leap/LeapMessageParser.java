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
package org.openhab.binding.lutron.internal.protocol.leap;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.protocol.leap.dto.Area;
import org.openhab.binding.lutron.internal.protocol.leap.dto.ButtonGroup;
import org.openhab.binding.lutron.internal.protocol.leap.dto.Device;
import org.openhab.binding.lutron.internal.protocol.leap.dto.ExceptionDetail;
import org.openhab.binding.lutron.internal.protocol.leap.dto.Header;
import org.openhab.binding.lutron.internal.protocol.leap.dto.OccupancyGroup;
import org.openhab.binding.lutron.internal.protocol.leap.dto.OccupancyGroupStatus;
import org.openhab.binding.lutron.internal.protocol.leap.dto.ZoneStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Class responsible for parsing incoming LEAP messages. Calls back to an object implementing the
 * LeapMessageParserCallbacks interface.
 *
 * Thanks to the authors of the pylutron-caseta Python API (github.com/gurumitts/pylutron-caseta), which I used as a
 * reference when first researching the LEAP protocol.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class LeapMessageParser {
    private final Logger logger = LoggerFactory.getLogger(LeapMessageParser.class);

    private final Gson gson;
    private final LeapMessageParserCallbacks callback;

    /**
     * LeapMessageParser Constructor
     *
     * @param callback Object implementing the LeapMessageParserCallbacks interface
     */
    public LeapMessageParser(LeapMessageParserCallbacks callback) {
        gson = new GsonBuilder().create();
        this.callback = callback;
    }

    /**
     * Parse and process a LEAP protocol message
     *
     * @param msg String containing the LEAP message
     */
    public void handleMessage(String msg) {
        if (msg.trim().equals("")) {
            return; // Ignore empty lines
        }
        logger.trace("Received message: {}", msg);

        try {
            JsonObject message = (JsonObject) JsonParser.parseString(msg);

            if (!message.has("CommuniqueType")) {
                logger.debug("No CommuniqueType found in message: {}", msg);
                return;
            }

            String communiqueType = message.get("CommuniqueType").getAsString();
            // CommuniqueType type = CommuniqueType.valueOf(communiqueType);
            logger.debug("Received CommuniqueType: {}", communiqueType);
            callback.validMessageReceived(communiqueType);

            switch (communiqueType) {
                case "CreateResponse":
                    return;
                case "ReadResponse":
                    handleReadResponseMessage(message);
                    break;
                case "UpdateResponse":
                    break;
                case "SubscribeResponse":
                    // Subscribe responses can contain bodies with data
                    handleReadResponseMessage(message);
                    return;
                case "UnsubscribeResponse":
                    return;
                case "ExceptionResponse":
                    handleExceptionResponse(message);
                    return;
                default:
                    logger.debug("Unknown CommuniqueType received: {}", communiqueType);
                    break;
            }
        } catch (JsonParseException e) {
            logger.debug("Error parsing message: {}", e.getMessage());
            return;
        }
    }

    /**
     * Method called by handleMessage() to handle all LEAP ExceptionResponse messages.
     *
     * @param message LEAP message
     */
    private void handleExceptionResponse(JsonObject message) {
        String detailMessage = "";

        try {
            JsonObject header = message.get("Header").getAsJsonObject();
            Header headerObj = gson.fromJson(header, Header.class);

            if (MessageBodyType.ExceptionDetail.toString().equalsIgnoreCase(headerObj.messageBodyType)
                    && message.has("Body")) {
                JsonObject body = message.get("Body").getAsJsonObject();
                ExceptionDetail exceptionDetail = gson.fromJson(body, ExceptionDetail.class);
                if (exceptionDetail != null) {
                    detailMessage = exceptionDetail.message;
                }
            }
            logger.debug("Exception response received. Status: {} URL: {} Message: {}", headerObj.statusCode,
                    headerObj.url, detailMessage);

        } catch (JsonParseException | IllegalStateException e) {
            logger.debug("Exception response received. Error parsing exception message: {}", e.getMessage());
            return;
        }
    }

    /**
     * Method called by handleMessage() to handle all LEAP ReadResponse and SubscribeResponse messages.
     *
     * @param message LEAP message
     */
    private void handleReadResponseMessage(JsonObject message) {
        try {
            JsonObject header = message.get("Header").getAsJsonObject();
            Header headerObj = gson.fromJson(header, Header.class);

            // if 204/NoContent response received for buttongroup request, create empty button map
            if (Request.BUTTON_GROUP_URL.equals(headerObj.url)
                    && Header.STATUS_NO_CONTENT.equalsIgnoreCase(headerObj.statusCode)) {
                callback.handleEmptyButtonGroupDefinition();
                return;
            }

            if (!header.has("MessageBodyType")) {
                logger.trace("No MessageBodyType in header");
                return;
            }
            String messageBodyType = header.get("MessageBodyType").getAsString();
            logger.trace("MessageBodyType: {}", messageBodyType);

            if (!message.has("Body")) {
                logger.debug("No Body found in message");
                return;
            }
            JsonObject body = message.get("Body").getAsJsonObject();

            switch (messageBodyType) {
                case "OnePingResponse":
                    parseOnePingResponse(body);
                    break;
                case "OneZoneStatus":
                    parseOneZoneStatus(body);
                    break;
                case "MultipleAreaDefinition":
                    parseMultipleAreaDefinition(body);
                    break;
                case "MultipleButtonGroupDefinition":
                    parseMultipleButtonGroupDefinition(body);
                    break;
                case "MultipleDeviceDefinition":
                    parseMultipleDeviceDefinition(body);
                    break;
                case "MultipleOccupancyGroupDefinition":
                    parseMultipleOccupancyGroupDefinition(body);
                    break;
                case "MultipleOccupancyGroupStatus":
                    parseMultipleOccupancyGroupStatus(body);
                    break;
                case "MultipleVirtualButtonDefinition":
                    break;
                default:
                    logger.debug("Unknown MessageBodyType received: {}", messageBodyType);
                    break;
            }
        } catch (JsonParseException | IllegalStateException e) {
            logger.debug("Error parsing message: {}", e.getMessage());
            return;
        }
    }

    private @Nullable <T extends AbstractMessageBody> T parseBodySingle(JsonObject messageBody, String memberName,
            Class<T> type) {
        try {
            if (messageBody.has(memberName)) {
                JsonObject jsonObject = messageBody.get(memberName).getAsJsonObject();
                @Nullable
                T obj = gson.fromJson(jsonObject, type);
                return obj;
            } else {
                logger.debug("Member name {} not found in JSON message", memberName);
                return null;
            }
        } catch (IllegalStateException | JsonSyntaxException e) {
            logger.debug("Error parsing JSON message: {}", e.getMessage());
            return null;
        }
    }

    private <T extends AbstractMessageBody> List<T> parseBodyMultiple(JsonObject messageBody, String memberName,
            Class<T> type) {
        List<T> objList = new LinkedList<T>();
        try {
            if (messageBody.has(memberName)) {
                JsonArray jsonArray = messageBody.get(memberName).getAsJsonArray();

                for (JsonElement element : jsonArray) {
                    JsonObject jsonObject = element.getAsJsonObject();
                    T obj = Objects.requireNonNull(gson.fromJson(jsonObject, type));
                    objList.add(obj);
                }
                return objList;
            } else {
                logger.debug("Member name {} not found in JSON message", memberName);
                return objList;
            }
        } catch (IllegalStateException | JsonSyntaxException e) {
            logger.debug("Error parsing JSON message: {}", e.getMessage());
            return objList;
        }
    }

    private void parseOnePingResponse(JsonObject messageBody) {
        logger.debug("Ping response received");
    }

    /**
     * Parses a OneZoneStatus message body. Calls handleZoneUpdate() to dispatch zone updates.
     */
    private void parseOneZoneStatus(JsonObject messageBody) {
        ZoneStatus zoneStatus = parseBodySingle(messageBody, "ZoneStatus", ZoneStatus.class);
        if (zoneStatus != null) {
            callback.handleZoneUpdate(zoneStatus);
        }
    }

    /**
     * Parses a MultipleAreaDefinition message body.
     */
    private void parseMultipleAreaDefinition(JsonObject messageBody) {
        logger.trace("Parsing area list");
        List<Area> areaList = parseBodyMultiple(messageBody, "Areas", Area.class);
        callback.handleMultipleAreaDefinition(areaList);
    }

    /**
     * Parses a MultipleOccupancyGroupDefinition message body.
     */
    private void parseMultipleOccupancyGroupDefinition(JsonObject messageBody) {
        logger.trace("Parsing occupancy group list");
        List<OccupancyGroup> oGroupList = parseBodyMultiple(messageBody, "OccupancyGroups", OccupancyGroup.class);
        callback.handleMultipleOccupancyGroupDefinition(oGroupList);
    }

    /**
     * Parses a MultipleOccupancyGroupStatus message body and updates occupancy status.
     */
    private void parseMultipleOccupancyGroupStatus(JsonObject messageBody) {
        logger.trace("Parsing occupancy group status list");
        List<OccupancyGroupStatus> statusList = parseBodyMultiple(messageBody, "OccupancyGroupStatuses",
                OccupancyGroupStatus.class);
        for (OccupancyGroupStatus status : statusList) {
            int groupNumber = status.getOccupancyGroup();
            if (groupNumber > 0) {
                logger.debug("OccupancyGroup: {} Status: {}", groupNumber, status.occupancyStatus);
                callback.handleGroupUpdate(groupNumber, status.occupancyStatus);
            }
        }
    }

    /**
     * Parses a MultipleDeviceDefinition message body and loads the zoneToDevice and deviceToZone maps. Also passes the
     * device data on to the discovery service and calls setBridgeProperties() with the hub's device entry.
     */
    private void parseMultipleDeviceDefinition(JsonObject messageBody) {
        List<Device> deviceList = parseBodyMultiple(messageBody, "Devices", Device.class);
        callback.handleMultipleDeviceDefintion(deviceList);
    }

    /**
     * Parse a MultipleButtonGroupDefinition message body and load the results into deviceButtonMap.
     */
    private void parseMultipleButtonGroupDefinition(JsonObject messageBody) {
        List<ButtonGroup> buttonGroupList = parseBodyMultiple(messageBody, "ButtonGroups", ButtonGroup.class);
        callback.handleMultipleButtonGroupDefinition(buttonGroupList);
    }
}
