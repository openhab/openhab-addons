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
 * The {@link ContentItemNotPresetableException} class is an exception
 *
 * @author Thomas Traunbauer - Initial contribution
 */
@NonNullByDefault
public class ContentItemNotPresetableException extends NoPresetFoundException {
    private static final long serialVersionUID = 1L;

    public ContentItemNotPresetableException() {
        super();
    }

    public ContentItemNotPresetableException(String message) {
        super(message);
    }

    public ContentItemNotPresetableException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContentItemNotPresetableException(Throwable cause) {
        super(cause);
    }
}
