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
package org.openhab.binding.freeboxos.internal.api.lan;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.rest.FbxDevice;

/**
 * The {@link LanInterface} is the Java class used to map the
 * structure used by the Lan Interface Browser API
 * https://dev.freebox.fr/sdk/os/lan/#lan-browser
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
class LanInterface extends FbxDevice {
    public static class LanInterfacesResponse extends Response<List<LanInterface>> {
    }

    public static class LanInterfaceResponse extends Response<LanInterface> {
    }

    // private @NonNullByDefault({}) String name;

    // public String getName() {
    // return name;
    // }
}
