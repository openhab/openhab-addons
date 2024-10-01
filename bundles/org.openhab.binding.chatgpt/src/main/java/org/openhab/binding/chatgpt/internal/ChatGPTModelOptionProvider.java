/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.chatgpt.internal.ChatGPTBindingConstants.CHANNEL_TYPE_UID_CHAT;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * The {@link ChatGPTModelOptionProvider} provides the available models from OpenAI as options for the channel
 * configuration.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = { ChatGPTModelOptionProvider.class, ConfigOptionProvider.class })
@NonNullByDefault
public class ChatGPTModelOptionProvider implements ThingHandlerService, ConfigOptionProvider {

    private @Nullable ThingHandler thingHandler;

    @Override
    public @Nullable Collection<ParameterOption> getParameterOptions(URI uri, String param, @Nullable String context,
            @Nullable Locale locale) {
        if (CHANNEL_TYPE_UID_CHAT.getAsString().equals(uri.toString())) {
            if ("model".equals(param)) {
                List<ParameterOption> options = new ArrayList<>();
                if (thingHandler instanceof ChatGPTHandler chatGPTHandler) {
                    chatGPTHandler.getModels().forEach(model -> options.add(new ParameterOption(model, model)));
                }
                return options;
            }
        }
        return null;
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.thingHandler = handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return thingHandler;
    }

    @Override
    public void activate() {
    }
}
