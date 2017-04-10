package org.openhab.binding.omnilink.protocol;

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
