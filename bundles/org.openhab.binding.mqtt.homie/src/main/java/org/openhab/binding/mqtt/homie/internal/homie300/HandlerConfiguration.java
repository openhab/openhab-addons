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
package org.openhab.binding.mqtt.homie.internal.homie300;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.homie.internal.handler.HomieThingHandler;

/**
 * The {@link HomieThingHandler} manages Things that are responsible for
 * Homie MQTT devices.
 * This class contains the necessary configuration for such a Thing handler.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HandlerConfiguration {
    /**
     * The MQTT prefix topic
     */
    public String basetopic = "homie";
    /**
     * The device id.
     */
    public String deviceid = "";
    /**
     * Indicates if retained topics should be removed when the Thing is deleted.
     */
    public boolean removetopics = false;
}
