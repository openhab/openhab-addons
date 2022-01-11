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

/**
 * The {@link SceneTypes} lists the difference scene types of this digitalSTROM-Library.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class SceneTypes {
    /**
     * This scene type represents a scene with an user defined name.
     */
    public static final String NAMED_SCENE = "namedScene";
    /**
     * This scene type represents a scene, which will be call on a hole zone.
     */
    public static final String ZONE_SCENE = "zoneScene";
    /**
     * This scene type represents a scene, which will be call on the hole apartment.
     */
    public static final String APARTMENT_SCENE = "appScene";
    /**
     * This scene type represents a scene, which will be call on a group.
     */
    public static final String GROUP_SCENE = "groupScene";
}
