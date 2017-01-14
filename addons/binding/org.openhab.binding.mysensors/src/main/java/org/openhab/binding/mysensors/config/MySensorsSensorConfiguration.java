/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.config;

/**
 * Parameters used for node / thing configuration.
 *
 * @author toberfoe
 *
 */
public class MySensorsSensorConfiguration {
    public String nodeId; // node ID in the MySensors network
    public String childId; // child ID in the MySensors network
    public boolean requestAck; // should a message request an ACK?
    public boolean revertState; // If no ACK was received after the defined retries, should the state of the item get
                                // reverted?
    public boolean smartSleep;
}
