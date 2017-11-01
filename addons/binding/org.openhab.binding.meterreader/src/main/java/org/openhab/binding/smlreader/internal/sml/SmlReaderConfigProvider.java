/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smlreader.internal.sml;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.eclipse.smarthome.config.core.ConfigOptionProvider;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.openhab.binding.smlreader.MeterReaderBindingConstants;
import org.openhab.binding.smlreader.internal.helper.Baudrate;
import org.openhab.binding.smlreader.internal.helper.ProtocolMode;

import gnu.io.NRSerialPort;

/**
 *
 * @author MatthiasS
 *
 */
public class SmlReaderConfigProvider implements ConfigOptionProvider {

    @Override
    public Collection<ParameterOption> getParameterOptions(URI uri, String param, Locale locale) {
        if (uri == null) {
            return null;
        }

        if (!MeterReaderBindingConstants.THING_TYPE_SMLREADER.getAsString().equals(uri.getSchemeSpecificPart())) {
            return null;
        }

        switch (param) {
            case MeterReaderBindingConstants.CONFIGURATION_PORT:
                List<ParameterOption> options = new ArrayList<ParameterOption>();
                for (String port : NRSerialPort.getAvailableSerialPorts()) {
                    options.add(new ParameterOption(port, port));
                }
                return options;

            case MeterReaderBindingConstants.CONFIGURATION_SERIAL_MODE:
                options = new ArrayList<ParameterOption>();

                for (ProtocolMode mode : ProtocolMode.values()) {
                    options.add(new ParameterOption(mode.name(), mode.toString()));
                }
                return options;

            case MeterReaderBindingConstants.CONFIGURATION_BAUDRATE:
                options = new ArrayList<ParameterOption>();

                for (Baudrate baudrate : Baudrate.values()) {
                    options.add(new ParameterOption(baudrate.getBaudrate() + "", baudrate.toString()));
                }
                return options;
        }
        return null;
    }

}
