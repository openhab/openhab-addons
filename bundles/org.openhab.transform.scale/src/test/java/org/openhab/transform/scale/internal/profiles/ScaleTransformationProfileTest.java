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
package org.openhab.transform.scale.internal.profiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.TimeSeries.Entry;
import org.openhab.core.types.TimeSeries.Policy;

/**
 * Tests for the scale profile
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class ScaleTransformationProfileTest {

    private static final String MY_SCALE_FUNCTION = "myScaleFunction";

    @Test
    public void testTimeSeriesFromHandler() throws TransformationException {
        String scaledStringValue = "scaled";

        ProfileCallback callback = mock(ProfileCallback.class);
        TransformationService transformationService = mock(TransformationService.class);
        when(transformationService.transform(any(), any())).thenReturn(scaledStringValue);

        ScaleTransformationProfile scaleProfile = createProfile(callback, transformationService, MY_SCALE_FUNCTION);

        TimeSeries ts = new TimeSeries(Policy.ADD);
        Instant now = Instant.now();
        ts.add(now, new DecimalType(23));

        scaleProfile.onTimeSeriesFromHandler(ts);

        ArgumentCaptor<TimeSeries> capture = ArgumentCaptor.forClass(TimeSeries.class);
        verify(callback, times(1)).sendTimeSeries(capture.capture());

        TimeSeries result = capture.getValue();
        assertEquals(ts.getStates().count(), result.getStates().count());
        Entry entry = result.getStates().findFirst().get();
        assertNotNull(entry);
        assertEquals(now, entry.timestamp());
        StringType strResult = (StringType) entry.state();
        assertEquals(scaledStringValue, strResult.toString());
    }

    private ScaleTransformationProfile createProfile(ProfileCallback callback,
            TransformationService transformationService, String function) {
        ProfileContext context = mock(ProfileContext.class);
        Configuration config = new Configuration();
        config.put(ScaleTransformationProfile.FUNCTION_PARAM, function);
        when(context.getConfiguration()).thenReturn(config);

        return new ScaleTransformationProfile(callback, context, transformationService);
    }
}
