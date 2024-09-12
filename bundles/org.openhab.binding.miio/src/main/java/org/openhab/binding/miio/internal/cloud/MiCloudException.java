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
package org.openhab.binding.miio.internal.cloud;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Will be thrown for cloud errors
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiCloudException extends Exception {
    /**
     * required variable to avoid IncorrectMultilineIndexException warning
     */
    private static final long serialVersionUID = -1280858607995252321L;

    public MiCloudException() {
        super();
    }

    public MiCloudException(@Nullable String message) {
        super(message);
    }

    public MiCloudException(@Nullable String message, @Nullable Exception e) {
        super(message, e);
    }
}
