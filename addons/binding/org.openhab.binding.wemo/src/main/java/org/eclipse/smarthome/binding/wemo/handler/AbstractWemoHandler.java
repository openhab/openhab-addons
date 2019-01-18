/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.wemo.handler;

import org.eclipse.smarthome.binding.wemo.internal.http.WemoHttpCall;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;

/**
 *
 * @author Stefan Triller
 *
 */
public abstract class AbstractWemoHandler extends BaseThingHandler {

    public AbstractWemoHandler(Thing thing) {
        super(thing);
    }

    protected WemoHttpCall wemoHttpCaller;

    public void setWemoHttpCaller(WemoHttpCall wemoHttpCaller) {
        this.wemoHttpCaller = wemoHttpCaller;
    }
}
