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
package org.openhab.binding.meater.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link MeaterAuthenticationException} is used when there is an authentication exception with MEATER REST API.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class MeaterAuthenticationException extends MeaterException {

    private static final long serialVersionUID = 2543564118231301158L;

    public MeaterAuthenticationException(Exception source) {
        super(source);
    }

    public MeaterAuthenticationException(String message) {
        super(message);
    }

    public MeaterAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
