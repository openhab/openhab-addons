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
package org.openhab.binding.homekit.internal.session;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for receiving event messages from a {@link HttpPayloadParser}
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public interface HttpReaderListener {

    /**
     * Invoked when the parser has produced a complete HTTP payload
     *
     * @param httpPayload the parsed HTTP payload
     */
    void onHttpPayload(HttpPayloadParser.HttpPayload httpPayload);

    /**
     * Invoked when the reader encounters an error
     *
     * @param error the error that occurred
     */
    void onHttpReaderError(Throwable error);

    /**
     * Invoked when the parser is closed
     * 
     * @param remainingData any remaining data that was not processed
     */
    void onHttpReaderClose(byte[] remainingData);
}
