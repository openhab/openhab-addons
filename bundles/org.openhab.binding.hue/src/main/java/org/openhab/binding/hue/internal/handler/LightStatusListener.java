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
package org.openhab.binding.hue.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hue.internal.dto.FullLight;

/**
 * The {@link LightStatusListener} is notified when a light status has changed or a light has been removed or added.
 *
 * @author Oliver Libutzki - Initial contribution
 * @author Denis Dudnik - switched to internally integrated source of Jue library, minor code cleanup
 */
@NonNullByDefault
public interface LightStatusListener {

    /**
     * This method returns the light id of the listener
     * 
     * @return
     */
    String getLightId();

    /**
     * This method is called whenever the state of the given light has changed. The new state can be obtained by
     * {@link FullLight#getState()}.
     *
     * @param light The light which received the state update.
     * @return
     */
    boolean onLightStateChanged(FullLight light);

    /**
     * This method is called whenever a light is removed.
     */
    void onLightRemoved();

    /**
     * This method is called whenever a light is reported as gone.
     */
    void onLightGone();

    /**
     * This method is called whenever a light is added.
     *
     * @param light The light which is added.
     */
    void onLightAdded(FullLight light);

    /**
     * The thing will block state updates for set time.
     * 
     * @param bypassTime
     */
    void setPollBypass(long bypassTime);

    /**
     * Unblock state updates.
     */
    void unsetPollBypass();
}
