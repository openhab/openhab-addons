/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.dbquery.internal.dbimpl.jdbc;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dbquery.internal.config.QueryConfiguration;
import org.openhab.binding.dbquery.internal.domain.Query;
import org.openhab.binding.dbquery.internal.domain.QueryFactory;
import org.openhab.binding.dbquery.internal.domain.QueryParameters;

/**
 * Influx2 implementation of {@link QueryFactory}
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class JdbcQueryFactory implements QueryFactory {

    @Override
    public Query createQuery(String query, @Nullable QueryConfiguration queryConfiguration) {
        return new JdbcQuery(query, Map.of());
    }

    @Override
    public Query createQuery(String query, QueryParameters parameters,
            @Nullable QueryConfiguration queryConfiguration) {
        return new JdbcQuery(query, parameters.getAll());
    }

    static class JdbcQuery implements Query {
        private final String query;
        private final Map<String, @Nullable Object> params;

        public JdbcQuery(String query, Map<String, @Nullable Object> params) {
            this.query = query;
            this.params = params;
        }

        public String getQuery() {
            return query;
        }

        public Map<String, @Nullable Object> getParams() {
            return params;
        }
    }
}
