/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.awtrixng.internal;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * A {@link PayloadChannel} links a channel to one field of a payload DTO (the settings of the clock, or an app): it
 * reads the channel state from the payload, and puts a command into it.
 *
 * @param <T> the payload type
 * @author Keith T. Garner - Initial contribution
 */
@NonNullByDefault
public class PayloadChannel<T> {

    private final Function<T, @Nullable State> reader;
    private final BiPredicate<T, Command> writer;

    /**
     * @param reader returns the state for the payload, or null if the field is not set
     * @param writer puts the command into the payload and returns true, or returns false if the command does not fit
     *            the channel
     */
    public PayloadChannel(Function<T, @Nullable State> reader, BiPredicate<T, Command> writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public @Nullable State toState(T payload) {
        return reader.apply(payload);
    }

    /**
     * Puts the command into the payload.
     *
     * @return false, and leaves the payload as it was, if the command does not fit the channel
     */
    public boolean apply(T payload, Command command) {
        return writer.test(payload, command);
    }

    /**
     * @return a new payload that only holds the command, or null if the command does not fit the channel
     */
    public @Nullable T toPayload(Supplier<T> factory, Command command) {
        T payload = factory.get();
        return writer.test(payload, command) ? payload : null;
    }
}
