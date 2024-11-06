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
package org.openhab.binding.electroluxappliance.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ElectroluxApplianceBridgeConfiguration} class contains fields mapping bridge configuration parameters.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class ElectroluxApplianceBridgeConfiguration {
    public String apiKey = "";
    public String refreshToken = "";
    public int refresh = 600;
}
