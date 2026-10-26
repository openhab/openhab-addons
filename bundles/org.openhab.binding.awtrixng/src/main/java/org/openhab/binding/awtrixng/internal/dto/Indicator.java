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
package org.openhab.binding.awtrixng.internal.dto;

/**
 * The state of one indicator, as reported in the {@code indicators} array of the device state.
 *
 * @author Keith T. Garner - Initial contribution
 */
public class Indicator {
    public boolean on;
    public String color;
    public int blinkMs;
    public int fadeMs;
}
