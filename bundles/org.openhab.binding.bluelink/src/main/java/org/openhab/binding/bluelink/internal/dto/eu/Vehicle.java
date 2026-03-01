/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bluelink.internal.dto.eu;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.bluelink.internal.model.IVehicle;

/**
 * EU vehicle record.
 *
 * @author Marcus Better - Initial contribution
 */
public record Vehicle(@Override String id, @Override @NonNull String vin, @Override String nickName,
        @Override @NonNull EngineType engineType, @Override String model, @Override int modelYear,
        boolean ccs2ProtocolSupport) implements IVehicle {
}
