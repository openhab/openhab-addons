/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.command;

/**
 * The {@link F_CubeCommand} is used to query and update the NTP servers used by the Cube.
 *
 * @author Marcel Verpaalen - Initial Contribution
 * @since 2.0
 *
 */
public class F_Command extends CubeCommand {

    private String ntpServer1 = "";
    private String ntpServer2 = "";

    /**
     * Queries the Cube for the NTP info
     */
    public F_Command() {
    }

    /**
     * Updates the Cube the NTP info
     *
     * @param ntpServer1
     * @param ntpServer2
     */
    public F_Command(String ntpServer1, String ntpServer2) {
        this.ntpServer1 = ntpServer1 != null ? ntpServer1 : "";
        this.ntpServer2 = ntpServer2 != null ? ntpServer2 : "";
    }

    @Override
    public String getCommandString() {
        String servers;
        if (ntpServer1.length() > 0 && ntpServer2.length() > 0) {
            servers = ntpServer1 + "," + ntpServer2;
        } else {
            servers = ntpServer1 + ntpServer2;
        }

        String cmd = "f:" + servers + '\r' + '\n';
        return cmd;
    }

    @Override
    public String getReturnStrings() {
        return "F:";
    }

}
