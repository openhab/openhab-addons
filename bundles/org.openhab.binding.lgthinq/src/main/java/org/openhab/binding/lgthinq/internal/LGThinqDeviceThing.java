/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.lgthinq.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.internal.handler.LGThinqBridgeHandler;
import org.openhab.binding.lgthinq.lgservices.model.Capability;
import org.openhab.binding.lgthinq.lgservices.model.LGDevice;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;

/**
 * The {@link LGThinqDeviceThing} is a main interface contract for all LG Thinq things
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public abstract class LGThinqDeviceThing extends BaseThingHandler {

    public LGThinqDeviceThing(Thing thing) {
        super(thing);
    }

    public abstract void onDeviceAdded(@NonNullByDefault LGDevice device);

    public abstract String getDeviceId();

    public abstract String getDeviceAlias();

    public abstract String getDeviceModelName();

    public abstract String getDeviceUriJsonConfig();

    public abstract boolean onDeviceStateChanged();

    public abstract void onDeviceRemoved();

    public abstract void onDeviceGone();

    public abstract void updateChannelDynStateDescription() throws LGThinqApiException;

    public abstract <T extends Capability> T getCapabilities() throws LGThinqApiException;

    protected abstract Logger getLogger();

    protected abstract void startCommandExecutorQueueJob();

    protected void initializeThing(@Nullable ThingStatus bridgeStatus) {
        getLogger().debug("initializeThing LQ Thinq {}. Bridge status {}", getThing().getUID(), bridgeStatus);
        String deviceId = getThing().getUID().getId();

        Bridge bridge = getBridge();
        if (!deviceId.isBlank()) {
            try {
                updateChannelDynStateDescription();
            } catch (LGThinqApiException e) {
                getLogger().error(
                        "Error updating channels dynamic options descriptions based on capabilities of the device. Fallback to default values.");
            }
            if (bridge != null) {
                LGThinqBridgeHandler handler = (LGThinqBridgeHandler) bridge.getHandler();
                // registry this thing to the bridge
                if (handler == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
                } else {
                    handler.registryListenerThing(this);
                    if (bridgeStatus == ThingStatus.ONLINE) {
                        updateStatus(ThingStatus.ONLINE);
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                    }
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-device-id");
        }
        // finally, start command queue, regardless of the thing state, as we can still try to send commands without
        // property ONLINE (the successful result from command request can put the thing in ONLINE status).
        startCommandExecutorQueueJob();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        getLogger().debug("bridgeStatusChanged {}", bridgeStatusInfo);
        super.bridgeStatusChanged(bridgeStatusInfo);
        // restart scheduler
        initializeThing(bridgeStatusInfo.getStatus());
    }
}
