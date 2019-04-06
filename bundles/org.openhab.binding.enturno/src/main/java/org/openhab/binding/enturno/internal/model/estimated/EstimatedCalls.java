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
 * Generated Plain Old Java Objects class for {@link EstimatedCalls} from JSON.
 *
 * @author Michal Kloc - Initial contribution
 */
public class EstimatedCalls
{
    private String aimedArrivalTime;

    private String date;

    private String realtime;

    private String expectedDepartureTime;

    private String forAlighting;

    private DestinationDisplay destinationDisplay;

    private String forBoarding;

    private ServiceJourney serviceJourney;

    private Quay quay;

    private String aimedDepartureTime;

    private String expectedArrivalTime;

    public String getAimedArrivalTime ()
    {
        return aimedArrivalTime;
    }

    public void setAimedArrivalTime (String aimedArrivalTime)
    {
        this.aimedArrivalTime = aimedArrivalTime;
    }

    public String getDate ()
    {
        return date;
    }

    public void setDate (String date)
    {
        this.date = date;
    }

    public String getRealtime ()
    {
        return realtime;
    }

    public void setRealtime (String realtime)
    {
        this.realtime = realtime;
    }

    public String getExpectedDepartureTime ()
    {
        return expectedDepartureTime;
    }

    public void setExpectedDepartureTime (String expectedDepartureTime)
    {
        this.expectedDepartureTime = expectedDepartureTime;
    }

    public String getForAlighting ()
    {
        return forAlighting;
    }

    public void setForAlighting (String forAlighting)
    {
        this.forAlighting = forAlighting;
    }

    public DestinationDisplay getDestinationDisplay ()
    {
        return destinationDisplay;
    }

    public void setDestinationDisplay (DestinationDisplay destinationDisplay)
    {
        this.destinationDisplay = destinationDisplay;
    }

    public String getForBoarding ()
    {
        return forBoarding;
    }

    public void setForBoarding (String forBoarding)
    {
        this.forBoarding = forBoarding;
    }

    public ServiceJourney getServiceJourney ()
    {
        return serviceJourney;
    }

    public void setServiceJourney (ServiceJourney serviceJourney)
    {
        this.serviceJourney = serviceJourney;
    }

    public Quay getQuay ()
    {
        return quay;
    }

    public void setQuay (Quay quay)
    {
        this.quay = quay;
    }

    public String getAimedDepartureTime ()
    {
        return aimedDepartureTime;
    }

    public void setAimedDepartureTime (String aimedDepartureTime)
    {
        this.aimedDepartureTime = aimedDepartureTime;
    }

    public String getExpectedArrivalTime ()
    {
        return expectedArrivalTime;
    }

    public void setExpectedArrivalTime (String expectedArrivalTime)
    {
        this.expectedArrivalTime = expectedArrivalTime;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [aimedArrivalTime = "+aimedArrivalTime+", date = "+date+", realtime = "+realtime+", expectedDepartureTime = "+expectedDepartureTime+", forAlighting = "+forAlighting+", destinationDisplay = "+destinationDisplay+", forBoarding = "+forBoarding+", serviceJourney = "+serviceJourney+", quay = "+quay+", aimedDepartureTime = "+aimedDepartureTime+", expectedArrivalTime = "+expectedArrivalTime+"]";
    }
}


