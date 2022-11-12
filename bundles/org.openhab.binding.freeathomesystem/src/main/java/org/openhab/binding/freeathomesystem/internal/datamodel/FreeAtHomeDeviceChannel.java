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

package org.openhab.binding.freeathomesystem.internal.datamodel;

import static org.openhab.binding.freeathomesystem.internal.datamodel.FreeAtHomeDatapoint.*;
import static org.openhab.binding.freeathomesystem.internal.util.FidTranslationUtils.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeathomesystem.internal.util.FidTranslationUtils;
import org.openhab.binding.freeathomesystem.internal.util.HexUtils;
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

    public List<FreeAtHomeDatapointGroup> datapointGroups = new ArrayList<>();

    public boolean createChannelFromJson(String deviceLabel, String channelId, JsonObject jsonObjectOfChannels,
            boolean isScene, boolean isRule) {

        JsonObject channelObject = jsonObjectOfChannels.getAsJsonObject(channelId);

        channelFunctionID = channelObject.get("functionID").getAsString();

        if (isScene == true) {
            channelFunctionID = channelFunctionID.substring(0, channelFunctionID.length() - 1) + "0";
        }

        if (!channelFunctionID.isEmpty()) {

            channelLabel = channelObject.get("displayName").getAsString();

            if (channelLabel.isEmpty()) {
                channelLabel = deviceLabel;
            }
        }

        switch (HexUtils.getIntegerFromHex(channelFunctionID)) {
            case FID_SWITCH_SENSOR: {
                this.channelId = channelId;

                logger.info("Switch sensor channel found - Channel FID: {}", channelFunctionID);

                /* usage of sensor channel is intentionally switched off */
                // FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                // newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_OUTPUT, 1, channelId, channelObject);
                //
                // datapointGroups.add(newDatapointGroup);

                break;
            }

            case FID_TRIGGER:
            case FID_SWITCH_ACTUATOR: {
                this.channelId = channelId;

                logger.info("Switch actuator channel created - Channel FID: {}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_OUTPUT, 256, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_INPUT, 1, channelId, channelObject);

                datapointGroups.add(newDatapointGroup);

                break;
            }

            case FID_ROOM_TEMPERATURE_CONTROLLER_MASTER_WITHOUT_FAN:
            case FID_RADIATOR_ACTUATOR_MASTER: {
                this.channelId = channelId;

                if (HexUtils.getIntegerFromHex(channelFunctionID) == FID_RADIATOR_ACTUATOR_MASTER) {
                    logger.info("Radiator actuator channel - Channel FID: {}", channelFunctionID);
                } else {
                    logger.info("Room temperature actuator channel - Channel FID: {}", channelFunctionID);
                }

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_OUTPUT, 304, channelId, channelObject);
                datapointGroups.add(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_OUTPUT, 333, channelId, channelObject);
                datapointGroups.add(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_OUTPUT, 331, channelId, channelObject);
                datapointGroups.add(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_OUTPUT, 54, channelId, channelObject);
                datapointGroups.add(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_OUTPUT, 51, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_INPUT, 320, channelId, channelObject);
                datapointGroups.add(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_OUTPUT, 68, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_INPUT, 58, channelId, channelObject);
                datapointGroups.add(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_OUTPUT, 56, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_INPUT, 66, channelId, channelObject);
                datapointGroups.add(newDatapointGroup);

                break;
            }

            case FID_WINDOW_DOOR_POSITION_SENSOR:
            case FID_WINDOW_DOOR_SENSOR: {

                this.channelId = channelId;

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_OUTPUT, 53, channelId, channelObject);
                datapointGroups.add(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_OUTPUT, 41, channelId, channelObject);
                datapointGroups.add(newDatapointGroup);

                logger.info("Window/Door position sensor channel created - Channel FID: {}", channelFunctionID);

                break;
            }

            case FID_SCENE_TRIGGER: {
                this.channelId = channelId;

                logger.info("Scene trigger channel - Channel FID: {}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_OUTPUT, 4, channelId, channelObject);

                datapointGroups.add(newDatapointGroup);

                break;
            }

            case FID_RULE_SWITCH: {
                this.channelId = channelId;

                logger.info("Rule channel - Channel FID: {}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_OUTPUT, 61698, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_INPUT, 61697, channelId, channelObject);

                datapointGroups.add(newDatapointGroup);

                break;
            }

            case FID_DES_DOOR_OPENER_ACTUATOR: {
                this.channelId = channelId;

                logger.info("Door opener actuator channel - Channel FID: {}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_OUTPUT, 256, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_INPUT, 2, channelId, channelObject);

                datapointGroups.add(newDatapointGroup);

                break;
            }

            case FID_DES_LEVEL_CALL_SENSOR:
            case FID_DES_DOOR_RINGING_SENSOR: {
                this.channelId = channelId;

                logger.info("Door ring sensor channel - Channel FID: {}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_INPUT, 2, channelId, channelObject);

                datapointGroups.add(newDatapointGroup);

                break;
            }

            case FID_DES_LIGHT_SWITCH_ACTUATOR: {
                this.channelId = channelId;

                logger.info("DES light switch channel - Channel FID: {}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_OUTPUT, 256, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_INPUT, 2, channelId, channelObject);

                datapointGroups.add(newDatapointGroup);

                break;
            }

            case FID_DIMMING_ACTUATOR: {
                this.channelId = channelId;

                logger.info("Dimming actuator channel - Channel FID: {}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_OUTPUT, 272, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_INPUT, 17, channelId, channelObject);

                datapointGroups.add(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_OUTPUT, 256, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_INPUT, 1, channelId, channelObject);

                datapointGroups.add(newDatapointGroup);

                break;
            }
            case FID_AWNING_ACTUATOR:
            case FID_ATTIC_WINDOW_ACTUATOR:
            case FID_BLIND_ACTUATOR:
            case FID_SHUTTER_ACTUATOR: {
                this.channelId = channelId;

                logger.info("Shutter actuator channel - Channel FID: {}", channelFunctionID);

                FreeAtHomeDatapointGroup newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_INPUT, 32, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_OUTPUT, 288, channelId, channelObject);
                datapointGroups.add(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_INPUT, 33, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_OUTPUT, 288, channelId, channelObject);
                datapointGroups.add(newDatapointGroup);

                newDatapointGroup = new FreeAtHomeDatapointGroup();
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_OUTPUT, 289, channelId, channelObject);
                newDatapointGroup.addDatapointToGroup(DATAPOINT_DIRECTION_INPUT, 35, channelId, channelObject);

                datapointGroups.add(newDatapointGroup);

                break;
            }

            default: {
                logger.info("Unknown channel found - Channel FID: {}", channelFunctionID);

                break;
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

    public String getFunctionIdText() {
        return FidTranslationUtils
                .getFunctionIdText(String.format("0x%04X", HexUtils.getIntegerFromHex(channelFunctionID)));
    }

    public int getNumberOfDatapointGroup() {
        return datapointGroups.size();
    }

    public FreeAtHomeDatapointGroup getDatapointGroup(int idx) {
        return datapointGroups.get(idx);
    }
}
