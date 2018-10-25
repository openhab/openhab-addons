/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.omnilink.protocol;

/**
 *
 * @author craigh
 *
 */
public enum AreaAlarmStatus {

    OFF(0, "Off"),
    DAY(1, "Day"),
    NIGHT(2, "Night"),
    AWAY(3, "Away"),
    VACATION(4, "Vacation"),
    DAY_INSTANT(5, "Day Instant"),
    NIGHT_DELAY(6, "Night Delay");

    private int mId;
    private String mText;
    private static AreaAlarmStatus[] mStatuses = { OFF, DAY, NIGHT };

    AreaAlarmStatus(int id, String text) {
        mId = id;
        mText = text;
    }

    public static AreaAlarmStatus alarmStatusFromId(int id) {
        return mStatuses[id];
    }

    @Override
    public String toString() {
        return mText;
    }
}
