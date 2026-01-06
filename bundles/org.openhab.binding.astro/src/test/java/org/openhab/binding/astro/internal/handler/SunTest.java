/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.astro.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.astro.internal.model.Range;
import org.openhab.binding.astro.internal.model.Sun;
import org.openhab.binding.astro.internal.model.SunPhaseName;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.StringType;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.UnDefType;

/***
 * A set of standard unit test of {@link Sun} class. In particular it checks if
 * {@link Sun#getAllRanges()} contains a correct {@link SunPhaseName}.
 *
 * @author Witold Markowski - Initial contribution
 * @see <a href="https://github.com/openhab/openhab-addons/issues/5006">[astro]
 *      Sun Phase returns UNDEF</a>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class SunTest {

    private @Nullable Sun sun;

    private @Mock @NonNullByDefault({}) TimeZoneProvider timeZoneProvider;
    private @Mock @NonNullByDefault({}) Thing thing;
    private @Mock @NonNullByDefault({}) CronScheduler scheduler;
    private @Mock @NonNullByDefault({}) LocaleProvider localeProvider;
    private @Mock @NonNullByDefault({}) Channel channel;

    private @NonNullByDefault({}) SunHandler handler;

    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("Asia/Gaza");

    @BeforeEach
    public void init() {
        sun = new Sun();
        when(timeZoneProvider.getTimeZone()).thenReturn(TIME_ZONE.toZoneId());
        when(localeProvider.getLocale()).thenReturn(Locale.ROOT);
        handler = new SunHandler(thing, scheduler, timeZoneProvider, localeProvider);
    }

    @Test
    public void testConstructor() throws Exception {
        Sun sun = this.sun;
        assertNotNull(sun);
        assertNotNull(sun.getPhase());

        when(channel.getUID()).thenReturn(new ChannelUID("astro:sun:home:phase#name"));
        assertEquals(UnDefType.UNDEF, handler.getState(channel));
    }

    @Test
    public void testGetStateWhenNullPhaseName() throws Exception {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.getPhase().setName(null);

        when(channel.getUID()).thenReturn(new ChannelUID("astro:sun:home:phase#name"));
        assertEquals(UnDefType.UNDEF, handler.getState(channel));
    }

    @Test
    public void testGetStateWhenNotNullPhaseName() throws Exception {
        Sun sun = this.sun;
        assertNotNull(sun);
        handler.publishPositionalInfo();
        sun = (Sun) handler.getPlanet();
        if (sun != null) {
            sun.getPhase().setName(SunPhaseName.DAYLIGHT);
        }

        when(channel.getUID()).thenReturn(new ChannelUID("astro:sun:home:phase#name"));
        assertEquals(new StringType("DAYLIGHT"), handler.getState(channel));
    }

    @Test
    public void testGetAllRangesForNight() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setNight(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.NIGHT));
    }

    @Test
    public void testGetAllRangesForMorningNight() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setMorningNight(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.MORNING_NIGHT));
    }

    @Test
    public void testGetAllRangesForAstroDawn() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setAstroDawn(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.ASTRO_DAWN));
    }

    @Test
    public void testGetAllRangesForNauticDawn() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setNauticDawn(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.NAUTIC_DAWN));
    }

    @Test
    public void testGetAllRangesForCivilDawn() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setCivilDawn(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.CIVIL_DAWN));
    }

    @Test
    public void testGetAllRangesForRise() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setRise(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.SUN_RISE));
    }

    @Test
    public void testGetAllRangesForDaylight() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setDaylight(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.DAYLIGHT));
    }

    @Test
    public void testGetAllRangesForNoon() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setNoon(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.NOON));
    }

    @Test
    public void testGetAllRangesForSet() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setSet(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.SUN_SET));
    }

    @Test
    public void testGetAllRangesForCivilDusk() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setCivilDusk(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.CIVIL_DUSK));
    }

    @Test
    public void testGetAllRangesForNauticDusk() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setNauticDusk(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.NAUTIC_DUSK));
    }

    @Test
    public void testGetAllRangesForAstroDusk() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setAstroDusk(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.ASTRO_DUSK));
    }

    @Test
    public void testGetAllRangesForEveningNight() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setEveningNight(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhaseName.EVENING_NIGHT));
    }
}
