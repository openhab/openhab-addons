/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.pushbullet.internal.model;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents errors in the response fetched from the API.
 *
 * @author Hakan Tandogan - Initial contribution
 * @author Hakan Tandogan - Migrated from openHAB 1 action with the same name
 */
public class PushError {

    @SerializedName("type")
    private String type;

    @SerializedName("message")
    private String message;

    @SerializedName("param")
    private String param;

    @SerializedName("cat")
    private String cat;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    @Override
    public String toString() {
        return "PushError {" + "type='" + type + '\'' + ", message='" + message + '\'' + ", param='" + param + '\''
                + ", cat='" + cat + '\'' + '}';
    }
}
