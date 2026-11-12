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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Response holding the name of the mower used in the name command.
 * 
 * @author Marco Meyer - Initial contribution
 */
@NonNullByDefault
public class Name extends RobonectAnswer {

    private @Nullable String name;

    /**
     * Returns the mower name.
     *
     * @return - The mower name.
     */
    public @Nullable String getName() {
        return name;
    }
}
