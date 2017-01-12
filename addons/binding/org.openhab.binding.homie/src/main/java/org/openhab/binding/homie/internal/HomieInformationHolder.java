package org.openhab.binding.homie.internal;

import static org.openhab.binding.homie.HomieBindingConstants.*;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * Cache entry to sum up single static information until all information required to create a Homie thing was gathered.
 *
 * @author Michael Kolb - Initial contribution
 *
 */
public class HomieInformationHolder {

    private String homie;
    private String name;
    private String implementation;

    public String getHomieSpecVersion() {
        return homie;
    }

    public void setHomieSpecVersion(String homieSpecVersion) {
        this.homie = homieSpecVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImplementation() {
        return implementation;
    }

    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    /**
     * Check if all required static information is available
     *
     * @return
     */
    public boolean isInformationComplete() {
        return StringUtils.isNotBlank(homie) && StringUtils.isNotBlank(name) && StringUtils.isNotBlank(implementation);
    }

    public DiscoveryResult toDiscoveryResult(ThingUID homieThing) {
        return DiscoveryResultBuilder.create(homieThing).withLabel(getName())
                .withProperty(THING_PROP_SPEC_VERSION, getHomieSpecVersion())
                .withProperty(THING_PROP_IMPL_VERSION, getImplementation()).build();

    }

    public void parse(String topic, String message) {
        if (StringUtils.endsWith(topic, "/$homie")) {
            setHomieSpecVersion(message);
        } else if (StringUtils.endsWith(topic, "/$name")) {
            setName(message);
        } else if (StringUtils.endsWith(topic, "/$implementation")) {
            setImplementation(message);
        }
    }
}
