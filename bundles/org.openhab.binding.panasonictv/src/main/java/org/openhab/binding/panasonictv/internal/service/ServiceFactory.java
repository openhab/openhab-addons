/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.panasonictv.internal.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.jupnp.UpnpService;
import org.openhab.binding.panasonictv.internal.service.api.PanasonicTvService;

/**
 * The {@link ServiceFactory} is helper class for creating Samsung TV related
 * services.
 *
 * @author Prakashbabu Sidaraddi - Initial contribution
 */
public class ServiceFactory {

    @SuppressWarnings("serial")
    private static final Map<String, Class<? extends PanasonicTvService>> serviceMap = Collections
            .unmodifiableMap(new HashMap<String, Class<? extends PanasonicTvService>>() {
                {
                    put(MediaRendererService.SERVICE_NAME, MediaRendererService.class);
                    put(RemoteControllerService.SERVICE_NAME, RemoteControllerService.class);
                }
            });

    /**
     * Create Panasonic TV service.
     * 
     * @param type
     * @param upnpIOService
     * @param upnpService
     * @param udn
     * @param pollingInterval
     * @param host
     * @param port
     * @return
     */
    public static PanasonicTvService createService(String type, UpnpIOService upnpIOService, UpnpService upnpService,
            String udn, int pollingInterval, String host, int port) {

        PanasonicTvService service = null;

        switch (type) {
            case MediaRendererService.SERVICE_NAME:
                service = new MediaRendererService(upnpIOService, udn, pollingInterval);
                break;
            case RemoteControllerService.SERVICE_NAME:
                service = RemoteControllerService.createUpnpService(upnpService, udn, host, port);
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
    public static Class<? extends PanasonicTvService> getClassByServiceName(String serviceName) {
        return serviceMap.get(serviceName);
    }
}
