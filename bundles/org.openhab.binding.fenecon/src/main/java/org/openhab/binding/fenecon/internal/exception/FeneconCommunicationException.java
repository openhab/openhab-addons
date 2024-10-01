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
package org.openhab.binding.fenecon.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The class {@link FeneconCommunicationException} is thrown if a communication problem occurs with the FENECON system.
 *
 * @author Philipp Schneider - Initial contribution
 */
@NonNullByDefault
public class FeneconCommunicationException extends FeneconException {

    private static final long serialVersionUID = -4334759327203382902L;

    public FeneconCommunicationException(String message) {
        super(message);
    }

    public FeneconCommunicationException(String message, Exception exception) {
        super(message, exception);
    }
}
