/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * @author Ganesh Ingle <ganesh.ingle@asvilabs.com>
 */

package org.openhab.binding.wakeonlan;

import static org.openhab.binding.wakeonlan.WakeOnLanBindingConstants.*;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.binding.wakeonlan.internal.WakeOnLanHandler;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WakeOnLanHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Ganesh Ingle - Initial contribution
 */
// @Component(immediate = true, configurationPid = "binding.wakeonlan", service = ThingHandlerFactory.class)
public class WakeOnLanHandlerFactory extends BaseThingHandlerFactory {

    // @Reference(policy = ReferencePolicy.DYNAMIC)
    protected volatile EventPublisher eventPublisher;
    // @Reference(policy = ReferencePolicy.DYNAMIC)
    protected volatile ItemRegistry itemRegistry;
    // @Reference(policy = ReferencePolicy.DYNAMIC)
    protected volatile ItemChannelLinkRegistry linkRegistry;
    // @Reference(policy = ReferencePolicy.DYNAMIC)
    protected volatile ThingRegistry thingRegistry;
    // @Reference(policy = ReferencePolicy.DYNAMIC)
    protected volatile SmarthomeCommandHelper ohCommandHelper;

    private static Logger logger = LoggerFactory.getLogger(BINDING_LOGGER_NAME);

    public EventPublisher getEventPublisher() {
        return eventPublisher;
    }

    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    public ItemChannelLinkRegistry getLinkRegistry() {
        return linkRegistry;
    }

    public ThingRegistry getThingRegistry() {
        return thingRegistry;
    }

    public SmarthomeCommandHelper getOhCommandHelper() {
        return ohCommandHelper;
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        Dictionary<String, Object> properties = componentContext.getProperties();
        logger.info("Wake On LAN OH2 Binding starting. symbolic name={}, version={}",
                bundleContext.getBundle().getSymbolicName(), bundleContext.getBundle().getVersion());
        Map<String, Object> propMap = new HashMap<>();
        Enumeration<String> e = properties.keys();
        while (e.hasMoreElements()) {
            String k = e.nextElement();
            Object v = properties.get(k);
            propMap.put(k, v);
        }
        modified(propMap);
    }

    protected void modified(Map<String, Object> properties) {
        if (properties == null) {
            return;
        }
        for (String k : properties.keySet()) {
            logger.debug("component property '{}' = '{}'", k, properties.get(k));
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(THING_TYPE_WOLDEVICE)) {
            return new WakeOnLanHandler(this, thing);
        }
        return null;
    }

}
