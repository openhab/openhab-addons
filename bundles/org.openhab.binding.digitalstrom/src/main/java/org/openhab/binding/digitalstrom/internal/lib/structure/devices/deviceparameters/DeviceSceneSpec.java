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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters;

import org.openhab.binding.digitalstrom.internal.lib.structure.scene.constants.Scene;

/**
 * The {@link DeviceSceneSpec} saves a digitalSTROM-Device scene mode.
 *
 * @author Alexander Betker - Initial contribution
 * @author Michael Ochel - add missing java-doc
 * @author Matthias Siegele - add missing java-doc
 */
public interface DeviceSceneSpec {

    /**
     * Returns the sceneID.
     *
     * @return sceneID
     */
    Scene getScene();

    /**
     * Returns true, if the don't care flag is set, otherwise false.
     *
     * @return true, if dont't care is set, otherwise false
     */
    boolean isDontCare();

    /**
     * Sets the don't care flag.
     *
     * @param dontcare to set
     */
    void setDontcare(boolean dontcare);

    /**
     * Returns true, if the local priority flag is set, otherwise false.
     *
     * @return true, if local priority is, set otherwise false
     */
    boolean isLocalPrio();

    /**
     * Sets the local priority flag.
     *
     * @param localPrio to set
     */
    void setLocalPrio(boolean localPrio);

    /**
     * Returns true, if the special mode flag is set, otherwise false.
     *
     * @return true, if special mode is set, otherwise false
     */
    boolean isSpecialMode();

    /**
     * Sets the special mode flag.
     *
     * @param specialMode to set
     */
    void setSpecialMode(boolean specialMode);

    /**
     * Returns true, if the flash mode flag is set, otherwise false.
     *
     * @return true, if flash mode is set, otherwise false
     */
    boolean isFlashMode();

    /**
     * Sets the flash mode flag.
     *
     * @param flashMode to set
     */
    void setFlashMode(boolean flashMode);
}
