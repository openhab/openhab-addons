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
package org.openhab.binding.avmfritz.internal.hardware;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.avmfritz.internal.dto.AVMFritzBaseModel;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link FritzAhaStatusListener} is notified when a new device has been added, removed or its status has changed.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public interface FritzAhaStatusListener {

    /**
     * This method is called whenever a device is added.
     *
     * @param device the {@link AVMFritzBaseModel}
     */
    void onDeviceAdded(AVMFritzBaseModel device);

    /**
     * This method is called whenever a device is updated.
     *
     * @param thingUID the {@link ThingUID}
     * @param device the {@link AVMFritzBaseModel}
     */
    void onDeviceUpdated(ThingUID thingUID, AVMFritzBaseModel device);

    /**
     * This method is called whenever a device is gone.
     *
     * @param thingUID the {@link ThingUID}
     */
    void onDeviceGone(ThingUID thingUID);
}
