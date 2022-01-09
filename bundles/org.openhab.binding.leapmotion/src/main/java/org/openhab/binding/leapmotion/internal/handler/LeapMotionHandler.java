/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.leapmotion.internal.handler;

import static org.openhab.binding.leapmotion.internal.LeapMotionBindingConstants.*;

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leapmotion.leap.CircleGesture;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.FingerList;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Listener;

/**
 * The {@link LeapMotionHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Thomas Eichstädt-Engelen - Initial version of listener logic
 *
 */
@NonNullByDefault
public class LeapMotionHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(LeapMotionHandler.class);

    private @NonNullByDefault({}) LeapMotionListener listener;
    private @NonNullByDefault({}) Controller leapController;

    public LeapMotionHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        this.listener = new LeapMotionListener();

        leapController = new Controller();
        leapController.setPolicyFlags(Controller.PolicyFlag.POLICY_BACKGROUND_FRAMES);
        leapController.addListener(this.listener);
        if (leapController.isConnected()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void dispose() {
        leapController.removeListener(this.listener);
        listener = null;
        leapController.delete();
        leapController = null;
    }

    private class LeapMotionListener extends Listener {

        private static final int RATE_LIMIT_IN_MS = 200;
        private long lastEvent = 0;
        private boolean noHand;

        public LeapMotionListener() {
        }

        @Override
        public void onConnect(@Nullable Controller controller) {
            if (controller != null) {
                controller.enableGesture(Gesture.Type.TYPE_KEY_TAP);
                controller.enableGesture(Gesture.Type.TYPE_CIRCLE);
            }
            updateStatus(ThingStatus.ONLINE);
        }

        @Override
        public void onDisconnect(@Nullable Controller controller) {
            updateStatus(ThingStatus.OFFLINE);
        }

        @Override
        public void onFrame(@Nullable Controller controller) {
            if (controller == null) {
                return;
            }
            Frame frame = controller.frame();

            GestureList gestures = frame.gestures();
            for (int i = 0; i < gestures.count(); i++) {
                Gesture gesture = gestures.get(i);
                switch (gesture.type()) {
                    case TYPE_KEY_TAP:
                        logger.debug("Key tap");
                        triggerChannel(CHANNEL_GESTURE, GESTURE_TAP);
                        break;
                    case TYPE_CIRCLE:
                        CircleGesture circle = new CircleGesture(gesture);
                        // Calculate clock direction using the angle between circle normal and pointable
                        boolean clockwiseness;
                        if (circle.pointable().direction().angleTo(circle.normal()) <= Math.PI / 4) {
                            // Clockwise if angle is less than 90 degrees
                            clockwiseness = true;
                        } else {
                            clockwiseness = false;
                        }

                        // Calculate angle swept since last frame
                        if (circle.state() == com.leapmotion.leap.Gesture.State.STATE_UPDATE) {
                            if (System.nanoTime() > lastEvent + TimeUnit.MILLISECONDS.toNanos(RATE_LIMIT_IN_MS)) {
                                logger.debug("Circle (clockwise={})", clockwiseness);
                                if (clockwiseness) {
                                    triggerChannel(CHANNEL_GESTURE, GESTURE_CLOCKWISE);
                                } else {
                                    triggerChannel(CHANNEL_GESTURE, GESTURE_ANTICLOCKWISE);
                                }
                                lastEvent = System.nanoTime();
                            }
                        }
                        break;
                    default:
                        logger.debug("Unknown gesture type.");
                        break;
                }
            }

            if (!frame.hands().isEmpty()) {
                noHand = false;
                // Get the first hand
                Hand hand = frame.hands().get(0);
                // Check if the hand has any fingers
                FingerList fingers = hand.fingers();
                if (System.nanoTime() > lastEvent + TimeUnit.MILLISECONDS.toNanos(RATE_LIMIT_IN_MS)) {
                    int height = (int) hand.palmPosition().getY();
                    logger.debug("Fingers shown {} @ {}", fingers.count(), height);
                    triggerChannel(CHANNEL_GESTURE, GESTURE_FINGERS + fingers.count() + "_" + height);
                    lastEvent = System.nanoTime();
                }
            } else {
                if (!noHand) {
                    noHand = true;
                    logger.debug("No hand");
                    triggerChannel(CHANNEL_GESTURE, GESTURE_NOHAND);
                }
            }
        }
    }
}
