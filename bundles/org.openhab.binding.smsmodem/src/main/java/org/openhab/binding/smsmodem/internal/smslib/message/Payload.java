/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

package org.openhab.binding.smsmodem.internal.smslib.message;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * Extracted from SMSLib
 *
 * @author Gwendal ROULLEAU - Initial contribution, extracted from SMSLib
 */
@NonNullByDefault
public class Payload {
    public enum Type {
        Text,
        Binary
    }

    private @Nullable String textData;

    private byte @Nullable [] binaryData;

    private Type type;

    public Payload(String data) {
        this.type = Type.Text;
        this.textData = data;
    }

    public Payload(byte[] data) {
        this.type = Type.Binary;
        this.binaryData = data.clone();
    }

    public Payload(Payload p) {
        this.type = p.getType();
        this.textData = (this.type == Type.Text ? p.getText() : "");
        byte[] bytes = p.getBytes();
        this.binaryData = (this.type == Type.Binary && bytes != null ? bytes.clone() : null);
    }

    public Type getType() {
        return this.type;
    }

    public @Nullable String getText() {
        return (this.type == Type.Text ? this.textData : null);
    }

    public byte @Nullable [] getBytes() {
        return (this.type == Type.Binary ? this.binaryData : null);
    }

    public boolean isMultipart() {
        return false;
    }
}
