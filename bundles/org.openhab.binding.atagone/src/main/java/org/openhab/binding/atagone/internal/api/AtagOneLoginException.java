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
package org.openhab.binding.atagone.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown when the portal rejects the credentials. Retrying would only lock the account.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
public class AtagOneLoginException extends AtagOneCommunicationException {

    private static final long serialVersionUID = 1L;

    public AtagOneLoginException(String message) {
        super(message);
    }
}
