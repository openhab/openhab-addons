/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.zoneminder.ZoneMinderConstants;

/**
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public enum RefreshPriority {
    PRIORITY_BATCH(1),
    PRIORITY_LOW(2),
    PRIORITY_NORMAL(3),
    PRIORITY_HIGH(4),
    PRIORITY_ALARM(10),
    DISABLED(0),
    UNKNOWN(-1);

    private int value;
    private static Map map = new HashMap<>();

    private RefreshPriority(int value) {
        this.value = value;
    }

    public static RefreshPriority valueOf(int pageType) {
        return (RefreshPriority) map.get(pageType);
    }

    public int getValue() {
        return value;
    }

    public static RefreshPriority fromConfigValue(String value) {
        if (value.equalsIgnoreCase(ZoneMinderConstants.CONFIG_VALUE_REFRESH_DISABLED)) {
            return DISABLED;
        } else if (value.equalsIgnoreCase(ZoneMinderConstants.CONFIG_VALUE_REFRESH_BATCH)) {
            return RefreshPriority.PRIORITY_BATCH;
        } else if (value.equalsIgnoreCase(ZoneMinderConstants.CONFIG_VALUE_REFRESH_LOW)) {
            return RefreshPriority.PRIORITY_LOW;
        } else if (value.equalsIgnoreCase(ZoneMinderConstants.CONFIG_VALUE_REFRESH_NORMAL)) {
            return RefreshPriority.PRIORITY_NORMAL;
        } else if (value.equalsIgnoreCase(ZoneMinderConstants.CONFIG_VALUE_REFRESH_HIGH)) {
            return RefreshPriority.PRIORITY_HIGH;
        } else if (value.equalsIgnoreCase(ZoneMinderConstants.CONFIG_VALUE_REFRESH_ALARM)) {
            return RefreshPriority.PRIORITY_ALARM;
        }
        return UNKNOWN;

    }

    public boolean isEqual(RefreshPriority refrenceVal) {
        if (value == refrenceVal.getValue()) {
            return true;
        }
        return false;
    }

    public boolean isLessThan(RefreshPriority refrenceVal) {
        if (value < refrenceVal.getValue()) {
            return true;
        }
        return false;
    }

    public boolean isGreaterThan(RefreshPriority refrenceVal) {
        if (value > refrenceVal.getValue()) {
            return true;
        }
        return false;
    }

    public boolean isPriorityActive(RefreshPriority refrenceVal) {
        if (value <= refrenceVal.getValue()) {
            return true;
        }
        return false;
    }
}
