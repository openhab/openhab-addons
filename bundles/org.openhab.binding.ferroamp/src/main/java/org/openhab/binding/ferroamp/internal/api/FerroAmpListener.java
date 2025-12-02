package org.openhab.binding.ferroamp.internal.api;

import java.util.Map;

/**
 * The {@link FerroAmpUpdateListener} Class is used to listen for updates from the Ferroamp system.
 *
 * @author Örjan Backsell - Initial contribution
 *
 */

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public class FerroAmpListener implements FerroAmpUpdateListener {

    public FerroAmpListener(DataType type, Map<@NonNull String, @Nullable String> keyValueMap) {
    }

    @Override
    public void onFerroAmpUpdateListener(@NonNull DataType type,
            @NonNull Map<@NonNull String, @Nullable String> keyValueMap) {
        // System.out.println("onFerroAmpUpdateListener... type = " + type);
        // System.out.println("onFerroAmpUpdateListener... keyValueMap = " + keyValueMap);
    }
}