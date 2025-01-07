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
package org.openhab.binding.livisismarthome.internal.client.api.entity.action;

import org.openhab.binding.livisismarthome.internal.client.api.entity.link.LinkDTO;

/**
 * Special {@link ActionDTO} to execute a restart.
 *
 * @author Sven Strohschein - Initial contribution
 */
public class RestartActionDTO extends ActionDTO {

    private static final String ACTION_TYPE_RESTART = "Restart";
    private static final String CONSTANT = "Constant";
    private static final String DEFAULT_RESTART_REASON = "The openHAB binding requested to restart the smart home controller.";

    public RestartActionDTO(String deviceId) {
        setType(ACTION_TYPE_RESTART);
        setTarget(LinkDTO.LINK_TYPE_DEVICE + deviceId);

        final ActionParamsDTO params = new ActionParamsDTO();
        params.setReason(new StringActionParamDTO(CONSTANT, DEFAULT_RESTART_REASON));
        setParams(params);
    }
}
