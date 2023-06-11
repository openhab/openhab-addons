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
package org.openhab.binding.volvooncall.internal.api;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.volvooncall.internal.VolvoOnCallException;
import org.openhab.binding.volvooncall.internal.VolvoOnCallException.ErrorType;
import org.openhab.binding.volvooncall.internal.dto.PostResponse;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ActionResultController} is responsible for triggering information
 * update after a post has been submitted to the webservice.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ActionResultController implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(ActionResultController.class);

    private final VocHttpApi service;
    private final ScheduledExecutorService scheduler;
    private final PostResponse postResponse;
    private final ThingHandler vehicle;

    public ActionResultController(VocHttpApi service, PostResponse postResponse, ScheduledExecutorService scheduler,
            ThingHandler vehicle) {
        this.postResponse = postResponse;
        this.service = service;
        this.scheduler = scheduler;
        this.vehicle = vehicle;
    }

    @Override
    public void run() {
        switch (postResponse.status) {
            case SUCCESSFULL:
            case FAILED:
                logger.debug("Action {} for vehicle {} resulted : {}.", postResponse.serviceType,
                        postResponse.vehicleId, postResponse.status);
                vehicle.handleCommand(vehicle.getThing().getChannels().get(0).getUID(), RefreshType.REFRESH);
                break;
            default:
                try {
                    scheduler.schedule(
                            new ActionResultController(service,
                                    service.getURL(postResponse.serviceURL, PostResponse.class), scheduler, vehicle),
                            10000, TimeUnit.MILLISECONDS);
                } catch (VolvoOnCallException e) {
                    if (e.getType() == ErrorType.SERVICE_UNAVAILABLE) {
                        scheduler.schedule(this, 10000, TimeUnit.MILLISECONDS);
                    }
                }
        }
    }
}
