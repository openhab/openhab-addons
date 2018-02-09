/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.harmonyhub.handler;

import org.eclipse.smarthome.core.thing.ThingStatus;

/**
 * the {@link HarmonyDeviceHandler} interface is for classes wishing to register
 * to be called back when a HarmonyHub status changes
 *
 * @author Dan Cunningham - Initial contribution
 *
 */
public interface HubStatusListener {
    public void hubStatusChanged(ThingStatus status);
}
