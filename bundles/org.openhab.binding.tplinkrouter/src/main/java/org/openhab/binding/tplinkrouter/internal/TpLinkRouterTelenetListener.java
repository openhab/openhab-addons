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
package org.openhab.binding.tplinkrouter.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TpLinkRouterTelenetListener} defines listener for telnet events.
 *
 * @author Olivier Marceau - Initial contribution
 */
@NonNullByDefault
public interface TpLinkRouterTelenetListener {

    /**
     * The telnet client has received a line.
     *
     * @param line the received line
     */
    void receivedLine(String line);

    /**
     * The telnet client encountered an IO error.
     */
    void onReaderThreadStopped();

    /**
     * The telnet client has been interrupted.
     */
    void onReaderThreadInterrupted();

    /**
     * The telnet client has successfully connected to the receiver.
     */
    void onReaderThreadStarted();

    /**
     * The telnet socket is unavailable.
     */
    void onCommunicationUnavailable();
}
