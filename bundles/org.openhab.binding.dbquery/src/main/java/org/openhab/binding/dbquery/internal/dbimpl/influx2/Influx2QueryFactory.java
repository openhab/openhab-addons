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
package org.openhab.binding.dbquery.internal.dbimpl.influx2;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dbquery.internal.config.QueryConfiguration;
import org.openhab.binding.dbquery.internal.dbimpl.StringSubstitutionParamsParser;
import org.openhab.binding.dbquery.internal.domain.Query;
import org.openhab.binding.dbquery.internal.domain.QueryFactory;
import org.openhab.binding.dbquery.internal.domain.QueryParameters;

/**
 * Influx2 implementation of {@link QueryFactory}
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class Influx2QueryFactory implements QueryFactory {

    @Override
    public Query createQuery(String query, @Nullable QueryConfiguration queryConfiguration) {
        return new Influx2Query(query);
    }

    @Override
    public Query createQuery(String query, QueryParameters parameters,
            @Nullable QueryConfiguration queryConfiguration) {
        return new Influx2Query(substituteParameters(query, parameters));
    }

    private String substituteParameters(String query, QueryParameters parameters) {
        return new StringSubstitutionParamsParser(query).getQueryWithParametersReplaced(parameters);
    }

    static class Influx2Query implements Query {
        private final String query;

        public Influx2Query(String query) {
            this.query = query;
        }

        String getQuery() {
            return query;
        }

        @Override
        public String toString() {
            return query;
        }
    }
}
