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
package org.openhab.binding.paradoxalarm.internal.model;

/**
 * The {@link EntityType} Enum of paradox entity types - zones, partitions, maybe more in the future...
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public enum EntityType {
    ZONE,
    PARTITION;

    @Override
    public String toString() {
        String upperCase = super.toString();
        String lowerCase = upperCase.toLowerCase();
        return lowerCase.replace(lowerCase.charAt(0), upperCase.charAt(0));
    }
}
