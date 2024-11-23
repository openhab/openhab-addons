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
package org.openhab.binding.bosesoundtouch.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link BoseSoundTouchNotFoundException} class is an exception
 *
 * @author Thomas Traunbauer - Initial contribution
 */
@NonNullByDefault
public class BoseSoundTouchNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public BoseSoundTouchNotFoundException() {
        super();
    }

    public BoseSoundTouchNotFoundException(String message) {
        super(message);
    }

    public BoseSoundTouchNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public BoseSoundTouchNotFoundException(Throwable cause) {
        super(cause);
    }
}
