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
package org.openhab.binding.onewiregpio.internal;

import java.math.BigDecimal;

/**
 * The {@link OneWireGpioConfiguration} Configuration class for easier configuration read through the reflection method
 * in
 * the framework.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class OneWireGpioConfiguration {
    public String gpio_bus_file;
    public Integer refresh_time;
    public BigDecimal precision;
}
