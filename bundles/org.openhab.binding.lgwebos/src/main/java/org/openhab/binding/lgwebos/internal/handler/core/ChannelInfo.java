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
/*
 * This file is based on:
 *
 * ChannelInfo
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

/**
 * {@link ChannelInfo} is a value object to describe a channel on WebOSTV.
 * The id value is mandatory when starting a channel. The channelName is a human readable friendly name, which is not
 * further interpreted by the TV.
 *
 * @author Hyun Kook Khang - Connect SDK initial contribution
 * @author Sebastian Prehn - Adoption for openHAB, removed minor major number, made immutable
 */
public class ChannelInfo {

    private String channelName;
    private String channelId;
    private String channelNumber;
    private String channelType;

    public ChannelInfo() {
        // no-argument constructor for gson
    }

    public ChannelInfo(String channelName, String channelId, String channelNumber, String channelType) {
        this.channelId = channelId;
        this.channelNumber = channelNumber;
        this.channelName = channelName;
        this.channelType = channelType;
    }

    public String getName() {
        return channelName;
    }

    public String getId() {
        return channelId;
    }

    public String getChannelNumber() {
        return channelNumber;
    }

    @Override
    public String toString() {
        return "ChannelInfo [channelId=" + channelId + ", channelNumber=" + channelNumber + ", channelName="
                + channelName + ", channelType=" + channelType + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((channelId == null) ? 0 : channelId.hashCode());
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
        ChannelInfo other = (ChannelInfo) obj;
        if (channelId == null) {
            if (other.channelId != null) {
                return false;
            }
        } else if (!channelId.equals(other.channelId)) {
            return false;
        }
        return true;
    }
}
