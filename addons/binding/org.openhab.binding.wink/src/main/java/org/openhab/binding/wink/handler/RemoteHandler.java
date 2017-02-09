package org.openhab.binding.wink.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;

import com.google.gson.JsonObject;

public class RemoteHandler extends WinkHandler {
    public RemoteHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        if (!this.deviceConfig.validateConfig()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid config.");
            return;
        }
        // TODO: Update status.
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    protected String getDeviceRequestPath() {
        return "remotes/" + this.deviceConfig.getDeviceId();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // TODO
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO
    }

    @Override
    public void sendCommandCallback(JsonObject jsonResult) {
    }

    @Override
    protected void updateDeviceStateCallback(JsonObject jsonDataBlob) {
    }

    @Override
    protected void pubNubMessageCallback(JsonObject jsonDataBlob) {
    }
}
