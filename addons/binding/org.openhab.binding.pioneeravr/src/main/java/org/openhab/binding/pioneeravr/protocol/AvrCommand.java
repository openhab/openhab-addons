/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.protocol;

/**
 * The base interface for an AVR command.
 * 
 * @author Antoine Besnard
 *
 */
public interface AvrCommand {

    /**
     * Represent a CommandType of command requests
     * 
     * @author Antoine Besnard
     *
     */
    public interface CommandType {

        /**
         * Return the command of this command type
         * 
         * @return
         */
        public String getCommand();

        /**
         * Return the name of the command type
         * 
         * @return
         */
        public String name();
    }

    /**
     * Return the command to send to the AVR.
     * 
     * @return
     */
    public String getCommand();

    /**
     * Return the the command type of this command.
     * 
     * @return
     */
    public CommandType getCommandType();

}
