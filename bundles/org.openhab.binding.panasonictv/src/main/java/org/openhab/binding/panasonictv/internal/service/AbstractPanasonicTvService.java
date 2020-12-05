/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.panasonictv.internal.service;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.xml.bind.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.panasonictv.internal.StatusEventDTO;
import org.openhab.binding.panasonictv.internal.api.PanasonicEventListener;
import org.openhab.binding.panasonictv.internal.api.PanasonicTvService;
import org.openhab.core.io.transport.upnp.UpnpIOParticipant;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractPanasonicTvService} is responsible for
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractPanasonicTvService implements UpnpIOParticipant, PanasonicTvService {
    private final Logger logger = LoggerFactory.getLogger(AbstractPanasonicTvService.class);

    protected final PanasonicEventListener listener;
    protected final String udn;
    private final String serviceId;
    private final String serviceName;
    private final Map<String, List<MediaRendererService.ChannelConverter>> converters;
    private final Set<String> supportedCommands;
    private final ScheduledExecutorService scheduler;
    private final int refreshInterval;

    private final Unmarshaller eventUnmarshaller;

    protected final UpnpIOService service;

    protected final Map<String, String> stateMap = new ConcurrentHashMap<>();

    private @Nullable ScheduledFuture<?> pollingJob;

    protected boolean eventsSubscribed = false;

    public AbstractPanasonicTvService(String udn, UpnpIOService service, PanasonicEventListener listener,
            ScheduledExecutorService scheduler, int refreshInterval, String serviceName, String serviceId,
            Set<String> supportedCommands, Map<String, List<MediaRendererService.ChannelConverter>> converters) {
        this.udn = udn;
        this.service = service;
        this.listener = listener;
        this.scheduler = scheduler;
        this.refreshInterval = refreshInterval;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.converters = converters;
        this.supportedCommands = supportedCommands;

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(StatusEventDTO.class);
            eventUnmarshaller = jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            logger.warn("Could not create unmarshaller for events: {}", e.getMessage());
            throw new IllegalStateException();
        }
    }

    @Override
    public void onServiceSubscribed(@Nullable String service, boolean succeeded) {
        logger.trace("Subscribed to service {} : {}", service, succeeded);
        if (serviceId.equals(service)) {
            eventsSubscribed = succeeded;
        }
    }

    @Override
    public void onStatusChanged(boolean status) {
        logger.debug("{} changed status: {}", serviceName, status);
    }

    @Override
    public String getUDN() {
        return udn;
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        logger.trace("Received: service='{}', variable='{}', value='{}'", service, variable, value);
        if ("LastChange".equals(variable) && value != null) {
            try {
                StatusEventDTO statusEvent = (StatusEventDTO) eventUnmarshaller.unmarshal(new StringReader(value));
                logger.debug("Extracted: {}", statusEvent);
            } catch (JAXBException e) {
                // TODO: ignore for now while we are still testing
            }
        }

        List<MediaRendererService.ChannelConverter> converters = this.converters.get(variable);

        if (variable == null || value == null || converters == null) {
            return;
        }

        // put returns previous value or null if no previous value
        if (value.equals(stateMap.put(variable, value))) {
            logger.trace("Value '{}' for {} hasn't changed, ignoring update", value, variable);
            return;
        }

        converters.forEach(converter -> listener.valueReceived(converter.channelName, converter.function.apply(value)));
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public Set<String> getSupportedChannelNames() {
        return supportedCommands;
    }

    @Override
    public abstract void handleCommand(String channel, Command command);

    @Override
    public void clearCache() {
        stateMap.clear();
    }

    static class ChannelConverter {
        public String channelName;
        public Function<String, ? extends State> function;

        public ChannelConverter(String channelName, Function<String, ? extends State> function) {
            this.channelName = channelName;
            this.function = function;
        }
    }

    @Override
    public void start() {
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            stop();
        }
        logger.debug("Start refresh task for service {}, interval={}", serviceName, refreshInterval);
        this.pollingJob = scheduler.scheduleWithFixedDelay(this::internalPolling, 0, refreshInterval,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
            this.pollingJob = null;
        }
        service.removeSubscription(this, serviceId);
    }

    protected boolean isRegistered() {
        return service.isRegistered(this);
    }

    protected void reportError(String message, @Nullable Throwable e) {
        listener.reportError(ThingStatusDetail.COMMUNICATION_ERROR, message, e);
    }

    private void internalPolling() {
        logger.trace("polling {}", serviceName);
        if (isRegistered()) {
            if (!eventsSubscribed) {
                service.addSubscription(this, serviceId, 600);
            }
            polling();
        } else {
            logger.debug("Service {} not registered, skipping polling, trying to register.", serviceName);
            reportError("UPnP device registration not found", null);
            service.registerParticipant(this);
        }
    }

    protected abstract void polling();

    protected void updateResourceState(String serviceId, String actionId, Map<String, String> inputs) {
        logger.trace("Invoking serviceId='{}', actionId='{}', inputs='{}'", serviceId, actionId, inputs);
        service.invokeAction(this, serviceId, actionId, inputs).forEach((k, v) -> onValueReceived(k, v, serviceId));
    }
}
