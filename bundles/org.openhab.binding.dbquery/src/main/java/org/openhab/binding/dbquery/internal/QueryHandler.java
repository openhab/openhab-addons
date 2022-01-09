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
package org.openhab.binding.dbquery.internal;

import static org.openhab.binding.dbquery.internal.DBQueryBindingConstants.CHANNEL_EXECUTE;
import static org.openhab.binding.dbquery.internal.DBQueryBindingConstants.TRIGGER_CHANNEL_CALCULATE_PARAMETERS;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dbquery.action.DBQueryActions;
import org.openhab.binding.dbquery.internal.config.QueryConfiguration;
import org.openhab.binding.dbquery.internal.domain.DBQueryJSONEncoder;
import org.openhab.binding.dbquery.internal.domain.Database;
import org.openhab.binding.dbquery.internal.domain.QueryParameters;
import org.openhab.binding.dbquery.internal.domain.QueryResult;
import org.openhab.binding.dbquery.internal.domain.QueryResultExtractor;
import org.openhab.binding.dbquery.internal.domain.ResultValue;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages query thing, handling it's commands and updating it's channels
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class QueryHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(QueryHandler.class);
    // Relax nullable rules as config can be only null when not initialized
    private @NonNullByDefault({}) QueryConfiguration config;
    private @NonNullByDefault({}) QueryResultExtractor queryResultExtractor;

    private @Nullable ScheduledFuture<?> scheduledQueryExecutionInterval;
    private @Nullable QueryResultChannelUpdater queryResultChannelUpdater;
    private Database database = Database.EMPTY;
    private final DBQueryJSONEncoder jsonEncoder = new DBQueryJSONEncoder();

    private @Nullable QueryExecution currentQueryExecution;
    private QueryResult lastQueryResult = QueryResult.NO_RESULT;

    public QueryHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(QueryConfiguration.class);
        queryResultExtractor = new QueryResultExtractor(config);

        initQueryResultChannelUpdater();
        updateStateWithParentBridgeStatus();
    }

    private void initQueryResultChannelUpdater() {
        ChannelStateUpdater channelStateUpdater = (channel, state) -> updateState(channel.getUID(), state);
        queryResultChannelUpdater = new QueryResultChannelUpdater(channelStateUpdater, this::getResultChannels2Update);
    }

    private void scheduleQueryExecutionIntervalIfNeeded() {
        int interval = config.getInterval();
        if (interval != QueryConfiguration.NO_INTERVAL && scheduledQueryExecutionInterval == null) {
            logger.trace("Scheduling query execution every {} seconds for {}", interval, getQueryIdentifier());
            scheduledQueryExecutionInterval = scheduler.scheduleWithFixedDelay(this::executeQuery, 0, interval,
                    TimeUnit.SECONDS);
        }
    }

    private ThingUID getQueryIdentifier() {
        return getThing().getUID();
    }

    private void cancelQueryExecutionIntervalIfNeeded() {
        ScheduledFuture<?> currentFuture = scheduledQueryExecutionInterval;
        if (currentFuture != null) {
            currentFuture.cancel(true);
            scheduledQueryExecutionInterval = null;
        }
    }

    @Override
    public void dispose() {
        cancelQueryExecutionIntervalIfNeeded();
        cancelCurrentQueryExecution();
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand for channel {} with command {}", channelUID, command);

        if (command instanceof RefreshType) {
            if (CHANNEL_EXECUTE.equals(channelUID.getId())) {
                executeQuery();
            }
        } else {
            logger.warn("Query Thing can only handle RefreshType commands as the thing is read-only");
        }
    }

    private synchronized void executeQuery() {
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            QueryExecution queryExecution = currentQueryExecution;
            if (queryExecution != null) {
                logger.debug("Previous query execution for {} discarded as a new one is requested",
                        getQueryIdentifier());
                cancelCurrentQueryExecution();
            }

            queryExecution = new QueryExecution(database, config, queryResultReceived);
            this.currentQueryExecution = queryExecution;

            if (config.isHasParameters()) {
                logger.trace("{} triggered to set parameters for {}", TRIGGER_CHANNEL_CALCULATE_PARAMETERS,
                        queryExecution);
                updateParametersChannel(QueryParameters.EMPTY);
                triggerChannel(TRIGGER_CHANNEL_CALCULATE_PARAMETERS);
            } else {
                queryExecution.execute();
            }
        } else {
            logger.debug("Execute query ignored because thing status is {}", getThing().getStatus());
        }
    }

    private synchronized void cancelCurrentQueryExecution() {
        QueryExecution current = currentQueryExecution;
        if (current != null) {
            current.cancel();
            currentQueryExecution = null;
        }
    }

    private void updateStateWithQueryResult(QueryResult queryResult) {
        var currentQueryResultChannelUpdater = queryResultChannelUpdater;
        var localCurrentQueryExecution = this.currentQueryExecution;
        lastQueryResult = queryResult;
        if (currentQueryResultChannelUpdater != null && localCurrentQueryExecution != null) {
            ResultValue resultValue = queryResultExtractor.extractResult(queryResult);
            updateCorrectChannel(resultValue.isCorrect());
            updateParametersChannel(localCurrentQueryExecution.getQueryParameters());
            if (resultValue.isCorrect()) {
                currentQueryResultChannelUpdater.updateChannelResults(resultValue.getResult());
            } else {
                currentQueryResultChannelUpdater.clearChannelResults();
            }
        } else {
            logger.warn(
                    "QueryResult discarded as queryResultChannelUpdater nor currentQueryExecution are not expected to be null");
        }
    }

    private void updateCorrectChannel(boolean correct) {
        updateState(DBQueryBindingConstants.CHANNEL_CORRECT, OnOffType.from(correct));
    }

    private void updateParametersChannel(QueryParameters queryParameters) {
        updateState(DBQueryBindingConstants.CHANNEL_PARAMETERS, new StringType(jsonEncoder.encode(queryParameters)));
    }

    private void updateStateWithParentBridgeStatus() {
        final @Nullable Bridge bridge = getBridge();
        DatabaseBridgeHandler databaseBridgeHandler;

        if (bridge != null) {
            @Nullable
            BridgeHandler bridgeHandler = bridge.getHandler();
            if (bridgeHandler instanceof DatabaseBridgeHandler) {
                databaseBridgeHandler = (DatabaseBridgeHandler) bridgeHandler;
                database = databaseBridgeHandler.getDatabase();
                if (bridge.getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
        if (status == ThingStatus.ONLINE) {
            scheduleQueryExecutionIntervalIfNeeded();
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        cancelCurrentQueryExecution();
        updateStateWithParentBridgeStatus();
    }

    public void setParameters(Map<String, @Nullable Object> parameters) {
        final @Nullable QueryExecution queryExecution = currentQueryExecution;
        if (queryExecution != null) {
            QueryParameters queryParameters = new QueryParameters(parameters);
            queryExecution.setQueryParameters(queryParameters);
            queryExecution.execute();
        } else {
            logger.trace("setParameters ignored as there is any executing query for {}", getQueryIdentifier());
        }
    }

    private final QueryExecution.QueryResultListener queryResultReceived = (QueryResult queryResult) -> {
        synchronized (QueryHandler.this) {
            logger.trace("queryResultReceived for {} : {}", getQueryIdentifier(), queryResult);
            updateStateWithQueryResult(queryResult);

            currentQueryExecution = null;
        }
    };

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(DBQueryActions.class);
    }

    public QueryResult getLastQueryResult() {
        return lastQueryResult;
    }

    private List<Channel> getResultChannels2Update() {
        return getThing().getChannels().stream().filter(channel -> isLinked(channel.getUID()))
                .filter(this::isResultChannel).collect(Collectors.toList());
    }

    private boolean isResultChannel(Channel channel) {
        @Nullable
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        return channelTypeUID != null && channelTypeUID.getId().startsWith("result");
    }
}
