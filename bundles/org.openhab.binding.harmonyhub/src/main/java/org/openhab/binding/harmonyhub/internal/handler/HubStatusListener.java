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
package org.openhab.binding.harmonyhub.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingStatus;

/**
 * the {@link HarmonyDeviceHandler} interface is for classes wishing to register
 * to be called back when a HarmonyHub status changes
 *
 * @author Dan Cunningham - Initial contribution
 * @author Wouter Born - Add null annotations
 */
@NonNullByDefault
public interface HubStatusListener {
    public void hubStatusChanged(ThingStatus status);
}
