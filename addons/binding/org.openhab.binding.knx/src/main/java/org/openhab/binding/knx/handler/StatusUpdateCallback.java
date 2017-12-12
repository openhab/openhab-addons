package org.openhab.binding.knx.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;

@NonNullByDefault
public interface StatusUpdateCallback {

    void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description);

    void updateStatus(ThingStatus status, ThingStatusDetail statusDetail);

    void updateStatus(ThingStatus status);

}
