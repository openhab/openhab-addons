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
package org.openhab.binding.clearone.internal;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * The {@link ClearOneConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Garry Mitchell - Initial contribution
 */
@Component(service = { ConfigOptionProvider.class, ClearOneDynamicConfigOptionProvider.class })
@NonNullByDefault
public class ClearOneDynamicConfigOptionProvider implements ConfigOptionProvider {

    private final Map<String, @Nullable List<ParameterOption>> channelOptionsMap = new ConcurrentHashMap<>();

    public void setParameterOptions(String param, List<ParameterOption> options) {
        channelOptionsMap.put(param, options);
    }

    @Override
    public @Nullable Collection<ParameterOption> getParameterOptions(URI uri, String param, @Nullable String context,
            @Nullable Locale locale) {
        List<ParameterOption> options = channelOptionsMap.get(param);
        // if (options == null) {
        // return null;
        // }
        return options;
    }

    @Deactivate
    public void deactivate() {
        channelOptionsMap.clear();
    }
}
