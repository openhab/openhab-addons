/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.chatgpt.internal;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * The {@link ChatGPTModelOptionProvider} provides the available models from an OpenAI API-compatible service as options
 * for the model configuration.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = ChatGPTModelOptionProvider.class)
@NonNullByDefault
public class ChatGPTModelOptionProvider implements ThingHandlerService, ConfigOptionProvider {
    private @Nullable ThingHandler thingHandler;

    @Override
    public @Nullable Collection<ParameterOption> getParameterOptions(URI uri, String param, @Nullable String context,
            @Nullable Locale locale) {
        if (!"model".equals(param)) {
            return null;
        }

        String scheme = uri.getScheme();
        String ssp = uri.getSchemeSpecificPart();
        ThingHandler localHandler = thingHandler;

        if (localHandler != null) {
            ThingUID thingUID = localHandler.getThing().getUID();
            if ("thing".equals(scheme) && thingUID.getAsString().equals(ssp)) {
                // URI matches the Thing this instance is bound to
                return getModelOptions();
            } else if ("channel".equals(scheme)) {
                try {
                    ChannelUID channelUID = new ChannelUID(ssp);
                    if (channelUID.getThingUID().equals(thingUID)) {
                        // URI matches a Channel of the Thing this instance is bound to
                        return getModelOptions();
                    }
                } catch (IllegalArgumentException e) {
                    // ignore IAE due to invalid channel UID
                }
            }
        }

        return null;
    }

    private Collection<ParameterOption> getModelOptions() {
        if (thingHandler instanceof ChatGPTHandler chatGPTHandler) {
            List<String> models = chatGPTHandler.getModels();
            if (!models.isEmpty()) {
                List<ParameterOption> options = new ArrayList<>();
                models.forEach(model -> options.add(new ParameterOption(model, model)));
                return options;
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.thingHandler = handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return thingHandler;
    }
}
