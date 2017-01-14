/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.event;

/**
 * Identifies the event that was received by the bridge.
 *
 * @author Andrea Cioni
 *
 */
public enum MySensorsEventType {
    INCOMING_MESSAGE,
    NEW_NODE_DISCOVERED,
    NODE_STATUS_UPDATE,
    BRIDGE_STATUS_UPDATE,
    CHILD_VALUE_UPDATED
}
