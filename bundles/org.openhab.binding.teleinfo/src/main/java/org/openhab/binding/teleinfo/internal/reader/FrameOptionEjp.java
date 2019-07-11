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
package org.openhab.binding.teleinfo.internal.reader;

/**
 * The {@link FrameOptionEjp} class defines a Teleinfo frame with EJP option.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public class FrameOptionEjp extends Frame {

    private static final long serialVersionUID = -1934715078822532494L;

    // EJP HN : Index heures normales si option = EJP (en Wh)
    // EJP HPM : Index heures de pointe mobile si option = EJP (en Wh)
    // PEJP : Préavis EJP si option = EJP 30mn avant période EJP

    public FrameOptionEjp() {
        // default constructor
    }

}
