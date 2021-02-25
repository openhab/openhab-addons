/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

package org.openhab.binding.freeathomesystem.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link FreeAtHomeSysApDeviceList} is the interface for SysAp and Test deive lists.
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public interface FreeAtHomeDeviceList {
    public boolean buildComponentList();

    public FreeAtHomeDeviceDescription getDeviceDescription(String id);

    public String getDeviceIdByIndex(int index);

    public int getNumberOfDevices();
}
