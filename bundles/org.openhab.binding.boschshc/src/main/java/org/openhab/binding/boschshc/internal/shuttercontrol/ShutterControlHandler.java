package org.openhab.binding.boschshc.internal.shuttercontrol;

import static org.openhab.binding.boschshc.internal.BoschSHCBindingConstants.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.boschshc.internal.BoschSHCBridgeHandler;
import org.openhab.binding.boschshc.internal.BoschSHCHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

/**
 * Utility functions to convert data between Bosch things and openHAB items
 */
final class DataConversion {
    public static int levelToOpenPercentage(double level) {
        return (int) Math.round((1 - level) * 100);
    }

    public static double openPercentageToLevel(double openPercentage) {
        return (100 - openPercentage) / 100.0;
    }
}

/**
 * Handler for a shutter control device
 */
public class ShutterControlHandler extends BoschSHCHandler {
    private final Logger logger = LoggerFactory.getLogger(BoschSHCHandler.class);

    final String ShutterControlServiceName = "ShutterControl";

    public ShutterControlHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            ShutterControlState state = this.getDeviceState();
            this.updateState(state);
        } else if (command instanceof UpDownType) {
            // Set full close/open as target state
            UpDownType upDownType = (UpDownType) command;
            ShutterControlState state = new ShutterControlState();
            if (upDownType == UpDownType.UP) {
                state.level = 1;
            } else if (upDownType == UpDownType.DOWN) {
                state.level = 0;
            } else {
                logger.warn("Received unknown UpDownType command: {}", upDownType);
                return;
            }
            this.setDeviceState(state);
        } else if (command instanceof StopMoveType) {
            // Set STOPPED operation state
            ShutterControlState state = new ShutterControlState();
            state.operationState = OperationState.STOPPED;
            this.setDeviceState(state);
        } else if (command instanceof PercentType) {
            // Set specific level
            PercentType percentType = (PercentType) command;
            double level = DataConversion.openPercentageToLevel(percentType.doubleValue());
            this.setDeviceState(new ShutterControlState(level));
        }
    }

    @Override
    public void processUpdate(String id, @NonNull JsonElement state) {
        try {
            Gson gson = new Gson();
            updateState(gson.fromJson(state, ShutterControlState.class));
        } catch (JsonSyntaxException e) {
            logger.warn("Received unknown update in Shutter Control: {}", state);
        }
    }

    private BoschSHCBridgeHandler getBridgeHandler() {
        Bridge bridge = this.getBridge();
        if (bridge == null) {
            return null;
        }
        return (BoschSHCBridgeHandler) bridge.getHandler();
    }

    private ShutterControlState getDeviceState() {
        BoschSHCBridgeHandler bridgeHandler = this.getBridgeHandler();
        if (bridgeHandler == null) {
            return null;
        }
        return bridgeHandler.refreshState(getThing(), ShutterControlServiceName, ShutterControlState.class);
    }

    private void setDeviceState(ShutterControlState state) {
        BoschSHCBridgeHandler bridgeHandler = this.getBridgeHandler();
        if (bridgeHandler == null) {
            return;
        }
        String deviceId = this.getBoschID();
        if (deviceId == null) {
            return;
        }
        bridgeHandler.putState(deviceId, ShutterControlServiceName, state);
    }

    private void updateState(ShutterControlState state) {
        // Convert level to open ratio
        int openPercentage = DataConversion.levelToOpenPercentage(state.level);
        updateState(CHANNEL_LEVEL, new PercentType(openPercentage));
    }
}
