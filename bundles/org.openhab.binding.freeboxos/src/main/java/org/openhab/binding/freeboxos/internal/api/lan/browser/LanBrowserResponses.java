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
package org.openhab.binding.freeboxos.internal.api.lan.browser;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.Response;

/**
 * The {@link LanBrowserResponses} holds known responses for this API class
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LanBrowserResponses {
    public static class HostsResponse extends Response<List<LanHost>> {
    }

    public static class HostResponse extends Response<LanHost> {

    }

    public static class InterfacesResponse extends Response<List<LanInterface>> {
    }

    public static class InterfaceResponse extends Response<LanInterface> {
    }
}
