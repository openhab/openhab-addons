package org.openhab.binding.evcc.internal.discovery;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.evcc.internal.EvccBindingConstants;
import org.openhab.binding.evcc.internal.discovery.mapper.BatteryDiscoveryMapper;
import org.openhab.binding.evcc.internal.discovery.mapper.EvccDiscoveryMapper;
import org.openhab.binding.evcc.internal.discovery.mapper.LoadpointDiscoveryMapper;
import org.openhab.binding.evcc.internal.discovery.mapper.SiteDiscoveryMapper;
import org.openhab.binding.evcc.internal.discovery.mapper.VehicleDiscoveryMapper;
import org.openhab.binding.evcc.internal.handler.EvccBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingTypeUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = EvccDiscoveryService.class, configurationPid = "discovery.evcc")
public class EvccDiscoveryService extends AbstractThingHandlerDiscoveryService<EvccBridgeHandler> {

    private static final int TIMEOUT = 5;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<EvccDiscoveryMapper> mappers = List.of(new LoadpointDiscoveryMapper(),
            new VehicleDiscoveryMapper(), new BatteryDiscoveryMapper(), new SiteDiscoveryMapper());

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(EvccBindingConstants.THING_TYPE_LOADPOINT,
            EvccBindingConstants.THING_TYPE_VEHICLE, EvccBindingConstants.THING_TYPE_BATTERY,
            EvccBindingConstants.THING_TYPE_SITE, EvccBindingConstants.THING_TYPE_PV);

    public EvccDiscoveryService() {
        super(EvccBridgeHandler.class, SUPPORTED_THING_TYPES, TIMEOUT, false);
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
        thingHandler.fetchEvccState().ifPresent(state -> {
            for (EvccDiscoveryMapper mapper : mappers) {
                Collection<DiscoveryResult> results = mapper.discover(state, thingHandler);
                results.forEach(this::thingDiscovered);
            }
        });
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }
}
