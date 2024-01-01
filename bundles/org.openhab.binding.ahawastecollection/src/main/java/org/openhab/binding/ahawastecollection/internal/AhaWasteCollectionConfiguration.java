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
package org.openhab.binding.ahawastecollection.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AhaWasteCollectionConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public class AhaWasteCollectionConfiguration {

    /**
     * Commune.
     */
    public String commune = "";

    /**
     * Street.
     */
    public String street = "";

    /**
     * House number.
     */
    public String houseNumber = "";

    /**
     * House number addon.
     */
    public String houseNumberAddon = "";

    /**
     * Collection place.
     */
    public String collectionPlace = "";
}
