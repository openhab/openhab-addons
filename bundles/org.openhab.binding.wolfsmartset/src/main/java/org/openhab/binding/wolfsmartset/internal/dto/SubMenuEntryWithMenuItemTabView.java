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
package org.openhab.binding.wolfsmartset.internal.dto;

/**
 * Link the SubMenuEntryDTO with the MenuItemTabViewDTO
 * 
 * @author Bo Biene - Initial contribution
 */
public class SubMenuEntryWithMenuItemTabView {
    public SubMenuEntryDTO subMenuEntryDTO;
    public MenuItemTabViewDTO menuItemTabViewDTO;

    public SubMenuEntryWithMenuItemTabView(SubMenuEntryDTO subMenuEntryDTO, MenuItemTabViewDTO menuItemTabViewDTO) {
        this.subMenuEntryDTO = subMenuEntryDTO;
        this.menuItemTabViewDTO = menuItemTabViewDTO;
    }
}
