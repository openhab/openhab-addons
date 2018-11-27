/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal.accessories;

import org.openhab.io.homekit.internal.HomekitTaggedItem;

import com.beowulfe.hap.HomekitAccessory;

/**
 * An accessory that is too complex to be represented by a single item. A
 * grouped accessory is made up of multiple items, each implementing a single
 * characteristic of the accessory.
 *
 * @author Andy Lintner
 */
public interface GroupedAccessory extends HomekitAccessory {

    public String getGroupName();

    public void addCharacteristic(HomekitTaggedItem item);

    public boolean isComplete();
}
