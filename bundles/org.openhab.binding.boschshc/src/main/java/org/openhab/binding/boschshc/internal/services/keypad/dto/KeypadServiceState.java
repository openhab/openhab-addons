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
package org.openhab.binding.boschshc.internal.services.keypad.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;
import org.openhab.binding.boschshc.internal.services.keypad.KeypadService;

/**
 * State object of the {@link KeypadService}.
 * <p>
 * Example JSON:
 * 
 * <pre>
 * {
 *   "@type":"keypadState",
 *   "keyCode":1,
 *   "keyName":"UPPER_LEFT_BUTTON",
 *   "eventType":"PRESS_SHORT",
 *   "eventTimestamp":1705130891435
 * }
 * </pre>
 * 
 * @author David Pace - Initial contribution
 *
 */
public class KeypadServiceState extends BoschSHCServiceState {

    public KeypadServiceState() {
        super("keypadState");
    }

    public int keyCode;

    public KeyName keyName;

    public KeyEventType eventType;

    public long eventTimestamp;
}
