/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
/*
 *
 */
package org.openhab.binding.sony.internal.scalarweb.protocols;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannel;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelDescriptor;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelTracker;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebConfig;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebState;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating ScalarWebProtocol objects.
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type
 */
public class ScalarWebProtocolFactory<T extends ThingCallback<ScalarWebChannel>> implements AutoCloseable {

    /** The logger. */
    // Logger
    private Logger logger = LoggerFactory.getLogger(ScalarWebProtocolFactory.class);

    /** The protocols. */
    private final Map<String, ScalarWebProtocol<T>> protocols = new HashMap<String, ScalarWebProtocol<T>>();

    /**
     * Instantiates a new scalar web protocol factory.
     *
     * @param tracker the tracker
     * @param state the state
     * @param config the config
     * @param bundleContext the bundle context
     * @param callback the callback
     */
    public ScalarWebProtocolFactory(ScalarWebChannelTracker tracker, ScalarWebState state, ScalarWebConfig config,
            BundleContext bundleContext, T callback) {
        Objects.requireNonNull(tracker, "tracker cannot be null");
        Objects.requireNonNull(state, "state cannot be null");
        Objects.requireNonNull(callback, "callback cannot be null");

        for (ScalarWebService service : state.getDevice().getServices()) {
            final String serviceName = service.getServiceName();
            switch (serviceName) {
                case ScalarWebService.AppControl:
                    protocols.put(ScalarWebService.AppControl,
                            new ScalarWebAppControlProtocol<T>(tracker, state, service, callback));
                    break;
                case ScalarWebService.Audio:
                    protocols.put(ScalarWebService.Audio,
                            new ScalarWebAudioProtocol<T>(tracker, state, service, callback));
                    break;

                case ScalarWebService.AvContent:
                    protocols.put(ScalarWebService.AvContent,
                            new ScalarWebAvContentProtocol<T>(tracker, state, service, config, callback));
                    break;

                case ScalarWebService.Browser:
                    protocols.put(ScalarWebService.Browser,
                            new ScalarWebBrowserProtocol<T>(tracker, state, service, callback));
                    break;

                case ScalarWebService.Cec:
                    protocols.put(ScalarWebService.Cec, new ScalarWebCecProtocol<T>(tracker, state, service, callback));
                    break;

                case ScalarWebService.System:
                    try {
                        protocols.put(ScalarWebService.System, new ScalarWebSystemProtocol<T>(tracker, state, service,
                                config, bundleContext, callback));
                    } catch (Exception e) {
                        logger.error("Exception creating the system protocol: {}", e.getMessage(), e);
                    }
                    break;

                case ScalarWebService.VideoScreen:
                    protocols.put(ScalarWebService.VideoScreen,
                            new ScalarWebVideoScreenProtocol<T>(tracker, state, service, callback));
                    break;

                default:
                    logger.debug("No protocol found for service {}", serviceName);
                    break;
            }
        }
    }

    /**
     * Gets the protocol.
     *
     * @param name the name
     * @return the protocol
     */
    public ScalarWebProtocol<T> getProtocol(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        return protocols.get(name);
    }

    /**
     * Gets the channel descriptors.
     *
     * @return the channel descriptors
     */
    public Collection<ScalarWebChannelDescriptor> getChannelDescriptors() {
        final List<ScalarWebChannelDescriptor> descriptors = new ArrayList<ScalarWebChannelDescriptor>();
        for (ScalarWebProtocol<T> protocol : protocols.values()) {
            descriptors.addAll(protocol.getChannelDescriptors());
        }

        return descriptors;
    }

    /**
     * Refresh all state.
     *
     * @param scheduler the scheduler
     */
    public void refreshAllState(ScheduledExecutorService scheduler) {
        for (final ScalarWebProtocol<T> protocol : protocols.values()) {
            // logger.debug(">>> refreshing all state ");
            scheduler.execute(new Runnable() {
                @Override
                public void run() {
                    // logger.debug(">>> refreshing state ");
                    protocol.refreshState();
                }
            });
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws Exception {
        for (ScalarWebProtocol<T> protocol : protocols.values()) {
            protocol.close();
        }
    }
}
