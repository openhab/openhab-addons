/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.allplay.internal;

import java.util.Dictionary;

/**
 * AllPlay binding properties.
 *
 * @author Dominic Lerbs - Initial contribution
 */
public class AllPlayBindingProperties {

    private final double rewindSkipTimeInSec;
    private final double fastForwardSkipTimeInSec;
    private final String callbackUrl;

    private static final String REWIND_SKIP_TIME_PROPERTY = "rewindSkipTimeInSec";
    private static final double REWIND_SKIP_TIME_DEFAULT_VALUE = 10;
    private static final String FAST_FORWARD_SKIP_TIME_PROPERTY = "fastForwardSkipTimeInSec";
    private static final double FAST_FORWARD_SKIP_TIME_DEFAULT_VALUE = 10;
    private static final String CALLBACK_URL = "callbackUrl";

    public AllPlayBindingProperties(Dictionary<String, Object> properties) {

        rewindSkipTimeInSec = getDoubleProperty(properties, REWIND_SKIP_TIME_PROPERTY, REWIND_SKIP_TIME_DEFAULT_VALUE);
        fastForwardSkipTimeInSec = getDoubleProperty(properties, FAST_FORWARD_SKIP_TIME_PROPERTY,
                FAST_FORWARD_SKIP_TIME_DEFAULT_VALUE);
        callbackUrl = (String) properties.get(CALLBACK_URL);
    }

    private double getDoubleProperty(Dictionary<String, Object> properties, String propertyKey, double defaultValue) {
        String value = (String) properties.get(propertyKey);
        return value != null ? Double.parseDouble(value) : defaultValue;
    }

    public double getRewindSkipTimeInSec() {
        return rewindSkipTimeInSec;
    }

    public double getFastForwardSkipTimeInSec() {
        return fastForwardSkipTimeInSec;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }
}
