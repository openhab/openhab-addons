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
package org.openhab.binding.jeelink.internal.pca301;

import org.openhab.binding.jeelink.internal.JeeLinkBindingConstants;
import org.openhab.binding.jeelink.internal.JeeLinkReadingConverter;
import org.openhab.binding.jeelink.internal.JeeLinkSensorHandler;
import org.openhab.binding.jeelink.internal.SensorDefinition;
import org.openhab.core.thing.Thing;

/**
 * Sensor Defintion of a PCA301 power switchable outlet.
 *
 * @author Volker Bier - Initial contribution
 */
public class Pca301SensorDefinition extends SensorDefinition<Pca301Reading> {

    public Pca301SensorDefinition() {
        super(JeeLinkBindingConstants.PCA301_SENSOR_THING_TYPE, "PCA301 power monitoring wireless socket", "24");
    }

    @Override
    public JeeLinkReadingConverter<Pca301Reading> createConverter() {
        return new Pca301ReadingConverter();
    }

    @Override
    public Class<Pca301Reading> getReadingClass() {
        return Pca301Reading.class;
    }

    @Override
    public JeeLinkSensorHandler<Pca301Reading> createHandler(Thing thing) {
        return new Pca301SensorHandler(thing, type);
    }
}
