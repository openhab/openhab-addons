/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fronius.internal.configuration;

import org.openhab.binding.fronius.internal.FroniusHandlerConfiguration;

/**
 * Creates {@link ServiceConfiguration} from {@link FroniusHandlerConfiguration}.
 *
 * @author Gerrit Beine
 */
public class ServiceConfigurationFactory {

    public ServiceConfiguration createConnectionConfiguration(final FroniusHandlerConfiguration configuration) {
        return createConnectionConfiguration(configuration, 1);
    }

    public ServiceConfiguration createConnectionConfiguration(final FroniusHandlerConfiguration configuration,
            final int device) {
        return new ServiceConfiguration(getHostname(configuration), device);
    }

    private String getHostname(final FroniusHandlerConfiguration configuration) {
        return configuration.hostname;
    }
}
