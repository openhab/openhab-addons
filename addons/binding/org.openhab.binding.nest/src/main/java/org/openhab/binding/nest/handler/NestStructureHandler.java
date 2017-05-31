package org.openhab.binding.nest.handler;

import static org.openhab.binding.nest.NestBindingConstants.*;

import java.util.Calendar;
import java.util.TimeZone;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.nest.NestBindingConstants;
import org.openhab.binding.nest.internal.NestUpdateRequest;
import org.openhab.binding.nest.internal.data.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Deals with the structures on the nest api, turning them into a thing in openhab. */
public class NestStructureHandler extends BaseNestHandler {
    private Logger logger = LoggerFactory.getLogger(NestStructureHandler.class);
    private Structure lastData;

    NestStructureHandler(Thing thing) {
        super(thing);
    }

    public void updateStructure(Structure structure) {
        logger.debug("Updating camera " + structure.getStructureId());
        if (lastData == null || !lastData.equals(structure)) {
            Channel chan = getThing().getChannel(CHANNEL_RUSH_HOUR_REWARDS_ENROLLMENT);
            updateState(chan.getUID(), structure.isRushHourRewardsEnrollement() ? OnOffType.ON : OnOffType.OFF);
            chan = getThing().getChannel(CHANNEL_COUNTRY_CODE);
            updateState(chan.getUID(), new StringType(structure.getCountryCode()));
            chan = getThing().getChannel(CHANNEL_POSTAL_CODE);
            updateState(chan.getUID(), new StringType(structure.getPostalCode()));
            chan = getThing().getChannel(CHANNEL_PEAK_PERIOD_START_TIME);
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.setTime(structure.getPeakPeriodStartTime());
            updateState(chan.getUID(), new DateTimeType(cal));
            chan = getThing().getChannel(CHANNEL_PEAK_PERIOD_START_TIME);
            cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.setTime(structure.getPeakPeriodEndTime());
            updateState(chan.getUID(), new DateTimeType(cal));
            chan = getThing().getChannel(CHANNEL_TIME_ZONE);
            updateState(chan.getUID(), new StringType(structure.getTimeZone()));
            chan = getThing().getChannel(CHANNEL_ETA_BEGIN);
            cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.setTime(structure.getEtaBegin());
            updateState(chan.getUID(), new DateTimeType(cal));
            chan = getThing().getChannel(CHANNEL_CO_ALARM_STATE);
            updateState(chan.getUID(), new StringType(structure.getCoAlarmState().toString()));
            chan = getThing().getChannel(CHANNEL_SMOKE_ALARM_STATE);
            updateState(chan.getUID(), new StringType(structure.getSmokeAlarmState().toString()));
            chan = getThing().getChannel(CHANNEL_AWAY);
            updateState(chan.getUID(), new StringType(structure.getAway().toString()));

            updateStatus(ThingStatus.ONLINE);

            // Setup the properties for this structure.
            updateProperty(PROPERTY_ID, structure.getStructureId());
        } else {
            logger.debug("Nothing to update, same as before.");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_AWAY)) {
            // Change the home/away state.
            if (command instanceof StringType) {
                StringType cmd = (StringType) command;
                // Set the mode to be the cmd value.
                addUpdateRequest("away", cmd.toString());
            }
        }

    }

    /** Creates the url to set a specific value on the thermostat. */
    private void addUpdateRequest(String field, Object value) {
        String structId = getThing().getProperties().get(NestBindingConstants.PROPERTY_ID);
        StringBuilder builder = new StringBuilder().append(NestBindingConstants.NEST_STRUCTURE_UPDATE_URL)
                .append(structId);
        NestUpdateRequest request = new NestUpdateRequest();
        request.setUpdateUrl(builder.toString());
        request.addValue(field, value);
        NestBridgeHandler bridge = (NestBridgeHandler) getBridge();
        bridge.addUpdateRequest(request);
    }

}
