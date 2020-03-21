package org.openhab.binding.fox.internal;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.DynamicCommandDescriptionProvider;
import org.eclipse.smarthome.core.types.CommandDescription;
import org.eclipse.smarthome.core.types.CommandDescriptionBuilder;
import org.eclipse.smarthome.core.types.CommandOption;
import org.osgi.service.component.annotations.Component;

@Component(service = { DynamicCommandDescriptionProvider.class, FoxDynamicCommandDescriptionProvider.class })
public class FoxDynamicCommandDescriptionProvider implements DynamicCommandDescriptionProvider {

    private final Map<ChannelUID, @Nullable List<CommandOption>> channelOptionsMap = new ConcurrentHashMap<>();

    /**
     * For a given channel UID, set a {@link List} of {@link CommandOption}s that should be used for the channel,
     * instead of the one defined statically in the {@link ChannelType}.
     *
     * @param channelUID the channel UID of the channel
     * @param options a {@link List} of {@link CommandOption}s
     */
    public void setCommandOptions(ChannelUID channelUID, List<CommandOption> options) {
        channelOptionsMap.put(channelUID, options);
    }

    @Override
    public @Nullable CommandDescription getCommandDescription(Channel channel,
            @Nullable CommandDescription originalCommandDescription, @Nullable Locale locale) {
        List<CommandOption> options = channelOptionsMap.get(channel.getUID());
        if (options == null) {
            return null;
        }

        return CommandDescriptionBuilder.create().withCommandOptions(options).build();
    }
}
