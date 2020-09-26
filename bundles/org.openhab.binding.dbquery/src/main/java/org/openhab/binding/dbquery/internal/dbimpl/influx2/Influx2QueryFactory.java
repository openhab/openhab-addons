package org.openhab.binding.dbquery.internal.dbimpl.influx2;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dbquery.internal.config.QueryConfiguration;
import org.openhab.binding.dbquery.internal.dbimpl.StringSubstitutionParamsParser;
import org.openhab.binding.dbquery.internal.domain.Query;
import org.openhab.binding.dbquery.internal.domain.QueryFactory;
import org.openhab.binding.dbquery.internal.domain.QueryParameters;

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
