/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.modbus.stiebeleltron.internal.dto;

/**
 * Dto class for the Runtime Block of a WPM3i compatible heat pump
 *
 * @author Thomas Burri - Initial contribution
 *
 */
public class RuntimeBlockWpm3i {

    public int runtimeCompressorHeating;
    public int runtimeCompressorHotwater;
    public int runtimeCompressorCooling;
    public int runtimeNhz1;
    public int runtimeNhz2;
    public int runtimeNhz12;
}
