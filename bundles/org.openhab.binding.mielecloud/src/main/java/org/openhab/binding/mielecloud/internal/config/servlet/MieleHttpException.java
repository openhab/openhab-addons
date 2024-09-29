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
package org.openhab.binding.mielecloud.internal.config.servlet;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception wrapping a HTTP error code for further processing.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class MieleHttpException extends Exception {
    private static final long serialVersionUID = 1825214275413952809L;

    private final int httpErrorCode;

    public MieleHttpException(int httpErrorCode) {
        this.httpErrorCode = httpErrorCode;
    }

    public int getHttpErrorCode() {
        return httpErrorCode;
    }
}
