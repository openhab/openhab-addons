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
package org.openhab.binding.freeathome.internal.datamodel;

import static org.openhab.binding.freeathome.internal.datamodel.FreeAtHomeDatapoint.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeathome.internal.util.FreeAtHomeGeneralException;
import org.openhab.binding.freeathome.internal.util.PidTranslationUtils;
import org.openhab.binding.freeathome.internal.valuestateconverter.BooleanValueStateConverter;
import org.openhab.binding.freeathome.internal.valuestateconverter.DecimalValueStateConverter;
import org.openhab.binding.freeathome.internal.valuestateconverter.ShuttercontrolValueStateConverter;
import org.openhab.binding.freeathome.internal.valuestateconverter.ValueStateConverter;
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

    public enum DatapointGroupDirection {
        UNDEFINED,
        INPUT,
        OUTPUT,
        INPUTOUTPUT
    }

    private DatapointGroupDirection datapointGroupDirection;
    private int pairingId;

    private String functionString = "";
    private @Nullable FreeAtHomeDatapoint inputDatapoint;
    private @Nullable FreeAtHomeDatapoint outputDatapoint;

    FreeAtHomeDatapointGroup() {
        datapointGroupDirection = DatapointGroupDirection.UNDEFINED;
        inputDatapoint = null;
        outputDatapoint = null;
    }

    boolean addDatapointToGroup(DatapointDirection direction, int neededPairingId, String channelId,
            JsonObject jsonObjectOfChannel) {
        FreeAtHomeDatapoint newDatapoint = new FreeAtHomeDatapoint();

        DatapointDirection resultingDirection = newDatapoint.searchForDatapoint(direction, neededPairingId, channelId,
                jsonObjectOfChannel);

        if (resultingDirection != DatapointDirection.UNKNOWN) {
            switch (resultingDirection) {
                case INPUT: {
                    inputDatapoint = newDatapoint;

                    pairingId = neededPairingId;

                    break;
                }
                case OUTPUT: {
                    outputDatapoint = newDatapoint;

                    if (inputDatapoint == null) {
                        pairingId = neededPairingId;
                    }

                    break;
                }
                case INPUTOUTPUT:
                case INPUT_AS_OUTPUT:
                case UNKNOWN:
                    break;
            }
        }

        if (inputDatapoint != null && outputDatapoint != null) {
            datapointGroupDirection = DatapointGroupDirection.INPUTOUTPUT;
        } else {
            if (inputDatapoint == null && outputDatapoint != null) {
                datapointGroupDirection = DatapointGroupDirection.OUTPUT;
            } else {
                if (inputDatapoint != null && outputDatapoint == null) {
                    datapointGroupDirection = DatapointGroupDirection.INPUT;
                } else {
                    datapointGroupDirection = DatapointGroupDirection.UNDEFINED;
                }
            }
        }

        return resultingDirection != DatapointDirection.UNKNOWN;
    }

    public void applyChangesForVirtualDevice() {
        // The input and output datapoints are meant from the device point of view. Because the virtual devices are
        // outside of the free@home system the input and output datapoint must be switched
        @Nullable
        FreeAtHomeDatapoint localDatapoint = inputDatapoint;

        inputDatapoint = outputDatapoint;
        outputDatapoint = localDatapoint;

        if (inputDatapoint != null && outputDatapoint != null) {
            datapointGroupDirection = DatapointGroupDirection.INPUTOUTPUT;
        } else {
            if (inputDatapoint == null && outputDatapoint != null) {
                datapointGroupDirection = DatapointGroupDirection.OUTPUT;
            } else {
                if (inputDatapoint != null && outputDatapoint == null) {
                    datapointGroupDirection = DatapointGroupDirection.INPUT;
                } else {
                    datapointGroupDirection = DatapointGroupDirection.UNDEFINED;
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

    public DatapointGroupDirection getDirection() {
        return datapointGroupDirection;
    }

    public boolean isDecimal() throws FreeAtHomeGeneralException {
        return PidTranslationUtils.PID_VALUETYPE_DECIMAL
                .equals(PidTranslationUtils.getValueTypeForPairingId(String.format("0x%04X", pairingId)));
    }

    public boolean isInteger() throws FreeAtHomeGeneralException {
        return PidTranslationUtils.PID_VALUETYPE_INTEGER
                .equals(PidTranslationUtils.getValueTypeForPairingId(String.format("0x%04X", pairingId)));
    }

    public boolean isReadOnly() {
        return (DatapointGroupDirection.INPUTOUTPUT == datapointGroupDirection) ? false
                : ((DatapointGroupDirection.INPUT == datapointGroupDirection) ? false : true);
    }

    public int getMax() throws FreeAtHomeGeneralException {
        return PidTranslationUtils.getMax(String.format("0x%04X", pairingId));
    }

    public int getMin() throws FreeAtHomeGeneralException {
        return PidTranslationUtils.getMin(String.format("0x%04X", pairingId));
    }

    public String getLabel() throws FreeAtHomeGeneralException {
        return PidTranslationUtils.getShortTextForPairingId(String.format("0x%04X", pairingId));
    }

    public String getDescription() throws FreeAtHomeGeneralException {
        return PidTranslationUtils.getDescriptionTextForPairingId(String.format("0x%04X", pairingId));
    }

    public String getValueType() throws FreeAtHomeGeneralException {
        return PidTranslationUtils.getValueTypeForPairingId(String.format("0x%04X", pairingId));
    }

    public String getTypePattern() throws FreeAtHomeGeneralException {
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
                logger.debug("Type pattern not found for PairingID {} - using default",
                        String.format("0x%04X", pairingId));
                break;
        }

        return pattern;
    }

    public ValueStateConverter getValueStateConverter() throws FreeAtHomeGeneralException {
        ValueStateConverter valueStateConverter = new BooleanValueStateConverter();

        functionString = PidTranslationUtils.getValueTypeForPairingId(String.format("0x%04X", pairingId));

        switch (functionString) {
            case PidTranslationUtils.PID_VALUETYPE_BOOLEAN:
                valueStateConverter = new BooleanValueStateConverter();
                break;
            case PidTranslationUtils.PID_VALUETYPE_DECIMAL:
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
                logger.debug("Value converter not found for PairingID {} - using default",
                        String.format("0x%04X", pairingId));
                break;
        }

        return valueStateConverter;
    }

    public String getOpenHabItemType() throws FreeAtHomeGeneralException {
        String itemTypeString = "";

        functionString = PidTranslationUtils.getValueTypeForPairingId(String.format("0x%04X", pairingId));

        switch (functionString) {
            case PidTranslationUtils.PID_VALUETYPE_BOOLEAN:
                itemTypeString = CoreItemFactory.SWITCH;
                break;
            case PidTranslationUtils.PID_VALUETYPE_DECIMAL:
            case PidTranslationUtils.PID_VALUETYPE_INTEGER:
                itemTypeString = CoreItemFactory.NUMBER;
                break;
            case PidTranslationUtils.PID_VALUETYPE_SHUTTERMOVEMENT:
                itemTypeString = CoreItemFactory.ROLLERSHUTTER;
                break;
            default:
                itemTypeString = CoreItemFactory.NUMBER;
                logger.debug("Item type constant not found for PairingID {} - using default",
                        String.format("0x%04X", pairingId));
                break;
        }

        return itemTypeString;
    }

    public String getOpenHabCategory() throws FreeAtHomeGeneralException {
        return PidTranslationUtils.getCategoryForPairingId(String.format("0x%04X", pairingId));
    }
}
