package org.openhab.binding.omnilink.handler;

import java.util.Optional;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.MessageTypes.statuses.Status;

/**
 *
 * @author Craig Hamilton
 *
 */
public abstract class AbstractOmnilinkHandler<T extends Status> extends BaseThingHandler {

    private static Logger logger = LoggerFactory.getLogger(AbstractOmnilinkHandler.class);

    public AbstractOmnilinkHandler(Thing thing) {
        super(thing);
    }

    private volatile Optional<T> status = Optional.empty();

    public OmnilinkBridgeHandler getOmnilinkBridgeHander() {
        return (OmnilinkBridgeHandler) getBridge().getHandler();
    }

    @Override
    public void initialize() {
        Optional<T> status = retrieveStatus();
        handleStatus(status.orElse(null)); // handle status will process null.
        super.initialize();
    }

    /**
     * Attempt to retrieve an updated status for this handler type.
     *
     * @return Optional with updated status if possible, empty optional otherwise.
     */
    protected abstract Optional<T> retrieveStatus();

    /**
     * Update channels associated with handler
     *
     * @param t Status object to update channels with
     */
    protected abstract void updateChannels(T t);

    /**
     * Process a status update for this handler. This will dispatch updateChannels where appropriate.
     *
     * @param t Status to process.
     */
    public void handleStatus(T t) {
        if (t != null) {
            this.status = Optional.of(t);
            updateChannels(t);
        } else {
            logger.warn("Received null status update");
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("channel linked: {} for zone {}", channelUID, getThingNumber());
        if (status.isPresent()) {
            updateChannels(status.get());
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            initialize();
        }
    }

    /**
     * Returns the bridge of the thing.
     *
     * @return returns the bridge of the thing or null if the thing has no
     *         bridge
     */
    @Override
    protected Bridge getBridge() {
        ThingUID bridgeUID = thing.getBridgeUID();
        synchronized (this) {
            if (bridgeUID != null && thingRegistry != null) {
                return (Bridge) thingRegistry.get(bridgeUID);
            } else {
                return null;
            }
        }
    }

    /**
     * Gets the configured number for a thing.
     *
     * @return Configured number for a thing.
     */
    protected int getThingNumber() {
        return ((Number) getThing().getConfiguration().get(OmnilinkBindingConstants.THING_PROPERTIES_NUMBER))
                .intValue();
    }
}
