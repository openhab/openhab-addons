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
 * Provider interface for range information.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public interface RangeProvider {
    /**
     * Get the minimum value.
     *
     * @return the minimum value, or {@code null} if not set
     */
    @Nullable
    Number min();

    /**
     * Get the maximum value.
     *
     * @return the maximum value, or {@code null} if not set
     */
    @Nullable
    Number max();

    /**
     * Get the step size.
     *
     * @return the step size, or {@code null} if not set
     */
    @Nullable
    Number stepSize();
}
