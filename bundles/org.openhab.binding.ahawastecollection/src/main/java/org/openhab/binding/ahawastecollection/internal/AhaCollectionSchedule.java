/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ahawastecollection.internal.CollectionDate.WasteType;

/**
 * Schedule that returns the next collection dates from the aha website.
 * 
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public interface AhaCollectionSchedule {

    /**
     * Returns the next collection dates per {@link WasteType}.
     */
    Map<WasteType, CollectionDate> getCollectionDates() throws IOException;
}
