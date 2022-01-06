/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.samsungtv.internal.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.samsungtv.internal.service.api.SamsungTvService;
import org.openhab.core.io.transport.upnp.UpnpIOService;

/**
 * The {@link ServiceFactory} is helper class for creating Samsung TV related
 * services.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class ServiceFactory {

    @SuppressWarnings("serial")
    private static final Map<String, Class<? extends SamsungTvService>> SERVICEMAP = Collections
            .unmodifiableMap(new HashMap<String, Class<? extends SamsungTvService>>() {
                {
                    put(MainTVServerService.SERVICE_NAME, MainTVServerService.class);
                    put(MediaRendererService.SERVICE_NAME, MediaRendererService.class);
                    put(RemoteControllerService.SERVICE_NAME, RemoteControllerService.class);
                }
            });

    /**
     * Create Samsung TV service.
     *
     * @param type
     * @param upnpIOService
     * @param udn
     * @param host
     * @param port
     * @return
     */
    public static @Nullable SamsungTvService createService(String type, UpnpIOService upnpIOService, String udn,
            String host, int port) {
        SamsungTvService service = null;

        switch (type) {
            case MainTVServerService.SERVICE_NAME:
                service = new MainTVServerService(upnpIOService, udn);
                break;
            case MediaRendererService.SERVICE_NAME:
                service = new MediaRendererService(upnpIOService, udn);
                break;
            // will not be created automatically
            case RemoteControllerService.SERVICE_NAME:
                service = RemoteControllerService.createUpnpService(host, port);
                break;

        }

        return service;
    }

    /**
     * Procedure to query amount of supported services.
     *
     * @return Amount of supported services
     */
    public static int getServiceCount() {
        return SERVICEMAP.size();
    }

    /**
     * Procedure to get service class by service name.
     *
     * @param serviceName Name of the service
     * @return Class of the service
     */
    public static @Nullable Class<? extends SamsungTvService> getClassByServiceName(String serviceName) {
        return SERVICEMAP.get(serviceName);
    }
}
