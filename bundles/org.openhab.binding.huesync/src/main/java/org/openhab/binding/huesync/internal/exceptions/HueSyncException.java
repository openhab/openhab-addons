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
package org.openhab.binding.huesync.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.huesync.internal.i18n.HueSyncLocalizer;

/**
 * Base class for all HueSyncExceptions
 * 
 * @author Patrik Gfeller - Initial Contribution
 */
@NonNullByDefault
public abstract class HueSyncException extends Exception {
    private static final long serialVersionUID = 0L;

    public HueSyncException(String message) {
        super(message.startsWith("@text") ? HueSyncLocalizer.getResourceString(message) : message);
    }
}
