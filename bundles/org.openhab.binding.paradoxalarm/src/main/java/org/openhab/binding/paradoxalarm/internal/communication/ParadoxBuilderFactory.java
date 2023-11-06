/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.paradoxalarm.internal.communication;

import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxRuntimeException;
import org.openhab.binding.paradoxalarm.internal.model.PanelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ParadoxBuilderFactory} used to create the proper communicator builder objects for different panel
 * types.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class ParadoxBuilderFactory {

    private final Logger logger = LoggerFactory.getLogger(ParadoxBuilderFactory.class);

    public ICommunicatorBuilder createBuilder(PanelType panelType) {
        switch (panelType) {
            case EVO48:
            case EVO96:
            case EVO192:
            case EVOHD:
                logger.debug("Creating new builder for Paradox {} system", panelType);
                return new EvoCommunicator.EvoCommunicatorBuilder(panelType);
            default:
                logger.debug("Unsupported panel type: {}", panelType);
                throw new ParadoxRuntimeException("Unsupported panel type: " + panelType);
        }
    }
}
