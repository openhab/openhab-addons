package org.openhab.binding.restify.internal;

import static org.openhab.binding.restify.internal.RestifyBindingConstants.CONFIGURATION_PID;

import java.io.Serial;
import java.io.Serializable;
import java.util.Dictionary;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RestifyBinding} stores global binding configuration and exposes it to other
 * binding components.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = CONFIGURATION_PID, service = { RestifyBinding.class, ManagedService.class })
public class RestifyBinding implements ManagedService, Serializable {
    @Serial
    private static final long serialVersionUID = 6114697727662078933L;
    private final Logger logger = LoggerFactory.getLogger(RestifyBinding.class);
    private volatile RestifyBindingConfig config = RestifyBindingConfig.DEFAULT;

    @Override
    @NonNullByDefault({})
    public void updated(Dictionary<String, ?> properties) {
        if (properties == null || properties.isEmpty()) {
            return;
        }

        this.config = new RestifyBindingConfig(getBoolean(properties, "enforceAuthentication", false));
        logger.debug("Loaded configuration: {}", config);
    }

    public RestifyBindingConfig getConfig() {
        return config;
    }

    private static boolean getBoolean(Dictionary<String, ?> properties, String key, boolean defaultValue) {
        var value = properties.get(key);
        return switch (value) {
            case Boolean bool -> bool;
            case String text -> Boolean.parseBoolean(text);
            default -> defaultValue;
        };
    }
}
