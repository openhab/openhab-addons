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
package org.openhab.binding.astro.internal.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.astro.internal.util.PropertyUtils;

/***
 * A set of standard unit test of {@link Sun} class. In particular it checks if
 * {@link Sun#getAllRanges()} contains a correct {@link SunPhaseName}.
 * 
 * @author Witold Markowski - Initial contribution
 * @see <a href="https://github.com/openhab/openhab2-addons/issues/5006">[astro]
 *      Sun Phase returns UNDEF</a>
 */
public class SunTest {

    private Sun sun;

    @Before
    public void init() {
        sun = new Sun();
    }

    @Test
    public void testConstructor() throws Exception {
        assertNotNull(sun.getPhase());
        assertEquals(UnDefType.UNDEF, PropertyUtils.getState(new ChannelUID("astro:sun:home:phase#name"), null, sun));
    }

    @Test
    public void testGetStateWhenNullPhaseName() throws Exception {
        sun.getPhase().setName(null);

        assertEquals(UnDefType.UNDEF, PropertyUtils.getState(new ChannelUID("astro:sun:home:phase#name"), null, sun));
    }

    @Test
    public void testGetStateWhenNotNullPhaseName() throws Exception {
        sun.getPhase().setName(SunPhaseName.DAYLIGHT);

        assertEquals(new StringType("DAYLIGHT"),
                PropertyUtils.getState(new ChannelUID("astro:sun:home:phase#name"), null, sun));
    }

    @Test(expected = NullPointerException.class)
    public void testGetStateWhenNullPhase() throws Exception {
        sun.setPhase(null);

        assertNull(sun.getPhase());
        assertEquals(UnDefType.UNDEF, PropertyUtils.getState(new ChannelUID("astro:sun:home:phase#name"), null, sun));
    }

    @Test
    public void testGetAllRangesForNight() {
        sun.setNight(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.NIGHT));
    }

    @Test
    public void testGetAllRangesForMorningNight() {
        sun.setMorningNight(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.MORNING_NIGHT));
    }

    @Test
    public void testGetAllRangesForAstroDawn() {
        sun.setAstroDawn(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.ASTRO_DAWN));
    }

    @Test
    public void testGetAllRangesForNauticDawn() {
        sun.setNauticDawn(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.NAUTIC_DAWN));
    }

    @Test
    public void testGetAllRangesForCivilDawn() {
        sun.setCivilDawn(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.CIVIL_DAWN));
    }

    @Test
    public void testGetAllRangesForRise() {
        sun.setRise(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.SUN_RISE));
    }

    @Test
    public void testGetAllRangesForDaylight() {
        sun.setDaylight(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.DAYLIGHT));
    }

    @Test
    public void testGetAllRangesForNoon() {
        sun.setNoon(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.NOON));
    }

    @Test
    public void testGetAllRangesForSet() {
        sun.setSet(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.SUN_SET));
    }

    @Test
    public void testGetAllRangesForCivilDusk() {
        sun.setCivilDusk(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.CIVIL_DUSK));
    }

    @Test
    public void testGetAllRangesForNauticDusk() {
        sun.setNauticDusk(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.NAUTIC_DUSK));
    }

    @Test
    public void testGetAllRangesForAstroDusk() {
        sun.setAstroDusk(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.ASTRO_DUSK));
    }

    @Test
    public void testGetAllRangesForEveningNight() {
        sun.setEveningNight(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.EVENING_NIGHT));
    }
}
