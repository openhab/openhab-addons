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
package org.openhab.binding.airvisualnode.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for AirVisual and AirVisual Pro models measurements data
 *
 * @author Oleg Davydyuk - Initial contribution
 */
@NonNullByDefault
public interface MeasurementsInterface {
    int getCo2Ppm();

    int getHumidityRH();

    int getPm25AQICN();

    int getPm25AQIUS();

    float getPm01Ugm3();

    float getPm10Ugm3();

    float getPm25Ugm3();

    float getTemperatureC();

    float getTemperatureF();

    int getVocPpb();
}
