/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.type;

import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.type.ThingType;

/**
 * Extends the ThingTypeProvider to manually add a ThingType.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface HomematicThingTypeProvider extends ThingTypeProvider {

    /**
     * Adds the ThingType to this provider.
     */
    public void addThingType(ThingType thingType);

}