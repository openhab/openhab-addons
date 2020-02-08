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
package org.openhab.binding.presence.internal.binding;

import static org.openhab.binding.presence.internal.binding.PresenceBindingConstants.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.presence.internal.ping.PingHandler;
import org.openhab.binding.presence.internal.smtp.SMTPHandler;
import org.openhab.binding.presence.internal.tcpport.TCPPortHandler;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

/**
 * The {@link PresenceHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Mike Dabbs - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.presence", service = ThingHandlerFactory.class)
public class PresenceHandlerFactory extends BaseThingHandlerFactory {
    final PresenceBindingConfiguration configuration = new PresenceBindingConfiguration();

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(THING_TYPE_PINGDEVICE, THING_TYPE_TCPPORTDEVICE, THING_TYPE_SMTPDEVICE).collect(Collectors.toSet());

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_PINGDEVICE.equals(thingTypeUID)) {
            return new PingHandler(thing, configuration);
        } else if (THING_TYPE_TCPPORTDEVICE.equals(thingTypeUID)) {
            return new TCPPortHandler(thing, configuration);
        } else if (THING_TYPE_SMTPDEVICE.equals(thingTypeUID)) {
            return new SMTPHandler(thing, configuration);
        }

        return null;
    }

    // The activate component call is used to access the bindings configuration
    @Activate
    protected void activate(ComponentContext componentContext, Map<String, Object> config) {
        super.activate(componentContext);
        modified(config);
    }

    @Override
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        // We update instead of replace the configuration object, so that if the user updates the
        // configuration, the values are automatically available in all handlers. Because they all
        // share the same instance.
        configuration.update(new Configuration(config).as(PresenceBindingConfiguration.class));
    }
}
