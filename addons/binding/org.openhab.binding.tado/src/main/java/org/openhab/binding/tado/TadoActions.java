/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tado;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.model.script.engine.action.ActionDoc;
import org.openhab.binding.tado.internal.TadoActionService;
import org.openhab.binding.tado.internal.TadoHvacChange;

/**
 * Action class to simplify HVAC change requests from rules.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
public class TadoActions {
    @ActionDoc(text = "Create a new HVAC change builder for the given zone")
    public static TadoHvacChange tadoHvacChange(String name) {
        return TadoActionService.getHvacChange(name);
    }

    @ActionDoc(text = "Create a new HVAC change builder for the given zone")
    public static TadoHvacChange tadoHvacChange(Thing zone) {
        return TadoActionService.getHvacChange(zone);
    }
}
