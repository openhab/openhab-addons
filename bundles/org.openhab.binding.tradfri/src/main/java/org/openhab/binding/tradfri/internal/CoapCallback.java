/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tradfri.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

import com.google.gson.JsonElement;

/**
 * The {@link CoapCallback} is receives coap response data asynchronously.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public interface CoapCallback {

    /**
     * This is being called, if new data is received from a CoAP request.
     *
     * @param data the received json structure
     */
    void onUpdate(JsonElement data);

    /**
     * Tells the listener to set the Thing status.
     * Should usually be directly passed on to updateStatus() on the ThingHandler.
     *
     * @param status The thing status
     * @param statusDetail the status detail
     */
    void setStatus(ThingStatus status, ThingStatusDetail statusDetail);
}
