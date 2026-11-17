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
package org.openhab.binding.knx.internal.project;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The flag overrides ETS stores on an object instance, as written in the file (normally {@code Enabled} or
 * {@code Disabled}). {@code null} means the instance does not override the flag; the value then comes from the
 * object reference or the object of the application program.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxObjectFlags(@Nullable String read, @Nullable String write, @Nullable String communication,
        @Nullable String transmit, @Nullable String update, @Nullable String readOnInit) {

    static final KnxObjectFlags NONE = new KnxObjectFlags(null, null, null, null, null, null);
}
