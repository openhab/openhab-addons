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
package org.openhab.binding.max.internal.command;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link FCommand} is used to query and update the NTP servers used by the Cube.
 *
 * @author Marcel Verpaalen - Initial Contribution
 */
@NonNullByDefault
public class FCommand extends CubeCommand {

    private String ntpServer1 = "";
    private String ntpServer2 = "";

    /**
     * Queries the Cube for the NTP info
     */
    public FCommand() {
    }

    /**
     * Updates the Cube the NTP info
     */
    public FCommand(@Nullable String ntpServer1, @Nullable String ntpServer2) {
        this.ntpServer1 = ntpServer1 != null ? ntpServer1 : "";
        this.ntpServer2 = ntpServer2 != null ? ntpServer2 : "";
    }

    @Override
    public String getCommandString() {
        final String servers;
        if (ntpServer1.length() > 0 && ntpServer2.length() > 0) {
            servers = ntpServer1 + "," + ntpServer2;
        } else {
            servers = ntpServer1 + ntpServer2;
        }
        return "f:" + servers + '\r' + '\n';
    }

    @Override
    public String getReturnStrings() {
        return "F:";
    }
}
