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

/**
 * A function of a space: a named set of group addresses, for instance one light.
 *
 * @param id the function id
 * @param name the name
 * @param type the function type as written ({@code FT-n}); the text for it is in {@code knx_master.xml}
 * @param groupAddresses the group addresses with their roles, in file order
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxFunction(String id, String name, String type, List<KnxFunctionGroupAddress> groupAddresses) {

    public KnxFunction {
        groupAddresses = List.copyOf(groupAddresses);
    }
}
