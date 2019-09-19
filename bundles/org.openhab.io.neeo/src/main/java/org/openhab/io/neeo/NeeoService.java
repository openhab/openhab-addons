/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.neeo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.binding.BindingInfoRegistry;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemStateChangedEvent;
import org.eclipse.smarthome.core.net.CidrAddress;
import org.eclipse.smarthome.core.net.NetworkAddressChangeListener;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.type.ChannelTypeRegistry;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.eclipse.smarthome.io.transport.mdns.MDNSClient;
import org.openhab.io.neeo.internal.AbstractServlet;
import org.openhab.io.neeo.internal.NeeoApi;
import org.openhab.io.neeo.internal.NeeoBrainServlet;
import org.openhab.io.neeo.internal.NeeoConstants;
import org.openhab.io.neeo.internal.NeeoDashboardServlet;
import org.openhab.io.neeo.internal.NeeoUtil;
import org.openhab.io.neeo.internal.ServiceContext;
import org.openhab.io.neeo.internal.discovery.BrainDiscovery;
import org.openhab.io.neeo.internal.discovery.DiscoveryListener;
import org.openhab.io.neeo.internal.discovery.MdnsBrainDiscovery;
import org.openhab.io.neeo.internal.models.NeeoSystemInfo;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main entry point for the integration service. The integration service will listen for brain broadcasts, connect
 * to the brain and handle communication with the brain (in addition to starting up the dashboard tile)
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
@Component(service = EventSubscriber.class, immediate = true, property = {
        "service.pid=org.openhab.io.neeo.NeeoService", "service.config.description.uri=io:neeo",
        "service.config.label=NEEO Integration", "service.config.category=io" })
public class NeeoService implements EventSubscriber, NetworkAddressChangeListener {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoService.class);

    /**
     * This is the context created in the activate method (and nulled in the deactivate method) that will provide the
     * context to all services for all servlets
     */
    private @Nullable ServiceContext context;

    // The following services are set by openHAB via the getter/setters
    private @Nullable HttpService httpService;
    private @Nullable ItemRegistry itemRegistry;
    private @Nullable BindingInfoRegistry bindingInfoRegistry;
    private @Nullable ThingRegistry thingRegistry;
    private @Nullable ThingTypeRegistry thingTypeRegistry;
    private @Nullable ItemChannelLinkRegistry itemChannelLinkRegistry;
    private @Nullable ChannelTypeRegistry channelTypeRegistry;
    private @Nullable MDNSClient mdnsClient;
    private @Nullable EventPublisher eventPublisher;
    private @Nullable NetworkAddressService networkAddressService;

    /** The main dashboard servlet. Only created in the activate method (and disposed of in the deactivate method) */
    private @Nullable NeeoDashboardServlet dashboardServlet;

    /**
     * The various servlets being used (should be one per brain + the status one)
     */
    private final List<NeeoBrainServlet> servlets = new CopyOnWriteArrayList<>();

    /** The brain discovery service */
    private @Nullable BrainDiscovery discovery;

    /** The discovery listener to the brain discovery service */
    private final DiscoveryListener discoveryListener = new DiscoveryListener() {

        @Override
        public void discovered(NeeoSystemInfo sysInfo, InetAddress ipAddress) {
            Objects.requireNonNull(sysInfo, "sysInfo cannot be null");
            Objects.requireNonNull(ipAddress, "ipAddress cannot be null");

            brainDiscovered(sysInfo, ipAddress);
        }

        @Override
        public void removed(NeeoSystemInfo sysInfo) {
            Objects.requireNonNull(sysInfo, "sysInfo cannot be null");

            brainRemoved(sysInfo);
        }

        @Override
        public void updated(NeeoSystemInfo sysInfo, InetAddress oldIpAddress, InetAddress newIpAddress) {
            Objects.requireNonNull(sysInfo, "sysInfo cannot be null");
            Objects.requireNonNull(oldIpAddress, "oldIpAddress cannot be null");
            Objects.requireNonNull(newIpAddress, "newIpAddress cannot be null");

            removed(sysInfo);
            discovered(sysInfo, newIpAddress);
        }
    };

    /**
     * The event filter to apply to this service
     */
    private final EventFilter eventFilter = event -> {
        logger.trace("apply: {}", event);

        for (NeeoBrainServlet ns : servlets) {
            final List<EventFilter> efs = ns.getEventFilters();
            if (efs != null) {
                for (EventFilter ef : efs) {
                    if (ef.apply(event)) {
                        logger.trace("apply (true): {}", event);
                        return true;
                    }
                }
            }
        }

        logger.trace("apply (false): {}", event);
        return false;
    };

    /**
     * Sets the http service.
     *
     * @param httpService the non-null http service
     */
    @Reference
    public void setHttpService(HttpService httpService) {
        Objects.requireNonNull(httpService, "httpService cannot be null");
        this.httpService = httpService;
    }

    /**
     * Unset http service.
     *
     * @param httpService the http service (ignored)
     */
    public void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    /**
     * Sets the item registry.
     *
     * @param itemRegistry the non-null item registry
     */
    @Reference
    public void setItemRegistry(ItemRegistry itemRegistry) {
        Objects.requireNonNull(itemRegistry, "itemRegistry cannot be null");
        this.itemRegistry = itemRegistry;
    }

    /**
     * Unset item registry.
     *
     * @param itemRegistry the item registry (ignored)
     */
    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    /**
     * Sets the binding info registry.
     *
     * @param bindingInfoRegistry the non-null binding info registry
     */
    @Reference
    public void setBindingInfoRegistry(BindingInfoRegistry bindingInfoRegistry) {
        Objects.requireNonNull(bindingInfoRegistry, "bindingInfoRegistry cannot be null");
        this.bindingInfoRegistry = bindingInfoRegistry;
    }

    /**
     * Unset binding info registry.
     *
     * @param bindingInfoRegistry the binding info registry (ignored)
     */
    public void unsetBindingInfoRegistry(BindingInfoRegistry bindingInfoRegistry) {
        this.bindingInfoRegistry = null;
    }

    /**
     * Sets the thing registry.
     *
     * @param thingRegistry the non-null thing registry
     */
    @Reference
    public void setThingRegistry(ThingRegistry thingRegistry) {
        Objects.requireNonNull(thingRegistry, "thingRegistry cannot be null");
        this.thingRegistry = thingRegistry;
    }

    /**
     * Unset thing registry.
     *
     * @param thingRegistry the thing registry (ignored)
     */
    public void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }

    /**
     * Sets the thing type registry.
     *
     * @param thingTypeRegistry the non-null thing type registry
     */
    @Reference
    public void setThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        Objects.requireNonNull(thingTypeRegistry, "thingTypeRegistry cannot be null");
        this.thingTypeRegistry = thingTypeRegistry;
    }

    /**
     * Unset thing type registry.
     *
     * @param thingTypeRegistry the thing type registry (ignored)
     */
    public void unsetThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = null;
    }

    /**
     * Sets the item channel link registry.
     *
     * @param itemChannelLinkRegistry the non-null item channel link registry
     */
    @Reference
    public void setItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        Objects.requireNonNull(itemChannelLinkRegistry, "itemChannelLinkRegistry cannot be null");
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    /**
     * Unset item channel link registry.
     *
     * @param itemChannelLinkRegistry the item channel link registry (ignored)
     */
    public void unsetItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = null;
    }

    /**
     * Sets the channel type registry.
     *
     * @param channelTypeRegistry the non-null channel type registry
     */
    @Reference
    public void setChannelTypeRegistry(ChannelTypeRegistry channelTypeRegistry) {
        Objects.requireNonNull(channelTypeRegistry, "channelTypeRegistry cannot be null");
        this.channelTypeRegistry = channelTypeRegistry;
    }

    /**
     * Unset channel type registry.
     *
     * @param channelTypeRegistry the channel type registry (ignored)
     */
    public void unsetChannelTypeRegistry(ChannelTypeRegistry channelTypeRegistry) {
        this.channelTypeRegistry = null;
    }

    /**
     * Sets the MDNS client.
     *
     * @param mdnsClient the non-null MDNS client
     */
    @Reference
    public void setMDNSClient(MDNSClient mdnsClient) {
        Objects.requireNonNull(mdnsClient, "mdnsClient cannot be null");
        this.mdnsClient = mdnsClient;
    }

    /**
     * Unset MDNS client.
     *
     * @param mdnsClient the mdns client (ignored)
     */
    public void unsetMDNSClient(MDNSClient mdnsClient) {
        this.mdnsClient = null;
    }

    /**
     * Sets the event publisher.
     *
     * @param eventPublisher the new event publisher
     */
    @Reference
    public void setEventPublisher(EventPublisher eventPublisher) {
        Objects.requireNonNull(eventPublisher, "eventPublisher cannot be null");
        this.eventPublisher = eventPublisher;
    }

    /**
     * Unset event publisher.
     *
     * @param eventPublisher the event publisher (ignored)
     */
    public void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    /**
     * Sets the network address service
     *
     * @param networkAddressService the network address service
     */
    @Reference
    public void setNetworkAddressService(NetworkAddressService networkAddressService) {
        Objects.requireNonNull(networkAddressService, "networkAddressService cannot be null");
        this.networkAddressService = networkAddressService;
        networkAddressService.addNetworkAddressChangeListener(this);
    }

    /**
     * Unsets network address service
     *
     * @param networkAddressService address service
     */
    public void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
        networkAddressService.removeNetworkAddressChangeListener(this);
        this.networkAddressService = null;
    }

    /**
     * Activates this service. The activation will start up the brain discovery service and register the dashboard tile
     *
     * @param componentContext the non-null component context
     */
    @Activate
    public void activate(final ComponentContext componentContext) {
        Objects.requireNonNull(componentContext, "componentContext cannot be null");

        logger.debug("Neeo Service activated");
        final ServiceContext localContext = new ServiceContext(componentContext, validate(httpService, "httpService"),
                validate(itemRegistry, "itemRegistry"), validate(bindingInfoRegistry, "bindingInfoRegistry"),
                validate(thingRegistry, "thingRegistry"), validate(thingTypeRegistry, "thingTypeRegistry"),
                validate(itemChannelLinkRegistry, "itemChannelLinkRegistry"),
                validate(channelTypeRegistry, "channelTypeRegistry"), validate(mdnsClient, "mdnsClient"),
                validate(eventPublisher, "eventPublisher"), validate(networkAddressService, "networkAddressService"));

        context = localContext;
        discovery = new MdnsBrainDiscovery(localContext);
        discovery.addListener(discoveryListener);

        try {
            final String servletUrl = NeeoConstants.WEBAPP_PREFIX + NeeoConstants.WEBAPP_DASHBOARD_PREFIX;
            dashboardServlet = new NeeoDashboardServlet(this, servletUrl, localContext);

            localContext.getHttpService().registerServlet(servletUrl, dashboardServlet, new Hashtable<>(),
                    localContext.getHttpService().createDefaultHttpContext());

            localContext.getHttpService().registerResources(NeeoConstants.WEBAPP_PREFIX, "web",
                    localContext.getHttpService().createDefaultHttpContext());
            logger.debug("Started NEEO Dashboard tile at {}", NeeoConstants.WEBAPP_PREFIX);
        } catch (ServletException | NamespaceException e) {
            logger.debug("Exception starting status servlet: {}", e.getMessage(), e);
        }

        // Start discovery and re-discover those that we already found
        BrainDiscovery localDiscovery = discovery;
        if (localDiscovery != null) {
            localDiscovery.startDiscovery();
        }
    }

    /**
     * Helper method to validate that the specific item wasn't null and convert it's type to a non-nullable type
     *
     * @param t the type to validate
     * @param name a non-null, non-empty name
     * @return the non-null type
     */
    private static <T> T validate(@Nullable T t, String name) {
        if (t == null) {
            throw new IllegalStateException(name + " was not instantiated");
        }
        return t;
    }

    /**
     * Deactivate the service
     *
     * @param componentContext the component context (ignored)
     */
    @Deactivate
    public void deactivate(ComponentContext componentContext) {
        logger.debug("Neeo Service deactivated");

        final BrainDiscovery localDiscovery = discovery;
        if (localDiscovery != null) {
            localDiscovery.removeListener(discoveryListener);
            localDiscovery.close();
        }

        final ServiceContext localContext = context;
        if (localContext != null) {
            localContext.getDefinitions().save();

            final HttpService service = localContext.getHttpService();
            for (NeeoBrainServlet servlet : servlets) {
                service.unregister(NeeoUtil.getServletUrl(servlet.getBrainStatus().getBrainId()));
                NeeoUtil.close(servlet);
            }
            servlets.clear();

            context = null;
        }

        if (dashboardServlet != null) {
            dashboardServlet.close();
            dashboardServlet = null;
        }

        logger.debug("Stopped NEEO Listener");
    }

    /**
     * Called when a brain is discovered. This method will start a new servlet for the brains hostname.
     *
     * @param sysInfo the non-null {@link NeeoSystemInfo}
     * @param ipAddress the non-null ip address of the brain
     */
    private void brainDiscovered(NeeoSystemInfo sysInfo, InetAddress ipAddress) {
        Objects.requireNonNull(sysInfo, "sysInfo cannot be null");
        Objects.requireNonNull(ipAddress, "ipAddress cannot be null");

        final ServiceContext localContext = context;
        if (localContext != null) {
            final String servletUrl = NeeoUtil.getServletUrl(sysInfo.getHostname());

            if (getServletByUrl(servletUrl) == null) {
                logger.debug("Brain discovered: {} at {} and starting servlet at {}", sysInfo.getHostname(), ipAddress,
                        servletUrl);
                try {
                    final NeeoBrainServlet newServlet = NeeoBrainServlet.create(localContext, servletUrl,
                            sysInfo.getHostname(), ipAddress);
                    servlets.add(newServlet);

                    localContext.getHttpService().registerServlet(servletUrl, newServlet,
                            new Hashtable<String, String>(), localContext.getHttpService().createDefaultHttpContext());
                    logger.debug("Started NEEO Listener at {}", servletUrl);
                } catch (NamespaceException | ServletException | IOException e) {
                    logger.error("Error during servlet startup", e);
                }
            } else {
                logger.debug("Brain servlet with URL of {} already exists - ignored", servletUrl);
            }
        }
    }

    /**
     * Called when the brain has been removed. This method will remove/stop the associated servlet.
     *
     * @param sysInfo the non-null {@link NeeoSystemInfo}
     */
    private void brainRemoved(NeeoSystemInfo sysInfo) {
        Objects.requireNonNull(sysInfo, "sysInfo cannot be null");

        final String servletUrl = NeeoUtil.getServletUrl(sysInfo.getHostname());

        final NeeoBrainServlet servlet = getServletByUrl(servletUrl);
        final ServiceContext localContext = context;

        if (servlet == null || localContext == null) {
            logger.debug("Tried to remove a servlet for {} but none were found - ignored.", servletUrl);
        } else {
            servlets.remove(servlet);
            localContext.getHttpService().unregister(NeeoUtil.getServletUrl(servlet.getBrainStatus().getBrainId()));
            NeeoUtil.close(servlet);
            logger.debug("Servlet at {} was successfully removed", servletUrl);
        }
    }

    /**
     * Adds a brain specified by the ip address
     *
     * @param ipAddress a non-null, non-empty ipAddress
     * @return true if found and added, false otherwise
     */
    public boolean addBrain(String ipAddress) {
        NeeoUtil.requireNotEmpty(ipAddress, "ipAddress cannot be empty");

        final BrainDiscovery localDiscovery = discovery;
        if (localDiscovery == null) {
            return false;
        } else {
            return localDiscovery.addDiscovered(ipAddress);
        }
    }

    /**
     * Removes a brain by shutting down the servlet related to it
     *
     * @param brainId a non-null, non-empty brainid
     * @return true if found and removed, false otherwise
     */
    public boolean removeBrain(String brainId) {
        NeeoUtil.requireNotEmpty(brainId, "brainId cannot be empty");
        final AbstractServlet servlet = getServlet(brainId);
        if (servlet == null) {
            logger.debug("Tried to remove a servlet for {} but none were found - ignored.", brainId);
            return false;
        }

        final String servletUrl = servlet.getServletUrl();
        final BrainDiscovery localDiscovery = discovery;
        if (localDiscovery == null) {
            return false;
        } else {
            return localDiscovery.removeDiscovered(servletUrl);
        }
    }

    /**
     * Returns an immutable list of {@link NeeoBrainServlet}. Please note this list also 'disconnected' (i.e. the list
     * is
     * not modified if servlets are added/removed)
     *
     * @return immutable list of {@link NeeoBrainServlet}
     */
    public List<NeeoBrainServlet> getServlets() {
        return Collections.unmodifiableList(servlets);
    }

    /**
     * Gets the {@link AbstractServlet} that is associated with the brain ID
     *
     * @param brainId the non-empty brain id
     * @return the servlet for the brainId or null if none
     */
    @Nullable
    public NeeoBrainServlet getServlet(String brainId) {
        NeeoUtil.requireNotEmpty(brainId, "brainId cannot be empty");

        final String url = NeeoUtil.getServletUrl(brainId);
        return getServletByUrl(url);
    }

    /**
     * Helper method to get a servlet by it's URL
     *
     * @param servletUrl a non-null, non-empty servlet URL
     * @return the servlet for the URL or null if not found
     */
    @Nullable
    private NeeoBrainServlet getServletByUrl(String servletUrl) {
        NeeoUtil.requireNotEmpty(servletUrl, "ServletURL cannot be empty");
        for (NeeoBrainServlet servlet : servlets) {
            if (StringUtils.equalsIgnoreCase(servletUrl, servlet.getServletUrl())) {
                return servlet;
            }
        }
        return null;
    }

    /**
     * Returns {@link ItemStateChangedEvent#TYPE} for the type of events to subscribe to
     *
     * @see org.eclipse.smarthome.core.events.EventSubscriber#getSubscribedEventTypes()
     */
    @Override
    public Set<String> getSubscribedEventTypes() {
        return Collections.singleton(ItemStateChangedEvent.TYPE);
    }

    @Nullable
    @Override
    public EventFilter getEventFilter() {
        return eventFilter;
    }

    @Override
    public void receive(final Event event) {
        logger.trace("receive: {}", event);
        for (AbstractServlet servlet : servlets) {
            servlet.receive(event);
        }
    }

    @Override
    public void onPrimaryAddressChanged(@Nullable String oldPrimaryAddress, @Nullable String newPrimaryAddress) {
        for (final NeeoBrainServlet servlet : servlets) {
            final NeeoApi api = servlet.getBrainApi();
            final NeeoSystemInfo sysInfo = api.getSystemInfo();
            final String brainIpAddress = api.getBrainIpAddress();
            brainRemoved(sysInfo);
            try {
                brainDiscovered(sysInfo, InetAddress.getByName(brainIpAddress));
            } catch (UnknownHostException e) {
                logger.warn(
                        "Already connected brain had an invalid IP Address (shouldn't happen!): name: {} address: {}",
                        api.getBrainId(), brainIpAddress);
            }
        }
    }

    @Override
    public void onChanged(List<CidrAddress> added, List<CidrAddress> removed) {
        // Implementation does nothing on this change notification
    }
}
