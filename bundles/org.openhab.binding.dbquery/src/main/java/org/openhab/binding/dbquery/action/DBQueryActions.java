package org.openhab.binding.dbquery.action;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dbquery.internal.QueryHandler;
import org.openhab.binding.dbquery.internal.error.UnnexpectedCondition;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ThingActionsScope(name = "dbquery")
public class DBQueryActions implements IDBQueryActions, ThingActions {
    private final Logger logger = LoggerFactory.getLogger(DBQueryActions.class);

    private @Nullable QueryHandler queryHandler;
    private @Nullable BridgeHandler bridgeHandler;

    @Override
    public void activate() {
        logger.info("activate actions");
    }

    @Override
    public void deactivate() {
        logger.info("deactivate actions");
    }

    @Override
    public String executeQuery(String query, Map<String, Object> parameters) {
        logger.info("executeQuery {} params={}", query, parameters);
        return "value";
    }

    @Override
    public QueryResult executeQueryNonScalar(String query, Map<String, Object> parameters) {
        logger.info("executeQueryNonScalar {} params={}", query, parameters);
        return new QueryResult(true);
    }

    @Override
    @RuleAction(label = "Set query parameters", description = "Set query parameters for a query")
    public void setQueryParameters(@ActionInput(name = "parameters") Map<String, Object> parameters) {
        logger.info("setQueryParameters {}", parameters);
        var queryHandler = getThingHandler();
        if (queryHandler instanceof QueryHandler) {
            ((QueryHandler) queryHandler).setParameters(parameters);
        } else {
            logger.warn("setQueryParameters called on wrong Thing, it must be a Query Thing");
        }
    }

    @Override
    public void setThingHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof QueryHandler)
            this.queryHandler = ((QueryHandler) thingHandler);
        else if (thingHandler instanceof BridgeHandler)
            this.bridgeHandler = ((BridgeHandler) bridgeHandler);
        else
            throw new UnnexpectedCondition("Not expected thing handler " + thingHandler);
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return queryHandler != null ? queryHandler : bridgeHandler;
    }
}
