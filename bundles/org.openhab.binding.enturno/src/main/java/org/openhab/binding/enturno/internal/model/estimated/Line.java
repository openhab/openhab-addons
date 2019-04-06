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
package org.openhab.binding.enturno.internal.model.estimated;

/**
 * Generated Plain Old Java Objects class for {@link Line} from JSON.
 *
 * @author Michal Kloc - Initial contribution
 */
public class Line
{
    private String name;

    private String transportMode;

    private String publicCode;

    private String id;

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public String getTransportMode ()
    {
        return transportMode;
    }

    public void setTransportMode (String transportMode)
    {
        this.transportMode = transportMode;
    }

    public String getPublicCode ()
    {
        return publicCode;
    }

    public void setPublicCode (String publicCode)
    {
        this.publicCode = publicCode;
    }

    public String getId ()
    {
        return id;
    }

    public void setId (String id)
    {
        this.id = id;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [name = "+name+", transportMode = "+transportMode+", publicCode = "+publicCode+", id = "+id+"]";
    }
}
