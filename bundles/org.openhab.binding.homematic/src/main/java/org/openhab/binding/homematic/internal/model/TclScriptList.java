/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.homematic.internal.model;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Simple class with the XStream mapping for a list of TclRega scripts. Used to load the resource
 * homematic/tclrega-scripts.xml.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@XStreamAlias("scripts")
public class TclScriptList {

    @XStreamImplicit
    private List<TclScript> scripts = new ArrayList<>();

    /**
     * Returns all scripts.
     */
    public List<TclScript> getScripts() {
        return scripts;
    }
}
