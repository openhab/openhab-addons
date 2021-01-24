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
package org.openhab.binding.ventaair.internal;

import java.math.BigDecimal;

/**
 * The {@link VentaAirDeviceConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Stefan Triller - Initial contribution
 */
public class VentaAirDeviceConfiguration {
    public String ipAddress;
    public String macAddress;
    public BigDecimal deviceType;
    public BigDecimal hash;
    public BigDecimal pollingTime;
}
