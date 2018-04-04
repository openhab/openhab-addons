/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.samsungtv.internal.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.openhab.binding.samsungtv.internal.service.api.SamsungTvService;

/**
 * The {@link ServiceFactory} is helper class for creating Samsung TV related
 * services.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class ServiceFactory {

    @SuppressWarnings("serial")
    private static final Map<String, Class<? extends SamsungTvService>> serviceMap = Collections
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
     * @param pollingInterval
     * @param host
     * @param port
     * @return
     */
    public static SamsungTvService createService(String type, UpnpIOService upnpIOService, String udn,
            int pollingInterval, String host, int port) {

        SamsungTvService service = null;

        switch (type) {
            case MainTVServerService.SERVICE_NAME:
                service = new MainTVServerService(upnpIOService, udn, pollingInterval);
                break;
            case MediaRendererService.SERVICE_NAME:
                service = new MediaRendererService(upnpIOService, udn, pollingInterval);
                break;
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
        return serviceMap.size();
    }

    /**
     * Procedure to get service class by service name.
     * 
     * @param serviceName Name of the service
     * @return Class of the service
     */
    public static Class<? extends SamsungTvService> getClassByServiceName(String serviceName) {
        return serviceMap.get(serviceName);
    }
}
