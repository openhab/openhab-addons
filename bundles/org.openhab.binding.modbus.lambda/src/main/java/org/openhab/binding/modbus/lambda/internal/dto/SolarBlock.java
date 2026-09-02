/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.modbus.lambda.internal.dto;

/**
 * Data transfer object for solar thermic component data
 *
 * @author Paul Frank - Initial contribution
 * @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus to add handler
 */
public class SolarBlock {
    public int solarErrorNumber;
    public int solarOperatingState;
    public int solarCollectorTemperature;
    public int solarBuffer1Temperature;
    public int solarBuffer2Temperature;
}
