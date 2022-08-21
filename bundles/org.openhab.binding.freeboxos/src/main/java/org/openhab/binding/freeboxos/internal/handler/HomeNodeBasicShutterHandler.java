package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.home.HomeManager;
import org.openhab.binding.freeboxos.internal.api.home.HomeNodeEndpointState;
import org.openhab.binding.freeboxos.internal.config.NodeConfiguration;
import org.openhab.binding.freeboxos.internal.config.NodeConfiguration.BasicShutter;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

public class HomeNodeBasicShutterHandler extends ApiConsumerHandler {

    public HomeNodeBasicShutterHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    void internalGetProperties(@NonNull Map<@NonNull String, @NonNull String> properties) throws FreeboxException {
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        BasicShutter config = getBasicShutterConfig();
        HomeManager manager = getManager(HomeManager.class);
        HomeNodeEndpointState state = manager.getEndpointsState(config.nodeId, config.stateSignalId, Boolean.class);
        Double percent = null;
        if ("bool".equals(state.getValueType())) {
            percent = Boolean.TRUE.equals(state.asBoolean()) ? 1.0 : 0.0;
        } else if ("int".equals(state.getValueType())) {
            Integer inValue = state.asInt();
            if (inValue != null) {
                percent = inValue.doubleValue() / 100.0;
            }
        }
        updateChannelDecimal(BASIC_SHUTTER, BASIC_SHUTTER_CMD, percent);
    }

    protected NodeConfiguration.BasicShutter getBasicShutterConfig() throws FreeboxException {
        return getConfigAs(NodeConfiguration.BasicShutter.class);
    }

    @Override
    protected boolean internalHandleCommand(@NonNull ChannelUID channelUID, @NonNull Command command)
            throws FreeboxException {
        BasicShutter config = getBasicShutterConfig();
        if (BASIC_SHUTTER_CMD.equals(channelUID.getIdWithoutGroup())) {
            if (command instanceof UpDownType) {
                if (command == UpDownType.UP) {
                    getManager(HomeManager.class).putCommand(config.nodeId, config.upSlotId, true);
                } else if (command == UpDownType.DOWN) {
                    getManager(HomeManager.class).putCommand(config.nodeId, config.downSlotId, true);
                }
            } else if (command instanceof StopMoveType) {
                if (command == StopMoveType.STOP) {
                    getManager(HomeManager.class).putCommand(config.nodeId, config.stopSlotId, true);
                }
            }
        }
        return super.internalHandleCommand(channelUID, command);
    }
}
