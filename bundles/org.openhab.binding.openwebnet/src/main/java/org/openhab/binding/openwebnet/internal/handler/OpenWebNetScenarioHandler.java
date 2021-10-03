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
import org.openwebnet4j.message.CENScenario;
import org.openwebnet4j.message.CENScenario.CENPressure;
import org.openwebnet4j.message.FrameException;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereLightAutom;
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

    private interface PressureEvent {
        @Override
        public String toString();
    }

    public enum CENPressureEvent implements PressureEvent {
        START_PRESSURE("START_PRESSURE"),
        SHORT_PRESSURE("SHORT_PRESSURE"),
        EXTENDED_PRESSURE("EXTENDED_PRESSURE"),
        RELEASE_EXTENDED_PRESSURE("RELEASE_EXTENDED_PRESSURE");

        private final String pressure;

        CENPressureEvent(final String pr) {
            this.pressure = pr;
        }

        public static @Nullable CENPressureEvent fromValue(String s) {
            Optional<CENPressureEvent> event = Arrays.stream(values()).filter(val -> s.equals(val.pressure))
                    .findFirst();
            return event.orElse(null);
        }

        @Override
        public String toString() {
            return pressure;
        }
    }

    private boolean isCENPlus = false;

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.SCENARIO_SUPPORTED_THING_TYPES;

    public OpenWebNetScenarioHandler(Thing thing) {
        super(thing);
        logger.debug("==OWN:ScenarioHandler== constructor");
        if (OpenWebNetBindingConstants.THING_TYPE_BUS_CENPLUS_SCENARIO_CONTROL.equals(thing.getThingTypeUID())) {
            isCENPlus = true;
            logger.debug("==OWN:ScenarioHandler== CEN+ device for thing: {}", getThing().getUID());
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        logger.debug("==OWN:ScenarioHandler== initialize() thing={}", thing.getUID());
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
                        logger.debug("==OWN:ScenarioHandler== added channel {} to thing: {}", i, getThing().getUID());
                    }
                }
                updateThing(thingBuilder.build());
            } else {
                logger.warn("==OWN:ScenarioHandler== invalid config parameter buttons='{}' for thing {}", buttonsConfig,
                        thing.getUID());
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
        logger.debug("==OWN:ScenarioHandler== handleMessage() for thing: {}", thing.getUID());
        if (msg.isCommand()) {
            triggerChannel((CEN) msg);
        } else {
            logger.debug("==OWN:ScenarioHandler== handleMessage() Ignoring unsupported DIM for thing {}. Frame={}",
                    getThing().getUID(), msg);
        }
    }

    private void triggerChannel(CEN cenMsg) {
        logger.debug("==OWN:ScenarioHandler== triggerChannel() for thing: {}", thing.getUID());
        Integer buttonNumber;
        try {
            buttonNumber = cenMsg.getButtonNumber();
        } catch (FrameException e) {
            logger.warn("==OWN:ScenarioHandler== cannot read CEN/CEN+ button. Ignoring message {}", cenMsg);
            return;
        }
        if (buttonNumber == null || buttonNumber < 0 || buttonNumber > 31) {
            logger.warn("==OWN:ScenarioHandler== invalid CEN/CEN+ button number: {}. Ignoring message {}", buttonNumber,
                    cenMsg);
            return;
        }
        Channel ch = thing.getChannel(CHANNEL_SCENARIO_BUTTON + buttonNumber);
        if (ch == null) { // we have found a new button for this device, let's add a new channel for the button
            ThingBuilder thingBuilder = editThing();
            ch = buttonToChannel(buttonNumber);
            thingBuilder.withChannel(ch);
            updateThing(thingBuilder.build());
            logger.info("==OWN:ScenarioHandler== added new channel {} to thing {}", ch.getUID(), getThing().getUID());
        }
        final Channel channel = ch;
        PressureEvent pressureEv = null;
        if (cenMsg instanceof CENScenario) {
            CENPressure cenPr = null;
            try {
                cenPr = ((CENScenario) cenMsg).getButtonPressure();
            } catch (FrameException e) {
                // TODO
            }
            if (cenPr == null) {
                logger.warn("==OWN:ScenarioHandler== invalid CENPressure. Frame: {}", cenMsg);
                return;
            }
            switch (cenPr) {
                case START_PRESSURE:
                    pressureEv = CENPressureEvent.START_PRESSURE;
                    break;
                case RELEASE_SHORT_PRESSURE:
                    pressureEv = CENPressureEvent.SHORT_PRESSURE;
                    break;
                case EXTENDED_PRESSURE:
                    pressureEv = CENPressureEvent.EXTENDED_PRESSURE;
                    break;
                case RELEASE_EXTENDED_PRESSURE:
                    pressureEv = CENPressureEvent.RELEASE_EXTENDED_PRESSURE;
                    break;
                default:
                    logger.warn("==OWN:ScenarioHandler== unsupported CENPressure. Frame: {}", cenMsg);
            }
        } else {
            // TODO same for CENPlus
        }
        if (pressureEv != null) {
            triggerChannel(channel.getUID(), pressureEv.toString());
        }
    }

    private Channel buttonToChannel(int buttonNumber) {
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, CHANNEL_TYPE_SCENARIO_BUTTON);
        return ChannelBuilder
                .create(new ChannelUID(getThing().getUID(), CHANNEL_SCENARIO_BUTTON + buttonNumber), "String")
                .withType(channelTypeUID).withKind(ChannelKind.TRIGGER).withLabel("Button " + buttonNumber).build();
    }

    /**
     * Construct a CEN/CEN+ virtual pressure message for this device given a pressureString and button number
     *
     * @param pressureString one START_PRESSURE, SHORT_PRESSURE etc.
     * @param button number [0-31]
     * @return CEN message
     * @throws IllegalArgumentException if button number or pressureString are invalid
     */
    public CEN pressureStrToMessage(String pressureString, int button) throws IllegalArgumentException {
        Where w = deviceWhere;
        if (w == null) {
            throw new IllegalArgumentException("pressureStrToMessage: deviceWhere is null");
        }
        if (isCENPlus) {
            throw new IllegalArgumentException("CEN+ unsupported");
        } else {
            CENPressureEvent prEvent = CENPressureEvent.fromValue(pressureString);
            if (prEvent != null) {
                switch (prEvent) {
                    case START_PRESSURE:
                        return CENScenario.virtualStartPressure(w.value(), button);
                    case SHORT_PRESSURE:
                        return CENScenario.virtualReleaseShortPressure(w.value(), button);
                    case EXTENDED_PRESSURE:
                        return CENScenario.virtualExtendedPressure(w.value(), button);
                    case RELEASE_EXTENDED_PRESSURE:
                        return CENScenario.virtualReleaseExtendedPressure(w.value(), button);
                    default:
                        throw new IllegalArgumentException("unsupported pressure type: " + pressureString);
                }
            } else {
                throw new IllegalArgumentException("unsupported pressure type: " + pressureString);
            }
        }
    }

    /*
     * private Integer channelToButton(ChannelUID channel) {
     * try {
     * return Integer.parseInt(channel.getId().substring(channel.getId().lastIndexOf("_") + 1));
     * } catch (NumberFormatException nfe) {
     * logger.warn("==OWN:ScenarioHandler== channelToButton() Exception: {}", nfe.getMessage());
     * return null;
     * }
     * }
     */
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
        // TODO Auto-generated method stub
    }

    @Override
    protected void refreshDevice(boolean refreshAll) {
        // TODO Auto-generated method stub
    }

    @Override
    protected Where buildBusWhere(String wStr) throws IllegalArgumentException {
        return new WhereLightAutom(wStr);
    }

    @Override
    protected void requestChannelState(ChannelUID channel) {
        // TODO Auto-generated method stub
    }
}
