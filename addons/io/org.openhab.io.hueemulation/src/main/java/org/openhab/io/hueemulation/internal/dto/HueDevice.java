/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal.dto;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.io.hueemulation.internal.DeviceType;

/**
 * Hue API device object
 *
 * @author Dan Cunningham - Initial contribution
 * @author David Graeff - Color lights and plugs
 */
@NonNullByDefault
public class HueDevice {
    public AbstractHueState state = new AbstractHueState();
    public final String type;
    public final String modelid;
    public final String uniqueid;
    public final String manufacturername;
    public final @Nullable String productname;
    public final String swversion;
    public final @Nullable String luminaireuniqueid = null;
    public final @Nullable String swconfigid;
    public final @Nullable String productid;
    public Boolean friendsOfHue = true;
    public final @Nullable String colorGamut;
    public @Nullable Boolean hascolor = null;

    public String name;
    /** Associated item UID */
    public transient Item item;
    public transient DeviceType deviceType;

    public static class Config {
        public final String archetype = "classicbulb";
        public final String function = "functional";
        public final String direction = "omnidirectional";
    };

    public Config config = new Config();

    public static class Streaming {
        public boolean renderer = false;
        public boolean proxy = false;
    };

    public static class Capabilities {
        public boolean certified = false;
        public final Streaming streaming = new Streaming();
        public final Object control = new Object();
    };

    public Capabilities capabilities = new Capabilities();

    /**
     * Create a hue device.
     *
     * @param targetType The state type
     * @param item
     * @param name
     * @param uniqueid
     * @param deviceType
     */
    public HueDevice(Item item, String uniqueid, DeviceType deviceType) {
        String label = item.getLabel();
        this.item = item;
        this.deviceType = deviceType;
        this.uniqueid = uniqueid;
        switch (deviceType) {
            case ColorType:
                this.name = label != null ? label : "";
                this.type = "Extended color light";
                this.modelid = "LCT010";
                this.colorGamut = "C";
                this.manufacturername = "Philips";
                this.swconfigid = "F921C859";
                this.swversion = "1.15.2_r19181";
                this.productid = "Philips-LCT010-1-A19ECLv4";
                this.productname = null;
                this.hascolor = true;
                this.capabilities.certified = true;
                break;
            case WhiteType:
                /** Hue White A19 - 3nd gen - white, 2700K only */
                this.name = label != null ? label : "";
                this.type = "Dimmable Light";
                this.modelid = "LWB006";
                this.colorGamut = null;
                this.manufacturername = "Philips";
                this.swconfigid = null;
                this.swversion = "66012040";
                this.productid = null;
                this.hascolor = false;
                this.productname = null;
                this.capabilities.certified = true;
                break;
            case WhiteTemperatureType:
                this.name = label != null ? label : "";
                this.type = "Color Temperature Light";
                this.modelid = "LTW001";
                this.colorGamut = "2200K-6500K";
                this.manufacturername = "Philips";
                this.swconfigid = null;
                this.swversion = "66012040";
                this.productid = null;
                this.hascolor = false;
                this.productname = null;
                this.capabilities.certified = true;
                break;
            default:
            case SwitchType:
                /**
                 * Pretend to be an OSRAM plug, there is no native Philips Hue plug on the market.
                 * Those are supported by most of the external apps and Alexa.
                 */
                this.name = label != null ? label : "";
                this.type = "On/Off plug-in unit";
                this.modelid = "Plug 01";
                this.colorGamut = null;
                this.manufacturername = "OSRAM";
                this.productname = "On/Off plug";
                this.swconfigid = null;
                this.swversion = "V1.04.12";
                this.productid = null;
                break;
        }

        setState(item.getState());
    }

    private void setState(State itemState) {
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
    }

    private <T extends AbstractHueState> T as(Class<T> type) throws ClassCastException {
        return type.cast(state);
    }

    /**
     * Apply the new received state from the REST PUT request.
     *
     * @param newState       New state
     * @param successApplied Output map "state-name"->value: All successfully applied items are added in here
     * @param errorApplied   Output: All erroneous items are added in here
     * @return Return a command computed via the incoming state object.
     */
    public @Nullable Command applyState(HueStateChange newState, Map<String, Object> successApplied,
            List<String> errorApplied) {
        // First synchronize the internal state information with the framework
        setState(item.getState());

        Command command = null;
        if (newState.on != null) {
            try {
                if (as(HueStatePlug.class).on != newState.on) {
                    as(HueStatePlug.class).on = newState.on;
                    command = OnOffType.from(newState.on);
                }
                successApplied.put("on", newState.on);
            } catch (ClassCastException e) {
                errorApplied.add("on");
            }
        }

        if (newState.bri != null) {
            try {
                if (as(HueStateBulb.class).bri != newState.bri) {
                    as(HueStateBulb.class).bri = newState.bri;
                    command = new PercentType(newState.bri * 100 / HueStateBulb.MAX_BRI);
                }
                successApplied.put("bri", newState.bri);
            } catch (ClassCastException e) {
                errorApplied.add("bri");
            }
        }

        if (newState.bri_inc != null) {
            try {
                int newBri = as(HueStateBulb.class).bri + newState.bri_inc;
                if (newBri < 0 || newBri > HueStateBulb.MAX_BRI) {
                    throw new ClassCastException();
                }
                command = new PercentType(newBri * 100 / HueStateBulb.MAX_BRI);
                successApplied.put("bri", newState.bri);
            } catch (ClassCastException e) {
                errorApplied.add("bri_inc");
            }
        }

        if (newState.sat != null) {

            try {
                HueStateColorBulb c = as(HueStateColorBulb.class);
                if (c.sat != newState.sat) {
                    c.sat = newState.sat;
                    command = c.toHSBType();
                }
                successApplied.put("sat", newState.sat);
            } catch (ClassCastException e) {
                errorApplied.add("sat");
            }
        }

        if (newState.hue != null) {
            try {
                HueStateColorBulb c = as(HueStateColorBulb.class);
                if (c.hue != newState.hue) {
                    c.hue = newState.hue;
                    command = c.toHSBType();
                }
                successApplied.put("hue", newState.hue);
            } catch (ClassCastException e) {
                errorApplied.add("hue");
            }
        }

        if (newState.sat_inc != null) {
            try {
                HueStateColorBulb c = as(HueStateColorBulb.class);
                int newV = c.sat + newState.sat_inc;
                if (newV < 0 || newV > HueStateColorBulb.MAX_SAT) {
                    throw new ClassCastException();
                }
                c.sat = newV;
                command = c.toHSBType();
                successApplied.put("sat", newState.sat);
            } catch (ClassCastException e) {
                errorApplied.add("sat_inc");
            }
        }

        if (newState.hue_inc != null) {
            try {
                HueStateColorBulb c = as(HueStateColorBulb.class);
                int newV = c.hue + newState.hue_inc;
                if (newV < 0 || newV > HueStateColorBulb.MAX_HUE) {
                    throw new ClassCastException();
                }
                c.hue = newV;
                command = c.toHSBType();
                successApplied.put("hue", newState.hue);
            } catch (ClassCastException e) {
                errorApplied.add("hue_inc");
            }
        }

        if (newState.ct != null) {
            try {
                if (as(HueStateBulb.class).ct != newState.ct) {
                    as(HueStateBulb.class).ct = newState.ct;
                    // We can't do anything here with a white color temperature.
                    // The core ESH color type does not support setting it.
                }
                successApplied.put("ct", newState.ct);
            } catch (ClassCastException e) {
                errorApplied.add("ct");
            }
        }

        if (newState.ct_inc != null) {
            try {
                HueStateColorBulb c = as(HueStateColorBulb.class);
                int newV = c.ct + newState.ct_inc;
                if (newV < 0 || newV > HueStateBulb.MAX_CT) {
                    throw new ClassCastException();
                }
                c.ct = newV;
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
            errorApplied.add("xy");
        }
        if (newState.xy_inc != null) {
            errorApplied.add("xy_inc");
        }

        return command;
    }

    public void updateItem(Item element) {
        item = element;
        setState(item.getState());
        // Just update the item label and item reference
        String label = element.getLabel();
        if (label != null) {
            name = label;
        }
    }
}
