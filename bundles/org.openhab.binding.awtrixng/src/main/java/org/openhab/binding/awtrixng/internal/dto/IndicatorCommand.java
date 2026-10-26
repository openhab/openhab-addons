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
 * The payload of {@code cmd/indicators/<n>}. An empty payload switches the indicator off.
 *
 * @author Keith T. Garner - Initial contribution
 */
public class IndicatorCommand {
    public String color;
    public Integer blinkMs;
    public Integer fadeMs;

    public IndicatorCommand(String color) {
        this.color = color;
    }
}
