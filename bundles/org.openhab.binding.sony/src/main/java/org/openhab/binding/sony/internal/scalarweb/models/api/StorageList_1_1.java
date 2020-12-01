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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The storage list information class used for deserialization only.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class StorageList_1_1 {

    /** The storage list items */
    private @Nullable List<@Nullable StorageListItem_1_1> items;

    /**
     * Constructor used for deserialization only
     */
    public StorageList_1_1() {
    }

    /**
     * Gets the list of storage items
     * 
     * @return the list of storage items
     */
    public @Nullable List<@Nullable StorageListItem_1_1> getItems() {
        return items;
    }
}
