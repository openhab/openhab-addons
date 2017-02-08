/*
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lightify.internal.link;

import java.util.Arrays;

/**
 * This class represents a Lightify light bulb or stripe.
 *
 * @author Christoph Engelbert (@noctarius2k) - Initial contribution
 */
public class LightifyLight extends LightifyLuminary {

    private final byte[] address;

    LightifyLight(LightifyLink lightifyLink, String name, byte[] address) {
        super(lightifyLink, name);
        this.address = address;
    }

    @Override
    public byte[] address() {
        return address;
    }

    @Override
    public String toString() {
        return "LightifyLight{" + "address=" + Arrays.toString(address) + ", super=" + super.toString() + '}';
    }

    @Override
    byte typeFlag() {
        return 0x00;
    }
}
