/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.loxone.internal.controls;

import static org.openhab.binding.loxone.internal.LxBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.loxone.internal.types.LxIRCTemperature;
import org.openhab.binding.loxone.internal.types.LxState;
import org.openhab.binding.loxone.internal.types.LxUuid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Intelligent Room Controller day timer on Loxone Miniserver.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxControlIRCDayTimer extends LxControl {
    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlIRCDayTimer(uuid);
        }

        @Override
        String getType() {
            return "ircdaytimer";
        }
    }

    private static final String STATE_CURRENT_TEMPERATURE_INDEX = "value";
    private static final String STATE_CURRENT_MODE = "mode";
    private static final String STATE_MODE_LIST = "modelist";

    private Map<Integer, String> modesList = new HashMap<>();

    private static Pattern pattern;
    private final Logger logger = LoggerFactory.getLogger(LxControlIRCDayTimer.class);

    LxControlIRCDayTimer(LxUuid uuid) {
        super(uuid);
    }

    static {
        // timer mode parsing pattern
        String regExp = "(\\d+):mode=([\\d-]+);name=\"(.+)\"";
        pattern = Pattern.compile(regExp);
    }

    @Override
    public void initialize(LxControlConfig config) {
        super.initialize(config);
        addChannel("String", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_TEXT),
                defaultChannelLabel + " / Current Temperature", "Timer's current temperature target", tags, null,
                () -> getTemperatureLabel(STATE_CURRENT_TEMPERATURE_INDEX));
        addChannel("String", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_TEXT),
                defaultChannelLabel + " / Mode", "Timer's current mode", tags, null, this::getCurrentMode);
    }

    @Override
    public void onStateChange(LxState state) {
        String stateName = state.getName();
        Object value = state.getStateValue();
        if (STATE_MODE_LIST.equals(stateName) && value instanceof String) {
            // the syntax of mode list is the following:
            // <mode-1>,<mode-2>,...
            // where <mode-N> is:
            // N:mode=<mode-id>;name=\"<mode-name>\"
            // for example:
            // 0:mode=-3;name=\"Haus im Tiefschlaf\",1:mode=-4;name=\"Erhöhter Wärmebedarf\", ...
            modesList.clear();
            String[] modes = ((String) value).trim().replace("\\", "").split(",");
            for (String mode : modes) {
                if (!mode.isEmpty()) {
                    Matcher matcher = pattern.matcher(mode);
                    if (matcher.find()) {
                        String idx = matcher.group(1);
                        String id = matcher.group(2);
                        String name = matcher.group(3);
                        logger.debug("Mode {}: id={}, name={}", idx, id, name);
                        try {
                            modesList.put(Integer.valueOf(id), name);
                        } catch (NumberFormatException e) {
                            logger.warn("Malformed mode id, mode {}: id={}, name={}", idx, id, name);
                        }
                    } else {
                        logger.warn("Malformed mode in timer mode list: {}", mode);
                    }
                }
            }
        }
        super.onStateChange(state);
    }

    private State getTemperatureLabel(String state) {
        Double val = getStateDoubleValue(state);
        if (val != null) {
            LxIRCTemperature temp = LxIRCTemperature.fromIndex(val.intValue());
            return new StringType(temp.getLabel());
        }
        return null;
    }

    private State getCurrentMode() {
        Double val = getStateDoubleValue(STATE_CURRENT_MODE);
        if (val != null) {
            Integer mode = val.intValue();
            String name = modesList.get(mode);
            if (name != null) {
                return new StringType(name);
            }
            logger.debug("Can't decode current timer mode {}, no such mode in modes list.", mode);
        }
        return UnDefType.UNDEF;
    }
}
