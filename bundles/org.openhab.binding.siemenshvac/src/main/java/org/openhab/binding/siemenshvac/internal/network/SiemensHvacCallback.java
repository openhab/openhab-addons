package org.openhab.binding.siemenshvac.internal.network;

import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public interface SiemensHvacCallback {
    /**
     * Runs callback code after response completion.
     */
    void execute(URI uri, int status, @Nullable Object response);

}
