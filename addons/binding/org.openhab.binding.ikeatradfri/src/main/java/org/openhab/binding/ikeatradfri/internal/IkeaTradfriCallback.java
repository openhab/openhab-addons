/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ikeatradfri.internal;

import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;

import com.google.gson.JsonElement;

/**
 * The {@link IkeaTradfriCallback} is notified by the bridge thing handler
 * with updated data from the Tradfri gateway or if any communication error occurs.
 *
 * @author Daniel Sundberg - Initial contribution
 * @author Kai Kreuzer - refactorings
 */
public interface IkeaTradfriCallback {

    /**
     * This is being called, if new data is received through CoAP
     *
     * @param data the received json structure
     */
    public void onDataUpdate(JsonElement data);

    /**
     * Tells the listener to set the Thing status.
     * Should usually be directly passed on to updateStatus() on the ThingHandler.
     *
     * @param status The thing status
     * @param statusDetail the status detail
     */
    public void setStatus(ThingStatus status, ThingStatusDetail statusDetail);
}
