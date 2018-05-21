/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.internal;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.DynamicStateDescriptionProvider;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * The {@link HyperionStateDescriptionProvider} class is a dynamic provider of state options while leaving other state
 * description fields as original.
 *
 * @author Gregory Moyer - Initial contribution
 * @author Daniel Walters - Adapted for Hyperion Binding
 */

@Component(service = { DynamicStateDescriptionProvider.class,
        HyperionStateDescriptionProvider.class }, immediate = true)
@NonNullByDefault
public class HyperionStateDescriptionProvider implements DynamicStateDescriptionProvider {

    private final Map<ChannelUID, List<StateOption>> channelOptionsMap = new ConcurrentHashMap<>();

    public void setStateOptions(ChannelUID channelUID, List<StateOption> options) {
        channelOptionsMap.put(channelUID, options);
    }

    @Override
    public @Nullable StateDescription getStateDescription(@NonNull Channel channel, @Nullable StateDescription original,
            @Nullable Locale locale) {
        List<StateOption> options = channelOptionsMap.get(channel.getUID());

        if (original != null) {
            return new StateDescription(original.getMinimum(), original.getMaximum(), original.getStep(),
                    original.getPattern(), original.isReadOnly(), options);
        }

        return new StateDescription(null, null, null, null, false, options);
    }

    @Deactivate
    public void deactivate() {
        channelOptionsMap.clear();
    }

}
