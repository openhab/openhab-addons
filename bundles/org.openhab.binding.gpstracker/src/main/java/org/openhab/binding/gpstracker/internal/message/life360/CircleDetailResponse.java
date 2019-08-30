/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.gpstracker.internal.message.life360;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * The {@link CircleDetailResponse} is a Life360 message POJO
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class CircleDetailResponse {
    @SerializedName("members")
    private List<MembersItem> members;

    @SerializedName("name")
    private String name;

    @SerializedName("id")
    private String id;

    @SerializedName("type")
    private String type;

    public void setMembers(List<MembersItem> members) {
        this.members = members;
    }

    public List<MembersItem> getMembers() {
        return members;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return
                "CircleDetailResponse{" +
                        ",members = '" + members + '\'' +
                        ",name = '" + name + '\'' +
                        ",id = '" + id + '\'' +
                        ",type = '" + type + '\'' +
                        "}";
    }
}
