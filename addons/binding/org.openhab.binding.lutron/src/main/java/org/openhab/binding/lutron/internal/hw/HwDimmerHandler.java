package org.openhab.binding.lutron.internal.hw;

import static org.openhab.binding.lutron.LutronBindingConstants.CHANNEL_LIGHTLEVEL;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;

public class HwDimmerHandler extends BaseThingHandler {
    // private Logger logger = LoggerFactory.getLogger(HwDimmerHandler.class);

    String address;
    Integer fadeTime = 1;
    Integer defaultLevel = 100;

    public HwDimmerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        HwDimmerConfig config = getThing().getConfiguration().as(HwDimmerConfig.class);

        address = config.getAddress();
        if (address == null || address.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Address not set");
            return;
        }

        fadeTime = config.getFadeTime();
        defaultLevel = config.getDefaultLevel();

        if (getThing().getBridgeUID() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
            return;
        }

        updateStatus(ThingStatus.ONLINE);
        queryLevel();
    }

    public String getAddress() {
        return address;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_LIGHTLEVEL)) {
            if (command instanceof Number) {
                int level = ((Number) command).intValue();
                outputLevel(level);
            } else if (command.equals(OnOffType.ON)) {
                outputLevel(defaultLevel);
            } else if (command.equals(OnOffType.OFF)) {
                outputLevel(0);
            }
        }
    }

    private HwSerialBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        } else if (!(bridge.getHandler() instanceof HwSerialBridgeHandler)) {
            return null;
        } else {
            return (HwSerialBridgeHandler) bridge.getHandler();
        }
    }

    private void queryLevel() {
        HwSerialBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR, "No bridge associated");
            return;
        }

        String cmd = String.format("RDL, %s", address);
        bridgeHandler.sendCommand(cmd);
    }

    private void outputLevel(Number level) {
        HwSerialBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR, "No bridge associated");
            return;
        }

        String cmd = String.format("FADEDIM, %s, %s, 0, %s", level, fadeTime, address);
        bridgeHandler.sendCommand(cmd);
    }

    public void handleLevelChange(Integer level) {
        updateState(CHANNEL_LIGHTLEVEL, new PercentType(level));
    }
}
