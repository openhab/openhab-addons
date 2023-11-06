/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.pushsafer.internal.config;

import static org.openhab.binding.pushsafer.internal.PushsaferBindingConstants.*;

import java.net.URI;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pushsafer.internal.dto.Icon;
import org.openhab.binding.pushsafer.internal.dto.Sound;
import org.openhab.binding.pushsafer.internal.handler.PushsaferAccountHandler;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link PushsaferConfigOptionProvider} class contains fields mapping thing configuration parameters.
 *
 * @author Kevin Siml - Initial contribution, forked from Christoph Weitkamp
 */
@Component(service = ConfigOptionProvider.class)
@NonNullByDefault
public class PushsaferConfigOptionProvider implements ConfigOptionProvider, ThingHandlerService {

    private @Nullable PushsaferAccountHandler accountHandler;

    @Override
    public @Nullable Collection<ParameterOption> getParameterOptions(URI uri, String param, @Nullable String context,
            @Nullable Locale locale) {
        PushsaferAccountHandler localAccountHandler = accountHandler;
        if (localAccountHandler != null) {
            if (PUSHSAFER_ACCOUNT.getAsString().equals(uri.getSchemeSpecificPart()) && CONFIG_SOUND.equals(param)) {
                List<Sound> sounds = localAccountHandler.getSounds();
                if (!sounds.isEmpty()) {
                    return sounds.stream().map(Sound::getAsParameterOption)
                            .sorted(Comparator.comparing(ParameterOption::getLabel))
                            .collect(Collectors.toUnmodifiableList());
                }
            }
            if (PUSHSAFER_ACCOUNT.getAsString().equals(uri.getSchemeSpecificPart()) && CONFIG_ICON.equals(param)) {
                List<Icon> icons = localAccountHandler.getIcons();
                if (!icons.isEmpty()) {
                    return icons.stream().map(Icon::getAsParameterOption)
                            .sorted(Comparator.comparing(ParameterOption::getLabel))
                            .collect(Collectors.toUnmodifiableList());
                }
            }
        }
        return null;
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.accountHandler = (PushsaferAccountHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return accountHandler;
    }
}
