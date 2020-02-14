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
package org.openhab.binding.webthings.internal.components;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.type.DynamicStateDescriptionProvider;
import org.eclipse.smarthome.core.types.StateDescription;
import org.osgi.service.component.annotations.Component;


/**
 * The {@link WebThingsDynamicStateDescriptionProvider} is responsible to update the thing configuration window dynamically
 * https://www.openhab.org/docs/developer/bindings/thing-xml.html#dynamic-state-command-description
 * @author schneider_sven - Initial contribution
 */
@NonNullByDefault
@Component
public class WebThingsDynamicStateDescriptionProvider implements DynamicStateDescriptionProvider {

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel, @Nullable StateDescription originalStateDescription, @Nullable Locale locale) {
        // TODO Switch in thing definition: Off = Load all things into the webthings server; On = List of specific things
        return null;
    }
    // Relevant https://community.openhab.org/t/how-to-change-a-channel-to-read-only-dynamically/75445/11
}
