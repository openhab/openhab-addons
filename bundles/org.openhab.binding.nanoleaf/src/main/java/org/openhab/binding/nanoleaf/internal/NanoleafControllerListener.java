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
package org.openhab.binding.nanoleaf.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nanoleaf.internal.model.ControllerInfo;
import org.openhab.core.thing.ThingUID;

/**
 * A {@link NanoleafControllerListener} is notified by the controller thing handler.
 * A listener may use it to discover additional things connected to the controller (bridge), such as individual panels.
 *
 * @author Martin Raepple - Initial contribution
 */

@NonNullByDefault
public interface NanoleafControllerListener {

    /**
     * This method is called after the bridge thing handler fetched the controller info
     *
     * @param bridge the Nanoleaf controller.
     * @param controllerInfo the controller data with panel information
     */
    void onControllerInfoFetched(ThingUID bridge, ControllerInfo controllerInfo);
}
