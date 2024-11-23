/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc1;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NhcLocation1} class represents the location Niko Home Control communication object. It contains all fields
 * representing a Niko Home Control location.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public final class NhcLocation1 {

    private final String name;

    public NhcLocation1(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
