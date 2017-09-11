package org.openhab.binding.fronius.internal.configuration;

import org.openhab.binding.fronius.internal.FroniusHandlerConfiguration;

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
