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
package org.openhab.binding.meater.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link MeaterException} is used when there is exception communicating with MEATER REST API.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class MeaterException extends Exception {

    private static final long serialVersionUID = 2543564118231301158L;

    public MeaterException(Exception source) {
        super(source);
    }

    public MeaterException(String message) {
        super(message);
    }

    public MeaterException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public @Nullable String getMessage() {
        Throwable throwable = getCause();
        if (throwable != null) {
            String localMessage = throwable.getMessage();
            if (localMessage != null) {
                return localMessage;
            }
        }
        return super.getMessage();
    }
}
