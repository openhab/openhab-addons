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
 * The {@link LGThinqApiException}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinqApiException extends LGThinqException {
    @Serial
    private static final long serialVersionUID = 202409261451L;
    protected ResultCodes apiReasonCode = ResultCodes.UNKNOWN;

    public LGThinqApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public LGThinqApiException(String message, Throwable cause, ResultCodes reasonCode) {
        super(message, cause);
        this.apiReasonCode = reasonCode;
    }

    public LGThinqApiException(String message) {
        super(message);
    }

    public LGThinqApiException(String message, ResultCodes resultCode) {
        super(message);
        this.apiReasonCode = resultCode;
    }

    public ResultCodes getApiReasonCode() {
        return apiReasonCode;
    }
}
