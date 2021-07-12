/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.amplipi.internal.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.openhab.binding.amplipi.internal.model.Status;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * AmpliPi
 *
 * <p>
 * This is the AmpliPi home audio system's control server. # Configuration This web interface allows you to control and
 * configure your AmpliPi device. At the moment the API is the only way to configure the AmpliPi. ## Try it out! __Using
 * this web interface to test API commands:__ 1. Go to an API request 1. Pick one of the examples 2. Edit it 3. Press
 * try button, it will send an API command/request to the AmpliPi __Try using the get status:__ 1. Go to [Status -> Get
 * Status](#get-/api/) 2. Click the Try button, you will see a response below with the full status/config of the AmpliPi
 * controller __Try creating a new group:__ 1. Go to [Group -> Create Group](#post-/api/group) 2. Click Example 3. Edit
 * the zones and group name 4. Click the try button, you will see a response with the newly created group __Here are
 * some other things that you might want to change:__ - [Stream -> Create new stream](#post-/api/stream) - [Zone ->
 * Update Zone](#patch-/api/zones/-zid-) (to change the zone name) - [Preset -> Create preset](#post-/api/preset) (Have
 * a look at the model to see what can be added here) # More Info Check out all of the different things you can do with
 * this API: - [Status](#tag--status) - [Source](#tag--source) - [Zone](#tag--zone) - [Group](#tag--group) -
 * [Stream](#tag--stream) - [Preset](#tag--preset) # OpenAPI This API is documented using the OpenAPI specification
 *
 */
@Path("/api")
@Tag(name = "")
public interface StatusApi {

    /**
     * Get Status
     *
     * Get the system status and configuration
     *
     */
    @GET

    @Produces({ "application/json" })
    public Status getStatusApiGet();

    /**
     * Get Status
     *
     * Get the system status and configuration
     *
     */
    @GET

    @Produces({ "application/json" })
    public Status getStatusApiGet_1();
}
