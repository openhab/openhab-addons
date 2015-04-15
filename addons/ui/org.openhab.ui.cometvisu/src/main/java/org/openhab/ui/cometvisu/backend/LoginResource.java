/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.cometvisu.backend;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.smarthome.io.rest.RESTResource;
import org.openhab.ui.cometvisu.backend.beans.LoginBean;
import org.openhab.ui.cometvisu.internal.Config;

/**
 * handles login request from the CometVisu client
 * currently this is just a placeholder and does no real authentification
 *
 * @author Tobias Bräutigam
 * @since 2.0.0
 */
@Path(Config.COMETVISU_BACKEND_ALIAS + "/" + Config.COMETVISU_BACKEND_LOGIN_ALIAS)
public class LoginResource implements RESTResource {

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getLogin(@Context HttpHeaders headers, @QueryParam("u") String user,
            @QueryParam("p") String password, @QueryParam("d") String device) {
        LoginBean bean = new LoginBean();
        bean.v = "0.0.1";
        bean.s = "0"; // Session-ID not needed with SSE
        return Response.ok(bean, MediaType.APPLICATION_JSON).build();
    }
}
