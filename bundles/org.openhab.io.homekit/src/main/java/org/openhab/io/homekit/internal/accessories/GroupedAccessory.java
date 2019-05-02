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
package org.openhab.io.homekit.internal.accessories;

import org.openhab.io.homekit.internal.HomekitTaggedItem;

import com.beowulfe.hap.HomekitAccessory;

/**
 * An accessory that is too complex to be represented by a single item. A
 * grouped accessory is made up of multiple items, each implementing a single
 * characteristic of the accessory.
 *
 * @author Andy Lintner - Initial contribution
 */
public interface GroupedAccessory extends HomekitAccessory {

    public String getGroupName();

    public void addCharacteristic(HomekitTaggedItem item);

    public boolean isComplete();
}
