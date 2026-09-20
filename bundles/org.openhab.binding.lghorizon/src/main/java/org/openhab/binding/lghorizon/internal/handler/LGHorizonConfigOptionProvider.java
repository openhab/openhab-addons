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
package org.openhab.binding.lghorizon.internal.handler;

import static org.openhab.binding.lghorizon.internal.LGHorizonBindingConstants.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lghorizon.internal.api.ProviderPresets;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.osgi.service.component.annotations.Component;

/**
 * Supplies the selection list for the {@code account} thing type's {@code provider} parameter directly from
 * {@link ProviderPresets}. Always includes an empty "Custom" entry so users can fall through to the advanced
 * {@code country}/{@code apiUrl}/{@code useRefreshToken} parameters for anything not in the list.
 *
 * @author Mark - Initial contribution
 */
@NonNullByDefault
@Component(service = ConfigOptionProvider.class)
public class LGHorizonConfigOptionProvider implements ConfigOptionProvider {

    private static final URI ACCOUNT_CONFIG_URI = URI.create("thing-type:" + THING_TYPE_ACCOUNT.getAsString());

    @Override
    public @Nullable Collection<ParameterOption> getParameterOptions(URI uri, String param, @Nullable String context,
            @Nullable Locale locale) {
        if (!ACCOUNT_CONFIG_URI.equals(uri) || !CONFIG_PROVIDER.equals(param)) {
            return null;
        }

        List<ParameterOption> presets = new ArrayList<>();
        for (Map.Entry<String, ProviderPresets.Preset> entry : ProviderPresets.all().entrySet()) {
            presets.add(new ParameterOption(entry.getKey(), entry.getValue().displayName()));
        }
        presets.sort((a, b) -> a.getLabel().compareToIgnoreCase(b.getLabel()));

        List<ParameterOption> options = new ArrayList<>();
        options.add(new ParameterOption("",
                "Custom / manual configuration (see Country / API URL / Use Refresh Token below)"));
        options.addAll(presets);
        return options;
    }
}
