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
package org.openhab.binding.net.internal.automation.modules;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.net.internal.handler.DataHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crccalc.CrcCalculator;

/**
 * Some automation actions to be used with a {@link NetThingActionsService}
 *
 * @author Pauli Anttila - initial contribution
 *
 */
@ThingActionsScope(name = "net")
@NonNullByDefault
public class NetThingActionsService implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(NetThingActionsService.class);

    private @Nullable DataHandler handler;

    @RuleAction(label = "Net inject data", description = "Action to inject data to data handler instance")
    public void injectData(@ActionInput(name = "hexaString") @Nullable String hexaString) {
        logger.debug("injectData called, data: '{}'", hexaString);

        if (handler == null) {
            throw new IllegalArgumentException("Net Action service ThingHandler can't be null!");
        } else if (hexaString == null) {
            throw new IllegalArgumentException("Net Action service parameters can't be null!");
        }

        if (handler != null) {
            handler.injectData(hexaString);
        }
    }

    public static void injectData(@Nullable ThingActions actions, @Nullable String hexaString) {
        if (actions instanceof NetThingActionsService) {
            ((NetThingActionsService) actions).injectData(hexaString);
        } else {
            throw new IllegalArgumentException("Instance is not an NetThingActionsService class.");
        }
    }

    @RuleAction(label = "Net calculate crc", description = "Action to calculate CRC cheksum over hexa string formatted binary data")
    public @ActionOutput(name = "crc", type = "java.lang.Long") Long calculateCrc(
            @ActionInput(name = "algo") @Nullable String algorithm,
            @ActionInput(name = "hexaString") @Nullable String hexaString) {
        logger.debug("calculateCrc called with algorithm: '{}' data: '{}'", algorithm, hexaString);

        if (handler == null) {
            throw new IllegalArgumentException("Net Action service ThingHandler can't be null!");
        } else if (algorithm == null || hexaString == null) {
            throw new IllegalArgumentException("Net Action service parameters can't be null!");
        }

        byte[] data = HexUtils.hexToBytes(hexaString);
        return new CrcCalculator(algorithm).Calc(data, 0, data.length);
    }

    public static Long calculateCrc(@Nullable ThingActions actions, @Nullable String algorithm,
            @Nullable String hexaString) {
        if (actions instanceof NetThingActionsService) {
            return ((NetThingActionsService) actions).calculateCrc(algorithm, hexaString);
        } else {
            throw new IllegalArgumentException("Instance is not an NetThingActionsService class.");
        }
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof DataHandler) {
            this.handler = (DataHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }
}
