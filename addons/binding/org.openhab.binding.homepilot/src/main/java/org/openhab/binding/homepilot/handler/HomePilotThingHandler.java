package org.openhab.binding.homepilot.handler;

import static org.openhab.binding.homepilot.HomePilotBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.homepilot.internal.HomePilotDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomePilotThingHandler extends BaseThingHandler {

    private static final Logger logger = LoggerFactory.getLogger(HomePilotThingHandler.class);

    public HomePilotThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        refresh();
        super.initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        HomePilotBridgeHandler handler = (HomePilotBridgeHandler) getBridge().getHandler();
        if (command instanceof RefreshType) {
            refresh();
        } else {
            switch (channelUID.getId()) {
                case CHANNEL_POSITION:
                    int position = 0;
                    if (command instanceof DecimalType) {
                        position = ((DecimalType) command).intValue();
                    } else if (command instanceof OnOffType) {
                        position = OnOffType.ON.equals(command) ? 100 : 0;
                    } else {
                        throw new IllegalStateException("unkown command type " + command.getClass());
                    }
                    position = position > 100 ? 100 : ((position < 0) ? 0 : position);
                    handler.getGateway().handleSetPosition(getThing().getUID().getId(), position);
                    updateState(CHANNEL_POSITION, new PercentType(position));
                    break;
                case CHANNEL_STATE:
                    handler.getGateway().handleSetOnOff(getThing().getUID().getId(), OnOffType.ON.equals(command));
                    updateState(CHANNEL_STATE, OnOffType.ON.equals(command) ? OnOffType.ON : OnOffType.OFF);
                    break;
                default:
                    throw new IllegalStateException("unknown channel id " + channelUID.getId() + " in " + channelUID);
            }
        }
    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        super.handleUpdate(channelUID, newState);
        logger.debug("handleUpdate " + channelUID + "; " + newState + "; " + getThing().getUID());
    }

    @Override
    public void thingUpdated(Thing thing) {
        super.thingUpdated(thing);
        logger.debug("thingUpdated " + thing);
    }

    void refresh() {
        HomePilotBridgeHandler handler = (HomePilotBridgeHandler) getBridge().getHandler();
        HomePilotDevice device = handler.getGateway().loadDevice(getThing().getUID().getId());
        switch (getThing().getThingTypeUID().getId()) {
            case ITEM_TYPE_SWITCH:
                updateState(CHANNEL_STATE, device.getPosition() == 0 ? OnOffType.OFF : OnOffType.ON);
                break;
            case ITEM_TYPE_ROLLERSHUTTER:
                int position = device.getPosition();
                position = position > 100 ? 100 : ((position < 0) ? 0 : position);
                // position must be between 0 and 100
                try {
                    updateState(CHANNEL_POSITION, new PercentType(position));
                } catch (Exception e) {
                    throw new RuntimeException("pos: '" + position + "'", e);
                }
                break;
            default:
                throw new IllegalStateException(
                        "unknown thing type " + getThing().getThingTypeUID().getId() + " in " + getThing());

        }
    }

    public static void main(String[] args) {
        new PercentType(0);
    }
}
