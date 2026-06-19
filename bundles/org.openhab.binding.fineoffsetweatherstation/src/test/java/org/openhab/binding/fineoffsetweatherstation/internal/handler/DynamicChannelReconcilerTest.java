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
package org.openhab.binding.fineoffsetweatherstation.internal.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.CHANNEL_TYPE_TEMPERATURE;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.THING_TYPE_SENSOR;
import static org.openhab.binding.fineoffsetweatherstation.internal.handler.DynamicChannelReconciler.MISSING_VALUE_REMOVAL_THRESHOLD;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.MeasureType;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelKind;

/**
 * Unit tests for {@link DynamicChannelReconciler}.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
class DynamicChannelReconcilerTest {

    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_SENSOR, "testThing");

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private MeasuredValue measuredValue(String channelPrefix) {
        return new MeasuredValue(MeasureType.TEMPERATURE, channelPrefix, null, CHANNEL_TYPE_TEMPERATURE,
                new DecimalType(42), channelPrefix, null);
    }

    private Channel channel(String id) {
        return ChannelBuilder.create(new ChannelUID(THING_UID, id)).withKind(ChannelKind.STATE).build();
    }

    /**
     * Convenience: apply the plan (add/remove) to a mutable channel list, mirroring what a handler would do after
     * calling reconcile().
     */
    private List<Channel> applyPlan(List<Channel> current, DynamicChannelReconciler.Plan plan) {
        List<Channel> result = new ArrayList<>(current);
        result.addAll(plan.channelsToAdd);
        result.removeAll(plan.channelsToRemove);
        return result;
    }

    // -----------------------------------------------------------------------
    // Case 1: new value creates a channel and posts its state
    // -----------------------------------------------------------------------

    @Test
    void newValueYieldsCreatedChannelAndPostedState() {
        DynamicChannelReconciler reconciler = new DynamicChannelReconciler(false);
        MeasuredValue value = measuredValue("temperature");

        DynamicChannelReconciler.Plan plan = reconciler.reconcile(List.of(value), List.of(),
                MeasuredValue::getChannelId, v -> channel(v.getChannelId()));

        assertThat(plan.channelsToAdd).hasSize(1);
        ChannelUID createdUID = plan.channelsToAdd.get(0).getUID();
        assertThat(createdUID.getId()).isEqualTo("temperature");
        assertThat(plan.statesToPost).containsEntry(createdUID, new DecimalType(42));
        assertThat(plan.channelsToRemove).isEmpty();
    }

    // -----------------------------------------------------------------------
    // Case 2: existing channel yields no add but a posted state
    // -----------------------------------------------------------------------

    @Test
    void existingChannelYieldsNoAddButPostedState() {
        DynamicChannelReconciler reconciler = new DynamicChannelReconciler(false);
        MeasuredValue value = measuredValue("temperature");
        Channel existing = channel("temperature");

        DynamicChannelReconciler.Plan plan = reconciler.reconcile(List.of(value), List.of(existing),
                MeasuredValue::getChannelId, v -> channel(v.getChannelId()));

        assertThat(plan.channelsToAdd).isEmpty();
        assertThat(plan.statesToPost).containsKey(existing.getUID());
        assertThat(plan.channelsToRemove).isEmpty();
    }

    // -----------------------------------------------------------------------
    // Case 3: removeOnlyCreatedChannels=true never removes a non-created channel
    // -----------------------------------------------------------------------

    @Test
    void removeOnlyCreatedDoesNotRemoveNonCreatedChannel() {
        DynamicChannelReconciler reconciler = new DynamicChannelReconciler(true);
        Channel staticSignal = channel("signal");
        List<Channel> currentChannels = List.of(staticSignal);

        // Reconcile THRESHOLD times without reporting "signal"
        for (int i = 0; i < MISSING_VALUE_REMOVAL_THRESHOLD; i++) {
            DynamicChannelReconciler.Plan plan = reconciler.reconcile(List.of(), currentChannels,
                    MeasuredValue::getChannelPrefix, v -> channel(v.getChannelPrefix()));
            assertThat(plan.channelsToRemove).doesNotContain(staticSignal);
        }
    }

    // -----------------------------------------------------------------------
    // Case 4: removeOnlyCreatedChannels=true removes a reconciler-created channel
    // after threshold, keeps it below threshold
    // -----------------------------------------------------------------------

    @Test
    void removeOnlyCreatedRemovesCreatedChannelAfterThreshold() {
        DynamicChannelReconciler reconciler = new DynamicChannelReconciler(true);
        MeasuredValue value = measuredValue("moisture");

        // First pass creates the channel
        DynamicChannelReconciler.Plan firstPlan = reconciler.reconcile(List.of(value), List.of(),
                MeasuredValue::getChannelPrefix, v -> channel(v.getChannelPrefix()));
        assertThat(firstPlan.channelsToAdd).hasSize(1);

        // Now include the created channel in subsequent current-channels list, mirroring handler commit
        List<Channel> currentChannels = new ArrayList<>(List.of(firstPlan.channelsToAdd.get(0)));

        // THRESHOLD - 1 passes missing → channel still there
        for (int i = 0; i < MISSING_VALUE_REMOVAL_THRESHOLD - 1; i++) {
            DynamicChannelReconciler.Plan plan = reconciler.reconcile(List.of(), currentChannels,
                    MeasuredValue::getChannelPrefix, v -> channel(v.getChannelPrefix()));
            assertThat(plan.channelsToRemove).isEmpty();
            currentChannels = applyPlan(currentChannels, plan);
        }

        // One more (total = THRESHOLD) → removed
        DynamicChannelReconciler.Plan lastPlan = reconciler.reconcile(List.of(), currentChannels,
                MeasuredValue::getChannelPrefix, v -> channel(v.getChannelPrefix()));
        assertThat(lastPlan.channelsToRemove).hasSize(1);
        assertThat(lastPlan.channelsToRemove.get(0).getUID().getId()).isEqualTo("moisture");
    }

    // -----------------------------------------------------------------------
    // Case 5: removeOnlyCreatedChannels=false removes ANY channel after threshold
    // (gateway semantics)
    // -----------------------------------------------------------------------

    @Test
    void removeAnyChannelAfterThresholdInGatewayMode() {
        DynamicChannelReconciler reconciler = new DynamicChannelReconciler(false);
        // A channel the reconciler did NOT create (pre-existing)
        Channel existing = channel("wind-speed");
        List<Channel> currentChannels = new ArrayList<>(List.of(existing));

        // THRESHOLD - 1 → still kept
        for (int i = 0; i < MISSING_VALUE_REMOVAL_THRESHOLD - 1; i++) {
            DynamicChannelReconciler.Plan plan = reconciler.reconcile(List.of(), currentChannels,
                    MeasuredValue::getChannelId, v -> channel(v.getChannelId()));
            assertThat(plan.channelsToRemove).isEmpty();
            currentChannels = applyPlan(currentChannels, plan);
        }

        // THRESHOLD → removed
        DynamicChannelReconciler.Plan lastPlan = reconciler.reconcile(List.of(), currentChannels,
                MeasuredValue::getChannelId, v -> channel(v.getChannelId()));
        assertThat(lastPlan.channelsToRemove).hasSize(1);
        assertThat(lastPlan.channelsToRemove.get(0).getUID().getId()).isEqualTo("wind-speed");
    }

    // -----------------------------------------------------------------------
    // Case 6: reappearing value resets the miss counter
    // -----------------------------------------------------------------------

    @Test
    void reappearingValueResetsMissCounter() {
        DynamicChannelReconciler reconciler = new DynamicChannelReconciler(false);
        MeasuredValue temperature = measuredValue("temperature");
        MeasuredValue humidity = measuredValue("humidity");

        // Create both channels
        DynamicChannelReconciler.Plan firstPlan = reconciler.reconcile(List.of(temperature, humidity), List.of(),
                MeasuredValue::getChannelId, v -> channel(v.getChannelId()));
        List<Channel> currentChannels = applyPlan(new ArrayList<>(), firstPlan);

        // Almost reach the threshold for humidity
        for (int i = 0; i < MISSING_VALUE_REMOVAL_THRESHOLD - 1; i++) {
            DynamicChannelReconciler.Plan plan = reconciler.reconcile(List.of(temperature), currentChannels,
                    MeasuredValue::getChannelId, v -> channel(v.getChannelId()));
            currentChannels = applyPlan(currentChannels, plan);
        }

        // Humidity reappears — resets counter
        DynamicChannelReconciler.Plan resetPlan = reconciler.reconcile(List.of(temperature, humidity), currentChannels,
                MeasuredValue::getChannelId, v -> channel(v.getChannelId()));
        currentChannels = applyPlan(currentChannels, resetPlan);

        // Almost reach the threshold again — humidity should still be present
        for (int i = 0; i < MISSING_VALUE_REMOVAL_THRESHOLD - 1; i++) {
            DynamicChannelReconciler.Plan plan = reconciler.reconcile(List.of(temperature), currentChannels,
                    MeasuredValue::getChannelId, v -> channel(v.getChannelId()));
            assertThat(plan.channelsToRemove).doesNotContain(currentChannels.stream()
                    .filter(c -> "humidity".equals(c.getUID().getId())).findFirst().orElseThrow());
            currentChannels = applyPlan(currentChannels, plan);
        }

        // humidity channel must still exist
        List<String> ids = currentChannels.stream().map(c -> c.getUID().getId()).toList();
        assertThat(ids).contains("humidity");
    }

    // -----------------------------------------------------------------------
    // Case 7: reset() clears debounce and creation state
    // -----------------------------------------------------------------------

    @Test
    void resetClearsAccumulatedState() {
        DynamicChannelReconciler reconciler = new DynamicChannelReconciler(true);
        MeasuredValue value = measuredValue("pressure");

        // First pass: create the channel via the reconciler
        DynamicChannelReconciler.Plan firstPlan = reconciler.reconcile(List.of(value), List.of(),
                MeasuredValue::getChannelId, v -> channel(v.getChannelId()));
        assertThat(firstPlan.channelsToAdd).hasSize(1);
        List<Channel> currentChannels = new ArrayList<>(List.of(firstPlan.channelsToAdd.get(0)));

        // Advance miss counter a few passes (below threshold) without reporting "pressure"
        for (int i = 0; i < MISSING_VALUE_REMOVAL_THRESHOLD - 1; i++) {
            DynamicChannelReconciler.Plan plan = reconciler.reconcile(List.of(), currentChannels,
                    MeasuredValue::getChannelId, v -> channel(v.getChannelId()));
            assertThat(plan.channelsToRemove).isEmpty();
        }

        // Reset clears both missingCounts and createdChannelIds
        reconciler.reset();

        // After reset, the channel is no longer tracked as reconciler-created, so with
        // removeOnlyCreatedChannels=true the still-present channel is NOT a removal candidate
        DynamicChannelReconciler.Plan afterResetMissing = reconciler.reconcile(List.of(), currentChannels,
                MeasuredValue::getChannelId, v -> channel(v.getChannelId()));
        assertThat(afterResetMissing.channelsToRemove).isEmpty();

        // And the value, when re-supplied, is treated as brand-new: channel is added again
        DynamicChannelReconciler.Plan afterResetPresent = reconciler.reconcile(List.of(value), List.of(),
                MeasuredValue::getChannelId, v -> channel(v.getChannelId()));
        ChannelUID recreatedUID = afterResetPresent.channelsToAdd.get(0).getUID();
        assertThat(afterResetPresent.channelsToAdd).hasSize(1);
        assertThat(recreatedUID.getId()).isEqualTo("pressure");
        assertThat(afterResetPresent.statesToPost).containsEntry(recreatedUID, new DecimalType(42));
    }
}
