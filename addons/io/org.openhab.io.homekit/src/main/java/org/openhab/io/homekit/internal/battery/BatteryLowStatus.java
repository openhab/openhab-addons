package org.openhab.io.homekit.internal.battery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;

@NonNullByDefault
public class BatteryLowStatus implements BatteryStatus {

    private SwitchItem batterySwitchItem;

    BatteryLowStatus(SwitchItem batterySwitchItem) {
        this.batterySwitchItem = batterySwitchItem;
    }

    @Override
    @Nullable
    public Boolean isLow() {
        OnOffType state = batterySwitchItem.getStateAs(OnOffType.class);
        if (state == null) {
            return null;
        } else {
            return state == OnOffType.ON;
        }
    }

    @Override
    public void subscribe(HomekitAccessoryUpdater updater, HomekitCharacteristicChangeCallback callback) {
        updater.subscribe(batterySwitchItem, callback);
    }

    @Override
    public void unsubscribe(HomekitAccessoryUpdater updater) {
        updater.unsubscribe(batterySwitchItem);
    }
}
