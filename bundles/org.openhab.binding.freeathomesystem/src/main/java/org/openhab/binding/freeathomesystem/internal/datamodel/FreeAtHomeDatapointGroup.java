/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class FreeAtHomeDatapointGroup {

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeDatapointGroup.class);

    public static final int DATAPOINTGROUP_DIRECTION_UNDEFINED = 0;
    public static final int DATAPOINTGROUP_DIRECTION_INPUT = 1;
    public static final int DATAPOINTGROUP_DIRECTION_OUTPUT = 2;
    public static final int DATAPOINTGROUP_DIRECTION_INPUTOUTPUT = 3;

    private int datapointGroupDirection;
    private int pairingId;

    private String functionString = "";
    private @Nullable FreeAtHomeDatapoint inputDatapoint;
    private @Nullable FreeAtHomeDatapoint outputDatapoint;

    @SuppressWarnings("null")
    FreeAtHomeDatapointGroup() {
        datapointGroupDirection = DATAPOINTGROUP_DIRECTION_UNDEFINED;
        inputDatapoint = null;
        outputDatapoint = null;
    }

    boolean addDatapointToGroup(int direction, int neededPairingId, String channelId, JsonObject jsonObjectOfChannel) {
        FreeAtHomeDatapoint newDatapoint = new FreeAtHomeDatapoint();

        int resultingDirection = newDatapoint.searchForDatapoint(direction, neededPairingId, channelId,
                jsonObjectOfChannel);

        if (resultingDirection != DATAPOINT_DIRECTION_UNKNOWN) {
            switch (resultingDirection) {
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

        return resultingDirection != DATAPOINT_DIRECTION_UNKNOWN;
    }

    @SuppressWarnings("null")
    public void applyChangesForVirtualDevice() {
        // The input and output datapoints are meant from the device point of view. Because the virtual devices are
        // outside of the free@home system the input and output datapoint must be switched
        @Nullable
        FreeAtHomeDatapoint localDatapoint = inputDatapoint;

        inputDatapoint = outputDatapoint;
        outputDatapoint = localDatapoint;

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

        return;
    }

    public @Nullable FreeAtHomeDatapoint getInputDatapoint() {
        return inputDatapoint;
    }

    public @Nullable FreeAtHomeDatapoint getOutputDatapoint() {
        return outputDatapoint;
    }

    public int getDirection() {
        return datapointGroupDirection;
    }

    public boolean isDecimal() {
        boolean ret = false;

        functionString = PidTranslationUtils.getValueTypeForPairingId(String.format("0x%04X", pairingId));

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

        functionString = PidTranslationUtils.getValueTypeForPairingId(String.format("0x%04X", pairingId));

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
        boolean result = true;

        if (DATAPOINTGROUP_DIRECTION_INPUTOUTPUT == datapointGroupDirection) {
            result = false;
        }

        if (DATAPOINTGROUP_DIRECTION_INPUT == datapointGroupDirection) {
            result = false;
        }

        return result;
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

        functionString = PidTranslationUtils.getValueTypeForPairingId(String.format("0x%04X", pairingId));

        switch (functionString) {
            case PidTranslationUtils.PID_VALUETYPE_DECIMAL:
                pattern = "%.1f";
                break;
            case PidTranslationUtils.PID_VALUETYPE_INTEGER:
                pattern = "%d";
                break;
            default:
                pattern = "";
                logger.debug("Type pattern not forund for PairingID {} - using default",
                        String.format("0x%04X", pairingId));
                break;
        }

        return pattern;
    }

    public ValueStateConverter getValueStateConverter() {
        ValueStateConverter valueStateConverter = new BooleanValueStateConverter();

        functionString = PidTranslationUtils.getValueTypeForPairingId(String.format("0x%04X", pairingId));

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
                valueStateConverter = new DecimalValueStateConverter();
                logger.debug("Value converter not forund for PairingID {} - using default",
                        String.format("0x%04X", pairingId));
                break;
        }

        return valueStateConverter;
    }

    public String getOpenHabItemType() {
        String itemTypeString = "";

        functionString = PidTranslationUtils.getValueTypeForPairingId(String.format("0x%04X", pairingId));

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
            default:
                itemTypeString = CoreItemFactory.NUMBER;
                logger.debug("Item type constant not forund for PairingID {} - using default",
                        String.format("0x%04X", pairingId));
                break;
        }

        return itemTypeString;
    }

    public String getOpenHabCategory() {
        return PidTranslationUtils.getCategoryForPairingId(String.format("0x%04X", pairingId));
    }
}
