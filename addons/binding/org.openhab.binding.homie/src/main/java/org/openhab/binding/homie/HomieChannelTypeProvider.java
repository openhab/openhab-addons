package org.openhab.binding.homie;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

public interface HomieChannelTypeProvider extends ChannelTypeProvider {

    public void addChannelType(ChannelTypeUID uid, boolean readOnly);

    public void addChannelGroupType(ChannelGroupTypeUID uid, String label);

    public void addChannelToGroup(ChannelUID channelId, ChannelTypeUID channelType, ChannelGroupTypeUID channelGroupId);
}
