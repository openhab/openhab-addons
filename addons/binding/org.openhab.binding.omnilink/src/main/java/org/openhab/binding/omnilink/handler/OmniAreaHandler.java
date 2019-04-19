package org.openhab.binding.omnilink.handler;

import java.util.EnumSet;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.openhab.binding.omnilink.OmnilinkBindingConstants.AreaAlarm;

public class OmniAreaHandler extends AbstractAreaHandler {

    private final static EnumSet<AreaAlarm> OMNI_ALARMS = EnumSet.of(AreaAlarm.BURGLERY, AreaAlarm.FIRE, AreaAlarm.GAS,
            AreaAlarm.AUXILARY, AreaAlarm.FREEZE, AreaAlarm.WATER, AreaAlarm.DURESS, AreaAlarm.TEMPERATURE);

    public OmniAreaHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected int getMode(ChannelUID channelUID) {
        switch (channelUID.getId()) {
            case OmnilinkBindingConstants.CHANNEL_AREA_SECURITY_MODE_DISARM:
                return OmniLinkCmd.CMD_SECURITY_OMNI_DISARM.getNumber();
            case OmnilinkBindingConstants.CHANNEL_AREA_SECURITY_MODE_DAY:
                return OmniLinkCmd.CMD_SECURITY_OMNI_DAY_MODE.getNumber();

            case OmnilinkBindingConstants.CHANNEL_AREA_SECURITY_MODE_NIGHT:
                return OmniLinkCmd.CMD_SECURITY_OMNI_NIGHT_MODE.getNumber();

            case OmnilinkBindingConstants.CHANNEL_AREA_SECURITY_MODE_AWAY:
                return OmniLinkCmd.CMD_SECURITY_OMNI_AWAY_MODE.getNumber();

            case OmnilinkBindingConstants.CHANNEL_AREA_SECURITY_MODE_VACATION:
                return OmniLinkCmd.CMD_SECURITY_OMNI_VACATION_MODE.getNumber();

            case OmnilinkBindingConstants.CHANNEL_AREA_SECURITY_MODE_DAY_INSTANT:
                return OmniLinkCmd.CMD_SECURITY_OMNI_DAY_INSTANCE_MODE.getNumber();

            case OmnilinkBindingConstants.CHANNEL_AREA_SECURITY_MODE_NIGHT_DELAYED:
                return OmniLinkCmd.CMD_SECURITY_OMNI_NIGHT_DELAYED_MODE.getNumber();

            default:
                throw new IllegalStateException("Unknown channel " + channelUID.getId());
        }
    }

    @Override
    protected EnumSet<AreaAlarm> getAlarms() {
        return OMNI_ALARMS;
    }

}
