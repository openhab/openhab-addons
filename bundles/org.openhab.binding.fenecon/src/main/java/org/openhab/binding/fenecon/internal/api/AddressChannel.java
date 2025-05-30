/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.fenecon.internal.api;

import java.util.Comparator;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AddressChannel} is a container class to identify a channel of a {@link Address}.
 *
 * @author Philipp Schneider - Initial contribution
 */
@NonNullByDefault
public record AddressChannel(String channel) implements Comparable<AddressChannel> {
    @Override
    public int compareTo(AddressChannel that) {
        return Objects.compare(this, that,
                Comparator.comparing(AddressChannel::channel).thenComparing(AddressChannel::channel));
    }
}
