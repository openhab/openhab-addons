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
package org.openhab.binding.elroconnects.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ElroConnectsAccountException} class is the exception for all cloud account errors.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
class ElroConnectsAccountException extends Exception {

    private static final long serialVersionUID = -1038059604759958044L;

    public ElroConnectsAccountException(String message) {
        super(message);
    }

    public ElroConnectsAccountException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
