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
package org.openhab.binding.atv.internal.client.core;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Implemented by protocol relay implementations to advertise which {@link Capability capabilities} they actually
 * support (i.e. which interface methods they override with a real implementation rather than inheriting the
 * not-supported default).
 *
 * <p>
 * Test suites verify the declaration is honest via reflection; runtime code uses it to compute feature
 * availability.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface CapabilitySource {

    /**
     * Returns the set of capabilities this implementation actually supports.
     *
     * @return supported capabilities (never {@code null})
     */
    Set<Capability> capabilities();
}
