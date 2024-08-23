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
package org.openhab.io.hueemulation.internal;

/**
 * The pure item type is not enough to decide how we expose an item.
 * We need to consider the assigned tags and category as well. This
 * computed device type is stored next to the item itself.
 *
 * @author David Graeff - Initial contribution
 */
public enum DeviceType {
    SwitchType,
    WhiteType,
    WhiteTemperatureType,
    ColorType
}
