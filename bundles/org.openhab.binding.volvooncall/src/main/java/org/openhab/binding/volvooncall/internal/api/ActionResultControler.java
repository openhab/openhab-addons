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
 * The {@link ActionResultControler} is responsible for triggering information
 * update after a post has been submitted to the webservice.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ActionResultControler implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(ActionResultControler.class);

    private final VocHttpApi service;
    private final ScheduledExecutorService scheduler;
    private PostResponse postResponse;

    private ThingHandler vehicle;

    public ActionResultControler(VocHttpApi service, PostResponse postResponse, ScheduledExecutorService scheduler,
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
                logger.info("Action {} for vehicle {} resulted : {}.", postResponse.serviceType.toString(),
                        postResponse.vehicleId, postResponse.status.toString());
                vehicle.handleCommand(vehicle.getThing().getChannels().get(0).getUID(), RefreshType.REFRESH);
                break;
            default:
                try {
                    postResponse = service.getURL(postResponse.serviceURL, PostResponse.class);
                    scheduler.schedule(this, 10000, TimeUnit.MILLISECONDS);
                } catch (VolvoOnCallException e) {
                    if (e.getType() == ErrorType.SERVICE_UNAVAILABLE || e.getType() == ErrorType.INTERRUPTED) {
                        scheduler.schedule(this, 10000, TimeUnit.MILLISECONDS);
                    }
                }
        }
    }
}
