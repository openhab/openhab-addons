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
package org.openhab.binding.homematic.internal.communicator.parser;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts the possible options from the remote control metadata and parses the DISPLAY_OPTIONS virtual datapoint.
 *
 * @author Gerhard Riegler - Initial contribution
 */

public class DisplayOptionsParser extends CommonRpcParser<Object, Void> {
    private final Logger logger = LoggerFactory.getLogger(DisplayOptionsParser.class);
    private static final String[] onOff = new String[] { "ON", "OFF" };
    private static final int IDX_NOT_FOUND = -1;
    private HmChannel channel;
    private String text;
    private int beep = 0;
    private int backlight = 0;
    private int unit = 0;
    private List<String> symbols = new ArrayList<>();

    public DisplayOptionsParser(HmChannel channel) {
        this.channel = channel;
    }

    @Override
    public Void parse(Object value) throws IOException {
        String valueString = toString(value);
        String optionsString = valueString == null ? null : valueString.replace(" ", "");
        if (optionsString != null) {
            int idxFirstSep = optionsString.indexOf(",");
            if (idxFirstSep == -1) {
                text = optionsString;
                optionsString = "";
            } else {
                text = optionsString.substring(0, idxFirstSep);
                optionsString = optionsString.substring(idxFirstSep + 1);
            }

            String[] options = optionsString.split(",");

            String[] availableSymbols = getAvailableSymbols(channel);
            String[] availableBeepOptions = getAvailableOptions(channel, DATAPOINT_NAME_BEEP);
            String[] availableBacklightOptions = getAvailableOptions(channel, DATAPOINT_NAME_BACKLIGHT);
            String[] availableUnitOptions = getAvailableOptions(channel, DATAPOINT_NAME_UNIT);

            String deviceAddress = channel.getDevice().getAddress();
            if (logger.isDebugEnabled()) {
                logger.debug("Remote control '{}' supports these beep options: {}", deviceAddress,
                        availableBeepOptions);
                logger.debug("Remote control '{}' supports these backlight options: {}", deviceAddress,
                        availableBacklightOptions);
                logger.debug("Remote control '{}' supports these unit options: {}", deviceAddress,
                        availableUnitOptions);
                logger.debug("Remote control '{}' supports these symbols: {}", deviceAddress, symbols);
            }

            if (options != null) {
                for (String parameter : options) {
                    logger.debug("Parsing remote control option '{}'", parameter);
                    beep = getIntParameter(availableBeepOptions, beep, parameter, DATAPOINT_NAME_BEEP, deviceAddress);
                    backlight = getIntParameter(availableBacklightOptions, backlight, parameter,
                            DATAPOINT_NAME_BACKLIGHT, deviceAddress);
                    unit = getIntParameter(availableUnitOptions, unit, parameter, DATAPOINT_NAME_UNIT, deviceAddress);

                    if (findInArray(availableSymbols, parameter) != IDX_NOT_FOUND) {
                        logger.debug("Symbol '{}' found for remote control '{}'", parameter, deviceAddress);
                        symbols.add(parameter);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the first found parameter index of the options.
     */
    private int getIntParameter(String[] options, int currentValue, String parameter, String parameterName,
            String deviceAddress) {
        int idx = findInArray(options, parameter);
        if (idx != IDX_NOT_FOUND) {
            if (currentValue == 0) {
                logger.debug("{} option '{}' found at index {} for remote control '{}'", parameterName, parameter,
                        idx + 1, deviceAddress);
                return idx + 1;
            } else {
                logger.warn("{} option already set for remote control '{}', ignoring '{}'!", parameterName,
                        deviceAddress, parameter);
                return currentValue;
            }
        } else {
            return currentValue;
        }
    }

    /**
     * Returns all possible options from the given datapoint.
     */
    private String[] getAvailableOptions(HmChannel channel, String datapointName) {
        HmDatapointInfo dpInfo = HmDatapointInfo.createValuesInfo(channel, datapointName);
        HmDatapoint dp = channel.getDatapoint(dpInfo);
        if (dp != null) {
            String[] dpOpts = dp.getOptions();
            String[] options = new String[dpOpts.length - 1];
            options = Arrays.copyOfRange(dpOpts, 1, dpOpts.length);
            for (String onOffString : onOff) {
                int onIdx = findInArray(options, onOffString);
                if (onIdx != IDX_NOT_FOUND) {
                    options[onIdx] = datapointName + "_" + onOffString;
                }
            }
            return options;
        }
        return new String[0];
    }

    private int findInArray(String[] arr, String searchString) {
        if (arr.length == 0) {
            return IDX_NOT_FOUND;
        }
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(searchString)) {
                return i;
            }
        }
        return IDX_NOT_FOUND;
    }

    /**
     * Returns all possible symbols from the remote control.
     */
    private String[] getAvailableSymbols(HmChannel channel) {
        List<String> symbols = new ArrayList<>();
        for (HmDatapoint datapoint : channel.getDatapoints()) {
            if (!datapoint.isReadOnly() && datapoint.isBooleanType()
                    && datapoint.getParamsetType() == HmParamsetType.VALUES
                    && !DATAPOINT_NAME_SUBMIT.equals(datapoint.getName())
                    && !DATAPOINT_NAME_INSTALL_TEST.equals(datapoint.getName())) {
                symbols.add(datapoint.getName());
            }
        }
        return symbols.toArray(new String[0]);
    }

    /**
     * Returns the parsed text.
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the parsed beep value.
     */
    public int getBeep() {
        return beep;
    }

    /**
     * Returns the parsed backlight value.
     */
    public int getBacklight() {
        return backlight;
    }

    /**
     * Returns the parsed unit value.
     */
    public int getUnit() {
        return unit;
    }

    /**
     * Returns the parsed symbols.
     */
    public List<String> getSymbols() {
        return symbols;
    }
}
