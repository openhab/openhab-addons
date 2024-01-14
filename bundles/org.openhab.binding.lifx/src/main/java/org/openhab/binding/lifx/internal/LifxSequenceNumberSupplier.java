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
package org.openhab.binding.lifx.internal;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Supplies sequence numbers for packets in the range [0, 255].
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class LifxSequenceNumberSupplier implements Supplier<Integer> {

    private static final int SEQUENCE_NUMBER_DIVISOR = 256;
    private final AtomicInteger sequenceNumber = new AtomicInteger(1);

    @Override
    public Integer get() {
        return sequenceNumber.getAndUpdate((value) -> (value + 1) % SEQUENCE_NUMBER_DIVISOR);
    }
}
