package org.openhab.binding.omnilink.handler;

import java.math.BigDecimal;

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
            int number;

            if (getThing().getConfiguration()
                    .get(OmnilinkBindingConstants.THING_PROPERTIES_NUMBER) instanceof BigDecimal) {
                number = ((BigDecimal) getThing().getConfiguration()
                        .get(OmnilinkBindingConstants.THING_PROPERTIES_NUMBER)).intValue();
            } else {
                number = (int) getThing().getConfiguration().get(OmnilinkBindingConstants.THING_PROPERTIES_NUMBER);
            }
            logger.debug("Setting mode {} on area {}", mode, number);
            // mode, codeNum, areaNum
            getOmnilinkBridgeHander().sendOmnilinkCommand(mode, 1, number);
        }
    }

    public void handleAreaEvent(AreaStatus status) {
        logger.debug("handle area event: mode:{} alarms:{} entryTimer:{} exitTimer:{}", status.getMode(),
                status.getAlarms(), status.getEntryTimer(), status.getExitTimer());

        /*
         * According to the spec, if the 3rd bit is set on a area mode, then that mode is in a delayed state.
         * Unfortunately, this is not the case, but we can fix that by looking to see if the entry/exit timer
         * is set and do this manually.
         */
        int mode = status.getEntryTimer() > 0 || status.getExitTimer() > 0 ? status.getMode() | 1 << 3
                : status.getMode();
        updateState(new ChannelUID(thing.getUID(), OmnilinkBindingConstants.CHANNEL_AREA_MODE), new DecimalType(mode));

        /*
         * Alarm status is actually 8 status, packed into each bit, so we loop through to see if a bit is set, note that
         * this means you can have multiple alarms set at once
         */
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
