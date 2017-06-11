package org.openhab.binding.omnilink.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.MessageTypes.statuses.ZoneStatus;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

public class ZoneHandler extends AbstractOmnilinkHandler {
	private Logger logger = LoggerFactory.getLogger(ZoneHandler.class);

	public ZoneHandler(Thing thing) {
		super(thing);
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		logger.debug("must handle command");
		if (command instanceof RefreshType) {
			String[] channelParts = channelUID.getAsString().split(UID.SEPARATOR);
			logger.debug("Zone '{}' got REFRESH command", thing.getLabel());
			Futures.addCallback(getOmnilinkBridgeHander().getZoneStatus(Integer.parseInt(channelParts[2])),
					new FutureCallback<ZoneStatus>() {
						@Override
						public void onFailure(Throwable arg0) {
							logger.error("Error refreshing unit status", arg0);
						}

						@Override
						public void onSuccess(ZoneStatus status) {
							handleZoneStatus(status);
						}
					});
		}
	}

	public void handleZoneStatus(ZoneStatus status) {

		int current = ((status.getStatus() >> 0) & 0x03);
		int latched = ((status.getStatus() >> 2) & 0x03);
		int arming = ((status.getStatus() >> 4) & 0x03);
		State contactState = current == 0 ? OpenClosedType.CLOSED : OpenClosedType.OPEN;

		logger.debug("handle Zone Status Change to state:{} current:{} latched:{} arming:{}", contactState, current,
				latched, arming);

		updateState(OmnilinkBindingConstants.CHANNEL_ZONE_CONTACT, contactState);
		updateState(OmnilinkBindingConstants.CHANNEL_ZONE_CURRENT_CONDITION, new DecimalType(current));
		updateState(OmnilinkBindingConstants.CHANNEL_ZONE_LATCHED_ALARM_STATUS, new DecimalType(latched));
		updateState(OmnilinkBindingConstants.CHANNEL_ZONE_ARMING_STATUS, new DecimalType(arming));

	}
}
