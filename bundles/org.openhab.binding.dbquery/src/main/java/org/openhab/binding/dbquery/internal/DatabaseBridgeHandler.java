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
package org.openhab.binding.dbquery.internal;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dbquery.action.DBQueryActions;
import org.openhab.binding.dbquery.internal.domain.Database;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation common to all implementation of database bridge
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public abstract class DatabaseBridgeHandler extends BaseBridgeHandler {
    private static final long RETRY_CONNECTION_ATTEMPT_TIME_SECONDS = 60;
    private final Logger logger = LoggerFactory.getLogger(DatabaseBridgeHandler.class);
    private Database database = Database.EMPTY;
    private @Nullable ScheduledFuture<?> retryConnectionAttemptFuture;

    public DatabaseBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        initConfig();

        database = createDatabase();

        connectDatabase();
    }

    private void connectDatabase() {
        logger.debug("connectDatabase {}", database);
        var completable = database.connect();
        updateStatus(ThingStatus.UNKNOWN);
        completable.thenAccept(result -> {
            if (result) {
                logger.trace("Succesfully connected to database {}", getThing().getUID());
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connect to database failed");
                if (retryConnectionAttemptFuture == null) {
                    scheduleRetryConnectionAttempt();
                }
            }
        });
    }

    protected void scheduleRetryConnectionAttempt() {
        logger.trace("Scheduled retry connection attempt every {}", RETRY_CONNECTION_ATTEMPT_TIME_SECONDS);
        retryConnectionAttemptFuture = scheduler.scheduleWithFixedDelay(this::connectDatabase,
                RETRY_CONNECTION_ATTEMPT_TIME_SECONDS, RETRY_CONNECTION_ATTEMPT_TIME_SECONDS, TimeUnit.SECONDS);
    }

    protected abstract void initConfig();

    @Override
    public void dispose() {
        cancelRetryConnectionAttemptIfPresent();
        disconnectDatabase();
    }

    protected void cancelRetryConnectionAttemptIfPresent() {
        ScheduledFuture<?> currentFuture = retryConnectionAttemptFuture;
        if (currentFuture != null) {
            currentFuture.cancel(true);
        }
    }

    private void disconnectDatabase() {
        var completable = database.disconnect();
        updateStatus(ThingStatus.UNKNOWN);
        completable.thenAccept(result -> {
            if (result) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "Successfully disconnected to database");
            } else {
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Disconnect to database failed");
            }
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No commands supported
    }

    abstract Database createDatabase();

    public Database getDatabase() {
        return database;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(DBQueryActions.class);
    }
}
