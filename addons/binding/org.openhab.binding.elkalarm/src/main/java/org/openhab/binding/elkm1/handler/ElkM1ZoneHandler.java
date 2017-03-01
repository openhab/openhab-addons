package org.openhab.binding.elkm1.handler;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.elkm1.ElkAlarmBindingConstants;
import org.openhab.binding.elkm1.internal.elk.ElkZoneConfig;
import org.openhab.binding.elkm1.internal.elk.ElkZoneStatus;

/**
 * Handler for the elk m1 zones. A zone is a device or thingy in each of the locations
 * that is used for the alarm.
 *
 * @author David Bennett - Initial Checkin
 *
 */
public class ElkM1ZoneHandler extends BaseThingHandler {
    public ElkM1ZoneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Updates with the zone information.
     *
     * @param config The config of the zone.
     * @param status The status of the zone.
     */
    public void updateZone(ElkZoneConfig config, ElkZoneStatus status) {
        ChannelUID channel = new ChannelUID(getThing().getUID(), ElkAlarmBindingConstants.CHANNEL_ZONE_STATUS);
        updateState(channel, new StringType(status.toString()));

        ChannelUID channelConfig = new ChannelUID(getThing().getUID(), ElkAlarmBindingConstants.CHANNEL_ZONE_CONFIG);
        updateState(channelConfig, new StringType(config.toString()));
    }
}
