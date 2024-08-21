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
package org.openhab.binding.somfymylink.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SomfyMyLinkException} is for throwing errors from the mylink binding
 *
 * @author Chris Johnson - Initial contribution
 */
@NonNullByDefault
public class SomfyMyLinkException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SomfyMyLinkException() {
        super();
    }

    public SomfyMyLinkException(String message) {
        super(message);
    }

    public SomfyMyLinkException(String message, Throwable cause) {
        super(message, cause);
    }

    public SomfyMyLinkException(Throwable cause) {
        super(cause);
    }
}
