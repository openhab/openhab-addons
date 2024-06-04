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
package org.openhab.binding.touchwand.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.touchwand.internal.dto.TouchWandUnitData;

/**
 * Interface for a listener on the {@link TouchWandWebSockets}.
 * When it is registered on the socket, it gets called back when {@link TouchWandWebSockets} receives data.
 *
 * @author Roie Geron - Initial contribution
 */
@NonNullByDefault
public interface TouchWandUnitStatusUpdateListener {

    void onDataReceived(TouchWandUnitData unitData);
}
