package org.openhab.binding.dbquery.internal.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dbquery.internal.config.QueryConfiguration;

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
