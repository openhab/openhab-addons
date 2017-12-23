/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.channel;

import static org.openhab.binding.knx.KNXBindingConstants.*;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.types.Command;

import tuwien.auto.calimero.exception.KNXFormatException;

@NonNullByDefault
class TypeNumber extends KNXChannelType {

    TypeNumber() {
        super(CHANNEL_NUMBER);
    }

    @Override
    protected String getDefaultDPT(String gaConfigKey) {
        return "9.001";
    }

    @Override
    protected Set<String> getAllGAKeys() {
        return Collections.singleton(GROUPADDRESS);
    }

    @Override
    public @Nullable CommandSpec getCommandSpec(Configuration configuration, Command command)
            throws KNXFormatException {
        return new CommandSpec(parse((String) configuration.get(GROUPADDRESS)), getDefaultDPT(GROUPADDRESS), command);
    }

}
