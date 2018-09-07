/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.internal.protocol.v1;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Effect} is a POJO for a response from the Hyperion server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class V1Response {

    @SerializedName("command")
    private String command;

    @SerializedName("success")
    private boolean success;

    @SerializedName("tan")
    private int tan;

    @SerializedName("info")
    private V1Info info;

    public String getCommand() {
        return command;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getTan() {
        return tan;
    }

    public V1Info getInfo() {
        return info;
    }

}
