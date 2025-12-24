/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.client.dto.cluster;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ClusterCommand}
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ClusterCommand {
    public String commandName;
    public Map<String, Object> args;

    /**
     * @param commandName
     * @param options
     */
    public ClusterCommand(String commandName, Map<String, Object> args) {
        this.commandName = commandName;
        this.args = args;
    }

    public ClusterCommand(String commandName) {
        this.commandName = commandName;
        this.args = Collections.emptyMap();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ClusterCommand other = (ClusterCommand) obj;
        if (commandName == null) {
            if (other.commandName != null) {
                return false;
            }
        } else if (!commandName.equals(other.commandName)) {
            return false;
        }
        if (args == null) {
            return other.args == null;
        }
        return args.equals(other.args);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((commandName == null) ? 0 : commandName.hashCode());
        result = prime * result + ((args == null) ? 0 : args.hashCode());
        return result;
    }
}
