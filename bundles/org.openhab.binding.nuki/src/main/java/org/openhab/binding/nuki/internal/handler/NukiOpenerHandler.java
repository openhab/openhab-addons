package org.openhab.binding.nuki.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nuki.internal.constants.NukiBindingConstants;
import org.openhab.binding.nuki.internal.constants.OpenerAction;
import org.openhab.binding.nuki.internal.dataexchange.BridgeLockActionResponse;
import org.openhab.binding.nuki.internal.dto.BridgeApiDeviceStateDto;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Thing handler for Nuki Opener
 *
 * @author Jan Vybíral - Initial contribution
 */
@NonNullByDefault
public class NukiOpenerHandler extends AbstractNukiDeviceHandler {

    public NukiOpenerHandler(Thing thing) {
        super(thing);
        logger.debug("Instantiating NukiOpenerHandler({})", thing);
    }

    @Override
    public void refreshState(BridgeApiDeviceStateDto state) {
        updateState(NukiBindingConstants.CHANNEL_OPENER_LOW_BATTERY, state.isBatteryCritical(), this::toSwitch);
        updateState(NukiBindingConstants.CHANNEL_OPENER_STATE, state.getState(), DecimalType::new);
        updateState(NukiBindingConstants.CHANNEL_OPENER_MODE, state.getMode(), DecimalType::new);
        updateState(NukiBindingConstants.CHANNEL_OPENER_RING_ACTION_STATE, state.getRingactionState(), this::toSwitch);
        updateState(NukiBindingConstants.CHANNEL_OPENER_RING_ACTION_TIMESTAMP, state.getRingActionTimestamp(),
                this::toDateTime);
    }

    @Override
    protected int getDeviceType() {
        return NukiBindingConstants.DEVICE_OPENER;
    }

    @Override
    protected boolean doHandleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case NukiBindingConstants.CHANNEL_OPENER_STATE:
                if (command instanceof DecimalType) {
                    @Nullable
                    OpenerAction action = OpenerAction.fromAction(((DecimalType) command).intValue());
                    if (action != null) {
                        BridgeLockActionResponse response = getNukiHttpClient().getOpenerAction(nukiId, action);
                        return handleResponse(response, channelUID.getAsString(), command.toString());
                    }
                }
                break;
        }
        return false;
    }
}
