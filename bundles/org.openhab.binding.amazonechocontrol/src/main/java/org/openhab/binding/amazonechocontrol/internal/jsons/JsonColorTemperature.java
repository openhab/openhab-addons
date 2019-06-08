package org.openhab.binding.amazonechocontrol.internal.jsons;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class JsonColorTemperature {
    public @Nullable String temperatureName;

    public JsonColorTemperature(String temperature) {
        temperatureName = temperature;
    }
}
