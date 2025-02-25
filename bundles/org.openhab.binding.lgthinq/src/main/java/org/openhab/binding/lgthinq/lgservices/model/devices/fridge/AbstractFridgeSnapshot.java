/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.model.devices.fridge;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.model.AbstractSnapshotDefinition;

/**
 * The {@link AbstractFridgeSnapshot}
 *
 * @author Nemer Daud - Initial contribution
 * @author Arne Seime - Complementary sensors
 */
@NonNullByDefault
public abstract class AbstractFridgeSnapshot extends AbstractSnapshotDefinition {
    public abstract String getTempUnit();

    public abstract String getFridgeStrTemp();

    public abstract String getFreezerStrTemp();

    public abstract String getDoorStatus();
}
