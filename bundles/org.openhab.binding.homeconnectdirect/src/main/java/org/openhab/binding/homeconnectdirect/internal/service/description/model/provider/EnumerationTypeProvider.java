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
package org.openhab.binding.homeconnectdirect.internal.service.description.model.provider;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Provider interface for enumeration type information.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public interface EnumerationTypeProvider {
    /**
     * Get the enumeration type.
     *
     * @return the enumeration type, or {@code null} if not set
     */
    @Nullable
    Integer enumerationType();

    /**
     * Get the enumeration type key.
     *
     * @return the enumeration type key, or {@code null} if not set
     */
    @Nullable
    String enumerationTypeKey();
}
