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
package org.openhab.binding.httphook.internal;

import java.io.Serializable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Martin Grześlowski - Binding authentication configuration
 */
@NonNullByDefault
public record HttpHookBindingConfig(boolean enforceAuthentication, @Nullable String defaultBasic,
        @Nullable String defaultBearer) implements Serializable {
    public static final HttpHookBindingConfig DEFAULT = new HttpHookBindingConfig(true, null, null);

    @Override
    public String toString() {
        // Configuration is logged. Keep credentials and any future secret fields out of this representation.
        return "HttpHookBindingConfig[enforceAuthentication=" + enforceAuthentication + "]";
    }
}
