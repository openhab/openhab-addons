package org.openhab.binding.vesync.internal.handlers;

import static org.openhab.binding.vesync.internal.VeSyncConstants.EMPTY_STRING;
import static org.openhab.binding.vesync.internal.VeSyncConstants.THING_TYPE_WIFI_SWITCH;
import static org.openhab.binding.vesync.internal.dto.requests.VeSyncProtocolConstants.DEVICE_GET_WIFI_SWITCH_STATUS;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.vesync.internal.VeSyncConstants;
import org.openhab.binding.vesync.internal.dto.requests.VeSyncRequestManagedDeviceBypassV2;
import org.openhab.binding.vesync.internal.dto.responses.VeSyncV2BypassWifiSwitchStatus;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class VeSyncDeviceWifiSwitchHandler extends VeSyncBaseDeviceHandler {

    public static final String DEV_TYPE_FAMILY_WIFI_SWITCH = "SWI";

    public static final int DEFAULT_AIR_PURIFIER_POLL_RATE = 120;

    public static final String DEV_FAMILY_CORE_WHOG_PLUG = "WHOG";
    public static final String DEV_FAMILY_CORE_ESW = "ESW";
    public static final String DEV_FAMILY_CORE_ESWL = "ESWL";

    public static final VeSyncDeviceMetadata COREESW = new VeSyncDeviceMetadata(DEV_FAMILY_CORE_ESW,
            Collections.emptyList(), Arrays.asList("ESW03-USA", "ESW01-EU", "ESW15-USA"));

    public static final VeSyncDeviceMetadata COREESWL = new VeSyncDeviceMetadata(DEV_FAMILY_CORE_ESWL,
            Collections.emptyList(), Arrays.asList("ESWL01", "ESWL03"));

    public static final VeSyncDeviceMetadata COREWHOPGPLUG = new VeSyncDeviceMetadata(DEV_FAMILY_CORE_WHOG_PLUG,
            Collections.emptyList(), List.of("WHOGPLUG"));

    public static final List<VeSyncDeviceMetadata> SUPPORTED_MODEL_FAMILIES = Arrays.asList(COREESW, COREESWL,
            COREWHOPGPLUG);

    private final Logger logger = LoggerFactory.getLogger(VeSyncDeviceWifiSwitchHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_WIFI_SWITCH);

    private final Object pollLock = new Object();

    public VeSyncDeviceWifiSwitchHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        customiseChannels();
    }

    @Override
    public String getDeviceFamilyProtocolPrefix() {
        return DEV_TYPE_FAMILY_WIFI_SWITCH;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        throw new UnsupportedOperationException("Unimplemented method 'handleCommand'");
    }

    @Override
    protected void pollForDeviceData(ExpiringCache<String> cachedResponse) {
        String response;
        VeSyncV2BypassWifiSwitchStatus wifiSwitchStatus;
        synchronized (pollLock) {
            response = cachedResponse.getValue();
            boolean cachedDataUsed = response != null;
            if (response == null) {
                logger.trace("Requesting fresh response");
                response = sendV2BypassCommand(DEVICE_GET_WIFI_SWITCH_STATUS,
                        new VeSyncRequestManagedDeviceBypassV2.EmptyPayload());
            } else {
                logger.trace("Using cached response {}", response);
            }

            if (response.equals(EMPTY_STRING)) {
                return;
            }

            wifiSwitchStatus = VeSyncConstants.GSON.fromJson(response, VeSyncV2BypassWifiSwitchStatus.class);

            if (wifiSwitchStatus == null) {
                return;
            }

            if (!cachedDataUsed) {
                cachedResponse.putValue(response);
            }
        }
    }

    @Override
    public @NonNull List<VeSyncDeviceMetadata> getSupportedDeviceMetadata() {
        return SUPPORTED_MODEL_FAMILIES;
    }
}
