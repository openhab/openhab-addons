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
package org.openhab.binding.dali.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dali.internal.handler.DaliException;

/**
 * The {@link DaliAddress} represents an address on the DALI bus.
 *
 * @author Robert Schmid - Initial contribution
 */
@NonNullByDefault
public abstract class DaliAddress {
    private DaliAddress() {
    }

    protected abstract <T extends DaliFrame> T addToFrame(T frame) throws DaliException;

    public static DaliAddress createShortAddress(int address) throws DaliException {
        if (address < 0 || address > 63) {
            throw new DaliException("address must be in the range 0..63");
        }
        return new DaliAddress() {
            @Override
            protected <T extends DaliFrame> T addToFrame(T frame) throws DaliException {
                if (frame.length() == 16) {
                    frame.data &= ~(1 << 15); // unset bit 15
                    frame.data |= ((address & 0b11111) << 9);
                } else if (frame.length() == 24) {
                    frame.data &= ~(1 << 23); // unset bit 23
                    frame.data |= ((address & 0b11111) << 17);
                } else {
                    throw new DaliException("Unsupported frame size");
                }
                return frame;
            }
        };
    }

    public static DaliAddress createBroadcastAddress() {
        return new DaliAddress() {
            @Override
            protected <T extends DaliFrame> T addToFrame(T frame) throws DaliException {
                if (frame.length() == 16) {
                    frame.data |= 0x7f << 9;
                } else if (frame.length() == 24) {
                    frame.data |= 0x7f << 17;
                } else {
                    throw new DaliException("Unsupported frame size");
                }
                return frame;
            }
        };
    }

    public static DaliAddress createBroadcastUnaddressedAddress() {
        return new DaliAddress() {
            @Override
            protected <T extends DaliFrame> T addToFrame(T frame) throws DaliException {
                if (frame.length() == 16) {
                    frame.data |= 0x7e << 9;
                } else if (frame.length() == 24) {
                    frame.data |= 0x7e << 17;
                } else {
                    throw new DaliException("Unsupported frame size");
                }
                return frame;
            }
        };
    }

    public static DaliAddress createGroupAddress(int address) throws DaliException {
        if (address < 0 || address > 31) {
            throw new DaliException("address must be in the range 0..31");
        }
        return new DaliAddress() {
            @Override
            protected <T extends DaliFrame> T addToFrame(T frame) throws DaliException {
                if (frame.length() == 16) {
                    if (address > 15) {
                        throw new DaliException("Groups 16..31 are not supported in 16-bit forward frames");
                    }
                    frame.data |= ((0x4 << 3) & (address & 0b111)) << 9;
                } else if (frame.length() == 24) {
                    frame.data |= ((0x2 << 4) & (address & 0b1111)) << 17;
                } else {
                    throw new DaliException("Unsupported frame size");
                }
                return frame;
            }
        };
    }
}
