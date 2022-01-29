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
package org.openhab.io.hueemulation.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.io.hueemulation.internal.dto.AbstractHueState;
import org.openhab.io.hueemulation.internal.dto.HueStateBulb;
import org.openhab.io.hueemulation.internal.dto.HueStateColorBulb;
import org.openhab.io.hueemulation.internal.dto.HueStateColorBulb.ColorMode;
import org.openhab.io.hueemulation.internal.dto.HueStatePlug;
import org.openhab.io.hueemulation.internal.dto.changerequest.HueStateChange;
import org.openhab.io.hueemulation.internal.dto.response.HueResponse;
import org.openhab.io.hueemulation.internal.dto.response.HueResponse.HueErrorMessage;
import org.openhab.io.hueemulation.internal.dto.response.HueSuccessResponseStateChanged;

/**
 * This utility class provides all kind of functions to convert between openHAB item states to Hue states and back
 * as well as applying a hue state change request to a hue state or openHAB item state.
 * <p>
 * It also provides methods to determine the hue type (plug, white bulb, coloured bulb), given an item.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class StateUtils {

    /**
     * Compute the hue state from a given item state and a device type.
     *
     * @param itemState The item state
     * @param deviceType The device type
     * @return A hue light state
     */
    public static AbstractHueState colorStateFromItemState(State itemState, @Nullable DeviceType deviceType) {
        if (deviceType == null) {
            return new HueStatePlug(false);
        }
        AbstractHueState state;
        switch (deviceType) {
            case ColorType:
                if (itemState instanceof HSBType) {
                    state = new HueStateColorBulb((HSBType) itemState);
                } else if (itemState instanceof PercentType) {
                    state = new HueStateColorBulb((PercentType) itemState, ((PercentType) itemState).intValue() > 0);
                } else if (itemState instanceof OnOffType) {
                    OnOffType t = (OnOffType) itemState;
                    state = new HueStateColorBulb(t == OnOffType.ON);
                } else {
                    state = new HueStateColorBulb(new HSBType());
                }
                break;
            case WhiteType:
            case WhiteTemperatureType:
                if (itemState instanceof HSBType) {
                    PercentType brightness = ((HSBType) itemState).getBrightness();
                    state = new HueStateBulb(brightness, brightness.intValue() > 0);
                } else if (itemState instanceof PercentType) {
                    PercentType brightness = (PercentType) itemState;
                    state = new HueStateBulb(brightness, brightness.intValue() > 0);
                } else if (itemState instanceof OnOffType) {
                    OnOffType t = (OnOffType) itemState;
                    state = new HueStateBulb(t == OnOffType.ON);
                } else {
                    state = new HueStateBulb(new PercentType(0), false);
                }
                break;
            case SwitchType:
            default:
                if (itemState instanceof OnOffType) {
                    OnOffType t = (OnOffType) itemState;
                    state = new HueStatePlug(t == OnOffType.ON);
                } else {
                    state = new HueStatePlug(false);
                }
        }
        return state;
    }

    /**
     * Computes an openHAB item state, given a hue state.
     *
     * <p>
     * This only proxies to the respective call
     * on the concrete hue state implementation.
     *
     * @throws IllegalStateException Thrown if the concrete hue state is not yet handled by this method.
     */
    public static State itemStateByHueState(AbstractHueState state) throws IllegalStateException {
        if (state instanceof HueStateColorBulb) {
            return state.as(HueStateColorBulb.class).toHSBType();
        } else if (state instanceof HueStateBulb) {
            return state.as(HueStateBulb.class).toBrightnessType();
        } else if (state instanceof HueStatePlug) {
            return state.as(HueStatePlug.class).toOnOffType();
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * An openHAB state is usually also a command. Cast the state.
     *
     * @throws IllegalStateException Throws if the cast fails.
     */
    public static Command commandByItemState(State state) throws IllegalStateException {
        if (state instanceof Command) {
            return (Command) state;
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Computes an openHAB command, given a hue state change request.
     *
     * @param changeRequest The change request
     */
    public static @Nullable Command computeCommandByChangeRequest(HueStateChange changeRequest) {
        List<HueResponse> responses = new ArrayList<>();
        return computeCommandByState(responses, "", new HueStateColorBulb(false), changeRequest);
    }

    /**
     * Apply the new received state from the REST PUT request.
     *
     * @param responses Creates a response entry for each success and each error. There is one entry per non-null field
     *            of {@link HueStateChange} created.
     * @param prefix The response entry prefix, for example "/groups/mygroupid/state/"
     * @param state The current item state
     * @param newState A state change DTO
     * @return Return a command computed via the incoming state object.
     */
    public static @Nullable Command computeCommandByState(List<HueResponse> responses, String prefix,
            AbstractHueState state, HueStateChange newState) {
        // Apply new state and collect success, error items
        Map<String, Object> successApplied = new TreeMap<>();
        List<String> errorApplied = new ArrayList<>();

        Command command = null;
        if (newState.on != null) {
            try {
                state.as(HueStatePlug.class).on = newState.on;
                command = OnOffType.from(newState.on);
                successApplied.put("on", newState.on);
            } catch (ClassCastException e) {
                errorApplied.add("on");
            }
        }

        if (newState.bri != null) {
            try {
                state.as(HueStateBulb.class).bri = newState.bri;
                command = new PercentType((int) (newState.bri * 100.0 / HueStateBulb.MAX_BRI + 0.5));
                successApplied.put("bri", newState.bri);
            } catch (ClassCastException e) {
                errorApplied.add("bri");
            }
        }

        if (newState.bri_inc != null) {
            try {
                int newBri = state.as(HueStateBulb.class).bri + newState.bri_inc;
                if (newBri < 0 || newBri > HueStateBulb.MAX_BRI) {
                    throw new IllegalArgumentException();
                }
                command = new PercentType((int) (newBri * 100.0 / HueStateBulb.MAX_BRI + 0.5));
                successApplied.put("bri", newState.bri);
            } catch (ClassCastException e) {
                errorApplied.add("bri_inc");
            } catch (IllegalArgumentException e) {
                errorApplied.add("bri_inc");
            }
        }

        if (newState.sat != null) {
            try {
                HueStateColorBulb c = state.as(HueStateColorBulb.class);
                c.sat = newState.sat;
                c.colormode = ColorMode.hs;
                command = c.toHSBType();
                successApplied.put("sat", newState.sat);
            } catch (ClassCastException e) {
                errorApplied.add("sat");
            }
        }

        if (newState.sat_inc != null) {
            try {
                HueStateColorBulb c = state.as(HueStateColorBulb.class);
                int newV = c.sat + newState.sat_inc;
                if (newV < 0 || newV > HueStateColorBulb.MAX_SAT) {
                    throw new IllegalArgumentException();
                }
                c.colormode = ColorMode.hs;
                c.sat = newV;
                command = c.toHSBType();
                successApplied.put("sat", newState.sat);
            } catch (ClassCastException e) {
                errorApplied.add("sat_inc");
            } catch (IllegalArgumentException e) {
                errorApplied.add("sat_inc");
            }
        }

        if (newState.hue != null) {
            try {
                HueStateColorBulb c = state.as(HueStateColorBulb.class);
                c.colormode = ColorMode.hs;
                c.hue = newState.hue;
                command = c.toHSBType();
                successApplied.put("hue", newState.hue);
            } catch (ClassCastException e) {
                errorApplied.add("hue");
            }
        }

        if (newState.hue_inc != null) {
            try {
                HueStateColorBulb c = state.as(HueStateColorBulb.class);
                int newV = c.hue + newState.hue_inc;
                if (newV < 0 || newV > HueStateColorBulb.MAX_HUE) {
                    throw new IllegalArgumentException();
                }
                c.colormode = ColorMode.hs;
                c.hue = newV;
                command = c.toHSBType();
                successApplied.put("hue", newState.hue);
            } catch (ClassCastException e) {
                errorApplied.add("hue_inc");
            } catch (IllegalArgumentException e) {
                errorApplied.add("hue_inc");
            }
        }

        if (newState.ct != null) {
            try {
                // We can't do anything here with a white color temperature.
                // The color type does not support setting it.

                // Adjusting the color temperature implies setting the mode to ct
                if (state instanceof HueStateColorBulb) {
                    HueStateColorBulb c = state.as(HueStateColorBulb.class);
                    c.sat = 0;
                    c.colormode = ColorMode.ct;
                    command = c.toHSBType();
                }
                successApplied.put("colormode", ColorMode.ct);
                successApplied.put("sat", 0);
                successApplied.put("ct", newState.ct);
            } catch (ClassCastException e) {
                errorApplied.add("ct");
            }
        }

        if (newState.ct_inc != null) {
            try {
                // We can't do anything here with a white color temperature.
                // The color type does not support setting it.

                // Adjusting the color temperature implies setting the mode to ct
                if (state instanceof HueStateColorBulb) {
                    HueStateColorBulb c = state.as(HueStateColorBulb.class);
                    if (c.colormode != ColorMode.ct) {
                        c.sat = 0;
                        command = c.toHSBType();
                        successApplied.put("colormode", c.colormode);
                    }
                }
                successApplied.put("ct", newState.ct);
            } catch (ClassCastException e) {
                errorApplied.add("ct_inc");
            }
        }

        if (newState.transitiontime != null) {
            successApplied.put("transitiontime", newState.transitiontime); // Pretend that worked
        }
        if (newState.alert != null) {
            successApplied.put("alert", newState.alert); // Pretend that worked
        }
        if (newState.effect != null) {
            successApplied.put("effect", newState.effect); // Pretend that worked
        }
        if (newState.xy != null) {
            try {
                HueStateColorBulb c = state.as(HueStateColorBulb.class);
                c.colormode = ColorMode.xy;
                c.bri = state.as(HueStateBulb.class).bri;
                c.xy[0] = newState.xy.get(0);
                c.xy[1] = newState.xy.get(1);
                command = c.toHSBType();
                successApplied.put("xy", newState.xy);
            } catch (ClassCastException e) {
                errorApplied.add("xy");
            }
        }
        if (newState.xy_inc != null) {
            try {
                HueStateColorBulb c = state.as(HueStateColorBulb.class);
                double newX = c.xy[0] + newState.xy_inc.get(0);
                double newY = c.xy[1] + newState.xy_inc.get(1);
                if (newX < 0 || newX > 1 || newY < 0 || newY > 1) {
                    throw new IllegalArgumentException();
                }
                c.colormode = ColorMode.xy;
                c.bri = state.as(HueStateBulb.class).bri;
                c.xy[0] = newX;
                c.xy[1] = newY;
                command = c.toHSBType();
                successApplied.put("xy", newState.xy_inc);
            } catch (ClassCastException e) {
                errorApplied.add("xy_inc");
            } catch (IllegalArgumentException e) {
                errorApplied.add("xy_inc");
            }
        }

        // Generate the response. The response consists of a list with an entry each for all
        // submitted change requests. If for example "on" and "bri" was send, 2 entries in the response are
        // expected.
        successApplied.forEach((t, v) -> {
            responses.add(new HueResponse(new HueSuccessResponseStateChanged(prefix + "/" + t, v)));
        });
        errorApplied.forEach(v -> {
            responses.add(
                    new HueResponse(new HueErrorMessage(HueResponse.NOT_AVAILABLE, prefix + "/" + v, "Could not set")));
        });

        return command;
    }

    public static @Nullable DeviceType determineTargetType(ConfigStore cs, Item element) {
        String category = element.getCategory();
        String type = element.getType();
        Set<String> tags = element.getTags();

        // Determine type, heuristically
        DeviceType t = null;

        // The user wants this item to be not exposed
        if (cs.ignoreItemsFilter.stream().anyMatch(tags::contains)) {
            return null;
        }

        // First consider the category
        if (category != null) {
            switch (category) {
                case "ColorLight":
                    t = DeviceType.ColorType;
                    break;
                case "Light":
                    t = DeviceType.SwitchType;
            }
        }

        // Then the tags
        if (cs.switchFilter.stream().anyMatch(tags::contains)) {
            t = DeviceType.SwitchType;
        }
        if (cs.whiteFilter.stream().anyMatch(tags::contains)) {
            t = DeviceType.WhiteTemperatureType;
        }
        if (cs.colorFilter.stream().anyMatch(tags::contains)) {
            t = DeviceType.ColorType;
        }

        // Last but not least, the item type
        if (t == null) {
            switch (type) {
                case CoreItemFactory.COLOR:
                    if (cs.colorFilter.isEmpty()) {
                        t = DeviceType.ColorType;
                    }
                    break;
                case CoreItemFactory.DIMMER:
                case CoreItemFactory.ROLLERSHUTTER:
                    if (cs.whiteFilter.isEmpty()) {
                        t = DeviceType.WhiteTemperatureType;
                    }
                    break;
                case CoreItemFactory.SWITCH:
                    if (cs.switchFilter.isEmpty()) {
                        t = DeviceType.SwitchType;
                    }
                    break;
            }
        }
        return t;
    }

    /**
     * Compute the hue state from a given item state and a device type.
     * If the item state matches the last command. the hue state is adjusted
     * to use the values from the last hue state change. This is done to prevent
     * Alexa reporting device errors.
     *
     * @param itemState The item state
     * @param deviceType The device type
     * @param lastCommand The last command
     * @param lastHueChange The last hue state change
     * @return A hue light state
     */
    public static AbstractHueState adjustedColorStateFromItemState(State itemState, @Nullable DeviceType deviceType,
            @Nullable Command lastCommand, @Nullable HueStateChange lastHueChange) {
        AbstractHueState hueState = colorStateFromItemState(itemState, deviceType);

        if (lastCommand != null && lastHueChange != null) {
            if (lastCommand instanceof HSBType) {
                if (hueState instanceof HueStateColorBulb && itemState.as(HSBType.class).equals(lastCommand)) {
                    HueStateColorBulb c = (HueStateColorBulb) hueState;

                    if (lastHueChange.bri != null) {
                        c.bri = lastHueChange.bri;
                    }
                    if (lastHueChange.hue != null) {
                        c.hue = lastHueChange.hue;
                    }
                    if (lastHueChange.sat != null) {
                        c.sat = lastHueChange.sat;
                    }
                    // Although we can't set a colour temperature in OH
                    // this keeps Alexa happy when asking to turn a light
                    // to white.
                    if (lastHueChange.ct != null) {
                        c.ct = lastHueChange.ct;
                    }
                }
            } else if (lastCommand instanceof PercentType) {
                if (hueState instanceof HueStateBulb && itemState != null
                        && lastCommand.equals(itemState.as(PercentType.class))) {
                    if (lastHueChange.bri != null) {
                        ((HueStateBulb) hueState).bri = lastHueChange.bri;
                    }
                }
            }
        }

        return hueState;
    }
}
