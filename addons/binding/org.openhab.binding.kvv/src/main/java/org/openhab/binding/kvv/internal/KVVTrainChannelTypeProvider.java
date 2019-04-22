package org.openhab.binding.kvv.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Component;

@Component(service = ChannelGroupTypeProvider.class)
public class KVVTrainChannelTypeProvider implements ChannelGroupTypeProvider, ChannelTypeProvider {

    private final List<ChannelType> channelTypes = Arrays.asList(
            ChannelTypeBuilder.state(new ChannelTypeUID("kvv", "name"), "Train Name", "String").build(),
            ChannelTypeBuilder.state(new ChannelTypeUID("kvv", "destination"), "Train Destination", "String").build(),
            ChannelTypeBuilder.state(new ChannelTypeUID("kvv", "eta"), "Train ETA", "Number").build());

    private final Map<ChannelGroupTypeUID, ChannelGroupType> channelGroupTypes;

    public KVVTrainChannelTypeProvider() {
        channelGroupTypes = new HashMap<ChannelGroupTypeUID, ChannelGroupType>();
    }

    public void addChannelGroupType(final int id, final String stationName) {
        final List<ChannelDefinition> channels = new ArrayList<ChannelDefinition>();
        for (final ChannelType ct : channelTypes) {
            channels.add(new ChannelDefinition(ct.getUID().getId(), ct.getUID()));
        }
        final ChannelGroupTypeUID gid = new ChannelGroupTypeUID("kvv", stationName + "-train" + id);
        this.channelGroupTypes.put(gid,
                ChannelGroupTypeBuilder.instance(gid, "KVV Train").withChannelDefinitions(channels).build());
    }

    @Override
    public @Nullable Collection<@NonNull ChannelType> getChannelTypes(@Nullable Locale locale) {
        return this.channelTypes;
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        for (final ChannelType ct : this.channelTypes) {
            if (ct.getUID().equals(channelTypeUID)) {
                return ct;
            }
        }
        return null;
    }

    @Override
    public @Nullable ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID,
            @Nullable Locale locale) {
        return this.channelGroupTypes.get(channelGroupTypeUID);
    }

    @Override
    public @Nullable Collection<@NonNull ChannelGroupType> getChannelGroupTypes(@Nullable Locale locale) {
        return this.channelGroupTypes.values();
    }

}
