/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.command;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.net.util.Base64;
import org.openhab.binding.max.internal.Utils;

/**
 * The {@link T_CubeCommand} is used to unlink MAX! devices from the Cube.
 *
 * @author Marcel Verpaalen - Initial Contribution
 * @since 2.0
 *
 */

public class T_Command extends CubeCommand {

    private static final int FORCE_UPDATE = 1;
    private static final int NO_FORCE_UPDATE = 0;

    ArrayList<String> rfAddresses = new ArrayList<String>();
    private boolean forceUpdate = true;

    /**
     * {@link T_CubeCommand}
     *
     * @param rfAddress
     * @param forceUpdate
     */
    public T_Command(String rfAddress, boolean forceUpdate) {
        this.rfAddresses.add(rfAddress);
        this.forceUpdate = forceUpdate;
    }

    /**
     * Adds a rooms for deletion
     *
     * @param rfAddress
     */
    public void addRoom(String rfAddress) {
        this.rfAddresses.add(rfAddress);
    }

    @Override
    public String getCommandString() {
        int updateForced = 0;
        if (forceUpdate) {
            updateForced = FORCE_UPDATE;
        } else {
            updateForced = NO_FORCE_UPDATE;
        }
        byte[] commandArray = null;
        for (String rfAddress : rfAddresses) {
            commandArray = ArrayUtils.addAll(Utils.hexStringToByteArray(rfAddress), commandArray);
        }
        String encodedString = Base64.encodeBase64StringUnChunked(commandArray);

        String cmd = "t:" + String.format("%02d", rfAddresses.size()) + "," + updateForced + "," + encodedString + '\r'
                + '\n';
        return cmd;
    }

    @Override
    public String getReturnStrings() {
        return "A:";
    }

}
