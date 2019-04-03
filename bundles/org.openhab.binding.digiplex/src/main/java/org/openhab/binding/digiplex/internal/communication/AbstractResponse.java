/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
 * Base class for all responses retrieved from PRT3 module.
 *
 * Handles success/failure.
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
public abstract class AbstractResponse implements DigiplexResponse {

    public final boolean success;

    public AbstractResponse() {
        this.success = true;
    }

    public AbstractResponse(boolean success) {
        this.success = success;
    }
}
