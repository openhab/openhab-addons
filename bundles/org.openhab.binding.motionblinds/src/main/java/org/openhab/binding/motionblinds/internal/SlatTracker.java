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
package org.openhab.binding.motionblinds.internal;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SlatTracker} follows the tilt of the slats of a venetian blind from the movements of the blind.
 *
 * <p>
 * The slats are turned by the travel of the blind itself: moving down turns them towards "closed, top visible"
 * (tilt 100%), moving up towards "closed, bottom visible" (tilt 0%). A full swing takes {@code tiltTravel} percent
 * of travel; further travel moves the blind with the slats closed.
 *
 * <p>
 * The last {@code tiltSlack} percent above fully lowered behave differently, because the bottom rail rests on the
 * sill: moving up from fully lowered does not turn the slats until the bottom rail lifts off, then they drop to
 * horizontal. Moving down in this zone turns them at half the normal rate. So from fully lowered, a tilt between
 * horizontal and closed with the top visible takes two moves: up to horizontal, then down again.
 *
 * <p>
 * The motor keeps its own angle estimate on the same principle: it changes by about
 * {@value #MOTOR_ANGLE_PER_PERCENT}° per percent of travel, limited to 0-180°. That range is wider than the swing of
 * the real slats, so the estimate itself drifts from the slats when the blind changes direction, but its changes
 * measure the travel much finer than the reported position, which is rounded to whole percents. The tracker
 * therefore accumulates the changes of the motor angle, and moves the blind with relative angle targets. The motor
 * overshoots angle targets by about {@value #MOTOR_OVERSHOOT}° in the direction of travel, which is compensated.
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
public class SlatTracker {

    /** Change of the motor angle for 1% of travel */
    static final int MOTOR_ANGLE_PER_PERCENT = 29;
    /** The motor stops this much after an angle target, in the direction of travel */
    static final int MOTOR_OVERSHOOT = 7;
    /** Extra travel to make sure the slats close: moving further with closed slats does not turn them */
    static final int CLOSE_MARGIN = 15;
    private static final int MAX_ANGLE = 180;
    /** Smaller changes are not sent to the motor */
    private static final int MIN_ANGLE_CHANGE = 3;
    /** The slats drop to horizontal when the bottom rail lifts off */
    private static final double TILT_AFTER_SLACK = 0.5;
    /** Rest of the slack that counts as travelled when following the motor: its overshoot varies by a few degrees */
    private static final int SLACK_TOLERANCE = 10;
    /** Rate of turning when moving down with the bottom rail setting down on the sill */
    private static final double SLACK_ZONE_RATE = 0.5;
    /** Tilts closer than this count as equal when choosing a move */
    private static final double TILT_EPSILON = 1e-6;

    /**
     * State of the slats.
     *
     * @param tilt 0 = closed with bottom visible, 0.5 = horizontal, 1 = closed with top visible
     * @param bottomDistance travel above fully lowered in motor degrees, {@code null} if not known
     */
    private record Slats(double tilt, @Nullable Double bottomDistance) {
    }

    /** Change of the motor angle that turns the slats from closed to closed */
    private final double swingAngle;
    /** Travel above fully lowered in motor degrees in which the bottom rail rests on the sill */
    private final double slackAngle;

    private @Nullable Integer position;
    private @Nullable Integer angle;
    private @Nullable Slats slats;

    public SlatTracker(double tiltTravel, double tiltSlack) {
        this.swingAngle = Math.max(0.5, tiltTravel) * MOTOR_ANGLE_PER_PERCENT;
        this.slackAngle = Math.max(0, tiltSlack) * MOTOR_ANGLE_PER_PERCENT;
    }

    /**
     * Update with the status reported by the motor.
     */
    public synchronized void update(@Nullable Integer newPosition, @Nullable Integer newAngle) {
        Integer oldPosition = position;
        Integer oldAngle = angle;
        Slats current = slats;
        if (current != null) {
            if (newAngle != null && oldAngle != null) {
                current = move(current, newAngle - oldAngle, SLACK_TOLERANCE);
            } else if (newPosition != null && oldPosition != null) {
                current = move(current, (newPosition - oldPosition) * MOTOR_ANGLE_PER_PERCENT, SLACK_TOLERANCE);
            }
        }

        if (newPosition != null && newPosition >= 100
                && (newAngle == null || newAngle > MAX_ANGLE - MOTOR_ANGLE_PER_PERCENT / 2)) {
            // fully lowered (the position is rounded, so the motor angle must be at its top as well)
            current = new Slats(1.0, 0.0);
        } else if (newAngle != null && newAngle >= MAX_ANGLE) {
            // the motor estimate is at its end stop, so the slats are as well
            current = new Slats(1.0, current == null ? null : current.bottomDistance());
        } else if (newAngle != null && newAngle <= 0) {
            current = new Slats(0.0, current == null ? null : current.bottomDistance());
        } else if (current == null && newAngle != null) {
            // first status: best guess from the motor estimate, which is fairly accurate when the blind is lowered
            current = new Slats(clamp((newAngle - MAX_ANGLE / 4.0) / (MAX_ANGLE / 2.0)), null);
        }

        if (newPosition != null) {
            position = newPosition;
        }
        if (newAngle != null) {
            angle = newAngle;
        }
        slats = current;
    }

    /**
     * @return the last motor angle, or {@code null} if not known yet
     */
    public synchronized @Nullable Integer getMotorAngle() {
        return angle;
    }

    /**
     * @return the tilt in percent (0 = closed with bottom visible, 50 = open, 100 = closed with top visible), or
     *         {@code null} if not known yet
     */
    public synchronized @Nullable Integer getTilt() {
        Slats current = slats;
        return current == null ? null : (int) Math.round(current.tilt() * 100);
    }

    /**
     * Calculate the {@code WriteDevice} data that turns the slats towards the given tilt at the current height.
     * From fully lowered, a tilt above horizontal needs two moves: call this again when the first one has finished.
     *
     * @return the data, an empty map if the slats are already there, or {@code null} if the state is not known yet
     */
    public synchronized @Nullable Map<String, Object> tiltCommand(int tiltPercent) {
        Integer currentAngle = angle;
        Slats current = slats;
        if (currentAngle == null || current == null) {
            return null;
        }
        double target = clamp(tiltPercent / 100.0);
        Double bottomDistance = current.bottomDistance();
        int angleChange;
        if (bottomDistance != null && bottomDistance < slackAngle - SLACK_TOLERANCE && target < current.tilt()
                && target > TILT_AFTER_SLACK) {
            // resting on the sill: these tilts can only be reached from horizontal, so lift the bottom rail first
            angleChange = (int) -Math.ceil(slackAngle - bottomDistance);
        } else {
            angleChange = bestAngleChange(current, currentAngle, target);
        }
        if (Math.abs(angleChange) - MOTOR_OVERSHOOT < MIN_ANGLE_CHANGE) {
            return Map.of();
        }
        if (target <= 0.0 || target >= 1.0) {
            angleChange += (int) Math.signum(angleChange) * CLOSE_MARGIN;
        }
        long targetAngle = Math.round(currentAngle + angleChange - Math.signum(angleChange) * MOTOR_OVERSHOOT);
        return Map.of("targetAngle", (int) Math.max(0, Math.min(MAX_ANGLE, targetAngle)));
    }

    /**
     * Find the smallest change of the motor angle that turns the slats closest to the target.
     */
    private int bestAngleChange(Slats current, int currentAngle, double target) {
        int best = 0;
        double bestError = Math.abs(current.tilt() - target);
        for (int change = -currentAngle; change <= MAX_ANGLE - currentAngle; change++) {
            // when choosing a move, the slack must be travelled completely
            double error = Math.abs(move(current, change, 0).tilt() - target);
            if (error < bestError - TILT_EPSILON
                    || (error < bestError + TILT_EPSILON && Math.abs(change) < Math.abs(best))) {
                best = change;
                bestError = error;
            }
        }
        return best;
    }

    /**
     * Apply a change of the motor angle to the slats.
     *
     * @param slackTolerance rest of the slack that counts as travelled when moving up
     */
    private Slats move(Slats current, double angleChange, double slackTolerance) {
        double tilt = current.tilt();
        Double distance = current.bottomDistance();
        if (distance == null) {
            return new Slats(clamp(tilt + angleChange / swingAngle), null);
        }
        double bottomDistance = distance;
        if (angleChange < 0) {
            double up = -angleChange;
            if (bottomDistance < slackAngle) {
                // the bottom rail rests on the sill: the slats do not turn until it lifts off
                double threshold = slackAngle - slackTolerance;
                double inZone = Math.min(up, slackAngle - bottomDistance);
                boolean liftsOff = bottomDistance < threshold && bottomDistance + inZone >= threshold;
                bottomDistance += inZone;
                up -= inZone;
                if (liftsOff) {
                    tilt = Math.min(tilt, TILT_AFTER_SLACK);
                }
            }
            tilt -= up / swingAngle;
            bottomDistance += up;
        } else {
            double down = angleChange;
            double aboveZone = Math.max(0, Math.min(down, bottomDistance - slackAngle));
            tilt += aboveZone / swingAngle;
            down -= aboveZone;
            bottomDistance -= aboveZone;
            double inZone = Math.min(down, bottomDistance);
            tilt += inZone * SLACK_ZONE_RATE / swingAngle;
            bottomDistance -= inZone;
        }
        return new Slats(clamp(tilt), bottomDistance);
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
