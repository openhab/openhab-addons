/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.astro.internal.action;

import java.time.ZonedDateTime;

import javax.measure.quantity.Angle;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.AstroBindingConstants;
import org.openhab.binding.astro.internal.handler.AstroThingHandler;
import org.openhab.binding.astro.internal.handler.SunHandler;
import org.openhab.binding.astro.internal.model.Radiation;
import org.openhab.binding.astro.internal.model.SunPhaseName;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.library.dimension.Intensity;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the automation thing actions for the Astro binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@ThingActionsScope(name = "astro")
@NonNullByDefault
public class AstroActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(AstroActions.class);
    private @Nullable AstroThingHandler handler;

    public AstroActions() {
        logger.debug("Astro actions service instanciated");
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof AstroThingHandler) {
            this.handler = (AstroThingHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "get the azimuth", description = "Get the azimuth for a given time.")
    public @Nullable @ActionOutput(name = "getAzimuth", label = "Azimuth", type = "org.openhab.core.library.types.QuantityType<javax.measure.quantity.Angle>") QuantityType<Angle> getAzimuth(
            @ActionInput(name = "date", label = "Date", required = false, description = "Considered date") @Nullable ZonedDateTime date) {
        logger.debug("Astro action 'getAzimuth' called");
        AstroThingHandler theHandler = this.handler;
        if (theHandler != null) {
            return theHandler.getAzimuth(date != null ? date : ZonedDateTime.now());
        } else {
            logger.info("Astro Action service ThingHandler is null!");
        }
        return null;
    }

    @RuleAction(label = "get the elevation", description = "Get the elevation for a given time.")
    public @Nullable @ActionOutput(name = "getElevation", label = "Elevation", type = "org.openhab.core.library.types.QuantityType<javax.measure.quantity.Angle>") QuantityType<Angle> getElevation(
            @ActionInput(name = "date", label = "Date", required = false, description = "Considered date") @Nullable ZonedDateTime date) {
        logger.debug("Astro action 'getElevation' called");
        AstroThingHandler theHandler = this.handler;
        if (theHandler != null) {
            return theHandler.getElevation(date != null ? date : ZonedDateTime.now());
        } else {
            logger.info("Astro Action service ThingHandler is null!");
        }
        return null;
    }

    @RuleAction(label = "get the total sun radiation", description = "Get the total sun radiation for a given time.")
    public @Nullable @ActionOutput(name = "getTotalRadiation", label = "Total Radiation", type = "org.openhab.core.library.types.QuantityType<org.openhab.core.library.dimension.Intensity>") QuantityType<Intensity> getTotalRadiation(
            @ActionInput(name = "date", label = "Date", required = false, description = "Considered date") @Nullable ZonedDateTime date) {
        logger.debug("Astro action 'getTotalRadiation' called");
        AstroThingHandler theHandler = this.handler;
        if (theHandler != null) {
            if (theHandler instanceof SunHandler sunHandler) {
                Radiation radiation = sunHandler.getRadiationAt(date != null ? date : ZonedDateTime.now());
                return radiation.getTotal();
            } else {
                logger.info("Astro Action service ThingHandler is not a SunHandler!");
            }
        } else {
            logger.info("Astro Action service ThingHandler is null!");
        }
        return null;
    }

    @RuleAction(label = "get the date time of a sun event", description = "Get the date time of a sun event.")
    public @Nullable @ActionOutput(name = "getEventTime", type = "java.time.ZonedDateTime") ZonedDateTime getEventTime(
            @ActionInput(name = "phaseName", label = "Phase", required = true, description = "Requested phase") String phaseName,
            @ActionInput(name = "date", label = "Date", required = false, description = "Considered date") @Nullable ZonedDateTime date,
            @ActionInput(name = "moment", label = "Moment", required = false, defaultValue = "START", description = "Either START or END") @Nullable String moment) {
        logger.debug("Sun action 'getEventTime' called");
        try {
            AstroThingHandler theHandler = this.handler;
            if (theHandler != null) {
                if (theHandler instanceof SunHandler) {
                    SunHandler handler = (SunHandler) theHandler;
                    SunPhaseName phase = SunPhaseName.valueOf(phaseName.toUpperCase());
                    return handler.getEventTime(phase, date != null ? date : ZonedDateTime.now(),
                            moment == null || AstroBindingConstants.EVENT_START.equalsIgnoreCase(moment));
                } else {
                    logger.info("Astro Action service ThingHandler is not a SunHandler!");
                }
            } else {
                logger.info("Astro Action service ThingHandler is null!");
            }
        } catch (IllegalArgumentException e) {
            logger.info("Parameter {} is not a valid phase name", phaseName);
        }
        return null;
    }

    public static @Nullable QuantityType<Angle> getElevation(ThingActions actions, @Nullable ZonedDateTime date) {
        return ((AstroActions) actions).getElevation(date);
    }

    public static @Nullable QuantityType<Angle> getAzimuth(ThingActions actions, @Nullable ZonedDateTime date) {
        return ((AstroActions) actions).getAzimuth(date);
    }

    public static @Nullable QuantityType<Intensity> getTotalRadiation(ThingActions actions,
            @Nullable ZonedDateTime date) {
        return ((AstroActions) actions).getTotalRadiation(date);
    }

    public static @Nullable ZonedDateTime getEventTime(ThingActions actions, @Nullable String phaseName,
            @Nullable ZonedDateTime date, @Nullable String moment) {
        if (phaseName != null) {
            return ((AstroActions) actions).getEventTime(phaseName, date, moment);
        } else {
            throw new IllegalArgumentException("phaseName can not be null");
        }
    }
}
