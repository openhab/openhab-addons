/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smlreader.internal;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.eclipse.smarthome.config.core.ConfigOptionProvider;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.openhab.binding.smlreader.SmlReaderBindingConstants;

import gnu.io.NRSerialPort;

public class SmlReaderConfigProvider implements ConfigOptionProvider {

    @Override
    public Collection<ParameterOption> getParameterOptions(URI uri, String param, Locale locale) {
        if (uri == null) {
            return null;
        }

        if (!SmlReaderBindingConstants.THING_TYPE_SMLREADER.getAsString().equals(uri.getSchemeSpecificPart())) {
            return null;
        }
        if (param.equals(SmlReaderBindingConstants.CONFIGURATION_PORT)) {

            List<ParameterOption> options = new ArrayList<ParameterOption>();
            for (String port : NRSerialPort.getAvailableSerialPorts()) {
                options.add(new ParameterOption(port, port));
            }
            return options;
        }
        return null;
    }

}
