package org.openhab.binding.knx.internal.profiles;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.StateProfile;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

@NonNullByDefault
public class KNXDefaultProfile implements StateProfile {

    public static final ProfileTypeUID UID = new ProfileTypeUID("knx", "default");

    private final ProfileCallback callback;

    public KNXDefaultProfile(ProfileCallback callback) {
        this.callback = callback;
    }

    @Override
    public @NonNull ProfileTypeUID getProfileTypeUID() {
        return UID;
    }

    @Override
    public void onStateUpdateFromItem(@NonNull State state) {
        // no-op
    }

    @Override
    public void onCommandFromItem(@NonNull Command command) {
        callback.handleCommand(command);
    }

    @Override
    public void onCommandFromHandler(@NonNull Command command) {
        if (command instanceof State) {
            callback.sendUpdate((State) command);
        }
    }

    @Override
    public void onStateUpdateFromHandler(@NonNull State state) {
        // no-op
    }

}
