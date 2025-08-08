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
package org.openhab.binding.lgthinq.lgservices.errors;

import java.io.Serial;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.model.ResultCodes;

/**
 * The LGThinqAccessException exception class that occurs when the LG API deny access to some service.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinqAccessException extends LGThinqApiException {
    @Serial
    private static final long serialVersionUID = 1L;

    public LGThinqAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public LGThinqAccessException(String message, Throwable cause, ResultCodes reasonCode) {
        super(message, cause, reasonCode);
    }

    public LGThinqAccessException(String message) {
        super(message);
    }

    public LGThinqAccessException(String message, ResultCodes resultCode) {
        super(message, resultCode);
    }
}
