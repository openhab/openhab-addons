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
package org.openhab.binding.knx.internal.rest;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * One reader of the live telegram stream, as {@link TelegramStream} sees it.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
interface StreamClient {

    /**
     * Hands an event to the reader without waiting for it to be written.
     *
     * @param data an answer object, written as JSON
     * @return false if the reader is finished; it is then dropped
     */
    boolean send(String event, Object data);

    /** {@return the number of events handed over that are not written yet} */
    int pending();

    /** {@return whether the reader is closed or a send to it has failed; asking sends nothing} */
    boolean finished();

    void close();
}
