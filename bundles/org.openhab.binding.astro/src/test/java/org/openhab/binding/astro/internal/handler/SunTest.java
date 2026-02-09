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

import java.time.Instant;
import java.time.InstantSource;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.astro.internal.model.Range;
import org.openhab.binding.astro.internal.model.Sun;
import org.openhab.binding.astro.internal.model.SunPhase;
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
 * {@link Sun#getAllRanges()} contains a correct {@link SunPhase}.
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
        handler = new SunHandler(thing, scheduler, timeZoneProvider, localeProvider,
                InstantSource.fixed(Instant.ofEpochMilli(1645671600000L)));
    }

    @AfterEach
    public void dispose() {
        handler.dispose();
    }

    @Test
    public void testConstructor() throws Exception {
        Sun sun = this.sun;
        assertNotNull(sun);

        when(channel.getUID()).thenReturn(new ChannelUID("astro:sun:home:phase#name"));
        assertEquals(UnDefType.UNDEF, handler.getState(channel));
    }

    @Test
    public void testGetStateWhenNullPhaseName() throws Exception {
        handler.publishDailyInfo();
        Sun sun = handler.sun;
        assertNotNull(sun);
        sun.setSunPhase(null);

        when(channel.getUID()).thenReturn(new ChannelUID("astro:sun:home:phase#name"));
        assertEquals(UnDefType.UNDEF, handler.getState(channel));
    }

    @Test
    public void testGetStateWhenNotNullPhaseName() throws Exception {
        handler.publishPositionalInfo();
        when(channel.getUID()).thenReturn(new ChannelUID("astro:sun:home:phase#name"));
        assertEquals(new StringType("NIGHT"), handler.getState(channel));
    }

    @Test
    public void testGetAllRangesForNight() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setRange(SunPhase.NIGHT, new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhase.NIGHT));
    }

    @Test
    public void testGetAllRangesForMorningNight() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setRange(SunPhase.MORNING_NIGHT, new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhase.MORNING_NIGHT));
    }

    @Test
    public void testGetAllRangesForAstroDawn() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setRange(SunPhase.ASTRO_DAWN, new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhase.ASTRO_DAWN));
    }

    @Test
    public void testGetAllRangesForNauticDawn() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setRange(SunPhase.NAUTIC_DAWN, new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhase.NAUTIC_DAWN));
    }

    @Test
    public void testGetAllRangesForCivilDawn() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setRange(SunPhase.CIVIL_DAWN, new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhase.CIVIL_DAWN));
    }

    @Test
    public void testGetAllRangesForRise() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setRise(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhase.SUN_RISE));
    }

    @Test
    public void testGetAllRangesForDaylight() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setRange(SunPhase.DAYLIGHT, new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhase.DAYLIGHT));
    }

    @Test
    public void testGetAllRangesForNoon() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setRange(SunPhase.NOON, new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhase.NOON));
    }

    @Test
    public void testGetAllRangesForSet() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setSet(new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhase.SUN_SET));
    }

    @Test
    public void testGetAllRangesForCivilDusk() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setRange(SunPhase.CIVIL_DUSK, new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhase.CIVIL_DUSK));
    }

    @Test
    public void testGetAllRangesForNauticDusk() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setRange(SunPhase.NAUTIC_DUSK, new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhase.NAUTIC_DUSK));
    }

    @Test
    public void testGetAllRangesForAstroDusk() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setRange(SunPhase.ASTRO_DUSK, new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhase.ASTRO_DUSK));
    }

    @Test
    public void testGetAllRangesForEveningNight() {
        Sun sun = this.sun;
        assertNotNull(sun);
        sun.setRange(SunPhase.EVENING_NIGHT, new Range());

        assertTrue(sun.getAllRanges().containsKey(SunPhase.EVENING_NIGHT));
    }
}
