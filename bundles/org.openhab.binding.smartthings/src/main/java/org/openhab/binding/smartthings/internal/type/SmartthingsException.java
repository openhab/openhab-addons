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
package org.openhab.binding.smartthings.internal.type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.dto.ErrorObject;

/**
 *
 * @author Laurent Arnal - Initial contribution
 *
 *         An exception that occurred while operating the binding
 *
 */
@NonNullByDefault
public class SmartthingsException extends Exception {
    private static final long serialVersionUID = -3398100220952729816L;
    @Nullable
    private ErrorObject err;

    public SmartthingsException(String message, Exception e) {
        super(message, e);
    }

    public SmartthingsException(String message) {
        super(message);
    }

    public SmartthingsException(String message, ErrorObject err) {
        super(message);
        this.err = err;
    }

    @Override
    public @Nullable String getMessage() {
        String msg = super.getMessage();
        ErrorObject errL = err;
        if (errL != null) {
            msg += "\r\n";
            msg += "code      : " + errL.requestId + "\r\n";
            msg += "requestId : " + errL.error.code + "\r\n";
            msg += "message   : " + errL.error.message + "\r\n";

            for (ErrorObject.Error.Detail detail : errL.error.details) {
                msg += "code      : " + detail.code() + "\r\n";
                msg += "target      : " + detail.target() + "\r\n";
                msg += "message      : " + detail.message() + "\r\n";
            }
        }

        return msg;
    }
}
