/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.cometvisu.internal.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemFactory;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import org.glassfish.jersey.media.sse.SseFeature;
import org.openhab.ui.cometvisu.internal.Config;
import org.openhab.ui.cometvisu.internal.backend.beans.StateBean;
import org.openhab.ui.cometvisu.internal.listeners.StateEventListener;
import org.openhab.ui.cometvisu.internal.util.SseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * handles read request from the CometVisu client every request initializes a
 * SSE communication
 *
 * @author Tobias Bräutigam
 * @since 2.0.0
 */
@Path(Config.COMETVISU_BACKEND_ALIAS + "/" + Config.COMETVISU_BACKEND_READ_ALIAS)
public class ReadResource implements EventBroadcaster, RESTResource {
    private final Logger logger = LoggerFactory.getLogger(ReadResource.class);

    private SseBroadcaster broadcaster = new SseBroadcaster();

    private final ExecutorService executorService;

    private ItemRegistry itemRegistry;

    private StateEventListener stateEventListener;

    private List<String> itemNames = new ArrayList<String>();
    private Map<Item, Map<String, Class<? extends State>>> items = new HashMap<Item, Map<String, Class<? extends State>>>();

    @Context
    private UriInfo uriInfo;

    @Context
    private HttpServletResponse response;

    @Context
    private HttpServletRequest request;

    private Collection<ItemFactory> itemFactories = new CopyOnWriteArrayList<ItemFactory>();

    public ReadResource() {
        this.executorService = Executors.newSingleThreadExecutor();
        this.stateEventListener = new StateEventListener();
        this.stateEventListener.setEventBroadcaster(this);
    }

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    protected void addItemFactory(ItemFactory itemFactory) {
        itemFactories.add(itemFactory);
    }

    protected void removeItemFactory(ItemFactory itemFactory) {
        itemFactories.remove(itemFactory);
    }

    /**
     * Subscribes the connecting client to the stream of events filtered by the
     * given eventFilter.
     *
     * @param eventFilter
     * @return {@link EventOutput} object associated with the incoming
     *         connection.
     * @throws IOException
     * @throws InterruptedException
     */
    @GET
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public Object getStates(@QueryParam("a") List<String> itemNames, @QueryParam("i") long index,
            @QueryParam("t") long time) throws IOException, InterruptedException {
        final EventOutput eventOutput = new EventOutput();

        this.itemNames = itemNames;

        broadcaster.add(eventOutput);

        // get all requested items and send their states to the client
        items = new HashMap<Item, Map<String, Class<? extends State>>>();
        // send the current states of all items to the client
        if (this.itemRegistry != null) {
            List<StateBean> states = new ArrayList<StateBean>();
            for (String cvItemName : itemNames) {
                try {
                    String[] parts = cvItemName.split(":");
                    String ohItemName = cvItemName;
                    Class<? extends State> stateClass = null;
                    if (parts.length == 2) {
                        String classPrefix = parts[0].toLowerCase();
                        if (Config.itemTypeMapper.containsKey(classPrefix)) {
                            stateClass = Config.itemTypeMapper.get(classPrefix);
                            classPrefix += ":";
                        } else {
                            logger.debug("no type found for '{}'", classPrefix);
                            classPrefix = "";
                        }
                        ohItemName = parts[1];
                    }
                    Item item = this.itemRegistry.getItem(ohItemName);
                    if (!items.containsKey(item)) {
                        items.put(item, new HashMap<String, Class<? extends State>>());
                    }
                    items.get(item).put(cvItemName, stateClass);
                    StateBean itemState = new StateBean();
                    itemState.name = cvItemName;

                    if (stateClass != null) {
                        itemState.state = item.getStateAs(stateClass).toString();
                        logger.trace("get state of '{}' as '{}' == '{}'", item, stateClass, itemState.state);
                    } else {
                        itemState.state = item.getState().toString();
                    }
                    states.add(itemState);
                } catch (ItemNotFoundException e) {
                    logger.error("{}", e.getLocalizedMessage());
                }
            }
            logger.debug("initially broadcasting {}/{} item states", states.size(), itemNames.size());
            broadcaster.broadcast(SseUtil.buildEvent(states));
        }
        // listen to state changes of the requested items
        registerItems();

        return eventOutput;
    }

    /**
     * listen for state changes from the requested items
     */
    @Override
    public void registerItems() {
        for (Item item : items.keySet()) {
            if (item instanceof GenericItem) {
                ((GenericItem) item).addStateChangeListener(stateEventListener);
            }
        }
    }

    /**
     * listens to state changes of the given item, if it is part of the
     * requested items
     *
     * @param item
     *            - the new item, that should be listened to
     */
    @Override
    public void registerItem(Item item) {
        if (item == null || items.containsKey(item) || !itemNames.contains(item.getName())) {
            return;
        }
        if (item instanceof GenericItem) {
            ((GenericItem) item).addStateChangeListener(stateEventListener);
        }
    }

    /**
     * listens to state changes of the given item, if it is part of the
     * requested items
     *
     * @param item
     *            - the new item, that should be listened to
     */
    @Override
    public void unregisterItem(Item item) {
        if (item == null || items.containsKey(item) || !itemNames.contains(item.getName())) {
            return;
        }
        if (item instanceof GenericItem) {
            ((GenericItem) item).removeStateChangeListener(stateEventListener);
            items.remove(item);
        }
    }

    /**
     * Broadcasts an event described by the given parameters to all currently
     * listening clients.
     *
     * @param item
     *            - the item which has changed
     * @param eventObject
     *            - bean that can be converted to a JSON object.
     */
    @Override
    public void broadcastEvent(final Object eventObject) {
        executorService.execute(new Runnable() {

            @Override
            public void run() {
                broadcaster.broadcast(SseUtil.buildEvent(eventObject));
            }
        });
    }

    @Override
    public Map<String, Class<? extends State>> getClientItems(Item item) {
        return items.get(item);
    }
}
