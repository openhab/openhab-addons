package org.openhab.binding.somfymylink.internal;

import static org.openhab.binding.somfymylink.internal.SomfyMyLinkBindingConstants.CHANNEL_SCENECONTROL;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SomfySceneHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfySceneHandler.class);

    public SomfySceneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        // try {
        if (CHANNEL_SCENECONTROL.equals(channelUID.getId())) {

            String targetId = channelUID.getThingUID().getId();

            if (command instanceof RefreshType) {
                // TODO: handle data refresh
                return;
            }

            /*
             * if (CHANNEL_SHADELEVEL.equals(channelUID.getId()) && command instanceof UpDownType) {
             * if (command.equals(UpDownType.DOWN)) {
             * getBridgeHandler().commandShadeDown(targetId);
             * } else {
             * getBridgeHandler().commandShadeUp(targetId);
             * }
             * }
             * if (CHANNEL_SHADELEVEL.equals(channelUID.getId()) && command instanceof StopMoveType) {
             * getBridgeHandler().commandShadeStop(targetId);
             * }
             */
        }
        // } catch (SomfyMyLinkException e) {
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        // } catch (Exception e) {
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        // }
    }

    protected SomfyMyLinkHandler getBridgeHandler() {
        return this.getBridge() != null ? (SomfyMyLinkHandler) this.getBridge().getHandler() : null;
    }

}