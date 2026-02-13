package org.openhab.binding.restify.internal;

import static java.util.Objects.requireNonNull;

import java.util.Dictionary;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link RestifyBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.restify", service = {})
public class RestifyBinding implements ManagedService {

    @Nullable
    private volatile RestifyBindingConfig config;

    @Override
    @NonNullByDefault({})
    public void updated(Dictionary<String, ?> properties) {
        if (properties == null || properties.isEmpty()) {
            return;
        }

        this.config = new RestifyBindingConfig((Boolean) properties.get("enforceAuthentication"));
    }

    public RestifyBindingConfig getConfig() {
        return requireNonNull(config);
    }
}
