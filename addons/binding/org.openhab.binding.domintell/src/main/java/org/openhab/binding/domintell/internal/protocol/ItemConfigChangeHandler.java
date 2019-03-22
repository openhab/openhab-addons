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
package org.openhab.binding.domintell.internal.protocol;

import org.openhab.binding.domintell.internal.protocol.model.Item;

/**
* The {@link ItemConfigChangeHandler} interface is used to handle item group changes
*
* @author Gabor Bicskei - Initial contribution
*/
public interface ItemConfigChangeHandler {
    void groupItemsChanged(Item item);
    void groupItemsTranslated();
}
