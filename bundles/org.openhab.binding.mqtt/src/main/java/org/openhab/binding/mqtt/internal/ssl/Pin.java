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
package org.openhab.binding.mqtt.internal.ssl;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A pin is either a public key pin or certificate pin and consists of the binary data
 * and the used hash algorithm.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class Pin {
    protected byte @Nullable [] pinData;
    protected @Nullable PinMessageDigest hashDigest;
    protected boolean learning;
    protected final PinType type;

    /**
     * To simplify the creating of a Pin instance, you can use the factory
     * methods `newCertificatePin` and `newPublicKeyPin` of this class.
     *
     * @param type The pin type
     * @param hashDigest The hash method
     * @param learning If the Pin is in learning mode.
     * @param pinData The pinned data
     */
    Pin(PinType type, @Nullable PinMessageDigest hashDigest, boolean learning, byte @Nullable [] pinData) {
        this.type = type;
        this.hashDigest = hashDigest;
        this.learning = learning;
        this.pinData = pinData;
    }

    public PinType getType() {
        return type;
    }

    public byte @Nullable [] getHash() {
        return pinData;
    }

    public void setLearningMode() {
        this.learning = true;
        this.pinData = null;
    }

    /**
     * This sets the pin instance to checking mode. The given
     * data is expected to be hashed in the Pins hashMethod.
     *
     * @param pinMessageDigest The signature algorithm message digest
     * @param data For instance SHA-256 hash data
     */
    public void setCheckMode(PinMessageDigest pinMessageDigest, byte[] data) {
        this.hashDigest = pinMessageDigest;
        this.learning = false;
        this.pinData = data;
    }

    public static Pin LearningPin(PinType pinType) {
        return new Pin(pinType, null, true, null);
    }

    public static Pin CheckingPin(PinType pinType, PinMessageDigest method, byte[] pinData) {
        return new Pin(pinType, method, false, pinData);
    }

    /**
     * Returns true if this pin is still learning.
     */
    public boolean isLearning() {
        return learning;
    }

    /**
     * This method is used to determine if the given digest is equal to the
     * one of this Pin. If this Pin is still learning, it will always return true.
     *
     * @param digestData SHA256 hash data of a public key or a certificate.
     * @return Returns true if equal
     */
    public boolean isEqual(byte[] digestData) {
        if (learning) {
            return true;
        }
        return Arrays.equals(pinData, digestData);
    }

    @Override
    public String toString() {
        byte[] pinData = this.pinData;
        PinMessageDigest hashDigest = this.hashDigest;
        if (hashDigest != null && pinData != null) {
            return type.name() + ":" + hashDigest.toHexString(pinData);
        } else if (hashDigest != null) {
            return type.name() + ":" + hashDigest.getMethod();
        } else {
            return type.name();
        }
    }
}
