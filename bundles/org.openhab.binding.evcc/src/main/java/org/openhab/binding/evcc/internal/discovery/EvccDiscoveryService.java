package org.openhab.binding.evcc.internal.discovery;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.evcc.internal.discovery.mapper.BatteryDiscoveryMapper;
import org.openhab.binding.evcc.internal.discovery.mapper.EvccDiscoveryMapper;
import org.openhab.binding.evcc.internal.discovery.mapper.LoadpointDiscoveryMapper;
import org.openhab.binding.evcc.internal.discovery.mapper.SiteDiscoveryMapper;
import org.openhab.binding.evcc.internal.discovery.mapper.VehicleDiscoveryMapper;
import org.openhab.binding.evcc.internal.handler.EvccBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = EvccDiscoveryService.class, configurationPid = "discovery.evcc")
public class EvccDiscoveryService extends AbstractThingHandlerDiscoveryService<EvccBridgeHandler> {

    private static final int TIMEOUT = 5;
    private static final int SCAN_INTERVAL = 5; // We scan every 5 seconds since we are using the cached response

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<EvccDiscoveryMapper> mappers = List.of(new LoadpointDiscoveryMapper(),
            new VehicleDiscoveryMapper(), new BatteryDiscoveryMapper(), new SiteDiscoveryMapper());

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_LOADPOINT, THING_TYPE_VEHICLE,
            THING_TYPE_BATTERY, THING_TYPE_SITE, THING_TYPE_PV, THING_TYPE_HEATING);

    private @Nullable ScheduledFuture<?> evccDiscoveryJob;

    public EvccDiscoveryService() {
        super(EvccBridgeHandler.class, SUPPORTED_THING_TYPES, TIMEOUT, true);
    }

    @Override
    public void activate() {
        super.activate();
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    protected void startScan() {
        logger.debug("Starte EVCC Discovery…");
        thingHandler.getCachedEvccState().ifPresent(state -> {
            for (EvccDiscoveryMapper mapper : mappers) {
                mapper.discover(state, thingHandler).forEach(thing -> {
                    logger.debug("Thing discovered {}", thing);
                    thingDiscovered(thing);
                });
            }
        });
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start evcc background discovery");
        boolean setUpBackgroundScan = false;
        if (evccDiscoveryJob == null) {
            setUpBackgroundScan = true;
        } else {
            setUpBackgroundScan = evccDiscoveryJob.isCancelled();
        }
        if (setUpBackgroundScan) {
            evccDiscoveryJob = scheduler.scheduleWithFixedDelay(() -> startScan(), 0, SCAN_INTERVAL, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop WeMo device background discovery");
        Optional.ofNullable(evccDiscoveryJob).ifPresent(backgroundScan -> {
            backgroundScan.cancel(isBackgroundDiscoveryEnabled());
        });
        evccDiscoveryJob = null;
    }
}
