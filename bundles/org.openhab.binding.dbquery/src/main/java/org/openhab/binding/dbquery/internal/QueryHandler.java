/**
 * Copyright (c) 2020-2020 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.dbquery.internal;

import static org.openhab.binding.dbquery.internal.DBQueryBindingConstants.CHANNEL_EXECUTE;
import static org.openhab.binding.dbquery.internal.DBQueryBindingConstants.TRIGGER_CHANNEL_CALCULATE_PARAMETERS;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.openhab.binding.dbquery.action.DBQueryActions;
import org.openhab.binding.dbquery.internal.config.QueryConfiguration;
import org.openhab.binding.dbquery.internal.domain.Database;
import org.openhab.binding.dbquery.internal.domain.QueryParameters;
import org.openhab.binding.dbquery.internal.domain.QueryResult;
import org.openhab.binding.dbquery.internal.domain.ResultValue;
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
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QueryHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class QueryHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(QueryHandler.class);
    // Relax nullable rules as config can be only null when not initialized
    private @NonNullByDefault({}) QueryConfiguration config;
    private @NonNullByDefault({}) QueryResultExtractor queryResultExtractor;

    private @Nullable ScheduledFuture scheduledQueryExecutionInterval;
    private @Nullable QueryResultChannelUpdater queryResultChannelUpdater;
    private Database database = Database.EMPTY;

    private @Nullable QueryExecution currentQueryExecution;

    public QueryHandler(Thing thing) {
        super(thing);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void initialize() {
        logger.trace("initialize query handler for {}", getQueryIdentifier());
        config = getConfigAs(QueryConfiguration.class);
        queryResultExtractor = new QueryResultExtractor(config);

        initQueryResultChannelUpdater();
        scheduleQueryExecutionIntervalIfNeeded();
        updateStateWithParentBridgeStatus();
    }

    private void initQueryResultChannelUpdater() {
        ChannelStateUpdater channelStateUpdater = (channel, state) -> updateState(channel.getUID(), state);
        queryResultChannelUpdater = new QueryResultChannelUpdater(channelStateUpdater, this::getChannels2Update);
    }

    private void scheduleQueryExecutionIntervalIfNeeded() {
        int interval = config.getInterval();
        if (interval != 0) {
            scheduledQueryExecutionInterval = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                logger.trace("Scheduling query execution every {} seconds for {}", interval, getQueryIdentifier());
                executeQuery();
            }, 0, interval, TimeUnit.SECONDS);
        }
    }

    @NotNull
    private ThingUID getQueryIdentifier() {
        return getThing().getUID();
    }

    private void cancelQueryExecutionIntervalIfNeeded() {
        var currentFuture = scheduledQueryExecutionInterval;
        if (currentFuture != null) {
            currentFuture.cancel(true);
        }
    }

    @Override
    public void dispose() {
        logger.trace("dispose query handler for {}", getQueryIdentifier());
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
            // TODO: HAndle s'afageix un nou canal o sobreescriure on ChannelLinked
        } else {
            logger.debug("Query Thing can only handle RefreshType commands as the thing is read-only");
        }
    }

    private synchronized void executeQuery() {
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            @Nullable
            QueryExecution queryExecution = currentQueryExecution;
            if (queryExecution != null) {
                logger.debug("Previous query execution for {} discarded as a new one is requested",
                        getQueryIdentifier());
                cancelCurrentQueryExecution();
            }

            queryExecution = new QueryExecution(database, config, queryResultReceived);
            this.currentQueryExecution = queryExecution;

            if (config.isHasParameters()) {
                triggerChannel(TRIGGER_CHANNEL_CALCULATE_PARAMETERS);
            } else {
                currentQueryExecution.execute();
            }
        } else {
            logger.debug("Execute query ignored because thing status is {}", getThing().getStatus());
        }
    }

    private synchronized void cancelCurrentQueryExecution() {
        @Nullable
        QueryExecution current = currentQueryExecution;
        if (current != null) {
            current.cancel();
            currentQueryExecution = null;
        }
    }

    private void updateStateWithQueryResult(QueryResult queryResult) {
        var currentQueryResultChannelUpdater = queryResultChannelUpdater;
        if (currentQueryResultChannelUpdater != null) {
            ResultValue resultValue = queryResultExtractor.extractResult(queryResult);
            if (resultValue.isCorrect())
                currentQueryResultChannelUpdater.updateChannelResults(resultValue.getResult());
            else
                currentQueryResultChannelUpdater.clearChannelResults();
        } else {
            logger.warn("QueryResult discarded as queryResultChannelUpdater is not expected to be null");
        }
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
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.trace("bridgeStatusChanged to {} for {}", bridgeStatusInfo.getStatus(), getQueryIdentifier());
        cancelCurrentQueryExecution();
        updateStateWithParentBridgeStatus();
    }

    public void setParameters(Map<String, @Nullable Object> parameters) {
        final @Nullable QueryExecution queryExecution = currentQueryExecution;
        if (queryExecution != null) {
            queryExecution.setQueryParameters(new QueryParameters(parameters));
            queryExecution.execute();
        } else {
            logger.trace("setParameters ignored as there is any executing query for {}", getQueryIdentifier());
        }
    }

    private final QueryExecution.QueryResultListener queryResultReceived = new QueryExecution.QueryResultListener() {
        @Override
        public synchronized void queryResultReceived(QueryResult queryResult) {
            logger.trace("queryResultReceived for {} : {}", getQueryIdentifier(), queryResult);
            updateStateWithQueryResult(queryResult);

            currentQueryExecution = null;
        }
    };

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(DBQueryActions.class);
    }

    private List<Channel> getChannels2Update() {
        return getThing().getChannels().stream().filter(channel -> isLinked(channel.getUID()))
                // .filter(channel -> channel.getUID())
                .collect(Collectors.toList());
    }
}
