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
package org.openhab.binding.digiplex.internal.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Unknown message from PRT3
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
public class UnknownResponse implements DigiplexResponse {

    public final String message;

    public UnknownResponse(String message) {
        this.message = message;
    }

    @Override
    public void accept(DigiplexMessageHandler visitor) {
        visitor.handleUnknownResponse(this);
    }
}
