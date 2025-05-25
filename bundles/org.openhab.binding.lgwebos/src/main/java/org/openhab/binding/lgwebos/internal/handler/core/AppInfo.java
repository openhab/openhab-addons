/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
/*
 * This file is based on:
 *
 * AppInfo
 * Connect SDK
 *
 * Copyright (c) 2014 LG Electronics.
 * Created by Hyun Kook Khang on 19 Jan 2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openhab.binding.lgwebos.internal.handler.core;

import com.google.gson.annotations.SerializedName;

/**
 * {@link AppInfo} is a value object to describe an application on WebOSTV.
 * The id value is mandatory when starting an application. The name is a human readable friendly name, which is not
 * further interpreted by the TV.
 *
 * @author Hyun Kook Khang - Connect SDK initial contribution
 * @author Sebastian Prehn - Adoption for openHAB, made immutable
 */
public class AppInfo {

    @SerializedName(value = "id", alternate = "appId")
    private String id;
    @SerializedName(value = "name", alternate = { "appName", "title" })
    private String name;
    @SerializedName(value = "playState")
    private String playState; // see MediaAppInfo for possible values

    public AppInfo() {
        // no-argument constructor for gson
    }

    public AppInfo(String id, String name, String playState) {
        this.id = id;
        this.name = name;
        this.playState = playState;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPlayState() {
        return playState;
    }

    @Override
    public String toString() {
        return "AppInfo [id=" + id + ", name=" + name + ", playState=" + playState + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AppInfo other = (AppInfo) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }
}
