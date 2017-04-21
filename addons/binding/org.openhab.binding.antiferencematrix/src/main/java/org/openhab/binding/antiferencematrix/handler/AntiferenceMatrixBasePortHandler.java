package org.openhab.binding.antiferencematrix.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;

public abstract class AntiferenceMatrixBasePortHandler extends BaseThingHandler {

    public AntiferenceMatrixBasePortHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        // Long running initialization should be done asynchronously in background.
        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
        getLogger().debug("Initalizing matrix port [{}]", getThing().getLabel());
        AntiferenceMatrixBridgeHandler bridge = getMatrixBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        doRefresh(bridge);
        getLogger().debug("Finished initalizing matrix port [{}]", getThing().getLabel());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        AntiferenceMatrixBridgeHandler bridge = getMatrixBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        if (command instanceof RefreshType) {
            doRefresh(bridge);
        } else {
            handleOtherCommand(channelUID, command, bridge);
        }
        updateStatusIfRequired(ThingStatus.ONLINE);
    }

    abstract void handleOtherCommand(ChannelUID channelUID, Command command, AntiferenceMatrixBridgeHandler bridge);

    abstract void doRefresh(AntiferenceMatrixBridgeHandler bridge);

    abstract Logger getLogger();

    protected void updateStatusIfRequired(ThingStatus status) {
        if (!status.equals(getThing().getStatus())) {
            updateStatus(status);
        }
    }

    protected AntiferenceMatrixBridgeHandler getMatrixBridge() {
        AntiferenceMatrixBridgeHandler bridge = (AntiferenceMatrixBridgeHandler) getBridge().getHandler();
        return bridge;
    }

}
