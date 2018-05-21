/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.internal.protocol.ng;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link NgResponse} is a POJO for a response from the Hyperion.ng server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class NgResponse {

    @SerializedName("command")
    private String command;

    @SerializedName("success")
    private boolean success;

    @SerializedName("tan")
    private int tan;

    @SerializedName("info")
    private NgInfo info;

    @SerializedName("error")
    private String error;

    public String getCommand() {
        return command;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getTan() {
        return tan;
    }

    public NgInfo getInfo() {
        return info;
    }

    public String getError() {
        return error;
    }

}
