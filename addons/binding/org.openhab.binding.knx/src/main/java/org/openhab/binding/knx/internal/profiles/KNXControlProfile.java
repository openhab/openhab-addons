package org.openhab.binding.knx.internal.profiles;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.StateProfile;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

@NonNullByDefault
public class KNXControlProfile implements StateProfile {

    public static final ProfileTypeUID UID = new ProfileTypeUID("knx", "control");

    private final ProfileCallback callback;

    public KNXControlProfile(ProfileCallback callback) {
        this.callback = callback;
    }

    @Override
    public @NonNull ProfileTypeUID getProfileTypeUID() {
        return UID;
    }

    @Override
    public void onStateUpdateFromItem(@NonNull State state) {
        if (state instanceof Command) {
            callback.handleCommand((Command) state);
        }
    }

    @Override
    public void onCommandFromItem(@NonNull Command command) {
        // no-op
    }

    @Override
    public void onCommandFromHandler(@NonNull Command command) {
        callback.sendCommand(command);
    }

    @Override
    public void onStateUpdateFromHandler(@NonNull State state) {
        // no-op
    }

}
