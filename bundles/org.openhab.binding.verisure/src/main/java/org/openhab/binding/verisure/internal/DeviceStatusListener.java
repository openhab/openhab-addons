/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.verisure.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.verisure.internal.dto.VerisureThingDTO;

/**
 * The {@link DeviceStatusListener} is notified when a device status has changed
 * or a device has been removed or added.
 *
 * @author Jarle Hjortland - Initial contribution
 * @author Jan Gustafsson - Updated after code review comments
 *
 */
@NonNullByDefault
public interface DeviceStatusListener<T extends VerisureThingDTO> {

    /**
     * This method is called whenever the state of the given device has changed.
     *
     * @param thing
     *            The thing that was changed.
     */
    void onDeviceStateChanged(T thing);

    /**
     * This method returns the thing's class
     */
    Class<T> getVerisureThingClass();
}
