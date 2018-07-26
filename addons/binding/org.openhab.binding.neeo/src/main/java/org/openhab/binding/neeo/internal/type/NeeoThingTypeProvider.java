/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.type.ThingType;

/**
 * Extends the ThingTypeProvider to manually add a ThingType.
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public interface NeeoThingTypeProvider extends ThingTypeProvider {

    /**
     * Adds the ThingType to this provider.
     *
     * @param thingType the thing type
     */
    public void addThingType(ThingType thingType);

}
