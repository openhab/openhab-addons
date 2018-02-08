/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal.listener;

import java.util.EventListener;

import org.eclipse.smarthome.core.thing.ThingStatus;

/**
 * The {@link BridgeStatusListener} interface is for classes wishing to register
 * to be called back when a ZigbeeOWNBridge status changes
 *
 * @author Antoine Laydier
 *
 */
public interface BridgeStatusListener extends EventListener {

    public void onBridgeStatusChange(ThingStatus status);

}
