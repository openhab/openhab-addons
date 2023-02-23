/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.ecowatt.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.CommunicationException;

/**
 * An exception used when the API limit is reached
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class EcowattApiLimitException extends CommunicationException {
    private static final long serialVersionUID = 1L;
    private int retryAfter;

    public EcowattApiLimitException(int retryAfter, String message, @Nullable Object @Nullable... msgParams) {
        super(message, msgParams);
        this.retryAfter = retryAfter;
    }

    public int getRetryAfter() {
        return retryAfter;
    }
}
