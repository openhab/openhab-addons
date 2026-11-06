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
package org.openhab.binding.atv.internal.client.core;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Method used by a protocol to take over selected capability interfaces.
 *
 * <p>
 * The relay supplies an implementation already bound to the calling protocol; the protocol invokes it with
 * the interface classes it wants to control exclusively (e.g. {@code Audio.class}, {@code RemoteControl.class})
 * and runs the returned {@link Runnable} to release the takeover again.
 *
 * @author Dan Cunningham - Initial contribution
 */
@FunctionalInterface
@NonNullByDefault
public interface TakeoverMethod {

    /** Takeover method that does nothing and releases nothing. */
    TakeoverMethod NO_OP = interfaces -> () -> {
    };

    /**
     * Takes over the given capability interfaces for the bound protocol.
     *
     * @param interfaces interface classes to take over
     * @return callback releasing the takeover
     */
    Runnable takeover(Class<?>... interfaces);
}
