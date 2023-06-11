/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.globalcache.internal.command;

/**
 * The {@link CommandInterface} interface class defines the methods that all command classes must implement.
 *
 * @author Mark Hilbush - Initial contribution
 */
public interface CommandInterface {

    /**
     * Get the module number to which the command will be sent
     *
     * @return module number as String
     */
    public String getModule();

    /**
     * Get the connector number to which the command will be sent
     *
     * @return connector number as String
     */
    public String getConnector();

    /**
     * Used by command implementations to parse the device's response
     */
    abstract void parseSuccessfulReply();

    /*
     * Used by command implementations to report a successful command execution
     */
    abstract void logSuccess();

    /*
     * Used by command implementations to report a failed command execution
     */
    abstract void logFailure();
}
