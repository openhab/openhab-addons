/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.FullLight;
import org.openhab.binding.hue.internal.HueBridge;

/**
 * The {@link LightStatusListener} is notified when a light status has changed or a light has been removed or added.
 *
 * @author Oliver Libutzki - Initial contribution
 * @author Denis Dudnik - switched to internally integrated source of Jue library, minor code cleanup
 */
@NonNullByDefault
public interface LightStatusListener {

    /**
     * This method is called whenever the state of the given light has changed. The new state can be obtained by
     * {@link FullLight#getState()}.
     *
     * @param bridge The bridge the changed light is connected to.
     * @param light The light which received the state update.
     */
    void onLightStateChanged(@Nullable HueBridge bridge, FullLight light);

    /**
     * This method is called whenever a light is removed.
     *
     * @param bridge The bridge the removed light was connected to.
     * @param light The light which is removed.
     */
    void onLightRemoved(@Nullable HueBridge bridge, FullLight light);

    /**
     * This method is called whenever a light is reported as gone.
     *
     * @param bridge The bridge the reported light was connected to.
     * @param light The light which is reported as gone.
     */
    void onLightGone(@Nullable HueBridge bridge, FullLight light);

    /**
     * This method is called whenever a light is added.
     *
     * @param bridge The bridge the added light was connected to.
     * @param light The light which is added.
     */
    void onLightAdded(@Nullable HueBridge bridge, FullLight light);
}
