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
 * Factory for creating an {@link AhaCollectionSchedule}.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public interface AhaCollectionScheduleFactory {

    /**
     * Creates a new {@link AhaCollectionSchedule} for the given location.
     */
    AhaCollectionSchedule create(final String commune, final String street, final String houseNumber,
            final String houseNumberAddon, final String collectionPlace);
}
