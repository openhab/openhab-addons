package org.openhab.binding.boschspexor.internal.discovery;

import static org.openhab.binding.boschspexor.internal.BoschSpexorBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschspexor.internal.api.model.Spexor;
import org.openhab.binding.boschspexor.internal.api.service.SpexorBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class BoschSpexorDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {
    private final Logger logger = LoggerFactory.getLogger(BoschSpexorDiscoveryService.class);

    private static final int DISCOVERY_TIME_SECONDS = 10;
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(SPEXOR_THING_TYPE);

    private @NonNullByDefault({}) SpexorBridgeHandler bridgeHandler;
    private @NonNullByDefault({}) ThingUID bridgeUID;

    private @Nullable ScheduledFuture<?> backgroundFuture;

    public BoschSpexorDiscoveryService(int timeout) throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIME_SECONDS);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public void activate() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, Boolean.TRUE);
        super.activate(properties);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        bridgeHandler.setDiscoveryService(null);
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof SpexorBridgeHandler) {
            bridgeHandler = (SpexorBridgeHandler) handler;
            bridgeUID = bridgeHandler.getUID();
            bridgeHandler.setDiscoveryService(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    protected synchronized void startBackgroundDiscovery() {
        stopBackgroundDiscovery();
        backgroundFuture = scheduler.scheduleWithFixedDelay(this::startScan, BACKGROUND_SCAN_REFRESH_MINUTES,
                BACKGROUND_SCAN_REFRESH_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    protected synchronized void stopBackgroundDiscovery() {
        if (backgroundFuture != null) {
            backgroundFuture.cancel(true);
            backgroundFuture = null;
        }
    }

    @Override
    protected void startScan() {
        if (bridgeHandler.isAuthorized()) {
            logger.debug("Starting spexor discovery for bridge {}", bridgeUID);
            bridgeHandler.listSpexors().forEach(this::thingDiscovered);
        }
    }

    private void thingDiscovered(Spexor spexor) {
        Map<String, Object> properties = new HashMap<>();

        properties.put(PROPERTY_SPEXOR_NAME, spexor.getName());
        properties.put(PROPERTY_SPEXOR_ID, spexor.getId());
        ThingUID thing = new ThingUID(SPEXOR_THING_TYPE, bridgeUID, spexor.getId());

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thing).withBridge(bridgeUID)
                .withProperties(properties).withRepresentationProperty(PROPERTY_SPEXOR_NAME).withLabel(spexor.getName())
                .build();

        thingDiscovered(discoveryResult);
    }
}
