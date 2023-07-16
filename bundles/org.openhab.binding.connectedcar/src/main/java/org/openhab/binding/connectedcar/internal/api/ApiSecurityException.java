/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.connectedcar.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link ApiSecurityException} indicates security exceptions like login failures
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class ApiSecurityException extends ApiException {
    private static final long serialVersionUID = 3774082851695011862L;

    public ApiSecurityException(String message) {
        super(message);
    }

    public ApiSecurityException(String message, Throwable e) {
        super(message, e);
    }

    public ApiSecurityException(String message, ApiResult apiResult) {
        super(message, apiResult);
    }
}
