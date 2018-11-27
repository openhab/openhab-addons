/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.discovery.project;

/**
 * An output device in a Lutron system such as a switch or dimmer.
 *
 * @author Allan Tong - Initial contribution
 */
public class Output {
    private String name;
    private Integer integrationId;
    private String type;

    public String getName() {
        return name;
    }

    public Integer getIntegrationId() {
        return integrationId;
    }

    public String getType() {
        return type;
    }

    public OutputType getOutputType() {
        try {
            return OutputType.valueOf(this.type);
        } catch (Exception e) {
            return null;
        }
    }
}
