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
package org.openhab.binding.senseenergy.internal.api.dto;

/**
 * {@link SenseEnergyWebSocketDevice} is dto for the websocket messages. Fields which are commented are not used in the
 * binding, but there for reference.
 *
 * @author Jeff James - Initial contribution
 */
public class SenseEnergyWebSocketDevice {
    public String id;
    public String name;
    // public String icon;
    public float w;
}
