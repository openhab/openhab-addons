package org.openhab.binding.omnilink.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.MessageTypes.statuses.AreaStatus;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

public class AreaHandler extends AbstractOmnilinkHandler {
	private Logger logger = LoggerFactory.getLogger(AreaHandler.class);

	public AreaHandler(Thing thing) {
		super(thing);
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		logger.debug("handleCommand: {}, command: {}", channelUID, command);
		String[] channelParts = channelUID.getAsString().split(UID.SEPARATOR);
		if (command instanceof RefreshType) {
			Futures.addCallback(getOmnilinkBridgeHander().getAreaStatus(Integer.parseInt(channelParts[2])),
					new FutureCallback<AreaStatus>() {

						@Override
						public void onFailure(Throwable arg0) {
							logger.error("Failure getting status", arg0);
						}

						@Override
						public void onSuccess(AreaStatus status) {
							logger.debug("handle area status: {}", status);
							handleAreaEvent(status);
						}
					});
		} else if (command instanceof DecimalType) {
			int mode = OmniLinkCmd.CMD_SECURITY_OMNI_DISARM.getNumber() + ((DecimalType) command).intValue();
			int number = (int) getThing().getConfiguration().get(OmnilinkBindingConstants.THING_PROPERTIES_NUMBER);
			logger.debug("Setting mode {} on area {}", mode, number);
			//mode, codeNum, areaNum
			getOmnilinkBridgeHander().sendOmnilinkCommand(mode, 1, number);
		}
	}

	public void handleAreaEvent(AreaStatus status) {
		logger.debug("handle area event: {}", status);
		updateState(new ChannelUID(thing.getUID(), OmnilinkBindingConstants.CHANNEL_AREA_MODE),
				new DecimalType(status.getMode()));
		for (int i = 0; i < OmnilinkBindingConstants.CHANNEL_AREA_ALARMS.length; i++) {
			if (((status.getAlarms() >> i) & 1) > 0) {
				updateState(new ChannelUID(thing.getUID(), OmnilinkBindingConstants.CHANNEL_AREA_ALARMS[i]),
						OnOffType.ON);
			} else {
				updateState(new ChannelUID(thing.getUID(), OmnilinkBindingConstants.CHANNEL_AREA_ALARMS[i]),
						OnOffType.OFF);
			}
		}
	}
}
