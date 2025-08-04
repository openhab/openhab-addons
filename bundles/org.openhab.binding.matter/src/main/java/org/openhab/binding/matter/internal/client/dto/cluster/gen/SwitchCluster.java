/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

// AUTO-GENERATED, DO NOT EDIT!

package org.openhab.binding.matter.internal.client.dto.cluster.gen;

import java.math.BigInteger;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Switch
 *
 * @author Dan Cunningham - Initial contribution
 */
public class SwitchCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x003B;
    public static final String CLUSTER_NAME = "Switch";
    public static final String CLUSTER_PREFIX = "switch";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_NUMBER_OF_POSITIONS = "numberOfPositions";
    public static final String ATTRIBUTE_CURRENT_POSITION = "currentPosition";
    public static final String ATTRIBUTE_MULTI_PRESS_MAX = "multiPressMax";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the maximum number of positions the switch has. Any kind of switch has a minimum of 2 positions. Also
     * see Multi Position Details for the case NumberOfPositions&gt;2.
     */
    public Integer numberOfPositions; // 0 uint8 R V
    /**
     * Indicates the position of the switch. The valid range is zero to NumberOfPositions - 1.
     * CurrentPosition value 0 shall be assigned to the default position of the switch: for example the &quot;open&quot;
     * state of a rocker switch, or the &quot;idle&quot; state of a push button switch.
     */
    public Integer currentPosition; // 1 uint8 R V
    /**
     * Indicates how many consecutive presses can be detected and reported by a momentary switch which supports
     * multi-press (MSM feature flag set).
     * For example, a momentary switch supporting single press, double press and triple press, but not quad press and
     * beyond, would return the value 3.
     * When more than MultiPressMax presses are detected within a multi-press sequence:
     * • The server for cluster revision &lt; 2 SHOULD generate a MultiPressComplete event with the
     * TotalNumberOfPressesCounted field set to the value of the MultiPressMax attribute, and avoid generating any
     * further InitialPress and MultiPressOngoing events until the switch has become fully idle (i.e. no longer in the
     * process of counting presses within the multipress).
     * • The server for cluster revision &gt;&#x3D; 2 shall generate a MultiPressComplete event with the
     * TotalNumberOfPressesCounted field set to zero (indicating an aborted sequence), and shall NOT generate any
     * further InitialPress and MultiPressOngoing events until the switch has become fully idle (i.e. no longer in the
     * process of counting presses within the multipress).
     * This approach avoids unintentionally causing intermediate actions where there is a very long sequence of presses
     * beyond MultiPressMax that may be taken in account specially by switches (e.g. to trigger special behavior such as
     * factory reset for which generating events towards the client is not appropriate).
     */
    public Integer multiPressMax; // 2 uint8 R V

    // Structs
    /**
     * This event shall be generated, when the latching switch is moved to a new position. It may have been delayed by
     * debouncing within the switch.
     */
    public static class SwitchLatched {
        /**
         * This field shall indicate the new value of the CurrentPosition attribute, i.e. after the move.
         */
        public Integer newPosition; // uint8

        public SwitchLatched(Integer newPosition) {
            this.newPosition = newPosition;
        }
    }

    /**
     * This event shall be generated, when the momentary switch starts to be pressed (after debouncing).
     */
    public static class InitialPress {
        /**
         * This field shall indicate the new value of the CurrentPosition attribute, i.e. while pressed.
         */
        public Integer newPosition; // uint8

        public InitialPress(Integer newPosition) {
            this.newPosition = newPosition;
        }
    }

    /**
     * This event shall be generated when the momentary switch has been pressed for a &quot;long&quot; time. The time
     * interval constituting a &quot;long&quot; time is manufacturer-determined, since it depends on the switch physics.
     * • When the AS feature flag is set, this event:
     * ◦ shall NOT be generated during a multi-press sequence (since a long press is a separate cycle from any
     * multi-press cycles);
     * ◦ shall only be generated after the first InitialPress following a MultiPressComplete when a long press is
     * detected after the idle time.
     * • Else, when the MSM feature flag is set, this event:
     * ◦ shall NOT be generated during a multi-press sequence (since a long press is a separate cycle from any
     * multi-press cycles);
     * ◦ shall only be generated after the first InitialPress following a MultiPressComplete when a long press is
     * detected after the idle time;
     * ◦ shall NOT be generated after a MultiPressOngoing event without an intervening MultiPressComplete event.
     * The above constraints imply that for a given activity detection cycle of a switch having MSM and/or MSL feature
     * flags set, the entire activity is either a single long press detection cycle of (InitialPress, LongPress,
     * LongRelease), or a single multi-press detection cycle (ending in MultiPressComplete), where presses that would
     * otherwise be reported as long presses are instead reported as a counted press in the MultiPressComplete event,
     * and as InitialPress/ShortRelease pairs otherwise (where applicable).
     * The rationale for this constraint is the ambiguity of interpretation of events when mixing long presses and
     * multi-press events.
     */
    public static class LongPress {
        /**
         * This field shall indicate the new value of the CurrentPosition attribute, i.e. while pressed.
         */
        public Integer newPosition; // uint8

        public LongPress(Integer newPosition) {
            this.newPosition = newPosition;
        }
    }

    /**
     * If the server has the Action Switch (AS) feature flag set, this event shall NOT be generated at all, since
     * setting the Action Switch feature flag forbids the Momentary Switch ShortRelease (MSR) feature flag from being
     * set. Otherwise, the following paragraphs describe the situations where this event is generated.
     * This event shall be generated, when the momentary switch has been released (after debouncing).
     * • If the server has the Momentary Switch LongPress (MSL) feature flag set, then this event shall be generated
     * when the switch is released if no LongPress event had been generated since the previous InitialPress event.
     * • If the server does not have the Momentary Switch LongPress (MSL) feature flag set, this event shall be
     * generated when the switch is released - even when the switch was pressed for a long time.
     * • Also see Section 1.13.7, “Sequence of generated events”.
     */
    public static class ShortRelease {
        /**
         * This field shall indicate the previous value of the CurrentPosition attribute, i.e. just prior to release.
         */
        public Integer previousPosition; // uint8

        public ShortRelease(Integer previousPosition) {
            this.previousPosition = previousPosition;
        }
    }

    /**
     * This event shall be generated, when the momentary switch has been released (after debouncing) and after having
     * been pressed for a long time, i.e. this event shall be generated when the switch is released if a LongPress event
     * has been generated since the previous InitialPress event. Also see Section 1.13.7, “Sequence of generated
     * events”.
     */
    public static class LongRelease {
        /**
         * This field shall indicate the previous value of the CurrentPosition attribute, i.e. just prior to release.
         */
        public Integer previousPosition; // uint8

        public LongRelease(Integer previousPosition) {
            this.previousPosition = previousPosition;
        }
    }

    /**
     * If the server has the Action Switch (AS) feature flag set, this event shall NOT be generated at all. Otherwise,
     * the following paragraphs describe the situations where this event is generated.
     * This event shall be generated to indicate how many times the momentary switch has been pressed in a multi-press
     * sequence, during that sequence. See Multi Press Details below.
     */
    public static class MultiPressOngoing {
        /**
         * This field shall indicate the new value of the CurrentPosition attribute, i.e. while pressed.
         */
        public Integer newPosition; // uint8
        /**
         * This field shall contain:
         * • a value of 2 when the second press of a multi-press sequence has been detected,
         * • a value of 3 when the third press of a multi-press sequence has been detected,
         * • a value of N when the Nth press of a multi-press sequence has been detected.
         */
        public Integer currentNumberOfPressesCounted; // uint8

        public MultiPressOngoing(Integer newPosition, Integer currentNumberOfPressesCounted) {
            this.newPosition = newPosition;
            this.currentNumberOfPressesCounted = currentNumberOfPressesCounted;
        }
    }

    /**
     * This event shall be generated to indicate how many times the momentary switch has been pressed in a multi-press
     * sequence, after it has been detected that the sequence has ended. See Multi Press Details.
     * The PreviousPosition field shall indicate the previous value of the CurrentPosition attribute, i.e. just prior to
     * release.
     * The TotalNumberOfPressesCounted field shall contain:
     * • a value of 0 when there was an aborted multi-press sequence, where the number of presses goes beyond
     * MultiPressMax presses,
     * • a value of 1 when there was exactly one press in a multi-press sequence (and the sequence has ended), i.e.
     * there was no double press (or more),
     * • a value of 2 when there were exactly two presses in a multi-press sequence (and the sequence has ended),
     * • a value of 3 when there were exactly three presses in a multi-press sequence (and the sequence has ended),
     * • a value of N when there were exactly N presses in a multi-press sequence (and the sequence has ended).
     * &gt; [!NOTE]
     * &gt; The introduction of TotalNumberOfPressesCounted supporting the value 0 may impact clients of switches using
     * cluster revision 1 since such servers would not use this value of TotalNumberOfPressesCounted to indicate an
     * aborted sequence. Clients SHOULD always act using the TotalNumberOfPressesCounted field taken into account since
     * for values from 1 to MultiPressMax, the user action that led to the event was different depending on the count.
     */
    public static class MultiPressComplete {
        public Integer previousPosition; // uint8
        public Integer totalNumberOfPressesCounted; // uint8

        public MultiPressComplete(Integer previousPosition, Integer totalNumberOfPressesCounted) {
            this.previousPosition = previousPosition;
            this.totalNumberOfPressesCounted = totalNumberOfPressesCounted;
        }
    }

    // Bitmaps
    public static class FeatureMap {
        /**
         * 
         * This feature flag is for a switch that maintains its position after being pressed (or turned).
         */
        public boolean latchingSwitch;
        /**
         * 
         * This feature flag is for a switch that does not maintain its position after being pressed (or turned). After
         * releasing, it goes back to its idle position.
         */
        public boolean momentarySwitch;
        /**
         * 
         * This feature flag is for a momentary switch that can distinguish and report release events.
         */
        public boolean momentarySwitchRelease;
        /**
         * 
         * This feature flag is for a momentary switch that can distinguish and report long presses from short presses.
         */
        public boolean momentarySwitchLongPress;
        /**
         * 
         * This feature flag is for a momentary switch that can distinguish and report double press and potentially
         * multiple presses with more events, such as triple press, etc.
         */
        public boolean momentarySwitchMultiPress;
        /**
         * 
         * This feature flag indicates simplified handling of events for multi-press-capable switches. See Multi Press
         * Details.
         */
        public boolean actionSwitch;

        public FeatureMap(boolean latchingSwitch, boolean momentarySwitch, boolean momentarySwitchRelease,
                boolean momentarySwitchLongPress, boolean momentarySwitchMultiPress, boolean actionSwitch) {
            this.latchingSwitch = latchingSwitch;
            this.momentarySwitch = momentarySwitch;
            this.momentarySwitchRelease = momentarySwitchRelease;
            this.momentarySwitchLongPress = momentarySwitchLongPress;
            this.momentarySwitchMultiPress = momentarySwitchMultiPress;
            this.actionSwitch = actionSwitch;
        }
    }

    public SwitchCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 59, "Switch");
    }

    protected SwitchCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "numberOfPositions : " + numberOfPositions + "\n";
        str += "currentPosition : " + currentPosition + "\n";
        str += "multiPressMax : " + multiPressMax + "\n";
        return str;
    }
}
