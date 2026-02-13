package org.openhab.binding.restify.internal;

import java.util.Dictionary;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link RestifyBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.restify", service = { RestifyBinding.class, ManagedService.class })
public class RestifyBinding implements ManagedService {

    private volatile RestifyBindingConfig config = RestifyBindingConfig.DEFAULT;

    @Override
    @NonNullByDefault({})
    public void updated(Dictionary<String, ?> properties) {
        if (properties == null || properties.isEmpty()) {
            return;
        }

        var enforceAuthentication = (Boolean) properties.get("enforceAuthentication");
        this.config = new RestifyBindingConfig(enforceAuthentication != null && enforceAuthentication);
    }

    public RestifyBindingConfig getConfig() {
        return config;
    }
}
