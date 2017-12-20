package org.openhab.binding.knx.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;

@NonNullByDefault
public interface StatusUpdateCallback {

    void updateStatus(ThingStatus status);

    void updateStatus(ThingStatus status, ThingStatusDetail thingStatusDetail, String message);

}
