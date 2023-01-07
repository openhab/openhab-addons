/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.nikobus.internal.protocol;

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nikobus.internal.protocol.NikobusCommand.Result;
import org.openhab.binding.nikobus.internal.utils.CRCUtil;

/**
 * The {@link NikobusCommand} class defines factory functions to create commands that can be send to Nikobus
 * installation.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
public class SwitchModuleCommandFactory {
    public static NikobusCommand createReadCommand(String address, SwitchModuleGroup group,
            Consumer<Result> resultConsumer) {
        checkAddress(address);

        String commandPayload = CRCUtil.appendCRC2("$10" + CRCUtil.appendCRC(group.getStatusRequest() + address));
        return new NikobusCommand(commandPayload, 27, 3, "$1C", resultConsumer);
    }

    public static NikobusCommand createWriteCommand(String address, SwitchModuleGroup group, String value,
            Consumer<Result> resultConsumer) {
        checkAddress(address);
        if (value.length() != 12) {
            throw new IllegalArgumentException(String.format("Value must have 12 chars but got '%s'", value));
        }

        String payload = group.getStatusUpdate() + address + value + "FF";
        return new NikobusCommand(CRCUtil.appendCRC2("$1E" + CRCUtil.appendCRC(payload)), 13, 5, "$0E", resultConsumer);
    }

    private static void checkAddress(String address) {
        if (address.length() != 4) {
            throw new IllegalArgumentException(String.format("Address must have 4 chars but got '%s'", address));
        }
    }
}
