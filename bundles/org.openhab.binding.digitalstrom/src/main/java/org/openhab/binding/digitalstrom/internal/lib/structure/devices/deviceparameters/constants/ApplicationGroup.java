package org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants;

import java.util.HashMap;
import java.util.Map;

public enum ApplicationGroup {

    LIGHTS((short) 1, Color.YELLOW),
    BLINDS((short) 2, Color.GREY),
    HEATING((short) 3, Color.BLUE),
    COOLING((short) 9, Color.BLUE),
    VENTILATION((short) 10, Color.BLUE),
    WINDOW((short) 11, Color.BLUE),
    RECIRCULATION((short) 12, Color.BLUE),
    APARTMENT_VENTILATION((short) 64, Color.BLUE),
    TEMPERATURE_CONTROL((short) 48, Color.BLUE),
    AUDIO((short) 4, Color.CYAN),
    VIDEO((short) 5, Color.MAGENTA),
    JOKER((short) 8, Color.BLACK),
    SINGLE_DEVICE((short) -1, Color.WHITE),
    SECURITY((short) -2, Color.RED),
    ACCESS((short) -3, Color.GREEN),
    UNDEFINED(null, Color.UNDEFINED);

    public enum Color {
        YELLOW,
        GREY,
        BLUE,
        CYAN,
        MAGENTA,
        BLACK,
        WHITE,
        RED,
        GREEN,
        UNDEFINED
    }

    private Short groupId;

    static final Map<Short, ApplicationGroup> APPLICATION_GROUPS = new HashMap<>();

    private Color color;

    static {
        for (ApplicationGroup applications : ApplicationGroup.values()) {
            APPLICATION_GROUPS.put(applications.getId(), applications);
        }
    }

    private ApplicationGroup(Short groupId, Color color) {
        this.groupId = groupId;
        this.color = color;
    }

    public Short getId() {
        return groupId;
    }

    /**
     * Returns the corresponding ApplicationGroup or ApplicationGroup.UNDEFINED if there is no
     * ApplicationGroup for the given groupId.
     * 
     * @param groupId
     * @return ApplicationGroup or ApplicationGroup.UNDEFINED
     */
    public static ApplicationGroup getGroup(Short groupId) {
        return APPLICATION_GROUPS.containsKey(groupId) ? APPLICATION_GROUPS.get(groupId) : ApplicationGroup.UNDEFINED;
    }

    public Color getColor() {
        return color;
    }
}
