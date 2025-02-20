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
package org.openhab.persistence.influxdb.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.persistence.FilterCriteria;

/**
 * Creates InfluxDB query sentence given an OpenHab persistence {@link FilterCriteria}
 *
 * @author Joan Pujol Espinar - Initial contribution
 */
@NonNullByDefault
public interface FilterCriteriaQueryCreator {
    /**
     * Create query from {@link FilterCriteria}
     *
     * @param criteria Criteria to create query from
     * @param retentionPolicy Name of the retentionPolicy/bucket to use in query
     * @param alias
     * @return Created query as a String
     */
    String createQuery(FilterCriteria criteria, String retentionPolicy, @Nullable String alias);

    default String getOperationSymbol(FilterCriteria.Operator operator, InfluxDBVersion version) {
        return switch (operator) {
            case EQ -> "=";
            case LT -> "<";
            case LTE -> "<=";
            case GT -> ">";
            case GTE -> ">=";
            case NEQ -> version == InfluxDBVersion.V1 ? "<>" : "!=";
        };
    }
}
