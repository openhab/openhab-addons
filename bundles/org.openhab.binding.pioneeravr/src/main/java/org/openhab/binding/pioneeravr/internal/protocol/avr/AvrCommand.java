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
package org.openhab.binding.pioneeravr.internal.protocol.avr;

/**
 * The base interface for an AVR command.
 *
 * @author Antoine Besnard - Initial contribution
 */
public interface AvrCommand {

    /**
     * Represent a CommandType of command requests
     */
    interface CommandType {
        /**
         * Return the command of this command type.
         *
         * @return
         */
        public String getCommand();

        /**
         * Return the command of this command type for the given zone.
         *
         * The first zone number is 1.
         *
         * @return
         */
        public String getCommand(int zone);

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
    String getCommand();

    /**
     * Return the number of the zone this command will be sent to.
     *
     * @return
     */
    int getZone();

    /**
     * Return the the command type of this command.
     *
     * @return
     */
    CommandType getCommandType();
}
