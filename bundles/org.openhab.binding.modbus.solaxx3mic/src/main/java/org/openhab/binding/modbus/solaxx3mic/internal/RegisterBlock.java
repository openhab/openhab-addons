/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.modbus.solaxx3mic.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RegisterBlock} describes possible values for an inverter's status
 * field
 *
 * @author Stanislaw Wawszczak - Initial contribution
 */
@NonNullByDefault
public class RegisterBlock {
    public int address;
    public int length;
    public RegisterBlockFunction function;

    RegisterBlock(int addr, int len, RegisterBlockFunction func) {
        address = addr;
        length = len;
        function = func;
    }
}
