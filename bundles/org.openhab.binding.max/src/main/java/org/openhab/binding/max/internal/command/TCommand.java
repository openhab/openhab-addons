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
package org.openhab.binding.max.internal.command;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.max.internal.Utils;

/**
 * The {@link T_CubeCommand} is used to unlink MAX! devices from the Cube.
 *
 * @author Marcel Verpaalen - Initial Contribution
 */
@NonNullByDefault
public class TCommand extends CubeCommand {

    private static final int FORCE_UPDATE = 1;
    private static final int NO_FORCE_UPDATE = 0;

    private final List<String> rfAddresses = new ArrayList<>();
    private final boolean forceUpdate;

    public TCommand(String rfAddress, boolean forceUpdate) {
        this.rfAddresses.add(rfAddress);
        this.forceUpdate = forceUpdate;
    }

    /**
     * Adds a rooms for deletion
     */
    public void addRoom(String rfAddress) {
        this.rfAddresses.add(rfAddress);
    }

    @Override
    public String getCommandString() {
        final int updateForced = forceUpdate ? FORCE_UPDATE : NO_FORCE_UPDATE;
        byte[] commandArray = null;
        for (String rfAddress : rfAddresses) {
            commandArray = ArrayUtils.addAll(Utils.hexStringToByteArray(rfAddress), commandArray);
        }
        String encodedString = Base64.getEncoder().encodeToString(commandArray);

        return "t:" + String.format("%02d", rfAddresses.size()) + "," + updateForced + "," + encodedString + "\r\n";
    }

    @Override
    public String getReturnStrings() {
        return "A:";
    }
}
