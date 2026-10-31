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
package org.openhab.persistence.victoriametrics.internal;

import java.util.Date;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.persistence.PersistenceItemInfo;

/**
 * Java bean used to return information about stored items
 *
 * @author Joan Pujol Espinar - Initial contribution
 * @author Franz - Initial VictoriaMetrics adaptation
 */
@NonNullByDefault
public class VictoriaMetricsPersistentItemInfo implements PersistenceItemInfo {
    private final String name;
    private final Integer count;

    public VictoriaMetricsPersistentItemInfo(Map.Entry<String, Integer> itemInfo) {
        this.name = itemInfo.getKey();
        this.count = itemInfo.getValue();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    @Nullable
    public Integer getCount() {
        return count;
    }

    @Override
    @Nullable
    public Date getEarliest() {
        return null; // TODO: Implement when available in VictoriaMetrics binding
    }

    @Override
    @Nullable
    public Date getLatest() {
        return null; // TODO: Implement when available in VictoriaMetrics binding
    }
}
