package org.openhab.binding.connectedcar.internal.handler;

import java.time.ZoneId;
import java.util.Map;

import org.openhab.binding.connectedcar.internal.TextResources;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeServiceStatus;
import org.openhab.binding.connectedcar.internal.provider.CarChannelTypeProvider;
import org.openhab.binding.connectedcar.internal.provider.ChannelDefinitions;
import org.openhab.binding.connectedcar.internal.provider.ChannelDefinitions.ChannelIdMapEntry;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeChargeThingHandler extends ThingBaseHandler {
    private final Logger logger = LoggerFactory.getLogger(FordVehicleHandler.class);

    public WeChargeThingHandler(Thing thing, TextResources resources, ZoneId zoneId, ChannelDefinitions idMapper,
            CarChannelTypeProvider channelTypeProvider) throws ApiException {
        super(thing, resources, zoneId, idMapper, channelTypeProvider);
    }

    @Override
    public boolean createBrandChannels(Map<String, ChannelIdMapEntry> channels) {
        return false;
    }

    /**
     * Register all available services
     */
    @Override
    public void registerServices() {
        services.clear();
        addService(new WeChargeServiceStatus(this, api));
    }

    @Override
    public boolean handleBrandCommand(ChannelUID channelUID, Command command) throws ApiException {
        String channelId = channelUID.getIdWithoutGroup();
        boolean processed = true;
        String action = "";
        String actionStatus = "";
        boolean switchOn = (command instanceof OnOffType) && (OnOffType) command == OnOffType.ON;
        logger.debug("{}: Channel {} received command {}", thingId, channelId, command);
        try {
            switch (channelId) {
                default:
                    processed = false;
            }
        } catch (RuntimeException /* ApiException */ e) {
            if (command instanceof OnOffType) {
                updateState(channelUID.getId(), OnOffType.OFF);
            }
            throw e;
        }

        if (processed && !action.isEmpty()) {
            logger.debug("{}: Action {} submitted, initial status={}", thingId, action, actionStatus);
        }
        return processed;
    }
}
