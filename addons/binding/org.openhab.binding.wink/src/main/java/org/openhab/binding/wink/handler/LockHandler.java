package org.openhab.binding.wink.handler;

import static org.openhab.binding.wink.WinkBindingConstants.CHANNEL_LOCKSTATE;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class LockHandler extends WinkHandler {

    public LockHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        if (!this.deviceConfig.validateConfig()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid config.");
            return;
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (channelUID.getId().equals(CHANNEL_LOCKSTATE)) {
            ReadDeviceState();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_LOCKSTATE)) {
            if (command.equals(OnOffType.ON)) {
                setLock(true);
            } else if (command.equals(OnOffType.OFF)) {
                setLock(false);
            } else if (command instanceof RefreshType) {
                logger.debug("Refreshing state");
                ReadDeviceState();
            }
        }
    }

    private void setLock(boolean lock) {
        if (lock) {
            sendCommand("{\"desired_state\": {\"locked\": true}}");
        } else {
            sendCommand("{\"desired_state\": {\"locked\": false}}");
        }
    }

    @Override
    protected String getDeviceRequestPath() {
        return "locks/" + this.deviceConfig.getDeviceId();
    }

    @Override
    protected void updateDeviceStateCallback(JsonObject jsonDataBlob) {
        updateState(jsonDataBlob);
    }

    @Override
    public void sendCommandCallback(JsonObject jsonResult) {
    }

    @Override
    protected void pubNubMessageCallback(JsonObject jsonDataBlob) {
        updateState(jsonDataBlob);
    }

    private void updateState(JsonObject jsonDataBlob) {
        boolean lockedStateLastReading = false;
        JsonElement lastReadingBlob = jsonDataBlob.get("last_reading");
        if (lastReadingBlob != null) {
            JsonElement lockedStateBlob = lastReadingBlob.getAsJsonObject().get("locked");
            if (lockedStateBlob != null) {
                lockedStateLastReading = lockedStateBlob.getAsBoolean();
            }
        }
        boolean lockedDesiredState = false;
        JsonElement desiredStateBlob = jsonDataBlob.get("desired_state");
        if (desiredStateBlob != null) {
            JsonElement lockedBlob = desiredStateBlob.getAsJsonObject().get("locked");
            if (lockedBlob != null) {
                lockedDesiredState = lockedBlob.getAsBoolean();
            }
        }
        // Don't update the state during a transition.
        if (lockedDesiredState == lockedStateLastReading) {
            State state = OnOffType.OFF;
            if (lockedStateLastReading) {
                state = OnOffType.ON;
            }
            updateState(CHANNEL_LOCKSTATE, state);
        }
    }

}
