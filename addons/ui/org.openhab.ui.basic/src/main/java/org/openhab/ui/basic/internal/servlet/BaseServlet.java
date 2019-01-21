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
package org.openhab.ui.basic.internal.servlet;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.io.http.servlet.SmartHomeBundleServlet;

/**
 * This is the base servlet class for other servlet in the Basic UI.
 *
 * @author Thomas.Eichstaedt-Engelen
 */
public abstract class BaseServlet extends SmartHomeBundleServlet {

    private static final long serialVersionUID = -4012800772403491132L;

    /** the root path of this web application */
    public static final String WEBAPP_ALIAS = "/basicui";

    protected ItemRegistry itemRegistry;

    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

}
