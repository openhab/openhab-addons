/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.model;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Simple class with the XStream mapping for a list of entries returned from a TclRega script.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@XStreamAlias("list")
public class TclScriptDataList {

    @XStreamImplicit(itemFieldName = "entry")
    private List<TclScriptDataEntry> entries = new ArrayList<TclScriptDataEntry>();

    /**
     * Returns all entries.
     */
    public List<TclScriptDataEntry> getEntries() {
        return entries;
    }

}
