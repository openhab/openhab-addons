/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.webthings.internal.dto;

import java.util.Map;

import org.eclipse.smarthome.core.items.dto.ItemDTO;
import org.eclipse.smarthome.core.types.CommandDescription;
import org.eclipse.smarthome.core.types.StateDescription;

/**
 * The {@link CompleteItemDTO}
 *
 * @author Sven Schneider - Initial contribution
 */
public class CompleteItemDTO extends ItemDTO {

    public String link;
    public String state;
    public String transformedState;
    public StateDescription stateDescription;
    public CommandDescription commandDescription;
    public Map<String, Object> metadata;
    public Boolean editable;

    public CompleteItemDTO(ItemDTO itemDTO, String link, String state, String transformedState,
            StateDescription stateDescription, CommandDescription commandDescription) {
        this.type = itemDTO.type;
        this.name = itemDTO.name;
        this.label = itemDTO.label;
        this.category = itemDTO.category;
        this.tags = itemDTO.tags;
        this.groupNames = itemDTO.groupNames;
        this.link = link;
        this.state = state;
        this.transformedState = transformedState;
        this.stateDescription = stateDescription;
        this.commandDescription = commandDescription;
    }
}
