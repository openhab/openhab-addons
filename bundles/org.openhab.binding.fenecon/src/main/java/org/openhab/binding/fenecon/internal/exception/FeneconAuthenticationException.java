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
 * The class {@link FeneconAuthenticationException} is thrown if a authentication on the FENECON system is not possible.
 *
 * @author Philipp Schneider - Initial contribution
 */
@NonNullByDefault
public class FeneconAuthenticationException extends FeneconException {

    private static final long serialVersionUID = -9206453599559316730L;

    public FeneconAuthenticationException(String message) {
        super(message);
    }
}
