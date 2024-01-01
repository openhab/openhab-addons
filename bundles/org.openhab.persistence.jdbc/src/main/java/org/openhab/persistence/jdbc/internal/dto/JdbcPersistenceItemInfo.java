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
package org.openhab.persistence.jdbc.internal.dto;

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.persistence.PersistenceItemInfo;

/**
 * Represents the item info for openHAB.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class JdbcPersistenceItemInfo implements PersistenceItemInfo {

    private final String name;
    private final @Nullable Integer count;
    private final @Nullable Date earliest;
    private final @Nullable Date latest;

    public JdbcPersistenceItemInfo(String name) {
        this(name, null, null, null);
    }

    public JdbcPersistenceItemInfo(String name, @Nullable Integer count, @Nullable Date earliest,
            @Nullable Date latest) {
        this.name = name;
        this.count = count;
        this.earliest = earliest;
        this.latest = latest;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public @Nullable Integer getCount() {
        return count;
    }

    @Override
    public @Nullable Date getEarliest() {
        return earliest;
    }

    @Override
    public @Nullable Date getLatest() {
        return latest;
    }
}
