/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.persistence.influxdb.internal;

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.persistence.PersistenceItemInfo;

/**
 * Java bean used to return information about stored items
 *
 * @author Joan Pujol Espinar - Initial contribution
 */
@NonNullByDefault
public class InfluxDBPersistentItemInfo implements PersistenceItemInfo {
    private final String name;
    private final Integer count;

    public InfluxDBPersistentItemInfo(String name, Integer count) {
        this.name = name;
        this.count = count;
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
        return null;
    }

    @Override
    @Nullable
    public Date getLatest() {
        return null;
    }
}
