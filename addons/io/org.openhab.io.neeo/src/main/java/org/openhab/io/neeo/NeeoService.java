/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.binding.BindingInfoRegistry;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemStateChangedEvent;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.type.ChannelTypeRegistry;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.eclipse.smarthome.io.transport.mdns.MDNSClient;
import org.openhab.io.neeo.internal.NeeoUtil;
import org.openhab.io.neeo.internal.ServiceContext;
import org.openhab.io.neeo.internal.discovery.BrainDiscovery;
import org.openhab.io.neeo.internal.discovery.DiscoveryListener;
import org.openhab.io.neeo.internal.discovery.MdnsBrainDiscovery;
import org.openhab.io.neeo.internal.models.NeeoSystemInfo;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main entry point for the transport service. The transport service will listen for brain broadcasts and create a
 * transport for that brain (in addition to starting up the dashboard tile)
 *
 * @author Tim Roberts - Initial contribution
 */
@Component(service = org.eclipse.smarthome.core.events.EventSubscriber.class, configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true, property = {
        "service.pid=org.openhab.io.neeo.NeeoService", "service.config.description.uri=io:neeo",
        "service.config.label=NEEO Transport", "service.config.category=io" }

)
public class NeeoService implements EventSubscriber {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoService.class);

    /**
     * This lock controls access to all 'context' type of state. This includes the {@link #context} and all services
     * that are set by openHAB
     */
    private final Lock contextLock = new ReentrantLock();

    /**
     * This is the context created in the activate method (and nulled in the deactivate method) that will provide the
     * context to all services for all servlets
     */
    private ServiceContext context;

    // The following services are set by openHAB via the getter/setters - access to them is controlled via contextLock
    private HttpService httpService;
    private ItemRegistry itemRegistry;
    private BindingInfoRegistry bindingInfoRegistry;
    private ThingRegistry thingRegistry;
    private ThingTypeRegistry thingTypeRegistry;
    private ItemChannelLinkRegistry itemChannelLinkRegistry;
    private ChannelTypeRegistry channelTypeRegistry;
    private MDNSClient mdnsClient;
    private EventPublisher eventPublisher;
    private NetworkAddressService networkAddressService;

    /** The main dashboard servlet. Only created in the activate method (and disposed of in the deactivate method) */
    private final AtomicReference<NeeoServlet> dashboardServlet = new AtomicReference<>(null);

    /**
     * The various servlets being used (should be one per brain + the status one)
     */
    private final List<NeeoServlet> servlets = new CopyOnWriteArrayList<>();

    /** The brain discovery service */
    private BrainDiscovery discovery;

    /** The event filter to use */
    private final AtomicReference<EventFilter> eventFilter = new AtomicReference<>();

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
     * Sets the http service.
     *
     * @param httpService the non-null http service
     */
    @Reference
    public void setHttpService(HttpService httpService) {
        Objects.requireNonNull(httpService, "httpService cannot be null");
        contextLock.lock();
        try {
            this.httpService = httpService;
        } finally {
            contextLock.unlock();
        }
    }

    /**
     * Unset http service.
     *
     * @param httpService the http service (ignored)
     */
    public void unsetHttpService(HttpService httpService) {
        contextLock.lock();
        try {
            this.httpService = null;
        } finally {
            contextLock.unlock();
        }
    }

    /**
     * Sets the item registry.
     *
     * @param itemRegistry the non-null item registry
     */
    @Reference
    public void setItemRegistry(ItemRegistry itemRegistry) {
        Objects.requireNonNull(itemRegistry, "itemRegistry cannot be null");
        contextLock.lock();
        try {
            this.itemRegistry = itemRegistry;
        } finally {
            contextLock.unlock();
        }
    }

    /**
     * Unset item registry.
     *
     * @param itemRegistry the item registry (ignored)
     */
    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        contextLock.lock();
        try {
            this.itemRegistry = null;
        } finally {
            contextLock.unlock();
        }
    }

    /**
     * Sets the binding info registry.
     *
     * @param bindingInfoRegistry the non-null binding info registry
     */
    @Reference
    public void setBindingInfoRegistry(BindingInfoRegistry bindingInfoRegistry) {
        Objects.requireNonNull(bindingInfoRegistry, "bindingInfoRegistry cannot be null");
        contextLock.lock();
        try {
            this.bindingInfoRegistry = bindingInfoRegistry;
        } finally {
            contextLock.unlock();
        }
    }

    /**
     * Unset binding info registry.
     *
     * @param bindingInfoRegistry the binding info registry (ignored)
     */
    public void unsetBindingInfoRegistry(BindingInfoRegistry bindingInfoRegistry) {
        contextLock.lock();
        try {
            this.bindingInfoRegistry = null;
        } finally {
            contextLock.unlock();
        }
    }

    /**
     * Sets the thing registry.
     *
     * @param thingRegistry the non-null thing registry
     */
    @Reference
    public void setThingRegistry(ThingRegistry thingRegistry) {
        Objects.requireNonNull(thingRegistry, "thingRegistry cannot be null");
        contextLock.lock();
        try {
            this.thingRegistry = thingRegistry;
        } finally {
            contextLock.unlock();
        }
    }

    /**
     * Unset thing registry.
     *
     * @param thingRegistry the thing registry (ignored)
     */
    public void unsetThingRegistry(ThingRegistry thingRegistry) {
        contextLock.lock();
        try {
            this.thingRegistry = null;
        } finally {
            contextLock.unlock();
        }
    }

    /**
     * Sets the thing type registry.
     *
     * @param thingTypeRegistry the non-null thing type registry
     */
    @Reference
    public void setThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        Objects.requireNonNull(thingTypeRegistry, "thingTypeRegistry cannot be null");
        contextLock.lock();
        try {
            this.thingTypeRegistry = thingTypeRegistry;
        } finally {
            contextLock.unlock();
        }
    }

    /**
     * Unset thing type registry.
     *
     * @param thingTypeRegistry the thing type registry (ignored)
     */
    public void unsetThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        contextLock.lock();
        try {
            this.thingTypeRegistry = null;
        } finally {
            contextLock.unlock();
        }
    }

    /**
     * Sets the item channel link registry.
     *
     * @param itemChannelLinkRegistry the non-null item channel link registry
     */
    @Reference
    public void setItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        Objects.requireNonNull(itemChannelLinkRegistry, "itemChannelLinkRegistry cannot be null");
        contextLock.lock();
        try {
            this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        } finally {
            contextLock.unlock();
        }
    }

    /**
     * Unset item channel link registry.
     *
     * @param itemChannelLinkRegistry the item channel link registry (ignored)
     */
    public void unsetItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        contextLock.lock();
        try {
            this.itemChannelLinkRegistry = null;
        } finally {
            contextLock.unlock();
        }
    }

    /**
     * Sets the channel type registry.
     *
     * @param channelTypeRegistry the non-null channel type registry
     */
    @Reference
    public void setChannelTypeRegistry(ChannelTypeRegistry channelTypeRegistry) {
        Objects.requireNonNull(channelTypeRegistry, "channelTypeRegistry cannot be null");
        contextLock.lock();
        try {
            this.channelTypeRegistry = channelTypeRegistry;
        } finally {
            contextLock.unlock();
        }
    }

    /**
     * Unset channel type registry.
     *
     * @param channelTypeRegistry the channel type registry (ignored)
     */
    public void unsetChannelTypeRegistry(ChannelTypeRegistry channelTypeRegistry) {
        contextLock.lock();
        try {
            this.channelTypeRegistry = null;
        } finally {
            contextLock.unlock();
        }
    }

    /**
     * Sets the MDNS client.
     *
     * @param mdnsClient the non-null MDNS client
     */
    @Reference
    public void setMDNSClient(MDNSClient mdnsClient) {
        Objects.requireNonNull(mdnsClient, "mdnsClient cannot be null");
        contextLock.lock();
        try {
            this.mdnsClient = mdnsClient;
        } finally {
            contextLock.unlock();
        }
    }

    /**
     * Unset MDNS client.
     *
     * @param mdnsClient the mdns client (ignored)
     */
    public void unsetMDNSClient(MDNSClient mdnsClient) {
        contextLock.lock();
        try {
            this.mdnsClient = null;
        } finally {
            contextLock.unlock();
        }
    }

    /**
     * Sets the event publisher.
     *
     * @param eventPublisher the new event publisher
     */
    @Reference
    public void setEventPublisher(EventPublisher eventPublisher) {
        Objects.requireNonNull(eventPublisher, "eventPublisher cannot be null");
        contextLock.lock();
        try {
            this.eventPublisher = eventPublisher;
        } finally {
            contextLock.unlock();
        }
    }

    /**
     * Unset event publisher.
     *
     * @param eventPublisher the event publisher (ignored)
     */
    public void unsetEventPublisher(EventPublisher eventPublisher) {
        contextLock.lock();
        try {
            this.eventPublisher = null;
        } finally {
            contextLock.unlock();
        }
    }

    /**
     * Sets the network address service
     *
     * @param networkAddressService the network address service
     */
    @Reference
    public void setNetworkAddressService(NetworkAddressService networkAddressService) {
        Objects.requireNonNull(networkAddressService, "networkAddressService cannot be null");
        contextLock.lock();
        try {
            this.networkAddressService = networkAddressService;
        } finally {
            contextLock.unlock();
        }
    }

    /**
     * Unset network address service
     *
     * @param network address service
     */
    public void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
        contextLock.lock();
        try {
            this.networkAddressService = null;
        } finally {
            contextLock.unlock();
        }
    }

    /**
     * Activates this service. The activation will start up the brain discovery service and register the dashboard tile
     *
     * @param componentContext the non-null component context
     */
    @Activate
    public void activate(final ComponentContext componentContext, Map<String, Object> config) {
        Objects.requireNonNull(componentContext, "componentContext cannot be null");

        logger.debug("Neeo Service activated");
        contextLock.lock();
        try {
            context = new ServiceContext(componentContext, httpService, itemRegistry, bindingInfoRegistry,
                    thingRegistry, thingTypeRegistry, itemChannelLinkRegistry, channelTypeRegistry, mdnsClient,
                    eventPublisher, networkAddressService);

            discovery = new MdnsBrainDiscovery(context);
            discovery.addListener(discoveryListener);

            try {
                final String servletUrl = NeeoConstants.WEBAPP_PREFIX + NeeoConstants.WEBAPP_STATUS_PREFIX;
                final NeeoServlet ns = new NeeoServlet(this, servletUrl, context);
                NeeoUtil.close(dashboardServlet.getAndSet(ns));

                context.getHttpService().registerServlet(servletUrl, ns, new Hashtable<>(),
                        context.getHttpService().createDefaultHttpContext());

                context.getHttpService().registerResources(NeeoConstants.WEBAPP_PREFIX, "web",
                        context.getHttpService().createDefaultHttpContext());
                logger.debug("Started NEEO Dashboard tile at {}", NeeoConstants.WEBAPP_PREFIX);
            } catch (ServletException | NamespaceException e) {
                logger.debug("Exception starting status servlet: {}", e.getMessage(), e);
            }
        } finally {
            contextLock.unlock();
        }

        // Start discovery and re-discover those that we already found
        discovery.startDiscovery();
    }

    /**
     * Deactivate the service
     *
     * @param componentContext the component context (ignored)
     */
    @Deactivate
    public void deactivate(ComponentContext componentContext) {
        logger.debug("Neeo Service deactivated");

        discovery.removeListener(discoveryListener);
        discovery.close();

        // DON'T clear the foundBrains cache - must survive restarts
        // since MDNS doesn't seem to find them again.

        // If we have a context, save any pending definition changes and
        // then close down all servlets
        contextLock.lock();
        try {
            if (context != null) {
                context.getDefinitions().save();

                final HttpService service = context.getHttpService();
                for (NeeoServlet servlet : servlets) {
                    service.unregister(NeeoUtil.getServletUrl(servlet.getBrainStatus().getBrainId()));
                    NeeoUtil.close(servlet);
                }
                servlets.clear();
                eventFilter.set(null);

                context = null;
            }
        } finally {
            contextLock.unlock();
        }

        NeeoUtil.close(dashboardServlet.getAndSet(null));

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

        contextLock.lock();
        try {
            // this should really never happen. The listener that calls this method is only created/activated in the
            // activation method AFTER the context has been created. The only possibility really is if a brain is
            // discovered during deactivation - which in that case we are shutting down anyway and can safetly ignore
            // the call
            if (context != null) {
                final String servletUrl = NeeoUtil.getServletUrl(sysInfo.getHostname());

                if (getServletByUrl(servletUrl) == null) {
                    logger.debug("Brain discovered: {} at {} and starting servlet at {}", sysInfo.getHostname(),
                            ipAddress, servletUrl);
                    try {
                        final NeeoServlet newServlet = new NeeoServlet(sysInfo.getHostname(), ipAddress, servletUrl,
                                context);
                        servlets.add(newServlet);
                        eventFilter.set(null); // regenerate the event filter

                        context.getHttpService().registerServlet(servletUrl, newServlet,
                                new Hashtable<String, String>(), context.getHttpService().createDefaultHttpContext());
                        logger.debug("Started NEEO Listener at {}", servletUrl);
                    } catch (NamespaceException | ServletException | IOException e) {
                        logger.error("Error during servlet startup", e);
                    }
                } else {
                    logger.debug("Brain servlet with URL of {} already exists - ignored", servletUrl);
                }
            }
        } finally {
            contextLock.unlock();
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

        final NeeoServlet servlet = getServletByUrl(servletUrl);

        if (servlet == null) {
            logger.debug("Tried to remove a servlet for {} but none were found - ignored.", servletUrl);
        } else {
            servlets.remove(servlet);
            context.getHttpService().unregister(NeeoUtil.getServletUrl(servlet.getBrainStatus().getBrainId()));
            eventFilter.set(null); // regenerate the event filter
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
        return discovery.addDiscovered(ipAddress);
    }

    /**
     * Removes a brain by shutting down the servlet related to it
     *
     * @param brainId a non-null, non-empty brainid
     * @return true if found and removed, false otherwise
     */
    public boolean removeBrain(String brainId) {
        NeeoUtil.requireNotEmpty(brainId, "brainId cannot be empty");
        final NeeoServlet servlet = getServlet(brainId);
        if (servlet == null) {
            logger.debug("Tried to remove a servlet for {} but none were found - ignored.", brainId);
            return false;
        }

        final String servletUrl = servlet.getServletUrl();
        return discovery.removeDiscovered(servletUrl);
    }

    /**
     * Returns an immutable list of {@link NeeoServlet}. Please note this list also 'disconnected' (i.e. the list is
     * not modified if servlets are added/removed)
     *
     * @return immutable list of {@link NeeoServlet}
     */
    public List<NeeoServlet> getServlets() {
        return Collections.unmodifiableList(servlets);
    }

    /**
     * Gets the {@link NeeoServlet} that is associated with the brain ID
     *
     * @param brainId the non-empty brain id
     * @return the servlet for the brainId or null if none
     */
    public NeeoServlet getServlet(String brainId) {
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
    private NeeoServlet getServletByUrl(String servletUrl) {
        NeeoUtil.requireNotEmpty(servletUrl, "ServletURL cannot be empty");
        for (NeeoServlet servlet : servlets) {
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

    /**
     * Returns a null for event filtering
     *
     * @see org.eclipse.smarthome.core.events.EventSubscriber#getEventFilter()
     */
    @Override
    public EventFilter getEventFilter() {
        logger.trace("getEventFilter");
        return eventFilter.getAndUpdate((ef) -> {
            if (ef == null) {
                logger.trace("getEventFilter - creating");
                final List<EventFilter> eventFilters = new ArrayList<>();
                for (NeeoServlet ns : servlets) {
                    final List<EventFilter> efs = ns.getEventFilters();
                    if (efs != null) {
                        eventFilters.addAll(efs);
                    }
                }

                return new EventFilter() {
                    @Override
                    public boolean apply(Event event) {
                        logger.trace("apply: {}", event);
                        for (EventFilter ef : eventFilters) {
                            if (ef.apply(event)) {
                                logger.trace("apply (true): {}", event);
                                return true;
                            }
                        }
                        logger.trace("apply (false): {}", event);
                        return false;
                    }
                };
            }

            return ef;
        });
    }

    /**
     * Forwards the event to all servlets (via {@link NeeoServlet#receive(Event)})
     *
     * @see org.eclipse.smarthome.core.events.EventSubscriber#receive(org.eclipse.smarthome.core.events.Event)
     */
    @Override
    public void receive(final Event event) {
        logger.trace("receive: {}", event);
        for (NeeoServlet servlet : servlets) {
            servlet.receive(event);
        }
    }
}
