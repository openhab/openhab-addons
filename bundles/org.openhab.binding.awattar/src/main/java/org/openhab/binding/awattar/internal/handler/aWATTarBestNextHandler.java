package org.openhab.binding.awattar.internal.handler;

import static org.openhab.binding.awattar.internal.aWATTarBindingConstants.BINDING_ID;
import static org.openhab.binding.awattar.internal.aWATTarUtil.getMillisToNextMinute;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.openhab.binding.awattar.internal.aWATTarBestpriceConfiguration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.*;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class aWATTarBestNextHandler extends aWATTarBestpriceHandler {

    private final Logger logger = LoggerFactory.getLogger(aWATTarBestNextHandler.class);
    private final int thingRefreshInterval = 60;
    private @Nullable ScheduledFuture<?> thingRefresher;

    public aWATTarBestNextHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
        config = new aWATTarBestpriceConfiguration();
        // ensure that we have some useful values at the beginning.
        config.consecutive = true;
        config.length = 5;
        config.rangeDuration = 2;
    }

    @Override
    public void initialize() {
        logger.trace("Initializing aWATTar bestnext handler {}", this);

        synchronized (this) {
            if (thingRefresher == null || thingRefresher.isCancelled()) {
                logger.trace("Start Thing refresh job at interval {} seconds.", thingRefreshInterval);
                thingRefresher = scheduler.scheduleAtFixedRate(this::refreshChannels, getMillisToNextMinute(1),
                        thingRefreshInterval * 1000, TimeUnit.MILLISECONDS);
            }

        }
        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void handleCommand(@NotNull ChannelUID channelUID, @NotNull Command command) {
        logger.trace("Handling command {} for channel {}", command, channelUID);
        if (command instanceof RefreshType) {
            refreshChannel(channelUID);
        } else {
            logger.debug("Binding {} only supports refresh command", BINDING_ID);
        }
    }

    public void refreshChannels() {
        logger.trace("Refreshing channels for {}", getThing().getUID());
        updateStatus(ThingStatus.ONLINE);
        for (Channel channel : getThing().getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (ChannelKind.STATE.equals(channel.getKind()) && isLinked(channelUID)) {
                logger.trace("Refreshing linked channel {}", channelUID);
                refreshChannel(channel.getUID());
            }
        }
    }
}
