/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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

/**
 * number channel type description
 * 
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
class TypeNumber extends KNXChannelType {

    TypeNumber() {
        super(CHANNEL_NUMBER, CHANNEL_NUMBER_CONTROL);
    }

    @Override
    protected String getDefaultDPT(String gaConfigKey) {
        return "9.001";
    }

    @Override
    protected Set<String> getAllGAKeys() {
        return Collections.singleton(GA);
    }

}
