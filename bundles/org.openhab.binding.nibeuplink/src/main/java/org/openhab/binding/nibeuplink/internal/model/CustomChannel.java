/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.nibeuplink.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * the channel class
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class CustomChannel extends Channel {

    CustomChannel(String id, String name) {
        super(id, name, ChannelGroup.CUSTOM);
        this.channelCode = "0";
    }

    /**
     * used to set the channelcode obtained from configuration file
     *
     * @param channelCode the channelCode to be set
     */
    public final void setCode(Integer channelCode) {
        if (getChannelGroup().equals(ChannelGroup.CUSTOM)) {
            this.channelCode = channelCode.toString();
        }
    }
}
