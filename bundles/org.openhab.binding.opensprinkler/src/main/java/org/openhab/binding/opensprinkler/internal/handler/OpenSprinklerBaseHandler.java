package org.openhab.binding.opensprinkler.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApi;

/**
 * @author Florian Schmidt - Refactoring
 */
@NonNullByDefault
public abstract class OpenSprinklerBaseHandler extends BaseThingHandler {
    public OpenSprinklerBaseHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Nullable
    protected OpenSprinklerApi getApi() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }
        BridgeHandler handler = bridge.getHandler();
        if (!(handler instanceof OpenSprinklerBaseBridgeHandler)) {
            return null;
        }
        try {
            return ((OpenSprinklerBaseBridgeHandler) handler).getApi();
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return null;
        }
    }

    public void updateChannels() {
        this.getThing().getChannels().forEach(channel -> {
            updateChannel(channel.getUID());
        });
        if (getApi() != null) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    protected abstract void updateChannel(ChannelUID uid);

}
