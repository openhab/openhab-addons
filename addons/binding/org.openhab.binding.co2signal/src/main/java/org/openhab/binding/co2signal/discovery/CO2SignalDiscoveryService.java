package org.openhab.binding.co2signal.discovery;

import static org.openhab.binding.co2signal.CO2SignalBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.i18n.LocationProvider;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CO2SignalDiscoveryService} creates things based on the configured location.
 *
 * @author Jens Viebig - Initial Contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.co2signal")
public class CO2SignalDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(CO2SignalDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 30;
    private static final int LOCATION_CHANGED_CHECK_INTERVAL = 60;
    private LocationProvider locationProvider;
    private ScheduledFuture<?> co2SignalDiscoveryJob;
    private PointType previousLocation;

    private static final ThingUID co2Thing = new ThingUID(THING_TYPE_CO2, LOCAL);

    /**
     * Creates a CO2SignalDiscoveryService with enabled auto start.
     */
    public CO2SignalDiscoveryService() {
        super(new HashSet<>(Arrays.asList(new ThingTypeUID(BINDING_ID, "-"))), DISCOVER_TIMEOUT_SECONDS, true);
    }

    @Override
    protected void activate(Map<String, Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    protected void modified(Map<String, Object> configProperties) {
        super.modified(configProperties);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting CO2Signal discovery scan");
        PointType location = locationProvider.getLocation();
        if (location == null) {
            logger.debug("LocationProvider.getLocation() is not set -> Will not provide any discovery results");
            return;
        }
        createResults(location);
    }

    @Override
    protected void startBackgroundDiscovery() {
        if (co2SignalDiscoveryJob == null) {
            co2SignalDiscoveryJob = scheduler.scheduleWithFixedDelay(() -> {
                PointType currentLocation = locationProvider.getLocation();
                if (!Objects.equals(currentLocation, previousLocation)) {
                    logger.debug("Location has been changed from {} to {}: Creating new discovery results",
                            previousLocation, currentLocation);
                    createResults(currentLocation);
                    previousLocation = currentLocation;
                }
            }, 0, LOCATION_CHANGED_CHECK_INTERVAL, TimeUnit.SECONDS);
            logger.debug("Scheduled CO2Signal location-changed job every {} seconds", LOCATION_CHANGED_CHECK_INTERVAL);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping CO2Signal background discovery");
        if (co2SignalDiscoveryJob != null && !co2SignalDiscoveryJob.isCancelled()) {
            if (co2SignalDiscoveryJob.cancel(true)) {
                co2SignalDiscoveryJob = null;
                logger.debug("Stopped CO2Signal background discovery");
            }
        }
    }

    public void createResults(PointType location) {
        String propGeolocation;
        propGeolocation = String.format("%s,%s", location.getLatitude(), location.getLongitude());
        thingDiscovered(DiscoveryResultBuilder.create(co2Thing).withLabel("Local Electricity CO2 Footprint")
                .withProperty("location", propGeolocation).build());
    }

    @Reference
    protected void setLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    protected void unsetLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = null;
    }

}