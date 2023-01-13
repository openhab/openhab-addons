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
package org.openhab.binding.freeboxos.internal.api.wifi.ap;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.Response;

/**
 * The {@link AccessPointResponses} holds known responses for this API class
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AccessPointResponses {
    public class ApHostsResponse extends Response<List<WifiStation>> {
    }

    public class AccessPointsResponse extends Response<List<WifiAp>> {
    }

    public class AccessPointResponse extends Response<WifiAp> {
    }

}
