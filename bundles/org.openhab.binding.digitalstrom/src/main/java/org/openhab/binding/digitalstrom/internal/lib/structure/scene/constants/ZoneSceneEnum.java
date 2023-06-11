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
package org.openhab.binding.digitalstrom.internal.lib.structure.scene.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link ZoneSceneEnum} lists all zone scenes which are available on the dSS-web-interface.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public enum ZoneSceneEnum implements Scene {

    DEEP_OFF((short) 68),
    STANDBY((short) 67),
    SLEEPING((short) 69),
    WAKEUP((short) 70);

    private final short sceneNumber;
    static final Map<Short, ZoneSceneEnum> ZONE_SCENES = new HashMap<>();

    static {
        for (ZoneSceneEnum zs : ZoneSceneEnum.values()) {
            ZONE_SCENES.put(zs.getSceneNumber(), zs);
        }
    }

    private ZoneSceneEnum(short sceneNumber) {
        this.sceneNumber = sceneNumber;
    }

    /**
     * Returns the {@link ZoneSceneEnum} of the given scene number.
     *
     * @param sceneNumber of the {@link ZoneSceneEnum}
     * @return ZoneSceneEnum
     */
    public static ZoneSceneEnum getZoneScene(short sceneNumber) {
        return ZONE_SCENES.get(sceneNumber);
    }

    /**
     * Returns true, if the given scene number contains in digitalSTROM zone scenes, otherwise false.
     *
     * @param sceneNumber to be checked
     * @return true, if contains, otherwise false
     */
    public static boolean containsScene(Short sceneNumber) {
        return ZONE_SCENES.keySet().contains(sceneNumber);
    }

    @Override
    public Short getSceneNumber() {
        return this.sceneNumber;
    }
}
