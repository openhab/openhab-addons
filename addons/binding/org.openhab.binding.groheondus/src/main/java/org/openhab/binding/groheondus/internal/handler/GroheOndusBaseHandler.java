package org.openhab.binding.groheondus.internal.handler;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.grohe.ondus.api.OndusService;
import org.grohe.ondus.api.model.BaseAppliance;
import org.grohe.ondus.api.model.Location;
import org.grohe.ondus.api.model.Room;
import org.openhab.binding.groheondus.internal.GroheOndusApplianceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GroheOndusBaseHandler<T extends BaseAppliance> extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(GroheOndusSenseGuardHandler.class);
    
    protected @Nullable GroheOndusApplianceConfiguration config;

    public GroheOndusBaseHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(GroheOndusApplianceConfiguration.class);

        OndusService ondusService = getOndusService();
        if (ondusService == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "No initialized OndusService available from bridge.");
            return;
        }

        T appliance = getAppliance(ondusService);
        if (appliance == null) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR, "Could not load appliance");
            return;
        }
        int pollingInterval = getPollingInterval(appliance);
        scheduler.scheduleAtFixedRate(this::updateChannels, 0, pollingInterval, TimeUnit.SECONDS);

        updateStatus(ThingStatus.UNKNOWN);
    }

    public @Nullable OndusService getOndusService() {
        if (getBridge() == null) {
            return null;
        }
        if (getBridge().getHandler() == null) {
            return null;
        }
        if (!(getBridge().getHandler() instanceof GroheOndusAccountHandler)) {
            return null;
        }
        try {
            return ((GroheOndusAccountHandler) getBridge().getHandler()).getService();
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return null;
        }
    }

    protected Room getRoom() {
        return new Room(config.roomId, getLocation());
    }

    protected Location getLocation() {
        return new Location(config.locationId);
    }

    protected @Nullable T getAppliance(@NonNull OndusService ondusService) {
        try {
            BaseAppliance appliance = ondusService.getAppliance(getRoom(), config.applianceId).orElse(null);
            if (appliance.getType() != getType()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Thing is not a GROHE SENSE Guard device.");
                return null;
            }
            return (T) appliance;
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.debug("Could not load appliance", e);
        }
        return null;
    }

    protected abstract int getPollingInterval(T appliance);

    public abstract void updateChannels();

    protected abstract int getType();
}
