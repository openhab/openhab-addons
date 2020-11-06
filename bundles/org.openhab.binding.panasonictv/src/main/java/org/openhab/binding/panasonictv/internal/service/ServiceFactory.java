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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.jupnp.UpnpService;
import org.openhab.core.io.transport.upnp.UpnpIOService;

/**
 * The {@link ServiceFactory} is helper class for creating Samsung TV related
 * services.
 *
 * @author Prakashbabu Sidaraddi - Initial contribution
 */
@NonNullByDefault
public class ServiceFactory {
    /**
     * Create Panasonic TV service.
     * 
     * @param type
     * @param upnpIOService
     * @param upnpService
     * @param udn
     * @param pollingInterval
     * @return
     */
    public static Optional<PanasonicTvService> createService(String type, UpnpIOService upnpIOService,
            UpnpService upnpService, String udn, int pollingInterval) {
        switch (type) {
            case MediaRendererService.SERVICE_NAME:
                return Optional.of(new MediaRendererService(upnpIOService, udn, pollingInterval));
            case RemoteControllerService.SERVICE_NAME:
                return Optional.of(RemoteControllerService.createUpnpService(upnpService, udn));
            default:
                return Optional.empty();
        }
    }
}
