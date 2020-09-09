/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.ZonedDateTime;

import javax.measure.quantity.Angle;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.astro.internal.AstroBindingConstants;
import org.openhab.binding.astro.internal.handler.AstroThingHandler;
import org.openhab.binding.astro.internal.handler.SunHandler;
import org.openhab.binding.astro.internal.model.SunPhaseName;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {AstroActions } defines rule actions for the Astro binding.
 * <p>
 * <b>Note:</b>The static method <b>invokeMethodOf</b> handles the case where
 * the test <i>actions instanceof AstroActions</i> fails. This test can fail
 * due to an issue in openHAB core v2.5.0 where the {@link AstroActions} class
 * can be loaded by a different classloader than the <i>actions</i> instance.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@ThingActionsScope(name = "astro")
@NonNullByDefault
public class AstroActions implements ThingActions, IAstroActions {

    private final Logger logger = LoggerFactory.getLogger(AstroActions.class);
    protected @Nullable AstroThingHandler handler;

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
        return this.handler;
    }

    @Override
    @RuleAction(label = "Astro : Get Azimuth", description = "Get the azimuth of the sun for a given time")
    public @Nullable @ActionOutput(name = "getAzimuth", label = "Azimuth", type = "org.eclipse.smarthome.core.library.types.QuantityType<javax.measure.quantity.Angle>") QuantityType<Angle> getAzimuth(
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

    @Override
    @RuleAction(label = "Astro : Get Elevation", description = "Get the Elevation of the sun for a given time")
    public @Nullable @ActionOutput(name = "getElevation", label = "Elevation", type = "org.eclipse.smarthome.core.library.types.QuantityType<javax.measure.quantity.Angle>") QuantityType<Angle> getElevation(
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

    @Override
    @RuleAction(label = "Sun : Get Event Time", description = "Get the date time of a given planet event")
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

    public static @Nullable QuantityType<Angle> getElevation(@Nullable ThingActions actions,
            @Nullable ZonedDateTime date) {
        return invokeMethodOf(actions).getElevation(date);
    }

    public static @Nullable QuantityType<Angle> getAzimuth(@Nullable ThingActions actions,
            @Nullable ZonedDateTime date) {
        return invokeMethodOf(actions).getAzimuth(date);
    }

    public static @Nullable ZonedDateTime getEventTime(@Nullable ThingActions actions, @Nullable String phaseName,
            @Nullable ZonedDateTime date, @Nullable String moment) {
        if (phaseName != null) {
            return invokeMethodOf(actions).getEventTime(phaseName, date, moment);
        } else {
            throw new IllegalArgumentException("phaseName can not be null");
        }
    }

    private static IAstroActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(AstroActions.class.getName())) {
            if (actions instanceof IAstroActions) {
                return (IAstroActions) actions;
            } else {
                return (IAstroActions) Proxy.newProxyInstance(IAstroActions.class.getClassLoader(),
                        new Class[] { IAstroActions.class }, (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException("Actions is not an instance of AstroActions");
    }
}
