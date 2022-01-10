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
package org.openhab.binding.innogysmarthome.internal.client.entity;

import org.openhab.binding.innogysmarthome.internal.client.entity.device.Gateway;

/**
 * Defines the structure of the status response
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class StatusResponse {
    /**
     * The innogy SmartHome gateway. Can be null in case there is no registered for the current logged in user.
     */
    public Gateway gateway;
}
