package org.openhab.binding.omnilink.handler;

import java.util.EnumSet;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.openhab.binding.omnilink.OmnilinkBindingConstants.AreaAlarm;

public class LuminaAreaHandler extends AbstractAreaHandler {

    private final static EnumSet<AreaAlarm> LUMINA_ALARMS = EnumSet.of(AreaAlarm.FREEZE, AreaAlarm.WATER,
            AreaAlarm.TEMPERATURE);

    public LuminaAreaHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected int getMode(ChannelUID channelUID) {
        switch (channelUID.getId()) {
            case OmnilinkBindingConstants.CHANNEL_AREA_SECURITY_MODE_HOME:
                return OmniLinkCmd.CMD_SECURITY_LUMINA_HOME_MODE.getNumber();
            case OmnilinkBindingConstants.CHANNEL_AREA_SECURITY_MODE_SLEEP:
                return OmniLinkCmd.CMD_SECURITY_LUMINA_SLEEP_MODE.getNumber();

            case OmnilinkBindingConstants.CHANNEL_AREA_SECURITY_MODE_AWAY:
                return OmniLinkCmd.CMD_SECURITY_LUMINA_AWAY_MODE.getNumber();

            case OmnilinkBindingConstants.CHANNEL_AREA_SECURITY_MODE_VACATION:
                return OmniLinkCmd.CMD_SECURITY_LUMINA_VACATION_MODE.getNumber();

            case OmnilinkBindingConstants.CHANNEL_AREA_SECURITY_MODE_PARTY:
                return OmniLinkCmd.CMD_SECURITY_LUMINA_PARTY_MODE.getNumber();

            case OmnilinkBindingConstants.CHANNEL_AREA_SECURITY_MODE_SPECIAL:
                return OmniLinkCmd.CMD_SECURITY_LUMINA_SPECIAL_MODE.getNumber();

            default:
                throw new IllegalStateException("Unknown channel " + channelUID.getId());
        }
    }

    @Override
    protected EnumSet<AreaAlarm> getAlarms() {
        return LUMINA_ALARMS;
    }

}
