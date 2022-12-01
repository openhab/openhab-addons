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

import org.eclipse.jdt.annotation.NonNullByDefault;
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
     * @return Created query as a String
     */
    String createQuery(FilterCriteria criteria, String retentionPolicy);

    default String getOperationSymbol(FilterCriteria.Operator operator, InfluxDBVersion version) {
        switch (operator) {
            case EQ:
                return "=";
            case LT:
                return "<";
            case LTE:
                return "<=";
            case GT:
                return ">";
            case GTE:
                return ">=";
            case NEQ:
                return version == InfluxDBVersion.V1 ? "<>" : "!=";
            default:
                throw new UnnexpectedConditionException("Not expected operator " + operator);
        }
    }
}
