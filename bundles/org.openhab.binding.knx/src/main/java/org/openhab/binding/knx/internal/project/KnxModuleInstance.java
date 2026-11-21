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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A module instance of a device with a modular application program ({@code ModuleInstance}).
 *
 * @param id the instance id, {@code MD-n_M-n_MI-n}; a submodule instance carries the whole path
 * @param refId the module in the application program, {@code MD-n_M-n}
 * @param repeatIndex the {@code RepeatIndex} as written, space separated {@code <a>x<b>} pairs; what the pairs
 *            mean is not known (format note §8.3), so it is not interpreted
 * @param arguments the arguments in file order
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxModuleInstance(String id, String refId, @Nullable String repeatIndex,
        List<KnxModuleArgument> arguments) {

    public KnxModuleInstance {
        arguments = List.copyOf(arguments);
    }
}
