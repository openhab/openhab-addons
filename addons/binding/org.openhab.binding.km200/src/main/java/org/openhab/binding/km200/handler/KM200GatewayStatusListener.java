/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.km200.handler;

import org.eclipse.smarthome.core.thing.ThingStatus;

/**
 * the {@link KM200GatewayHandler} interface is for classes wishing to register
 * to be called back when a gateway status changes
 *
 * @author Markus Eckhardt - Initial contribution
 */
public interface KM200GatewayStatusListener {
    public void gatewayStatusChanged(ThingStatus status);
}
