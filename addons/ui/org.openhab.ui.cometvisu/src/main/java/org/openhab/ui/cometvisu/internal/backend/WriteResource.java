/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.cometvisu.internal.backend;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.TypeParser;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.openhab.ui.cometvisu.internal.Config;
import org.openhab.ui.cometvisu.internal.backend.beans.SuccessBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * handles state updates send by the CometVisu client and forwars them to the EventPublisher
 *
 * @author Tobias Bräutigam
 * @since 2.0.0
 */
@Path(Config.COMETVISU_BACKEND_ALIAS + "/" + Config.COMETVISU_BACKEND_WRITE_ALIAS)
public class WriteResource implements RESTResource {
    private final Logger logger = LoggerFactory.getLogger(WriteResource.class);

    private ItemRegistry itemRegistry;

    private EventPublisher eventPublisher;

    @Context
    private UriInfo uriInfo;

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public Response setState(@Context HttpHeaders headers, @QueryParam("a") String itemName,
            @QueryParam("v") String value, @QueryParam("ts") long timestamp) {
        if (logger.isDebugEnabled()) {
            logger.debug("Received CV write request at '{}' for item '{}' with value '{}'.", uriInfo.getPath(),
                    itemName, value);
        }
        Item item;
        try {
            item = itemRegistry.getItem(itemName);
            Command command = TypeParser.parseCommand(item.getAcceptedCommandTypes(), value);
            SuccessBean bean = new SuccessBean();
            if (command != null) {
                eventPublisher.post(ItemEventFactory.createCommandEvent(item.getName(), command));
                bean.success = 1;
            } else {
                bean.success = 0;
            }
            return Response.ok(bean, MediaType.APPLICATION_JSON).build();
        } catch (ItemNotFoundException e) {
            logger.error("{}", e.getLocalizedMessage());
            return Response.notAcceptable(null).build();
        }
    }

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }
}
