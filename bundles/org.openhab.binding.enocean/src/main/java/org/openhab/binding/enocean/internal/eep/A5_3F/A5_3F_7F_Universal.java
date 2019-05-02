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
package org.openhab.binding.enocean.internal.eep.A5_3F;

import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.transform.actions.Transformation;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.enocean.internal.config.EnOceanChannelTransformationConfig;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class A5_3F_7F_Universal extends _4BSMessage {

    // This class is currently not used => instead use Generic4BS

    public A5_3F_7F_Universal() {
        super();
    }

    public A5_3F_7F_Universal(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command command,
            Map<String, State> currentState, Configuration config) {

        if (config != null) {
            try {
                EnOceanChannelTransformationConfig transformationInfo = config
                        .as(EnOceanChannelTransformationConfig.class);
                String c = Transformation.transform(transformationInfo.transformationType,
                        transformationInfo.transformationFunction, command.toString());

                if (c != null && c != command.toString()) {
                    setData(HexUtils.hexToBytes(c));
                }

            } catch (Exception e) {
                logger.debug("Command {} could not transformed", command.toString());
            }
        }
    }
}
