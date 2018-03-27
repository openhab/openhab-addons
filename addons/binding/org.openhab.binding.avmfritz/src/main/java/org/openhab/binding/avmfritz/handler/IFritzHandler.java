/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.handler;

import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.avmfritz.internal.ahamodel.DeviceModel;
import org.openhab.binding.avmfritz.internal.hardware.FritzahaWebInterface;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaUpdateXmlCallback;

/**
 * Interface defining common methods for devices supporting the AHA webservice
 * interface.
 *
 * @author Robert Bausdorf
 *
 */
public interface IFritzHandler {

    /**
     * Called from {@link FritzahaWebInterface#authenticate()} to update
     * the bridge status because updateStatus is protected.
     *
     * @param status Bridge status
     * @param statusDetail Bridge status detail
     * @param description Bridge status description
     */
    public void setStatusInfo(ThingStatus status, ThingStatusDetail statusDetail, String description);

    /**
     * Called from {@link FritzAhaUpdateXmlCallback} to provide new values for
     * things.
     *
     * @param model Device model with updated data.
     */
    public void addDeviceList(DeviceModel model);

    /**
     * Provides the web interface object.
     *
     * @return The web interface object
     */
    public FritzahaWebInterface getWebInterface();
}
