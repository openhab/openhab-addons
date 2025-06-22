/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ring.internal;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ring.internal.device.RingDevice;

/**
 * The AccountHandler implements this interface to facilitate the
 * use of the common services.
 *
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */
@NonNullByDefault
public interface RingAccount {
    Collection<RingDevice> getAllDevices();

    @Nullable
    RingDevice getDevice(String id);
}
