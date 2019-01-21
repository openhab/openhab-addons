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
package org.openhab.ui.classic.internal.render;

/**
 * An exception used by {@link WidgetRenderer}s, if an error occurs.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class RenderException extends Exception {

    private static final long serialVersionUID = -3801828613192343641L;

    public RenderException(String msg) {
        super(msg);
    }

}
