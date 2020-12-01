/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class represents a target and is used to specify a target of some operation
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class Target {
    /** Well known targets */
    public static final String OUTPUTTERMINAL = "outputTerminal";

    /** The target */
    private final String target;

    /** Constructs the target using a default target */
    public Target() {
        this.target = "";
    }

    /**
     * Constructs the target using a specified target
     * 
     * @param target a non-null, possibly empty (for default) target
     */
    public Target(final String target) {
        this.target = target;
    }

    /**
     * Gets the target
     * 
     * @return the target
     */
    public String getTarget() {
        return target;
    }
}
