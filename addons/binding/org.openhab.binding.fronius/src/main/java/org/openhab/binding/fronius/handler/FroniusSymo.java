package org.openhab.binding.fronius.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.fronius.internal.configuration.ServiceConfiguration;
import org.openhab.binding.fronius.internal.configuration.ServiceConfigurationFactory;
import org.openhab.binding.fronius.internal.model.ActiveDeviceInfo;
import org.openhab.binding.fronius.internal.service.InterverRealtimeDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FroniusSymo extends FroniusHandler {

    private final Logger logger = LoggerFactory.getLogger(FroniusSymo.class);

    public FroniusSymo(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        ActiveDeviceInfo activeDeviceInfo = activeDeviceInfoService.getData();

        // if there are more than one inverters, the device must be set in configuration
        if (activeDeviceInfo.inverterCount() > 1) {
            int device = handlerConfiguration.device.intValue();
            if (activeDeviceInfo.inverters().contains(device)) {
                logger.debug("Found inverter device from cluster: {}", device);
                ServiceConfiguration configuration = new ServiceConfigurationFactory()
                        .createConnectionConfiguration(handlerConfiguration, device);
                interverRealtimeDataService = new InterverRealtimeDataService(configuration);
            }
        } else if (activeDeviceInfo.inverterCount() > 0) {
            int device = activeDeviceInfo.inverters().iterator().next();
            logger.debug("Found single inverter device: {}", device);
            ServiceConfiguration configuration = new ServiceConfigurationFactory()
                    .createConnectionConfiguration(handlerConfiguration, device);
            interverRealtimeDataService = new InterverRealtimeDataService(configuration);
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        };

        startAutomaticRefresh(runnable);
    }
}
