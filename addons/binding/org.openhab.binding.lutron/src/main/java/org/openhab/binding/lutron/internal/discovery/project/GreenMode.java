/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.discovery.project;

/**
 * A Green Mode subsystem in a Lutron RadioRA2 controller
 *
 * @author Bob Adair - Initial contribution
 */
public class GreenMode {

    private String name;
    private Integer integrationId;

    public String getName() {
        // There may be no name in the XML document
        return name != null ? name : "Green Mode Subsystem";
    }

    public Integer getIntegrationId() {
        return integrationId;
    }

}
