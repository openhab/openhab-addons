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
 * An argument of a module instance. The argument's name, and the meaning of an allocator id, are in the
 * application program.
 *
 * @param refId the argument reference as written ({@code MD-n_A-n}, or the short form of a submodule argument)
 * @param value the value as written: a number, a text, or an allocator id ({@code L-n}); {@code null} when
 *            there is none
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxModuleArgument(String refId, @Nullable String value) {
}
