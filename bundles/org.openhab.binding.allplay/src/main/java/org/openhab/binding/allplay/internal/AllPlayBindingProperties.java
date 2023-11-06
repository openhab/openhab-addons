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
package org.openhab.binding.allplay.internal;

import java.util.Dictionary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AllPlay binding properties.
 *
 * @author Dominic Lerbs - Initial contribution
 */
public class AllPlayBindingProperties {

    private final Logger logger = LoggerFactory.getLogger(AllPlayBindingProperties.class);

    private final int rewindSkipTimeInSec;
    private final int fastForwardSkipTimeInSec;
    private final String callbackUrl;
    private final String zoneMemberSeparator;

    private static final String REWIND_SKIP_TIME_PROPERTY = "rewindSkipTimeInSec";
    private static final int REWIND_SKIP_TIME_DEFAULT_VALUE = 10;
    private static final String FAST_FORWARD_SKIP_TIME_PROPERTY = "fastForwardSkipTimeInSec";
    private static final int FAST_FORWARD_SKIP_TIME_DEFAULT_VALUE = 10;
    private static final String CALLBACK_URL = "callbackUrl";
    private static final String ZONE_MEMBER_SEPARATOR_PROPERTY = "zoneMemberSeparator";
    private static final String ZONE_MEMBER_SEPARATOR_DEFAULT_VALUE = ",";

    public AllPlayBindingProperties(Dictionary<String, Object> properties) {
        rewindSkipTimeInSec = getIntegerProperty(properties, REWIND_SKIP_TIME_PROPERTY, REWIND_SKIP_TIME_DEFAULT_VALUE);
        fastForwardSkipTimeInSec = getIntegerProperty(properties, FAST_FORWARD_SKIP_TIME_PROPERTY,
                FAST_FORWARD_SKIP_TIME_DEFAULT_VALUE);
        callbackUrl = (String) properties.get(CALLBACK_URL);
        zoneMemberSeparator = getStringProperty(properties, ZONE_MEMBER_SEPARATOR_PROPERTY,
                ZONE_MEMBER_SEPARATOR_DEFAULT_VALUE);
    }

    public int getRewindSkipTimeInSec() {
        return rewindSkipTimeInSec;
    }

    public int getFastForwardSkipTimeInSec() {
        return fastForwardSkipTimeInSec;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    private int getIntegerProperty(Dictionary<String, Object> properties, String propertyKey, int defaultValue) {
        Object configValue = properties.get(propertyKey);
        int value = defaultValue;
        if (configValue instanceof String stringValue) {
            try {
                value = Integer.parseInt(stringValue);
            } catch (NumberFormatException e) {
                logger.warn("Unable to convert value {} for config property {} to integer. Using default value.",
                        configValue, propertyKey);
            }
        } else if (configValue instanceof Integer) {
            value = (int) configValue;
        }
        return value;
    }

    public String getZoneMemberSeparator() {
        return zoneMemberSeparator;
    }

    private String getStringProperty(Dictionary<String, Object> properties, String propertyKey, String defaultValue) {
        String value = (String) properties.get(propertyKey);
        return value != null ? value : defaultValue;
    }
}
