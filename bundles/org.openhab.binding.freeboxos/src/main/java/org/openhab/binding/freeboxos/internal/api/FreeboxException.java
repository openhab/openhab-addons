/**
<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main
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
package org.openhab.binding.freeboxos.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.Response.ErrorCode;
<<<<<<< Upstream, based on origin/main

/**
 * Exception for errors when using the Freebox API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FreeboxException extends Exception {
    private static final long serialVersionUID = 9197365222439228186L;
    private ErrorCode errorCode = ErrorCode.NONE;

    public FreeboxException(String format, Object... args) {
        super(String.format(format, args));
    }

    public FreeboxException(Exception cause, String format, Object... args) {
        super(String.format(format, args), cause);
    }

    public FreeboxException(ErrorCode errorCode, String message) {
        this(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
=======
 * Copyright (c) 2010-2022 Contributors to the openHAB project
=======
 * Copyright (c) 2010-2023 Contributors to the openHAB project
>>>>>>> 006a813 Saving work before instroduction of ArrayListDeserializer
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
package org.openhab.binding.freeboxos.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.ErrorCode;
=======
>>>>>>> e4ef5cc Switching to Java 17 records

/**
 * Exception for errors when using the Freebox API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FreeboxException extends Exception {
    private static final long serialVersionUID = 9197365222439228186L;
    private ErrorCode errorCode = ErrorCode.NONE;

    public FreeboxException(String format, Object... args) {
        super(String.format(format, args));
    }

    public FreeboxException(Exception cause, String format, Object... args) {
        super(String.format(format, args), cause);
    }

    public FreeboxException(ErrorCode errorCode, String message) {
        this(message);
        this.errorCode = errorCode;
    }

<<<<<<< Upstream, based on origin/main
    public FreeboxException(ErrorCode errorCode) {
        this(errorCode, errorCode.toString(), null);
    }

    public @Nullable ErrorCode getErrorCode() {
>>>>>>> 46dadb1 SAT warnings handling
=======
    public ErrorCode getErrorCode() {
>>>>>>> 006a813 Saving work before instroduction of ArrayListDeserializer
        return errorCode;
    }
}
