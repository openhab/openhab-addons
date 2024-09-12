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
package org.openhab.binding.pushover.internal.config;

import static org.openhab.binding.pushover.internal.PushoverBindingConstants.*;

import java.net.URI;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pushover.internal.dto.Sound;
import org.openhab.binding.pushover.internal.handler.PushoverAccountHandler;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * The {@link PushoverConfigOptionProvider} class contains fields mapping thing configuration parameters.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = { PushoverConfigOptionProvider.class, ConfigOptionProvider.class })
public class PushoverConfigOptionProvider implements ConfigOptionProvider, ThingHandlerService {

    private @Nullable PushoverAccountHandler accountHandler;

    @Override
    public @Nullable Collection<ParameterOption> getParameterOptions(URI uri, String param, @Nullable String context,
            @Nullable Locale locale) {
        PushoverAccountHandler localAccountHandler = accountHandler;
        if (localAccountHandler != null && PUSHOVER_ACCOUNT.getAsString().equals(uri.getSchemeSpecificPart())
                && CONFIG_SOUND.equals(param)) {
            List<Sound> sounds = localAccountHandler.getSounds();
            if (!sounds.isEmpty()) {
                return sounds.stream().map(Sound::getAsParameterOption)
                        .sorted(Comparator.comparing(ParameterOption::getLabel))
                        .collect(Collectors.toUnmodifiableList());
            }
        }
        return null;
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.accountHandler = (PushoverAccountHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return accountHandler;
    }
}
