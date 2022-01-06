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
package org.openhab.binding.pushbullet.internal.model;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the push request sent to the API.
 *
 * @author Hakan Tandogan - Initial contribution
 * @author Hakan Tandogan - Migrated from openHAB 1 action with the same name
 */
public class Push {

    @SerializedName("title")
    private String title;

    @SerializedName("body")
    private String body;

    @SerializedName("type")
    private String type;

    @SerializedName("email")
    private String email;

    @SerializedName("channel_tag")
    private String channelTag;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getChannel() {
        return channelTag;
    }

    public void setChannel(String channelTag) {
        this.channelTag = channelTag;
    }

    @Override
    public String toString() {
        return "Push {" + "title='" + title + '\'' + ", body='" + body + '\'' + ", type='" + type + '\'' + ", email='"
                + email + '\'' + ", channelTag='" + channelTag + '\'' + '}';
    }
}
