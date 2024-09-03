/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mideaac.internal.security;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Decryption8370Result} Protocol. V3 Only
 *
 * @author Jacek Dobrowolski - Initial Contribution
 */
@NonNullByDefault
public class Decryption8370Result {
    public ArrayList<byte[]> getResponses() {
        return responses;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    ArrayList<byte[]> responses;
    byte[] buffer;

    public Decryption8370Result(ArrayList<byte[]> responses, byte[] buffer) {
        super();
        this.responses = responses;
        this.buffer = buffer;
    }
}
