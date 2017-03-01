package org.openhab.binding.elkm1.internal.elk;

/**
 * The definition of the zone.
 *
 * @author David Benentt - Initial Contribution
 */
public enum ElkZoneDefinition {
    Disabled(0),
    BurglerAlarmEntryExit1(1),
    BurglerAlarmEntryExit2(2),
    BurglerPerimeterInstant(3),
    BurglerInterior(4),
    BurglerInteriorFollower(5),
    BurglerInteriorNight(6),
    BurglerInteriorNightDelay(7),
    Burgler24Hour(8),
    FireAlarm(9),
    FireVerified(10),
    FireSupervisory(11),
    AuxAlarm1(12),
    AuxAlarm2(13),
    KeyFob(14),
    NonAlarm(15),
    CarbonMonoxide(16),
    EmergencyAlarm(17);

    int value;

    private ElkZoneDefinition(int val) {
        this.value = val;
    }

    /**
     * Turns this into a nice integer.
     *
     * @return
     */
    public int toInt() {
        return this.value;
    }

    /**
     * Get a zone def from the int value.
     *
     * @param i
     * @return
     */
    public static ElkZoneDefinition fromInt(int i) {
        for (ElkZoneDefinition def : ElkZoneDefinition.values()) {
            if (def.toInt() == i) {
                return def;
            }
        }
        return null;
    }
}
