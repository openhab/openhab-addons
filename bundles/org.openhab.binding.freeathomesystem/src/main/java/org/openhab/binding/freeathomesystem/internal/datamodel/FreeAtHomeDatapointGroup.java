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

import org.openhab.binding.freeathomesystem.internal.util.PidTranslationUtils;
import org.openhab.binding.freeathomesystem.internal.valuestateconverter.BooleanValueStateConverter;
import org.openhab.binding.freeathomesystem.internal.valuestateconverter.DecimalValueStateConverter;
import org.openhab.binding.freeathomesystem.internal.valuestateconverter.ShuttercontrolValueStateConverter;
import org.openhab.binding.freeathomesystem.internal.valuestateconverter.ValueStateConverter;
import org.openhab.core.library.CoreItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * @author andras
 *
 */
public class FreeAtHomeDatapointGroup {

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeDatapointGroup.class);

    public static final int DATAPOINTGROUP_DIRECTION_UNDEFINED = 0;
    public static final int DATAPOINTGROUP_DIRECTION_INPUT = 1;
    public static final int DATAPOINTGROUP_DIRECTION_OUTPUT = 2;
    public static final int DATAPOINTGROUP_DIRECTION_INPUTOUTPUT = 3;

    private int datapointGroupFunctionId;
    private int datapointGroupDirection;
    private int pairingId;
    private String functionString;
    private FreeAtHomeDatapoint inputDatapoint;
    private FreeAtHomeDatapoint outputDatapoint;

    private ValueStateConverter valueStateConverter;

    FreeAtHomeDatapointGroup() {
        datapointGroupDirection = DATAPOINTGROUP_DIRECTION_UNDEFINED;
        inputDatapoint = null;
        outputDatapoint = null;
        functionString = null;
    }

    boolean addDatapointToGroup(int direction, int neededPairingId, String channelId, JsonObject jsonObjectOfChannel) {

        FreeAtHomeDatapoint newDatapoint = new FreeAtHomeDatapoint();

        boolean result = newDatapoint.searchForDatapoint(direction, neededPairingId, channelId, jsonObjectOfChannel);

        if (result == true) {
            switch (direction) {
                case DATAPOINT_DIRECTION_INPUT: {
                    inputDatapoint = newDatapoint;

                    pairingId = neededPairingId;

                    break;
                }
                case DATAPOINT_DIRECTION_OUTPUT: {
                    outputDatapoint = newDatapoint;

                    if (inputDatapoint == null) {
                        pairingId = neededPairingId;
                    }

                    break;
                }
            }
        }

        if (inputDatapoint != null && outputDatapoint != null) {
            datapointGroupDirection = DATAPOINTGROUP_DIRECTION_INPUTOUTPUT;
        } else {
            if (inputDatapoint == null && outputDatapoint != null) {
                datapointGroupDirection = DATAPOINTGROUP_DIRECTION_OUTPUT;
            } else {
                if (inputDatapoint != null && outputDatapoint == null) {
                    datapointGroupDirection = DATAPOINTGROUP_DIRECTION_INPUT;
                } else {
                    datapointGroupDirection = DATAPOINTGROUP_DIRECTION_UNDEFINED;
                }
            }
        }

        return true;
    }

    public FreeAtHomeDatapoint getInputDatapoint() {
        return inputDatapoint;
    }

    public FreeAtHomeDatapoint getOutputDatapoint() {
        return outputDatapoint;
    }

    public int getDirection() {
        return datapointGroupDirection;
    }

    public boolean isDecimal() {
        boolean ret = false;

        if (functionString == null) {
            functionString = PidTranslationUtils.getValueTypeForPairingId(String.format("0x%04X", pairingId));

            if (functionString == null) {
                functionString = "";
            }
        }

        switch (functionString) {
            case PidTranslationUtils.PID_VALUETYPE_DECIMAL:
                ret = true;
                break;
            default:
                ret = false;
                break;
        }

        return ret;
    }

    public boolean isInteger() {
        boolean ret = false;

        if (functionString == null) {
            functionString = PidTranslationUtils.getValueTypeForPairingId(String.format("0x%04X", pairingId));

            if (functionString == null) {
                functionString = "";
            }
        }

        switch (functionString) {
            case PidTranslationUtils.PID_VALUETYPE_INTEGER:
                ret = true;
                break;
            default:
                ret = false;
                break;
        }

        return ret;
    }

    public boolean isReadOnly() {
        return (DATAPOINTGROUP_DIRECTION_INPUTOUTPUT == datapointGroupDirection) ? false : true;
    }

    public int getMax() {
        return PidTranslationUtils.getMax(String.format("0x%04X", pairingId));
    }

    public int getMin() {
        return PidTranslationUtils.getMin(String.format("0x%04X", pairingId));
    }

    public String getLabel() {
        return PidTranslationUtils.getShortTextForPairingId(String.format("0x%04X", pairingId));
    }

    public String getDescription() {
        return PidTranslationUtils.getDescriptionTextForPairingId(String.format("0x%04X", pairingId));
    }

    public String getValueType() {
        return PidTranslationUtils.getValueTypeForPairingId(String.format("0x%04X", pairingId));
    }

    public String getTypePattern() {
        String pattern = "";

        if (functionString == null) {
            functionString = PidTranslationUtils.getValueTypeForPairingId(String.format("0x%04X", pairingId));

            if (functionString == null) {
                functionString = "";
            }
        }

        switch (functionString) {
            case PidTranslationUtils.PID_VALUETYPE_DECIMAL:
                pattern = "%.1f";
                break;
            case PidTranslationUtils.PID_VALUETYPE_INTEGER:
                pattern = "%d";
                break;
            default:
                pattern = "";
                break;
        }

        return pattern;
    }

    public ValueStateConverter getValueStateConverter() {

        if (valueStateConverter == null) {

            if (functionString == null) {
                functionString = PidTranslationUtils.getValueTypeForPairingId(String.format("0x%04X", pairingId));

                if (functionString == null) {
                    functionString = "";
                }
            }

            switch (functionString) {
                case PidTranslationUtils.PID_VALUETYPE_BOOLEAN:
                    valueStateConverter = new BooleanValueStateConverter();
                    break;
                case PidTranslationUtils.PID_VALUETYPE_DECIMAL:
                    valueStateConverter = new DecimalValueStateConverter();
                    break;
                case PidTranslationUtils.PID_VALUETYPE_INTEGER:
                    valueStateConverter = new DecimalValueStateConverter();
                    break;
                case PidTranslationUtils.PID_VALUETYPE_STRING:
                    break;
                case PidTranslationUtils.PID_VALUETYPE_SHUTTERMOVEMENT:
                    valueStateConverter = new ShuttercontrolValueStateConverter();
                    break;
                default:
                    valueStateConverter = new BooleanValueStateConverter();
                    break;
            }
        }

        return valueStateConverter;
    }

    public String getOpenHabItemType() {
        String itemTypeString = "";

        if (functionString == null) {
            functionString = PidTranslationUtils.getValueTypeForPairingId(String.format("0x%04X", pairingId));

            if (functionString == null) {
                functionString = "";
            }
        }

        switch (functionString) {
            case PidTranslationUtils.PID_VALUETYPE_BOOLEAN:
                itemTypeString = CoreItemFactory.SWITCH;
                break;
            case PidTranslationUtils.PID_VALUETYPE_DECIMAL:
                itemTypeString = CoreItemFactory.NUMBER;
                break;
            case PidTranslationUtils.PID_VALUETYPE_INTEGER:
                itemTypeString = CoreItemFactory.NUMBER;
                break;
            case PidTranslationUtils.PID_VALUETYPE_SHUTTERMOVEMENT:
                itemTypeString = CoreItemFactory.ROLLERSHUTTER;
                break;
            // case FreeAtHomeUtils.PID_VALUETYPE_STRING:
            // break;
            default:
                logger.info("wrong datapoint {}", String.format("0x%04X", pairingId));
                break;
        }

        return itemTypeString;
    }

    public String getOpenHabCategory() {
        return PidTranslationUtils.getCategoryForPairingId(String.format("0x%04X", pairingId));
    }
}
