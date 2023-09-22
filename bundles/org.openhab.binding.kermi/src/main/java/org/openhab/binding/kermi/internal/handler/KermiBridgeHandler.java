package org.openhab.binding.kermi.internal.handler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.kermi.internal.KermiBridgeConfiguration;
import org.openhab.binding.kermi.internal.KermiCommunicationException;
import org.openhab.binding.kermi.internal.api.KermiHttpUtil;
import org.openhab.binding.kermi.internal.model.KermiSiteInfo;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class KermiBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(KermiBridgeHandler.class);
    private static final int DEFAULT_REFRESH_PERIOD = 10;
    private final Set<KermiBaseThingHandler> services = new HashSet<>();
    private @Nullable ScheduledFuture<?> refreshJob;

    private KermiHttpUtil httpUtil;
    private KermiSiteInfo kermiSiteInfo;

    public KermiBridgeHandler(Bridge bridge, KermiHttpUtil httpUtil, KermiSiteInfo kermiSiteInfo) {
        super(bridge);
        this.httpUtil = httpUtil;
        this.kermiSiteInfo = kermiSiteInfo;
    }

    @Override
    public void initialize() {
        final KermiBridgeConfiguration config = getConfigAs(KermiBridgeConfiguration.class);

        boolean validConfig = true;
        String errorMsg = null;

        String hostname = config.hostname;
        if (hostname == null || hostname.isBlank()) {
            errorMsg = "Parameter 'hostname' is mandatory and must be configured";
            validConfig = false;
        }
        String password = config.password;
        if (password == null || password.isBlank()) {
            errorMsg = "Parameter 'password' is mandatory and must be configured";
            validConfig = false;
        }

        if (config.refreshInterval != null && config.refreshInterval <= 0) {
            errorMsg = "Parameter 'refresh' must be at least 1 second";
            validConfig = false;
        }

        if (validConfig) {
            httpUtil.setHostname(config.hostname);
            httpUtil.setPassword(config.password);
            startAutomaticRefresh();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        }
    }

    @Override
    public void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        System.out.println("hallo");
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof KermiBaseThingHandler) {
            this.services.add((KermiBaseThingHandler) childHandler);
            restartAutomaticRefresh();
        } else {
            logger.debug("Child handler {} not added because it is not an instance of KermiBaseThingHandler",
                    childThing.getUID().getId());
        }
    }

    private void restartAutomaticRefresh() {
        if (refreshJob != null) { // refreshJob should be null if the config isn't valid
            refreshJob.cancel(false);
            startAutomaticRefresh();
        }
    }

    private void startAutomaticRefresh() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            final KermiBridgeConfiguration config = getConfigAs(KermiBridgeConfiguration.class);
            Runnable runnable = () -> {
                try {
                    checkBridgeOnline(config);
                    if (getThing().getStatus() != ThingStatus.ONLINE) {
                        updateStatus(ThingStatus.ONLINE);
                    }
                    for (KermiBaseThingHandler service : services) {
                        service.refresh(config);
                    }
                } catch (KermiCommunicationException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
                    kermiSiteInfo.clearSiteInfo();
                }
            };

            int delay = (config.refreshInterval != null) ? config.refreshInterval.intValue() : DEFAULT_REFRESH_PERIOD;
            refreshJob = scheduler.scheduleWithFixedDelay(runnable, 1, delay, TimeUnit.SECONDS);
        }

    }

    private void checkBridgeOnline(KermiBridgeConfiguration config) throws KermiCommunicationException {
        httpUtil.executeCheckBridgeOnline();
    }

}
