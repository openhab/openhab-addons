package org.openhab.binding.homie.internal;

import static org.openhab.binding.homie.HomieBindingConstants.*;
import static org.openhab.binding.homie.internal.conventionv200.HomieConventions.*;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.homie.internal.conventionv200.HomieTopic;

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

    /**
     * Create a discovery result out of the gathered information
     * 
     * @param homieThing
     * @return
     */
    public DiscoveryResult toDiscoveryResult(ThingUID homieThing) {
        return DiscoveryResultBuilder.create(homieThing).withLabel(getName())
                .withProperty(THING_PROP_SPEC_VERSION, getHomieSpecVersion())
                .withProperty(THING_PROP_IMPL_VERSION, getImplementation()).build();

    }

    /**
     * Parse a mqtt message for relevant information
     * 
     * @param topic
     * @param message
     */
    public void parse(HomieTopic topic, String message) {
        if (topic.isInternalProperty()) {
            String propname = topic.getInternalPropertyName();
            if (StringUtils.equals(propname, HOMIE_TOPIC_SUFFIX)) {
                setHomieSpecVersion(message);
            } else if (StringUtils.equals(propname, NAME_TOPIC_SUFFIX)) {
                setName(message);
            } else if (StringUtils.equals(propname, IMPLEMENTATION_TOPIC_SUFFIX)) {
                setImplementation(message);
            }
        }

    }
}
