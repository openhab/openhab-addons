/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.channel;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.types.Type;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXFormatException;

/**
 * Command meta-data
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public class CommandSpec extends AbstractSpec {

    private final Type command;
    private final @Nullable GroupAddress groupAddress;

    public CommandSpec(@Nullable ChannelConfiguration channelConfiguration, String defaultDPT, Type command)
            throws KNXFormatException {
        super(channelConfiguration, defaultDPT);
        if (channelConfiguration != null) {
            this.groupAddress = new GroupAddress(channelConfiguration.getMainGA().getGA());

        } else {
            this.groupAddress = null;
        }
        this.command = command;
    }

    public Type getCommand() {
        return command;
    }

    public @Nullable GroupAddress getGroupAddress() {
        return groupAddress;
    }

}
