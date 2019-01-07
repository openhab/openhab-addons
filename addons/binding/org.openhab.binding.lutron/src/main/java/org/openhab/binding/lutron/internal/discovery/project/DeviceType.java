/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.discovery.project;

/**
 * Type of input device in a Lutron system.
 *
 * @author Allan Tong - Initial contribution
 */
public enum DeviceType {
    HYBRID_SEETOUCH_KEYPAD,
    MAIN_REPEATER,
    MOTION_SENSOR,
    PICO_KEYPAD,
    SEETOUCH_KEYPAD,
    SEETOUCH_TABLETOP_KEYPAD,
    VISOR_CONTROL_RECEIVER
}
