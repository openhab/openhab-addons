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
package org.openhab.binding.km200.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingStatus;

/**
 * the {@link KM200GatewayHandler} interface is for classes wishing to register
 * to be called back when a gateway status changes
 *
 * @author Markus Eckhardt - Initial contribution
 */
@NonNullByDefault
public interface KM200GatewayStatusListener {
    public void gatewayStatusChanged(ThingStatus status);
}
