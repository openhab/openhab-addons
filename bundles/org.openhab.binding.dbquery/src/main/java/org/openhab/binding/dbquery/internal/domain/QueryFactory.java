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
package org.openhab.binding.dbquery.internal.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dbquery.internal.config.QueryConfiguration;

/**
 * Abstracts operations needed to create a query from its thing configuration
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public interface QueryFactory {
    Query createQuery(String query, @Nullable QueryConfiguration queryConfiguration);

    Query createQuery(String query, QueryParameters parameters, @Nullable QueryConfiguration queryConfiguration);

    QueryFactory EMPTY = new QueryFactory() {
        @Override
        public Query createQuery(String query, @Nullable QueryConfiguration queryConfiguration) {
            return Query.EMPTY;
        }

        @Override
        public Query createQuery(String query, QueryParameters parameters,
                @Nullable QueryConfiguration queryConfiguration) {
            return Query.EMPTY;
        }
    };
}
