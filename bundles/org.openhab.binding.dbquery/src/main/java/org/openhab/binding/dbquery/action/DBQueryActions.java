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
package org.openhab.binding.dbquery.action;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dbquery.internal.DatabaseBridgeHandler;
import org.openhab.binding.dbquery.internal.QueryHandler;
import org.openhab.binding.dbquery.internal.domain.ExecuteNonConfiguredQuery;
import org.openhab.binding.dbquery.internal.domain.QueryResult;
import org.openhab.binding.dbquery.internal.domain.ResultRow;
import org.openhab.binding.dbquery.internal.error.UnnexpectedCondition;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joan Pujol - Initial contribution
 */
@ThingActionsScope(name = "dbquery")
@NonNullByDefault
public class DBQueryActions implements IDBQueryActions, ThingActions {
    private final Logger logger = LoggerFactory.getLogger(DBQueryActions.class);

    private @Nullable QueryHandler queryHandler;
    private @Nullable DatabaseBridgeHandler databaseBridgeHandler;

    @Override
    @RuleAction(label = "Execute query", description = "Execute query synchronously (use with care)")
    public ActionQueryResult executeQuery(String query, Map<String, @Nullable Object> parameters,
            int timeoutInSeconds) {
        logger.debug("executeQuery from action {} params={}", query, parameters);
        var currentDatabaseBridgeHandler = databaseBridgeHandler;
        if (currentDatabaseBridgeHandler != null) {
            QueryResult queryResult = new ExecuteNonConfiguredQuery(currentDatabaseBridgeHandler.getDatabase())
                    .executeSynchronously(query, parameters, Duration.ofSeconds(timeoutInSeconds));
            logger.debug("executeQuery from action result {}", queryResult);
            return queryResult2ActionQueryResult(queryResult);
        } else {
            logger.warn("Execute queried ignored as databaseBridgeHandler is null");
            return new ActionQueryResult(false, null);
        }
    }

    private ActionQueryResult queryResult2ActionQueryResult(QueryResult queryResult) {
        return new ActionQueryResult(queryResult.isCorrect(),
                queryResult.getData().stream().map(DBQueryActions::resultRow2Map).collect(Collectors.toList()));
    }

    private static Map<String, @Nullable Object> resultRow2Map(ResultRow resultRow) {
        Map<String, @Nullable Object> map = new HashMap<>();
        for (String column : resultRow.getColumnNames()) {
            map.put(column, resultRow.getValue(column));
        }
        return map;
    }

    @Override
    @RuleAction(label = "Set query parameters", description = "Set query parameters for a query")
    public void setQueryParameters(@ActionInput(name = "parameters") Map<String, @Nullable Object> parameters) {
        logger.debug("setQueryParameters {}", parameters);
        var queryHandler = getThingHandler();
        if (queryHandler instanceof QueryHandler) {
            ((QueryHandler) queryHandler).setParameters(parameters);
        } else {
            logger.warn("setQueryParameters called on wrong Thing, it must be a Query Thing");
        }
    }

    @Override
    @RuleAction(label = "Get last query result", description = "Get last result from a query")
    public ActionQueryResult getLastQueryResult() {
        var currentQueryHandler = queryHandler;
        if (currentQueryHandler != null) {
            return queryResult2ActionQueryResult(queryHandler.getLastQueryResult());
        } else {
            logger.warn("getLastQueryResult ignored as queryHandler is null");
            return new ActionQueryResult(false, null);
        }
    }

    @Override
    public void setThingHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof QueryHandler) {
            this.queryHandler = ((QueryHandler) thingHandler);
        } else if (thingHandler instanceof DatabaseBridgeHandler) {
            this.databaseBridgeHandler = ((DatabaseBridgeHandler) thingHandler);
        } else {
            throw new UnnexpectedCondition("Not expected thing handler " + thingHandler);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return queryHandler != null ? queryHandler : databaseBridgeHandler;
    }
}
