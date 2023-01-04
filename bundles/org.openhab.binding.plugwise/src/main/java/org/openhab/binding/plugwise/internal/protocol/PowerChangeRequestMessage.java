/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.plugwise.internal.protocol;

import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.POWER_CHANGE_REQUEST;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Requests the power state of a relay device (Circle, Circle+, Stealth) to be switched on/off. The current power state
 * of a device is retrieved by sending an {@link InformationRequestMessage} and reading the
 * {@link InformationResponseMessage#getPowerState()} value.
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
 */
public class PowerChangeRequestMessage extends Message {

    public PowerChangeRequestMessage(MACAddress macAddress, boolean powerState) {
        super(POWER_CHANGE_REQUEST, macAddress, powerState ? "01" : "00");
    }
}
