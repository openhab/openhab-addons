package org.openhab.binding.fronius.handler;

import static org.openhab.binding.fronius.FroniusBindingConstants.*;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.fronius.internal.configuration.ServiceConfiguration;
import org.openhab.binding.fronius.internal.configuration.ServiceConfigurationFactory;
import org.openhab.binding.fronius.internal.model.ActiveDeviceInfo;
import org.openhab.binding.fronius.internal.model.StorageRealtimeData;
import org.openhab.binding.fronius.internal.service.InterverRealtimeDataService;
import org.openhab.binding.fronius.internal.service.StorageRealtimeDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FroniusSymoHybrid extends FroniusHandler {

    private final Logger logger = LoggerFactory.getLogger(FroniusSymoHybrid.class);
    private StorageRealtimeDataService storageRealtimeDataService;

    public FroniusSymoHybrid(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        ActiveDeviceInfo activeDeviceInfo = activeDeviceInfoService.getData();

        // if there are more than one inverters, the device must be set in configuration
        if (activeDeviceInfo.inverterCount() > 1) {
            final int device = handlerConfiguration.device.intValue();
            if (activeDeviceInfo.inverters().contains(device)) {
                logger.debug("Found inverter device from cluster: {}", device);
                final ServiceConfiguration configuration = new ServiceConfigurationFactory()
                        .createConnectionConfiguration(handlerConfiguration, device);
                interverRealtimeDataService = new InterverRealtimeDataService(configuration);
            }
        } else if (activeDeviceInfo.inverterCount() > 0) {
            final int device = activeDeviceInfo.inverters().iterator().next();
            logger.debug("Found single inverter device: {}", device);
            final ServiceConfiguration configuration = new ServiceConfigurationFactory()
                    .createConnectionConfiguration(handlerConfiguration, device);
            interverRealtimeDataService = new InterverRealtimeDataService(configuration);
        }

        if (activeDeviceInfo.storageCount() > 0) {
            final int device = activeDeviceInfo.storages().iterator().next();
            logger.debug("Found storage device: {}", device);
            final ServiceConfiguration configuration = new ServiceConfigurationFactory()
                    .createConnectionConfiguration(handlerConfiguration, device);
            storageRealtimeDataService = new StorageRealtimeDataService(configuration);
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        };

        startAutomaticRefresh(runnable);
    }

    @Override
    protected void refresh() {
        super.refresh();
        refreshStorageData();
    }

    private void refreshStorageData() {
        if (storageRealtimeDataService != null) {
            StorageRealtimeData data = storageRealtimeDataService.getData();
            if (data.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            } else {
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_STORAGE_CURRENT), data.getCurrent());
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_STORAGE_VOLTAGE), data.getVoltage());
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_STORAGE_CHARGE), data.getCharge());
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_STORAGE_CAPACITY), data.getCapacity());
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_STORAGE_TEMPERATURE), data.getTemperature());
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_STORAGE_CODE), data.getCode());
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_STORAGE_TIMESTAMP), data.getTimestamp());
            }
        }
    }
}
