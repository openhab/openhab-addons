/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
