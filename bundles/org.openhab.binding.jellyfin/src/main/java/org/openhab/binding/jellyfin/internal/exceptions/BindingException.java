/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.i18n.ResourceHelper;

/**
 * Base class for all JellyfinExceptions.
 * 
 * @author Patrik Gfeller - Initial Contribution
 */
@NonNullByDefault
public abstract class BindingException extends Exception {
    private static final long serialVersionUID = 0L;

    public BindingException(String message) {
        super(message.startsWith("@text") ? ResourceHelper.getResourceString(message) : message);
    }
}
