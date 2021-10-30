/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.openwebnet.internal.handler;

import static org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants;
import org.openhab.binding.openwebnet.internal.actions.OpenWebNetCENActions;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.CEN;
import org.openwebnet4j.message.CEN.Pressure;
import org.openwebnet4j.message.CENPlusScenario;
import org.openwebnet4j.message.CENPlusScenario.CENPlusPressure;
import org.openwebnet4j.message.CENScenario;
import org.openwebnet4j.message.CENScenario.CENPressure;
import org.openwebnet4j.message.FrameException;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereCEN;
import org.openwebnet4j.message.Who;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetScenarioHandler} is responsible for handling commands/messages for CEN/CEN+ Scenarios. It
 * extends the abstract {@link OpenWebNetThingHandler}.
 *
 * @author Massimo Valla - Initial contribution
 */
@NonNullByDefault
public class OpenWebNetScenarioHandler extends OpenWebNetThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetScenarioHandler.class);

    private interface PressEvent {
        @Override
        public String toString();
    }

    private enum CENPressEvent implements PressEvent {
        CEN_EVENT_START_PRESS("START_PRESS"),
        CEN_EVENT_SHORT_PRESS("SHORT_PRESS"),
        CEN_EVENT_EXTENDED_PRESS("EXTENDED_PRESS"),
        CEN_EVENT_RELEASE_EXTENDED_PRESS("RELEASE_EXTENDED_PRESS");

        private final String press;

        CENPressEvent(final String pr) {
            this.press = pr;
        }

        public static @Nullable CENPressEvent fromValue(String s) {
            Optional<CENPressEvent> event = Arrays.stream(values()).filter(val -> s.equals(val.press)).findFirst();
            return event.orElse(null);
        }

        @Override
        public String toString() {
            return press;
        }
    }

    private enum CENPlusPressEvent implements PressEvent {
        CENPLUS_EVENT_SHORT_PRESS("SHORT_PRESS"),
        CENPLUS_EVENT_START_EXTENDED_PRESS("START_EXTENDED_PRESS"),
        CENPLUS_EVENT_EXTENDED_PRESS("EXTENDED_PRESS"),
        CENPLUS_EVENT_RELEASE_EXTENDED_PRESS("RELEASE_EXTENDED_PRESS");

        private final String press;

        CENPlusPressEvent(final String pr) {
            this.press = pr;
        }

        public static @Nullable CENPlusPressEvent fromValue(String s) {
            Optional<CENPlusPressEvent> event = Arrays.stream(values()).filter(val -> s.equals(val.press)).findFirst();
            return event.orElse(null);
        }

        @Override
        public String toString() {
            return press;
        }
    }

    private boolean isCENPlus = false;

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.SCENARIO_SUPPORTED_THING_TYPES;

    public OpenWebNetScenarioHandler(Thing thing) {
        super(thing);
        if (OpenWebNetBindingConstants.THING_TYPE_BUS_CENPLUS_SCENARIO_CONTROL.equals(thing.getThingTypeUID())) {
            isCENPlus = true;
            logger.debug("created CEN+ device for thing: {}", getThing().getUID());
        } else {
            logger.debug("created CEN device for thing: {}", getThing().getUID());
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        Object buttonsConfig = getConfig().get(CONFIG_PROPERTY_SCENARIO_BUTTONS);
        if (buttonsConfig != null) {
            Set<Integer> buttons = csvStringToSetInt((String) buttonsConfig);
            if (!buttons.isEmpty()) {
                ThingBuilder thingBuilder = editThing();
                Channel ch;
                for (Integer i : buttons) {
                    ch = thing.getChannel(CHANNEL_SCENARIO_BUTTON + i);
                    if (ch == null) {
                        thingBuilder.withChannel(buttonToChannel(i));
                        logger.debug("added channel {} to thing: {}", i, getThing().getUID());
                    }
                }
                updateThing(thingBuilder.build());
            } else {
                logger.warn("invalid config parameter buttons='{}' for thing {}", buttonsConfig, thing.getUID());
            }
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(OpenWebNetCENActions.class);
    }

    @Override
    protected String ownIdPrefix() {
        if (isCENPlus) {
            return Who.CEN_PLUS_SCENARIO_SCHEDULER.value().toString();
        } else {
            return Who.CEN_SCENARIO_SCHEDULER.value().toString();
        }
    }

    @Override
    protected void handleMessage(BaseOpenMessage msg) {
        super.handleMessage(msg);
        if (msg.isCommand()) {
            triggerChannel((CEN) msg);
        } else {
            logger.debug("handleMessage() Ignoring unsupported DIM for thing {}. Frame={}", getThing().getUID(), msg);
        }
    }

    private void triggerChannel(CEN cenMsg) {
        Integer buttonNumber;
        try {
            buttonNumber = cenMsg.getButtonNumber();
        } catch (FrameException e) {
            logger.warn("cannot read CEN/CEN+ button. Ignoring message {}", cenMsg);
            return;
        }
        if (buttonNumber == null || buttonNumber < 0 || buttonNumber > 31) {
            logger.warn("invalid CEN/CEN+ button number: {}. Ignoring message {}", buttonNumber, cenMsg);
            return;
        }
        Channel ch = thing.getChannel(CHANNEL_SCENARIO_BUTTON + buttonNumber);
        if (ch == null) { // we have found a new button for this device, let's add a new channel for the button
            ThingBuilder thingBuilder = editThing();
            ch = buttonToChannel(buttonNumber);
            thingBuilder.withChannel(ch);
            updateThing(thingBuilder.build());
            logger.info("added new channel {} to thing {}", ch.getUID(), getThing().getUID());
        }
        final Channel channel = ch;
        PressEvent pressEv = null;
        Pressure press = null;
        try {
            press = cenMsg.getButtonPressure();
        } catch (FrameException e) {
            logger.warn("invalid CEN/CEN+ Press. Ignoring message {}", cenMsg);
            return;
        }
        if (press == null) {
            logger.warn("invalid CEN/CEN+ Press. Ignoring message {}", cenMsg);
            return;
        }

        if (cenMsg instanceof CENScenario) {
            switch ((CENPressure) press) {
                case START_PRESSURE:
                    pressEv = CENPressEvent.CEN_EVENT_START_PRESS;
                    break;
                case RELEASE_SHORT_PRESSURE:
                    pressEv = CENPressEvent.CEN_EVENT_SHORT_PRESS;
                    break;
                case EXTENDED_PRESSURE:
                    pressEv = CENPressEvent.CEN_EVENT_EXTENDED_PRESS;
                    break;
                case RELEASE_EXTENDED_PRESSURE:
                    pressEv = CENPressEvent.CEN_EVENT_RELEASE_EXTENDED_PRESS;
                    break;
                default:
                    logger.warn("unsupported CENPress. Ignoring message {}", cenMsg);
                    return;
            }
        } else {
            switch ((CENPlusPressure) press) {
                case SHORT_PRESSURE:
                    pressEv = CENPlusPressEvent.CENPLUS_EVENT_SHORT_PRESS;
                    break;
                case START_EXTENDED_PRESSURE:
                    pressEv = CENPlusPressEvent.CENPLUS_EVENT_START_EXTENDED_PRESS;
                    break;
                case EXTENDED_PRESSURE:
                    pressEv = CENPlusPressEvent.CENPLUS_EVENT_EXTENDED_PRESS;
                    break;
                case RELEASE_EXTENDED_PRESSURE:
                    pressEv = CENPlusPressEvent.CENPLUS_EVENT_RELEASE_EXTENDED_PRESS;
                    break;
                default:
                    logger.warn("unsupported CENPlusPress. Ignoring message {}", cenMsg);
                    return;
            }
        }

        triggerChannel(channel.getUID(), pressEv.toString());
    }

    private Channel buttonToChannel(int buttonNumber) {
        ChannelTypeUID channelTypeUID;
        if (isCENPlus) {
            channelTypeUID = new ChannelTypeUID(BINDING_ID, CHANNEL_TYPE_CEN_PLUS_BUTTON_EVENT);
        } else {
            channelTypeUID = new ChannelTypeUID(BINDING_ID, CHANNEL_TYPE_CEN_BUTTON_EVENT);
        }
        return ChannelBuilder
                .create(new ChannelUID(getThing().getUID(), CHANNEL_SCENARIO_BUTTON + buttonNumber), "String")
                .withType(channelTypeUID).withKind(ChannelKind.TRIGGER).withLabel("Button " + buttonNumber).build();
    }

    /**
     * Construct a CEN/CEN+ virtual press message for this device given a pressString and button number
     *
     * @param pressString one START_PRESS, SHORT_PRESS etc.
     * @param button number [0-31]
     * @return CEN message
     * @throws IllegalArgumentException if button number or pressString are invalid
     */
    public CEN pressStrToMessage(String pressString, int button) throws IllegalArgumentException {
        Where w = deviceWhere;
        if (w == null) {
            throw new IllegalArgumentException("pressStrToMessage: deviceWhere is null");
        }
        if (isCENPlus) {
            CENPlusPressEvent prEvent = CENPlusPressEvent.fromValue(pressString);
            if (prEvent != null) {
                switch (prEvent) {
                    case CENPLUS_EVENT_SHORT_PRESS:
                        return CENPlusScenario.virtualShortPressure(w.value(), button);
                    case CENPLUS_EVENT_START_EXTENDED_PRESS:
                        return CENPlusScenario.virtualStartExtendedPressure(w.value(), button);
                    case CENPLUS_EVENT_EXTENDED_PRESS:
                        return CENPlusScenario.virtualExtendedPressure(w.value(), button);
                    case CENPLUS_EVENT_RELEASE_EXTENDED_PRESS:
                        return CENPlusScenario.virtualReleaseExtendedPressure(w.value(), button);
                    default:
                        throw new IllegalArgumentException("unsupported press type: " + pressString);
                }
            } else {
                throw new IllegalArgumentException("unsupported press type: " + pressString);
            }
        } else {
            CENPressEvent prEvent = CENPressEvent.fromValue(pressString);
            if (prEvent != null) {
                switch (prEvent) {
                    case CEN_EVENT_START_PRESS:
                        return CENScenario.virtualStartPressure(w.value(), button);
                    case CEN_EVENT_SHORT_PRESS:
                        return CENScenario.virtualReleaseShortPressure(w.value(), button);
                    case CEN_EVENT_EXTENDED_PRESS:
                        return CENScenario.virtualExtendedPressure(w.value(), button);
                    case CEN_EVENT_RELEASE_EXTENDED_PRESS:
                        return CENScenario.virtualReleaseExtendedPressure(w.value(), button);
                    default:
                        throw new IllegalArgumentException("unsupported press type: " + pressString);
                }
            } else {
                throw new IllegalArgumentException("unsupported press type: " + pressString);
            }
        }
    }

    private static Set<Integer> csvStringToSetInt(String s) {
        TreeSet<Integer> intSet = new TreeSet<Integer>();
        String sNorm = s.replaceAll("\\s", "");
        Scanner sc = new Scanner(sNorm);
        sc.useDelimiter(",");
        while (sc.hasNextInt()) {
            intSet.add(sc.nextInt());
        }
        sc.close();
        return intSet;
    }

    @Override
    protected void handleChannelCommand(ChannelUID channel, Command command) {
        logger.warn("CEN/CEN+ channels are trigger channels and do not handle commands");
    }

    @Override
    protected void refreshDevice(boolean refreshAll) {
        logger.debug("CEN/CEN+ channels are trigger channels and do not have state");
    }

    @Override
    protected Where buildBusWhere(String wStr) throws IllegalArgumentException {
        return new WhereCEN(wStr);
    }

    @Override
    protected void requestChannelState(ChannelUID channel) {
        logger.debug("CEN/CEN+ channels are trigger channels and do not have state");
    }
}
