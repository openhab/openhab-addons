/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.nanoleaf.internal.commanddescription;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants;
import org.openhab.binding.nanoleaf.internal.NanoleafControllerListener;
import org.openhab.binding.nanoleaf.internal.handler.NanoleafControllerHandler;
import org.openhab.binding.nanoleaf.internal.model.ControllerInfo;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseDynamicCommandDescriptionProvider;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.type.DynamicCommandDescriptionProvider;
import org.openhab.core.types.CommandOption;
import org.osgi.service.component.annotations.Component;

/**
 * This class provides the available effects as dynamic options as they are read from the Nanoleaf controller.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
@NonNullByDefault
@Component(service = { DynamicCommandDescriptionProvider.class })
public class NanoleafCommandDescriptionProvider extends BaseDynamicCommandDescriptionProvider
        implements NanoleafControllerListener, ThingHandlerService {

    private @Nullable ChannelUID effectChannelUID;

    private @Nullable NanoleafControllerHandler bridgeHandler;

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.bridgeHandler = (NanoleafControllerHandler) handler;
        NanoleafControllerHandler localHandler = this.bridgeHandler;
        if (localHandler != null) {
            localHandler.registerControllerListener(this);
        }

        effectChannelUID = new ChannelUID(handler.getThing().getUID(), NanoleafBindingConstants.CHANNEL_EFFECT);
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void deactivate() {
        NanoleafControllerHandler localHandler = this.bridgeHandler;
        if (localHandler != null) {
            localHandler.unregisterControllerListener(this);
        }
        super.deactivate();
    }

    @Override
    public void onControllerInfoFetched(ThingUID bridge, ControllerInfo controllerInfo) {
        List<String> effects = controllerInfo.getEffects().getEffectsList();
        ChannelUID uid = effectChannelUID;
        if (effects != null && uid != null && uid.getThingUID().equals(bridge)) {
            List<CommandOption> commandOptions = effects.stream() //
                    .map(effect -> new CommandOption(effect, effect)) //
                    .collect(Collectors.toList());
            setCommandOptions(uid, commandOptions);
        }
    }
}
