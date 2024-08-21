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
package org.openhab.binding.boschindego.internal.dto.response;

import org.openhab.binding.boschindego.internal.dto.Battery;
import org.openhab.binding.boschindego.internal.dto.Garden;
import org.openhab.binding.boschindego.internal.dto.response.runtime.DeviceStateRuntimes;

/**
 * Response for operating data.
 * 
 * @author Jacob Laursen - Initial contribution
 */
public class OperatingDataResponse {
    public DeviceStateRuntimes runtime;

    public Battery battery;

    public Garden garden;
}
