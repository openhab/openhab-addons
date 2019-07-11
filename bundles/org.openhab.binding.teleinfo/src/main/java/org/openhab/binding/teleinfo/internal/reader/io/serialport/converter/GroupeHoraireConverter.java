/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.teleinfo.internal.reader.io.serialport.converter;

import org.openhab.binding.teleinfo.internal.reader.FrameOptionHeuresCreuses.GroupeHoraire;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.ConvertionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GroupeHoraireConverter} class defines a converter to translate a Teleinfo String value into
 * {@link GroupeHoraire} object.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public class GroupeHoraireConverter implements Converter {

    private static Logger logger = LoggerFactory.getLogger(GroupeHoraireConverter.class);

    @Override
    public Object convert(String value) throws ConvertionException {
        logger.debug("convert(String) [start]");
        if (logger.isTraceEnabled()) {
            logger.trace("value = " + value);
        }

        GroupeHoraire convertedValue = null;
        switch (value) {
            case "A":
                convertedValue = GroupeHoraire.A;
                break;
            case "C":
                convertedValue = GroupeHoraire.C;
                break;
            case "D":
                convertedValue = GroupeHoraire.D;
                break;
            case "E":
                convertedValue = GroupeHoraire.E;
                break;
            case "Y":
                convertedValue = GroupeHoraire.Y;
                break;
            default:
                throw new ConvertionException(value);
        }

        logger.debug("convert(String) [end]");
        return convertedValue;
    }

}
