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
package org.openhab.binding.mielecloud.internal.config;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;

/**
 * Generator for templates which can be copy-pasted into .things files by the user.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public class ThingsTemplateGenerator {
    /**
     * Creates a template for the bridge.
     *
     * @param bridgeId Id of the bridge (last part of the thing UID).
     * @param locale Locale for accessing the Miele cloud service.
     * @return The template.
     */
    public String createBridgeConfigurationTemplate(String bridgeId, String email, String locale) {
        var builder = new StringBuilder();
        builder.append("Bridge ");
        builder.append(MieleCloudBindingConstants.THING_TYPE_BRIDGE.getAsString());
        builder.append(":");
        builder.append(bridgeId);
        builder.append(" [ email=\"");
        builder.append(email);
        builder.append("\", locale=\"");
        builder.append(locale);
        builder.append("\" ]");
        return builder.toString();
    }

    /**
     * Creates a complete template containing the bridge and all paired devices.
     *
     * @param bridge The bridge which is used to pair the things.
     * @param pairedThings The paired things.
     * @param discoveryResults The discovery results which can be paired.
     * @return The template.
     */
    public String createBridgeAndThingConfigurationTemplate(Bridge bridge, List<Thing> pairedThings,
            List<DiscoveryResult> discoveryResults) {
        StringBuilder result = new StringBuilder();
        result.append(createBridgeConfigurationTemplate(bridge.getUID().getId(),
                bridge.getConfiguration().get(MieleCloudBindingConstants.CONFIG_PARAM_EMAIL).toString(),
                getLocale(bridge)));
        result.append(" {\n");

        for (Thing thing : pairedThings) {
            result.append("    ").append(createThingConfigurationTemplate(thing)).append("\n");
        }

        for (DiscoveryResult discoveryResult : discoveryResults) {
            result.append("    ").append(createThingConfigurationTemplate(discoveryResult)).append("\n");
        }

        result.append("}");
        return result.toString();
    }

    private String getLocale(Bridge bridge) {
        var locale = bridge.getConfiguration().get(MieleCloudBindingConstants.CONFIG_PARAM_LOCALE);
        if (locale instanceof String str) {
            return str;
        } else {
            return "en";
        }
    }

    private String createThingConfigurationTemplate(Thing thing) {
        StringBuilder result = new StringBuilder();
        result.append("Thing ").append(thing.getThingTypeUID().getId()).append(" ").append(thing.getUID().getId())
                .append(" ");

        final String label = thing.getLabel();
        if (label != null) {
            result.append("\"").append(label).append("\" ");
        }

        result.append("[ ");
        result.append("deviceIdentifier=\"");
        result.append(
                thing.getConfiguration().get(MieleCloudBindingConstants.CONFIG_PARAM_DEVICE_IDENTIFIER).toString());
        result.append("\"");
        result.append(" ]");
        return result.toString();
    }

    private String createThingConfigurationTemplate(DiscoveryResult discoveryResult) {
        return "Thing " + discoveryResult.getThingTypeUID().getId() + " " + discoveryResult.getThingUID().getId()
                + " \"" + discoveryResult.getLabel() + "\" [ deviceIdentifier=\""
                + getProperty(discoveryResult, MieleCloudBindingConstants.CONFIG_PARAM_DEVICE_IDENTIFIER) + "\" ]";
    }

    private String getProperty(DiscoveryResult discoveryResult, String propertyName) {
        var value = discoveryResult.getProperties().get(MieleCloudBindingConstants.CONFIG_PARAM_DEVICE_IDENTIFIER);
        if (value == null) {
            return "";
        } else {
            return value.toString();
        }
    }
}
