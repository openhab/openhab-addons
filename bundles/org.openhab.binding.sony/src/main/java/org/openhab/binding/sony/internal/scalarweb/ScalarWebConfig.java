/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.AbstractConfig;

/**
 * Configuration class for the scalar web service
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ScalarWebConfig extends AbstractConfig {

    /** The access code */
    private @Nullable String accessCode;

    /** The commands map file */
    private @Nullable String commandsMapFile;

    /** The URL to the IRCC service */
    private @Nullable String irccUrl;

    /** The model name */
    private @Nullable String modelName;

    // ---- the following properties are not part of the config.xml (and are properties) ----

    /** The commands map file */
    private @Nullable String discoveredCommandsMapFile;

    /** The commands map file */
    private @Nullable String discoveredModelName;

    /**
     * Returns the IP address or host name
     *
     * @return the IP address or host name
     */
    public @Nullable String getIpAddress() {
        try {
            return new URL(getDeviceAddress()).getHost();
        } catch (final MalformedURLException e) {
            return null;
        }
    }

    /**
     * Gets the access code
     *
     * @return the access code
     */
    public @Nullable String getAccessCode() {
        return accessCode;
    }

    /**
     * Sets the access code
     *
     * @param accessCode the new access code
     */
    public void setAccessCode(final String accessCode) {
        this.accessCode = accessCode;
    }

    /**
     * Gets the commands map file
     *
     * @return the commands map file
     */
    public @Nullable String getCommandsMapFile() {
        final String localCommandsMapFile = commandsMapFile;
        return StringUtils.defaultIfEmpty(localCommandsMapFile, discoveredCommandsMapFile);
    }

    /**
     * Sets the commands map file
     *
     * @param commandsMapFile the new commands map file
     */
    public void setCommandsMapFile(final String commandsMapFile) {
        this.commandsMapFile = commandsMapFile;
    }

    /**
     * Sets the discovered commands map file
     *
     * @param discoveredCommandsMapFile the new commands map file
     */
    public void setDiscoveredCommandsMapFile(final String discoveredCommandsMapFile) {
        this.discoveredCommandsMapFile = discoveredCommandsMapFile;
    }

    /**
     * Get the IRCC url to use
     *
     * @return the ircc url
     */
    public @Nullable String getIrccUrl() {
        return irccUrl;
    }

    /**
     * Sets the IRCC url to use
     *
     * @param irccUrl the ircc url
     */
    public void setIrccUrl(final String irccUrl) {
        this.irccUrl = irccUrl;
    }

    /**
     * Get the model name
     *
     * @return the model name
     */
    public @Nullable String getModelName() {
        return StringUtils.defaultIfEmpty(modelName, discoveredModelName);
    }

    /**
     * Sets the model name
     *
     * @param modelName the model name
     */
    public void setModelName(final String modelName) {
        this.modelName = modelName;
    }

    /**
     * Get the discovered model name
     *
     * @return the discovered model name
     */
    public @Nullable String getDiscoveredModelName() {
        return discoveredModelName;
    }

    /**
     * Sets the discovered model name
     *
     * @param irccUrl the discovered model name
     */
    public void setDiscoveredModelName(final @Nullable String discoveredModelName) {
        this.discoveredModelName = discoveredModelName;
    }

    @Override
    public Map<String, Object> asProperties() {
        final Map<String, Object> props = super.asProperties();

        props.put("discoveredCommandsMapFile", StringUtils.defaultIfEmpty(discoveredCommandsMapFile, ""));
        props.put("discoveredModelName", StringUtils.defaultIfEmpty(discoveredModelName, ""));

        conditionallyAddProperty(props, "accessCode", accessCode);
        conditionallyAddProperty(props, "commandsMapFile", commandsMapFile);
        conditionallyAddProperty(props, "irccUrl", irccUrl);
        conditionallyAddProperty(props, "modelName", modelName);

        return props;
    }
}
