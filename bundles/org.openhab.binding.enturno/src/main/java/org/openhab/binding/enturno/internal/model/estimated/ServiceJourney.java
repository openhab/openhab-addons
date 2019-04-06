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
 * Generated Plain Old Java Objects class for {@link ServiceJourney} from JSON.
 *
 * @author Michal Kloc - Initial contribution
 */
public class ServiceJourney
{
    private JourneyPattern journeyPattern;

    public JourneyPattern getJourneyPattern ()
    {
        return journeyPattern;
    }

    public void setJourneyPattern (JourneyPattern journeyPattern)
    {
        this.journeyPattern = journeyPattern;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [journeyPattern = "+journeyPattern+"]";
    }
}
