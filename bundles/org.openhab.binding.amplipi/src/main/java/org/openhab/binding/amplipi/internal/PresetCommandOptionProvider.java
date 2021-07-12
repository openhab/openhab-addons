package org.openhab.binding.amplipi.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amplipi.internal.model.Preset;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.binding.BaseDynamicCommandDescriptionProvider;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.CommandDescription;
import org.openhab.core.types.CommandOption;

@NonNullByDefault
public class PresetCommandOptionProvider extends BaseDynamicCommandDescriptionProvider implements ThingHandlerService {

    private @Nullable AmpliPiHandler handler;

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = (AmpliPiHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @Override
    public @Nullable CommandDescription getCommandDescription(Channel channel,
            @Nullable CommandDescription originalCommandDescription, @Nullable Locale locale) {
        ChannelTypeUID typeUID = channel.getChannelTypeUID();
        if (typeUID != null && AmpliPiBindingConstants.CHANNEL_PRESET.equals(typeUID.getId()) && handler != null) {
            List<CommandOption> options = new ArrayList<>();
            List<Preset> presets = handler.getPresets();
            for (Preset preset : presets) {
                options.add(new CommandOption(preset.getId().toString(), preset.getName()));
            }
            setCommandOptions(channel.getUID(), options);
        }
        return super.getCommandDescription(channel, originalCommandDescription, locale);
    }
}
