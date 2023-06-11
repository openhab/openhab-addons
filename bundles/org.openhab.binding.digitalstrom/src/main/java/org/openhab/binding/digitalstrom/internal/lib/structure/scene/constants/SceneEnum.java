/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.lib.structure.scene.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link SceneEnum} lists all available scenes of digitalSTROM.
 *
 * @author Alexander Betker - initial contributer
 * @version digitalSTROM-API 1.14.5
 *
 * @author Michael Ochel - add new scenes and missing java-doc
 * @author Mathias Siegele - add new scenes and missing java-doc
 *
 */
public enum SceneEnum implements Scene {

    /*
     * see http://developer.digitalstrom.org/Architecture/ds-basics.pdf appendix B, page 44
     */

    /* Area scene commands */
    AREA_1_OFF((short) 1), // Set output value to Preset Area 1 Off (Default: Off)
    AREA_1_ON((short) 6), // Set output value to Preset Area 1 On (Default: On)
    AREA_1_INCREMENT((short) 43), // Initial command to increment output value
    AREA_1_DECREMENT((short) 42), // Initial command to decrement output value
    AREA_1_STOP((short) 52), // Stop output value change at current position
    AREA_STEPPING_CONTINUE((short) 10), // Next step to increment or decrement

    AREA_2_OFF((short) 2), // Set output value to Area 2 Off (Default: Off)
    AREA_2_ON((short) 7), // Set output value to Area 2 On (Default: On)
    AREA_2_INCREMENT((short) 45), // Initial command to increment output value
    AREA_2_DECREMENT((short) 44), // Initial command to decrement output value
    AREA_2_STOP((short) 53), // Stop output value change at current position

    AREA_3_OFF((short) 3), // Set output value to Area 3 Off (Default: Off)
    AREA_3_ON((short) 8), // Set output value to Area 3 On (Default: On)
    AREA_3_INCREMENT((short) 47), // Initial command to increment output value
    AREA_3_DECREMENT((short) 46), // Initial command to decrement output value
    AREA_3_STOP((short) 54), // Stop output value change at current position

    AREA_4_OFF((short) 4), // Set output value to Area 4 Off (Default: Off)
    AREA_4_ON((short) 9), // Set output value to Area 4 On (Default: On)
    AREA_4_INCREMENT((short) 49), // Initial command to increment output value
    AREA_4_DECREMENT((short) 48), // Initial command to decrement output value
    AREA_4_STOP((short) 55), // Stop output value change at current position

    /* local pushbutton scene commands */
    DEVICE_ON((short) 51), // Local on
    DEVICE_OFF((short) 50), // Local off
    DEVICE_STOP((short) 15), // Stop output value change at current position

    /* special scene commands */
    MINIMUM((short) 13), // Minimum output value
    MAXIMUM((short) 14), // Maximum output value
    STOP((short) 15), // Stop output value change at current position
    AUTO_OFF((short) 40), // slowly fade down to off

    /* stepping scene commands */
    INCREMENT((short) 12), // Increment output value (in the basic.pdf it is 11 but its wrong)
    DECREMENT((short) 11), // Decrement output value (in the basic.pdf it is 12 but its wrong)

    /* presets */
    PRESET_0((short) 0), // Set output value to Preset 0 (Default: Off)
    PRESET_1((short) 5), // Set output value to Preset 1 (Default: On)
    PRESET_2((short) 17), // Set output value to Preset 2
    PRESET_3((short) 18), // Set output value to Preset 3
    PRESET_4((short) 19), // Set output value to Preset 4

    PRESET_10((short) 32), // Set output value to Preset 10 (Default: Off)
    PRESET_11((short) 33), // Set output value to Preset 11 (Default: On)
    PRESET_12((short) 20), // Set output value to Preset 12
    PRESET_13((short) 21), // Set output value to Preset 13
    PRESET_14((short) 22), // Set output value to Preset 14

    PRESET_20((short) 34), // Set output value to Preset 20 (Default: Off)
    PRESET_21((short) 35), // Set output value to Preset 21 (Default: On)
    PRESET_22((short) 23), // Set output value to Preset 22
    PRESET_23((short) 24), // Set output value to Preset 23
    PRESET_24((short) 25), // Set output value to Preset 24

    PRESET_30((short) 36), // Set output value to Preset 30 (Default: Off)
    PRESET_31((short) 37), // Set output value to Preset 31 (Default: On)
    PRESET_32((short) 26), // Set output value to Preset 32
    PRESET_33((short) 27), // Set output value to Preset 33
    PRESET_34((short) 28), // Set output value to Preset 34

    PRESET_40((short) 38), // Set output value to Preset 40 (Default: Off)
    PRESET_41((short) 39), // Set output value to Preset 41 (Default: On)
    PRESET_42((short) 29), // Set output value to Preset 42
    PRESET_43((short) 30), // Set output value to Preset 43
    PRESET_44((short) 31), // Set output value to Preset 44

    /* group independent scene commands */
    DEEP_OFF((short) 68),
    ENERGY_OVERLOAD((short) 66),
    STANDBY((short) 67),
    ZONE_ACTIVE((short) 75),
    ALARM_SIGNAL((short) 74),
    AUTO_STANDBY((short) 64),
    ABSENT((short) 72),
    PRESENT((short) 71),
    SLEEPING((short) 69),
    WAKEUP((short) 70),
    DOOR_BELL((short) 73),
    PANIC((short) 65),
    FIRE((short) 76),
    ALARM_1((short) 74),
    ALARM_2((short) 83),
    ALARM_3((short) 84),
    ALARM_4((short) 85),
    WIND((short) 86),
    NO_WIND((short) 87),
    RAIN((short) 88),
    NO_RAIN((short) 89),
    HAIL((short) 90),
    NO_HAIL((short) 91);

    private final short sceneNumber;
    static final Map<Short, SceneEnum> DIGITALSTROM_SCENES = new HashMap<>();

    static {
        for (SceneEnum zs : SceneEnum.values()) {
            DIGITALSTROM_SCENES.put(zs.getSceneNumber(), zs);
        }
    }

    private SceneEnum(short sceneNumber) {
        this.sceneNumber = sceneNumber;
    }

    /**
     * Returns the {@link SceneEnum} for the given scene number.
     *
     * @param sceneNumber of the {@link SceneEnum}
     * @return SceneEnum
     */
    public static SceneEnum getScene(short sceneNumber) {
        return DIGITALSTROM_SCENES.get(sceneNumber);
    }

    /**
     * Returns true, if the given scene number contains in digitalSTROM scenes, otherwise false.
     *
     * @param sceneNumber to be checked
     * @return true, if contains otherwise false
     */
    public static boolean containsScene(Short sceneNumber) {
        return DIGITALSTROM_SCENES.keySet().contains(sceneNumber);
    }

    @Override
    public Short getSceneNumber() {
        return this.sceneNumber;
    }
}
