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
package org.openhab.binding.freeboxos.internal.api.wifi;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.rest.FbxDevice;

/**
 * The {@link AccessPoint} is the Java class used to map the "SwitchStatus"
 * structure used by the response of the switch status API
 * https://dev.freebox.fr/sdk/os/switch/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AccessPoint extends FbxDevice {
    public class AccessPointsResponse extends Response<List<AccessPoint>> {
    }

    public class AccessPointResponse extends Response<AccessPoint> {
    }

    // private int id;
    // private @NonNullByDefault({}) String name;

    // public String getName() {
    // return name;
    // }

    // public int getId() {
    // return id;
    // }
}
