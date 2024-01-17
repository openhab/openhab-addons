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
package org.openhab.binding.wolfsmartset.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Will be thrown for cloud errors
 *
 * @author Bo Biene - Initial contribution
 */
@NonNullByDefault
public class WolfSmartsetCloudException extends Exception {
    /**
     * required variable to avoid IncorrectMultilineIndexException warning
     */
    private static final Long serialVersionUID = -1280858607995252321L;

    public WolfSmartsetCloudException() {
        super();
    }

    public WolfSmartsetCloudException(@Nullable String message) {
        super(message);
    }

    public WolfSmartsetCloudException(@Nullable String message, @Nullable Exception e) {
        super(message, e);
    }
}
