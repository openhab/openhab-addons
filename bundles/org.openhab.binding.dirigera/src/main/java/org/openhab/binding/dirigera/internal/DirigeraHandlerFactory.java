/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal;

import static org.openhab.binding.dirigera.internal.Constants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.WWWAuthenticationProtocolHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.dirigera.internal.discovery.DirigeraDiscoveryManager;
import org.openhab.binding.dirigera.internal.handler.DirigeraHandler;
import org.openhab.binding.dirigera.internal.handler.LightHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DirigeraHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.dirigera", service = ThingHandlerFactory.class)
public class DirigeraHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(DirigeraHandlerFactory.class);
    private final DirigeraDiscoveryManager discoveryManager;
    private final Storage<String> bindingStorage;
    private final HttpClient insecureClient;

    @Activate
    public DirigeraHandlerFactory(@Reference HttpClientFactory hcf, @Reference StorageService storageService,
            final @Reference NetworkAddressService networkService, final @Reference DirigeraDiscoveryManager manager) {
        this.discoveryManager = manager;
        this.insecureClient = new HttpClient(new SslContextFactory.Client(true));
        insecureClient.setUserAgentField(null);
        try {
            this.insecureClient.start();
            // from https://github.com/jetty-project/jetty-reactive-httpclient/issues/33#issuecomment-777771465
            insecureClient.getProtocolHandlers().remove(WWWAuthenticationProtocolHandler.NAME);
        } catch (Exception e) {
            // catching exception is necessary due to the signature of HttpClient.start()
            logger.warn("Failed to start http client: {}", e.getMessage());
            throw new IllegalStateException("Could not create HttpClient", e);
        }
        String ip = networkService.getPrimaryIpv4HostAddress();
        if (ip == null) {
            logger.warn("Cannot find host IP");
            ip = "";
        } else {
            manager.initialize(insecureClient, ip);
        }
        bindingStorage = storageService.getStorage(BINDING_ID);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        boolean isSupported = SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
        // logger.warn("Request for {} is suppoerted {}", thingTypeUID, isSupported);
        return isSupported;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_GATEWAY.equals(thingTypeUID)) {
            return new DirigeraHandler((Bridge) thing, insecureClient, bindingStorage, discoveryManager);
        } else if (THING_TYPE_COLOR_LIGHT.equals(thingTypeUID)) {
            return new LightHandler(thing, COLOR_LIGHT_MAP);
        } else {
            logger.info("Request for {} doesn't match {}", thingTypeUID, THING_TYPE_GATEWAY);
        }

        return null;
    }
}
