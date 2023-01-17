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
package org.openhab.binding.smartmeter.internal;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartmeter.SmartMeterBindingConstants;
import org.openhab.binding.smartmeter.internal.conformity.Conformity;
import org.openhab.binding.smartmeter.internal.helper.Baudrate;
import org.openhab.binding.smartmeter.internal.helper.ProtocolMode;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.osgi.service.component.annotations.Component;

/**
 * Provides the configuration options for a meter device.
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@Component(service = ConfigOptionProvider.class)
@NonNullByDefault
public class SmartMeterConfigProvider implements ConfigOptionProvider {

    @Override
    public @Nullable Collection<ParameterOption> getParameterOptions(URI uri, String param, @Nullable String context,
            @Nullable Locale locale) {
        if (!SmartMeterBindingConstants.THING_TYPE_SMLREADER.getAsString().equals(uri.getSchemeSpecificPart())) {
            return null;
        }

        switch (param) {
            case SmartMeterBindingConstants.CONFIGURATION_SERIAL_MODE:
                List<ParameterOption> options = new ArrayList<>();

                for (ProtocolMode mode : ProtocolMode.values()) {
                    options.add(new ParameterOption(mode.name(), mode.toString()));
                }
                return options;

            case SmartMeterBindingConstants.CONFIGURATION_BAUDRATE:
                options = new ArrayList<>();

                for (Baudrate baudrate : Baudrate.values()) {
                    options.add(new ParameterOption(baudrate.getBaudrate() + "", baudrate.toString()));
                }
                return options;

            case SmartMeterBindingConstants.CONFIGURATION_CONFORMITY:
                options = new ArrayList<>();

                for (Conformity conformity : Conformity.values()) {
                    options.add(new ParameterOption(conformity.name(), conformity.toString()));
                }
                return options;
        }
        return null;
    }
}
