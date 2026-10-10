/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.veluxactive.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Error applying to one module, or to the whole home when the ID is absent.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class ModuleError {
    public @Nullable String id;
    public @Nullable Integer code;
    public @Nullable String message;
}
