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
package org.openhab.binding.lgthinq.lgservices.model.washer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link WMStop}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class WMStop {
    static final WMStop EMPTY_WM_STOP = new WMStop();
    private String command = "";
    private Data data = Data.EMPTY_DATA;

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Data getData() {
        return data;
    }
}
