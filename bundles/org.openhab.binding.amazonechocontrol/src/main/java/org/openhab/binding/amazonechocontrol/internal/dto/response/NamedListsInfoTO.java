/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.dto.response;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link NamedListsInfoTO} encapsulates the response of /api/namedLists/listId
 *
 * @author Jan N. Klug - Initial contribution
 */
public class NamedListsInfoTO {
    public List<String> listIds;
    public long updatedDate;
    public String type;
    public int version;
    public boolean defaultList;
    public boolean archived;
    public String itemId;
    public long createdDate;
    public Object listReorderVersion;
    public Object originalAudioId;
    public String customerId;
    public String name;
    public Object nbestItems;

    @Override
    public @NonNull String toString() {
        return "NamedListsInfoTO{listIds=" + listIds + ", updatedDate=" + updatedDate + ", type='" + type
                + "', version=" + version + ", defaultList=" + defaultList + ", archived=" + archived + ", itemId='"
                + itemId + "', createdDate=" + createdDate + ", listReorderVersion=" + listReorderVersion
                + ", originalAudioId=" + originalAudioId + ", customerId='" + customerId + "', name=" + name
                + ", nbestItems=" + nbestItems + "}";
    }
}
