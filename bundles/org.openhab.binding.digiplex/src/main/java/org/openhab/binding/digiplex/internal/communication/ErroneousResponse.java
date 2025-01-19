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
package org.openhab.binding.digiplex.internal.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Erroneous message from PRT3.
 *
 * Message that is invalid, which happens sometimes due to communication errors.
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
public class ErroneousResponse implements DigiplexResponse {

    public final String message;

    public ErroneousResponse(String message) {
        this.message = message;
    }

    @Override
    public void accept(DigiplexMessageHandler visitor) {
        visitor.handleErroneousResponse(this);
    }
}
