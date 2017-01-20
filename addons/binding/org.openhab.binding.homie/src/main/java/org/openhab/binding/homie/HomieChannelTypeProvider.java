package org.openhab.binding.homie;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

public interface HomieChannelTypeProvider extends ChannelTypeProvider {

    public void addChannelType(ChannelTypeUID uid, boolean readOnly);

    public void addChannelGroupType(ChannelGroupTypeUID uid, String label);

    public void addChannelToGroup(ChannelUID channelId, ChannelTypeUID channelType, ChannelGroupTypeUID channelGroupId);

    public ChannelTypeUID createChannelTypeBySettings(String unit, BigDecimal min, BigDecimal max, BigDecimal step,
            String itemType, boolean isReadonly, String category);
}
