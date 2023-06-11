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
package org.openhab.binding.airvisualnode.internal.json;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.airvisualnode.internal.json.airvisual.Settings;
import org.openhab.binding.airvisualnode.internal.json.airvisual.Status;

/**
 * Interface for AirVisual and AirVisual Pro models
 *
 * @author Oleg Davydyuk - Initial contribution
 */
@NonNullByDefault
public interface NodeDataInterface {
    DateAndTime getDateAndTime();

    MeasurementsInterface getMeasurements();

    String getSerialNumber();

    Settings getSettings();

    Status getStatus();
}
