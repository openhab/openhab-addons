package org.openhab.binding.dbquery.internal.dbimpl;

import java.util.regex.Pattern;

import org.openhab.binding.dbquery.internal.domain.QueryParameters;

public class StringSubstitutionParamsParser {
    private final Pattern paramPattern = Pattern.compile("\\$\\{([\\w_]*?)}");
    private final String query;

    public StringSubstitutionParamsParser(String query) {
        this.query = query;
    }

    public String getQueryWithParametersReplaced(QueryParameters queryParameters) {
        var matcher = paramPattern.matcher(query);
        int idx = 0;
        StringBuilder substitutedQuery = new StringBuilder();
        while (matcher.find()) {
            String nonParametersPart = query.substring(idx, matcher.start());
            String parameterName = matcher.group(1);
            substitutedQuery.append(nonParametersPart);
            substitutedQuery.append(parameterValue(parameterName, queryParameters));
            idx = matcher.end();
        }
        if (idx < query.length())
            substitutedQuery.append(query.substring(idx));

        return substitutedQuery.toString();
    }

    private String parameterValue(String parameterName, QueryParameters queryParameters) {
        var parameter = queryParameters.getParameter(parameterName);
        if (parameter != null)
            return parameter.toString();
        else
            return "";
    }
}
