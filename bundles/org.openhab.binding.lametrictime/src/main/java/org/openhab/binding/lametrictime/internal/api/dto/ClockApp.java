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
package org.openhab.binding.lametrictime.internal.api.dto;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.SortedMap;
import java.util.TreeMap;

import org.openhab.binding.lametrictime.internal.api.local.dto.BooleanParameter;
import org.openhab.binding.lametrictime.internal.api.local.dto.Parameter;
import org.openhab.binding.lametrictime.internal.api.local.dto.StringParameter;
import org.openhab.binding.lametrictime.internal.api.local.dto.UpdateAction;

/**
 * Implementation class for the ClockApp.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class ClockApp extends CoreApplication {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final String NAME = "com.lametric.clock";

    private static final String ACTION_ALARM = "clock.alarm";

    private static final String PARAMETER_ENABLED = "enabled";
    private static final String PARAMETER_TIME = "time";
    private static final String PARAMETER_WAKE_WITH_RADIO = "wake_with_radio";

    public ClockApp() {
        super(NAME);
    }

    public CoreAction setAlarm(Boolean enabled, LocalTime time, Boolean wakeWithRadio) {
        SortedMap<String, Parameter> parameters = new TreeMap<>();

        if (enabled != null) {
            parameters.put(PARAMETER_ENABLED, new BooleanParameter().withValue(enabled));
        }

        if (time != null) {
            parameters.put(PARAMETER_TIME, new StringParameter().withValue(time.format(TIME_FORMATTER)));
        }

        if (wakeWithRadio != null) {
            parameters.put(PARAMETER_WAKE_WITH_RADIO, new BooleanParameter().withValue(wakeWithRadio));
        }

        return new CoreAction(this, new UpdateAction().withId(ACTION_ALARM).withParameters(parameters));
    }

    public CoreAction stopAlarm() {
        SortedMap<String, Parameter> parameters = new TreeMap<>();
        parameters.put(PARAMETER_ENABLED, new BooleanParameter().withValue(false));

        return new CoreAction(this, new UpdateAction().withId(ACTION_ALARM).withParameters(parameters));
    }
}
