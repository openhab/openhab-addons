package org.openhab.io.homekit.internal.battery;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;

public class NoBatteryStatus implements BatteryStatus {

    @Override
    public @Nullable Boolean isLow() {
        return false;
    }

    @Override
    public void subscribe(@NonNull HomekitAccessoryUpdater updater,
            @NonNull HomekitCharacteristicChangeCallback callback) {
        // do nothing
    }

    @Override
    public void unsubscribe(@NonNull HomekitAccessoryUpdater updater) {
        // do nothing
    }
}
