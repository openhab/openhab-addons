/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.octopusenergy.internal.handler;

import static org.openhab.binding.octopusenergy.internal.OctopusEnergyBindingConstants.*;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.octopusenergy.internal.OctopusEnergyApiHelper;
import org.openhab.binding.octopusenergy.internal.dto.GasMeterPoint;
import org.openhab.binding.octopusenergy.internal.exception.RecordNotFoundException;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OctopusEnergyGasMeterPointHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class OctopusEnergyGasMeterPointHandler extends OctopusEnergyBaseMeterPointHandler {

    private final Logger logger = LoggerFactory.getLogger(OctopusEnergyGasMeterPointHandler.class);

    public OctopusEnergyGasMeterPointHandler(Thing thing, OctopusEnergyApiHelper apiHelper) {
        super(thing, apiHelper);
    }

    public GasMeterPoint getMeterPoint() throws RecordNotFoundException {
        return apiHelper.getAccounts().getGasMeterPoint(thing.getUID().getId());
    }

    @Override
    protected void updateThing() {
        try {
            GasMeterPoint meterPoint = getMeterPoint();
            logger.debug("Updating all channels for meter point : {}", meterPoint);
            updateState(CHANNEL_METERPOINT_MPRN, new StringType(meterPoint.mprn));
            try {
                updateState(CHANNEL_METERPOINT_CURRENT_TARIFF,
                        new StringType(meterPoint.getAgreementAsOf(ZonedDateTime.now()).tariffCode));
            } catch (RecordNotFoundException e) {
                updateState(CHANNEL_METERPOINT_CURRENT_TARIFF, UnDefType.UNDEF);
            }
        } catch (RecordNotFoundException e) {
            logger.debug("Trying to update unknown meter point: {}", thing.getUID().getId());
        }
    }
}
