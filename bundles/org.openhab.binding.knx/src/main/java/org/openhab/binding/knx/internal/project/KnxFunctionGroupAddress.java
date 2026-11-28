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
 * A group address used by a function ({@code GroupAddressRef}).
 *
 * @param id the id of the reference itself
 * @param name the name of the reference
 * @param groupAddressId the group address, in the short form {@code GA-n}; {@link KnxProject#groupAddress(String)}
 *            resolves it
 * @param role the role as written, a CamelCase word or a GUID. Its meaning is not known (format note §5.4), so
 *            it is kept as text
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxFunctionGroupAddress(String id, String name, String groupAddressId, @Nullable String role) {
}
