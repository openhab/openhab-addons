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
package org.openhab.binding.lametrictime.internal.api.local.dto;

import java.util.SortedMap;

/**
 * Pojo for update action.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class UpdateAction extends Action {
    @Override
    public UpdateAction withId(String id) {
        super.setId(id);
        return this;
    }

    @Override
    public UpdateAction withParameters(SortedMap<String, Parameter> parameters) {
        super.setParameters(parameters);
        return this;
    }
}
