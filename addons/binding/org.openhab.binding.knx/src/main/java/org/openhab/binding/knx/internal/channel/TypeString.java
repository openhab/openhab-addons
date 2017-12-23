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

import tuwien.auto.calimero.dptxlator.DPTXlatorString;
import tuwien.auto.calimero.exception.KNXFormatException;

@NonNullByDefault
class TypeString extends KNXChannelType {

    TypeString() {
        super(CHANNEL_STRING);
    }

    @Override
    protected Set<String> getAllGAKeys() {
        return Collections.singleton(GROUPADDRESS);
    }

    @Override
    public @Nullable CommandSpec getCommandSpec(Configuration configuration, Command command)
            throws KNXFormatException {
        return getDefaultCommandSpec(configuration, GROUPADDRESS, command);
    }

    @Override
    protected String getDefaultDPT(String gaConfigKey) {
        return DPTXlatorString.DPT_STRING_8859_1.getID();
    }

}
