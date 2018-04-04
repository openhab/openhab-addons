/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
