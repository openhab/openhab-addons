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
package org.openhab.binding.tailwind.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumerates all supported Tailwind garage controller models.
 *
 * @author Dave J Schoepel - Initial contribution
 */
@NonNullByDefault
public enum TailwindModel {

    /**
     * Please also remember to add supported models to the README.md
     * iC3 Pro
     * Format is model name, max number of doors for that specific model
     */

    IC3("iQ3", 3);

    private final String id;
    private final int maxDoors;

    /**
     * Constructor for the Tailwind model class used to identify models during discovery and
     * create the Tailwind Controller thing.
     *
     * @param id - Model number
     * @param maxDoors - maximum number of doors that can be configured for the model
     */
    private TailwindModel(String id, int maxDoors) {
        this.id = id;
        this.maxDoors = maxDoors;
    }

    /**
     * Method to get the model id from the TailwindModel enum
     *
     * @return id - Tailwind model
     */
    public String getId() {
        return id;
    }

    /**
     * Method to get the maximum doors allowed for the controller from the TailwindModel enum
     *
     * @return maxDoors - maximum number of doors that can be configured for the model
     */
    public int getMaxDoors() {
        return maxDoors;
    }
}
