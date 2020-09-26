package org.openhab.binding.dbquery.internal;

import java.util.Collection;
import java.util.List;

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

public abstract class DatabaseBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(DatabaseBridgeHandler.class);
    private Database database = Database.EMPTY;

    public DatabaseBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void initialize() {
        logger.trace("initialize bridge {}", getThing().getUID());
        initConfig();

        database = createDatabase();

        var completable = database.connect();
        updateStatus(ThingStatus.UNKNOWN);
        completable.thenAccept(result -> {
            if (result) {
                logger.trace("Succesfully connected to database {}", getThing().getUID());
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.trace("Connect to database {} failed", getThing().getUID());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        });
    }

    protected abstract void initConfig();

    @Override
    public void dispose() {
        var completable = database.disconnect();
        updateStatus(ThingStatus.UNKNOWN);
        completable.thenAccept(result -> {
            if (result) {
                logger.trace("Succesfully disconnected to database {}", getBridge().getUID());
                updateStatus(ThingStatus.OFFLINE);
            } else {
                logger.trace("Disconnect to database {} failed", getBridge().getUID());
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR);
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
