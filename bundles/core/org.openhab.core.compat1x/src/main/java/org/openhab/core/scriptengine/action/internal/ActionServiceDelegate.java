/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.scriptengine.action.internal;

import org.eclipse.smarthome.model.script.engine.action.ActionService;

/**
 * This class serves as a mapping from the "old" org.openhab namespace to the new org.eclipse.smarthome
 * namespace for the action service. It wraps an instance with the old interface
 * into a class with the new interface.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class ActionServiceDelegate implements ActionService {

    private org.openhab.core.scriptengine.action.ActionService service;

    public ActionServiceDelegate(org.openhab.core.scriptengine.action.ActionService service) {
        this.service = service;
    }

    @Override
    public String getActionClassName() {
        return service.getActionClassName();
    }

    @Override
    public Class<?> getActionClass() {
        return service.getActionClass();
    }

}
