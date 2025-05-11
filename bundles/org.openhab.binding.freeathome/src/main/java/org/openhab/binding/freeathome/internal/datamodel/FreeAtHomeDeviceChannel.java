/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.freeathome.internal.datamodel;

import static org.openhab.binding.freeathome.internal.datamodel.FreeAtHomeDatapoint.*;
import static org.openhab.binding.freeathome.internal.util.FidTranslationUtils.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeathome.internal.datamodel.FreeAtHomeDatapoint.DatapointDirection;
import org.openhab.binding.freeathome.internal.util.FidTranslationUtils;
import org.openhab.binding.freeathome.internal.util.FreeAtHomeGeneralException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link FreeAtHomeDeviceChannel} holding the information of device channels
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class FreeAtHomeDeviceChannel {

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeDeviceChannel.class);

    private String channelLabel = "";
    private String channelId = "";
    private String channelFunctionID = "";

    private List<FreeAtHomeDatapointGroup> datapointGroups = new ArrayList<>();

    public boolean createChannelFromJson(String deviceLabel, String channelId, JsonObject jsonObjectOfChannels,
            boolean isScene, boolean isRule) {
        JsonObject channelObject = jsonObjectOfChannels.getAsJsonObject(channelId);

        channelFunctionID = channelObject.get("functionID").getAsString();

        // check whether this is a valid channel
        if (channelFunctionID.isEmpty()) {
            // invalid channel found
            logger.info("Invalid channel function ID found - Devicelabel: {} Channel: {}", deviceLabel, channelId);

            return false;
        }

        if (!channelFunctionID.isEmpty()) {
            channelLabel = channelObject.get("displayName").getAsString();

            if (channelLabel.isEmpty()) {
                channelLabel = deviceLabel;
            }
        }

        if (isScene) {
            channelFunctionID = channelFunctionID.substring(0, channelFunctionID.length() - 1) + "0";
        }

        logger.debug("createChannelFromJson - deviceLabel: {} channelLabel {} channelId: {} isScene {} isRule {} ",
                deviceLabel, channelLabel, channelId, isScene, isRule);

        switch (Integer.parseInt(channelFunctionID, 16)) {
            case FID_PANEL_SWITCH_SENSOR:
            case FID_SWITCH_SENSOR: {
                this.channelId = channelId;

                logger.debug("Switch sensor channel found - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 1, channelId, channelObject);

                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_PANEL_DIMMING_SENSOR:
            case FID_DIMMING_SENSOR: {
                this.channelId = channelId;

                logger.debug("Dimming sensor channel found - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 17, channelId, channelObject);

                addDatapointGroup(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 1, channelId, channelObject);

                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_TRIGGER:
            case FID_SWITCH_ACTUATOR: {
                this.channelId = channelId;

                logger.debug("Switch actuator channel created - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 256, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DatapointDirection.INPUT, 1, channelId, channelObject);

                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_MOVEMENT_DETECTOR_FLEX:
            case FID_MOVEMENT_DETECTOR: {
                this.channelId = channelId;

                logger.debug("Movement detector channel found - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 6, channelId, channelObject);

                addDatapointGroup(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 7, channelId, channelObject);

                addDatapointGroup(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 1027, channelId, channelObject);

                addDatapointGroup(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.INPUT_AS_OUTPUT, 256, channelId,
                        channelObject);

                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_ROOM_TEMPERATURE_CONTROLLER_MASTER_WITHOUT_FAN:
            case FID_RADIATOR_ACTUATOR_MASTER: {
                this.channelId = channelId;

                if (Integer.parseInt(channelFunctionID, 16) == FID_RADIATOR_ACTUATOR_MASTER) {
                    logger.debug("Radiator actuator channel - Channel FID: 0x{}", channelFunctionID);
                } else {
                    logger.debug("Room temperature actuator channel - Channel FID: 0x{}", channelFunctionID);
                }

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 304, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 333, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 331, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 54, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 51, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DatapointDirection.INPUT, 320, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 68, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DatapointDirection.INPUT, 58, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 56, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DatapointDirection.INPUT, 66, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                // Additional channel for RTC device
                if (Integer.parseInt(channelFunctionID, 16) == FID_ROOM_TEMPERATURE_CONTROLLER_MASTER_WITHOUT_FAN) {
                    newDatapointGroup = new FreeAtHomeDatapointGroup();
                    newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 48, channelId, channelObject);
                    addDatapointGroup(newDatapointGroup);
                }

                break;
            }
            case FID_WINDOW_DOOR_POSITION_SENSOR:
            case FID_WINDOW_DOOR_SENSOR: {
                this.channelId = channelId;

                logger.debug("Window/Door position sensor channel created - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                // 53 AL_WINDOW_DOOR Window/Door Open = 1 / closed = 0
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 53, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                // 41 AL_WINDOW_DOOR_POSITION Window/Door position Delivers position for Window/Door(Open/Tilted/Closed)
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 41, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_SCENE_TRIGGER: {
                this.channelId = channelId;

                logger.debug("Scene trigger channel - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 4, channelId, channelObject);

                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_RULE_SWITCH: {
                this.channelId = channelId;

                logger.debug("Rule channel - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 61698, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DatapointDirection.INPUT, 61697, channelId, channelObject);

                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_DES_DOOR_OPENER_ACTUATOR: {
                this.channelId = channelId;

                logger.debug("Door opener actuator channel - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 256, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DatapointDirection.INPUT, 2, channelId, channelObject);

                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_DES_LEVEL_CALL_SENSOR:
            case FID_DES_DOOR_RINGING_SENSOR: {
                this.channelId = channelId;

                logger.debug("Door ring sensor channel - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 2, channelId, channelObject);

                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_DES_LIGHT_SWITCH_ACTUATOR: {
                this.channelId = channelId;

                logger.debug("DES light switch channel - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 256, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DatapointDirection.INPUT, 2, channelId, channelObject);

                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_DIMMING_ACTUATOR_FLEX:
            case FID_DIMMING_ACTUATOR: {
                this.channelId = channelId;

                logger.debug("Dimming actuator channel - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 272, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DatapointDirection.INPUT, 17, channelId, channelObject);

                addDatapointGroup(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 256, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DatapointDirection.INPUT, 1, channelId, channelObject);

                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_DIMMING_SENSOR_PUSHBUTTON_TYPE0:
            case FID_DIMMING_SENSOR_PUSHBUTTON_TYPE1:
            case FID_DIMMING_SENSOR_PUSHBUTTON_TYPE2:
            case FID_DIMMING_SENSOR_PUSHBUTTON_TYPE3:
            case FID_DIMMING_SENSOR_PUSHBUTTON_TYPE4:
            case FID_DIMMING_SENSOR_PUSHBUTTON_TYPE5:
            case FID_DIMMING_SENSOR_PUSHBUTTON_TYPE6:
            case FID_DIMMING_SENSOR_PUSHBUTTON_TYPE7: {
                this.channelId = channelId;

                logger.debug("Dimming actuator channel - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 17, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DatapointDirection.INPUT, 272, channelId, channelObject);

                addDatapointGroup(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 1, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DatapointDirection.INPUT, 256, channelId, channelObject);

                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_AWNING_ACTUATOR:
            case FID_ATTIC_WINDOW_ACTUATOR:
            case FID_BLIND_ACTUATOR:
            case FID_BLIND_ACTUATOR_WIRELESS:
            case FID_SHUTTER_ACTUATOR: {
                this.channelId = channelId;

                logger.debug("Shutter actuator channel - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.INPUT, 32, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 288, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.INPUT, 33, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 288, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 289, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DatapointDirection.INPUT, 35, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                /*
                 * 290 AL_CURRENT_ABSOLUTE_POSITION_SLATS_PERCENTAGE Current Absolute Position Slats Percentage Indicate
                 * the current position of the slats in percentage
                 * 36 AL_SET_ABSOLUTE_POSITION_SLATS_PERCENTAGE Set Absolute Position Slats Moves the slats into a
                 * specified position
                 */
                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 290, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DatapointDirection.INPUT, 36, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_BLIND_SENSOR: {
                this.channelId = channelId;

                logger.warn("Blind sensor channel - Channel FID: 0x{}, not implemented yet", channelFunctionID);

                break;
            }
            case FID_BRIGHTNESS_SENSOR: {
                this.channelId = channelId;

                logger.debug("Brightness sensor channel - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 1026, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 1027, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_RAIN_SENSOR: {
                this.channelId = channelId;

                logger.debug("Rain sensor channel - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 39, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 1029, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 1030, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_TEMPERATURE_SENSOR: {
                this.channelId = channelId;

                logger.debug("Temperature sensor channel - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                if (newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 38, channelId, channelObject)) {
                    addDatapointGroup(newDatapointGroup);
                }

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                if (newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 1024, channelId, channelObject)) {
                    addDatapointGroup(newDatapointGroup);
                }

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                if (newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 304, channelId, channelObject)) {
                    addDatapointGroup(newDatapointGroup);
                }

                break;
            }
            case FID_WIND_SENSOR: {
                this.channelId = channelId;

                logger.debug("Wind sensor channel - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 37, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 1025, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 1028, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_AIRQUALITYSENSOR_CO: {
                this.channelId = channelId;

                logger.debug("AQS CO channel - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 1564, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_AIRQUALITYSENSOR_CO2: {
                this.channelId = channelId;

                logger.debug("AQS CO2 channel - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 1563, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_AIRQUALITYSENSOR_HUMIDITY: {
                this.channelId = channelId;

                logger.debug("AQS Humidity channel - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 337, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_AIRQUALITYSENSOR_PRESSURE: {
                this.channelId = channelId;

                logger.debug("AQS Pressure channel - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 1562, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_AIRQUALITYSENSOR_NO2: {
                this.channelId = channelId;

                logger.debug("AQS NO2 channel - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 1565, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_AIRQUALITYSENSOR_O3: {
                this.channelId = channelId;

                logger.debug("AQS O3 channel - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 1566, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_AIRQUALITYSENSOR_PM10: {
                this.channelId = channelId;

                logger.debug("AQS PM10 channel - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 1567, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_AIRQUALITYSENSOR_VOC: {
                this.channelId = channelId;

                logger.debug("AQS VOC channel - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 1569, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_AIRQUALITYSENSOR_PM25: {
                this.channelId = channelId;

                logger.debug("AQS PM25 channel - Channel FID: 0x{}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 1568, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_WIND_ALARM_SENSOR: { // 0x000C Wind Alarm
                this.channelId = channelId;
                logger.debug("Wind Alarm channel - Channel FID: 0x{}", channelFunctionID);
                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 37, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_RAIN_ALARM_SENSOR: { // 0x000E Rain Alarm
                this.channelId = channelId;
                logger.debug("Rain Alarm channel - Channel FID: 0x{}", channelFunctionID);
                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DatapointDirection.OUTPUT, 39, channelId, channelObject);
                addDatapointGroup(newDatapointGroup);

                break;
            }
            case FID_SCENE_SENSOR: {
                this.channelId = channelId;

                logger.warn("Scene sensor channel - Channel FID: 0x{} is not yet implemented", channelFunctionID);

                break;
            }
            default: {
                logger.debug("Unknown channel found - Channel FID: 0x{}", channelFunctionID);

                return false;
            }
        }

        return true;
    }

    public String getChannelIdforDatapoint() {
        return channelId;
    }

    public String getChannelLabel() {
        return channelLabel;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getFunctionId() {
        return channelFunctionID;
    }

    public @Nullable String getFunctionIdText() throws FreeAtHomeGeneralException {
        String functionIdText = FidTranslationUtils
                .getFunctionIdText(String.format("0x%04X", Integer.parseInt(channelFunctionID, 16)));
        return functionIdText;
    }

    public int getNumberOfDatapointGroup() {
        return datapointGroups.size();
    }

    public FreeAtHomeDatapointGroup getDatapointGroup(int idx) {
        return datapointGroups.get(idx);
    }

    public void applyChangesForVirtualDevice() {
        for (FreeAtHomeDatapointGroup localDatapointGroup : datapointGroups) {
            localDatapointGroup.applyChangesForVirtualDevice();
        }
    }

    private void addDatapointGroup(FreeAtHomeDatapointGroup DatapointGroup) {
        if (DatapointGroup.isValid()) {
            logger.debug("Datapoint group is added");
            datapointGroups.add(DatapointGroup);
        } else {
            /*
             * There is no constantrelationship between Function IDs and the required pairing IDs. As a result, a
             * "needed" pairing ID may sometimes not exist. In such cases, we avoid adding an invalid datapoint group.
             */
            logger.warn("Datapoint group is not added, because it is not valid");
        }
    }
}
