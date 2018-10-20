/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.gmailparadoxparser.internal;

import static org.openhab.binding.gmailparadoxparser.internal.GmailParadoxParserBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link GmailParadoxParserHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.gmailparadoxparser", service = ThingHandlerFactory.class)
public class GmailParadoxParserHandlerFactory extends BaseThingHandlerFactory {

    // private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS;
    // static {
    // Set<ThingTypeUID> temporarySet = Collections.emptySet();
    // temporarySet.add(PANEL_COMMUNICATION_THING_TYPE_UID);
    // temporarySet.add(PARTITION_THING_TYPE_UID);
    // SUPPORTED_THING_TYPES_UIDS = new HashSet<>(temporarySet);
    // }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return PANEL_COMMUNICATION_THING_TYPE_UID.equals(thingTypeUID) || PARTITION_THING_TYPE_UID.equals(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (PARTITION_THING_TYPE_UID.equals(thingTypeUID)) {
            return new ParadoxPartitionHandler(thing);
        } else if (PANEL_COMMUNICATION_THING_TYPE_UID.equals(thingTypeUID)) {
            return new ParadoxCommunicationHandler(thing);
        }

        return null;
    }
}
