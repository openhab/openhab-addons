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
package org.openhab.binding.sony.internal.ircc;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.AbstractConfig;

/**
 * Configuration class for the {@link IrccHandler}.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class IrccConfig extends AbstractConfig {
    /** The access code */
    private @Nullable String accessCode;

    /** The commands map file */
    private @Nullable String commandsMapFile;

    // ---- the following properties are not part of the config.xml (and are properties) ----

    /** The commands map file. */
    private @Nullable String discoveredCommandsMapFile;

    /**
     * Gets the access code
     *
     * @return the access code
     */
    public @Nullable String getAccessCode() {
        return accessCode;
    }

    /**
     * Sets the access code.
     *
     * @param accessCode the new access code
     */
    public void setAccessCode(final String accessCode) {
        this.accessCode = accessCode;
    }

    /**
     * Gets the commands map file.
     *
     * @return the commands map file
     */
    public @Nullable String getCommandsMapFile() {
        return StringUtils.defaultIfEmpty(commandsMapFile, discoveredCommandsMapFile);
    }

    /**
     * Sets the commands map file.
     *
     * @param commandsMapFile the new commands map file
     */
    public void setCommandsMapFile(final String commandsMapFile) {
        this.commandsMapFile = commandsMapFile;
    }

    /**
     * Sets the discovered commands map file.
     *
     * @param discoveredCommandsMapFile the new commands map file
     */
    public void setDiscoveredCommandsMapFile(final String discoveredCommandsMapFile) {
        this.discoveredCommandsMapFile = discoveredCommandsMapFile;
    }

    @Override
    public Map<String, Object> asProperties() {
        final Map<String, Object> props = super.asProperties();

        props.put("discoveredCommandsMapFile", StringUtils.defaultIfEmpty(discoveredCommandsMapFile, ""));

        conditionallyAddProperty(props, "accessCode", accessCode);
        conditionallyAddProperty(props, "commandsMapFile", commandsMapFile);
        return props;
    }
}
