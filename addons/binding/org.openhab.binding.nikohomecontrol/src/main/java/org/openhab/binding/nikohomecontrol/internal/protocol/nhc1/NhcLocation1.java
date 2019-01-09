/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
        return this.name;
    }
}
