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
package org.openhab.binding.argoclima.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The class {@code ArgoApiProtocolViolationException} is thrown for if any API protocol violation occurs. These errors
 * are rare and not propagated to the end-user directly (as Thing status), so not localized
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class ArgoApiProtocolViolationException extends Exception {

    private static final long serialVersionUID = -3438043281963104252L;

    public ArgoApiProtocolViolationException(String message) {
        super(message);
    }

    public ArgoApiProtocolViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
