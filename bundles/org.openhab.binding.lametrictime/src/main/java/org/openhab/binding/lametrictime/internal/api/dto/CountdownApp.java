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
package org.openhab.binding.lametrictime.internal.api.dto;

import java.util.SortedMap;
import java.util.TreeMap;

import org.openhab.binding.lametrictime.internal.api.local.dto.BooleanParameter;
import org.openhab.binding.lametrictime.internal.api.local.dto.IntegerParameter;
import org.openhab.binding.lametrictime.internal.api.local.dto.Parameter;
import org.openhab.binding.lametrictime.internal.api.local.dto.UpdateAction;

/**
 * Implementation class for the CountdownApp.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class CountdownApp extends CoreApplication {
    private static final String NAME = "com.lametric.countdown";

    private static final String ACTION_CONFIGURE = "countdown.configure";
    private static final String ACTION_PAUSE = "countdown.pause";
    private static final String ACTION_RESET = "countdown.reset";
    private static final String ACTION_START = "countdown.start";

    private static final String PARAMETER_DURATION = "duration";
    private static final String PARAMETER_START_NOW = "start_now";

    public CountdownApp() {
        super(NAME);
    }

    public CoreAction configure(int duration, boolean startNow) {
        SortedMap<String, Parameter> parameters = new TreeMap<>();
        parameters.put(PARAMETER_DURATION, new IntegerParameter().withValue(duration));
        parameters.put(PARAMETER_START_NOW, new BooleanParameter().withValue(startNow));

        return new CoreAction(this, new UpdateAction().withId(ACTION_CONFIGURE).withParameters(parameters));
    }

    public CoreAction pause() {
        return new CoreAction(this, new UpdateAction().withId(ACTION_PAUSE));
    }

    public CoreAction reset() {
        return new CoreAction(this, new UpdateAction().withId(ACTION_RESET));
    }

    public CoreAction start() {
        return new CoreAction(this, new UpdateAction().withId(ACTION_START));
    }
}
