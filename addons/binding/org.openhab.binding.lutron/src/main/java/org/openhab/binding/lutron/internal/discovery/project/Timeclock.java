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
 * A Timeclock subsystem in a Lutron RadioRA2 or HWQS controller
 *
 * @author Bob Adair - Initial contribution
 */
public class Timeclock {

    private String name;
    private Integer integrationId;

    public String getName() {
        return name;
    }

    public Integer getIntegrationId() {
        return integrationId;
    }

}
