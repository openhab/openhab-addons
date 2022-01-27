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
package org.openhab.io.hueemulation.internal.dto;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.GenericItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.types.Command;
import org.openhab.io.hueemulation.internal.DeviceType;
import org.openhab.io.hueemulation.internal.StateUtils;
import org.openhab.io.hueemulation.internal.dto.changerequest.HueStateChange;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Hue API device object
 *
 * @author Dan Cunningham - Initial contribution
 * @author David Graeff - Color lights and plugs
 * @author Florian Lentz - XY Support
 */
@NonNullByDefault
public class HueLightEntry {
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
    public @Nullable Boolean friendsOfHue = true;
    public final @Nullable String colorGamut;
    public @Nullable Boolean hascolor = null;

    public String name;
    /** Associated item UID */
    public @NonNullByDefault({}) transient GenericItem item;
    public transient DeviceType deviceType;
    public transient @Nullable Command lastCommand = null;
    public transient @Nullable HueStateChange lastHueChange = null;

    public static class Config {
        public final String archetype = "classicbulb";
        public final String function = "functional";
        public final String direction = "omnidirectional";
    }

    public Config config = new Config();

    public static class Streaming {
        public boolean renderer = false;
        public boolean proxy = false;
    }

    public static class Capabilities {
        public boolean certified = false;
        public final Streaming streaming = new Streaming();
        public final Object control = new Object();
    }

    public Capabilities capabilities = new Capabilities();

    private HueLightEntry() {
        this(new StringItem(""), "", DeviceType.SwitchType);
    }

    /**
     * Create a hue device.
     *
     * @param item The associated item
     * @param uniqueid The unique id
     * @param deviceType The device type decides which capabilities this device has
     */
    public HueLightEntry(GenericItem item, String uniqueid, DeviceType deviceType) {
        String label = item.getLabel();
        this.item = item;
        this.deviceType = deviceType;
        this.uniqueid = uniqueid;
        switch (deviceType) {
            case ColorType:
                this.name = label != null ? label : "";
                this.type = "Extended Color light";
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
                this.type = "Dimmable light";
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
                this.type = "Color temperature light";
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
            case SwitchType:
            default:
                /**
                 * Pretend to be an OSRAM plug, there is no native Philips Hue plug on the market.
                 * Those are supported by most of the external apps and Alexa.
                 */
                this.name = label != null ? label : "";
                this.type = "On/off light";
                this.modelid = "Plug 01";
                this.colorGamut = null;
                this.manufacturername = "OSRAM";
                this.productname = "On/Off plug";
                this.swconfigid = null;
                this.swversion = "V1.04.12";
                this.productid = null;
                this.hascolor = false;
                this.friendsOfHue = null;
                break;
        }

        state = StateUtils.colorStateFromItemState(item.getState(), deviceType);
    }

    /**
     * This custom serializer updates the light state and label, before serializing.
     */
    @NonNullByDefault({})
    public static class Serializer implements JsonSerializer<HueLightEntry> {

        static class HueDeviceHelper extends HueLightEntry {
        }

        @Override
        public JsonElement serialize(HueLightEntry product, Type type, JsonSerializationContext context) {
            product.state = StateUtils.adjustedColorStateFromItemState(product.item.getState(), product.deviceType,
                    product.lastCommand, product.lastHueChange);
            String label = product.item.getLabel();
            if (label != null) {
                product.name = label;
            }

            JsonElement jsonSubscription = context.serialize(product, HueDeviceHelper.class);
            return jsonSubscription;
        }
    }

    /**
     * Replaces the associated openHAB item of this hue device with the given once
     * and also synchronizes/updates the color information of this hue device with the item.
     *
     * @param element A replace item
     */
    public void updateItem(GenericItem element) {
        item = element;
        state = StateUtils.colorStateFromItemState(item.getState(), deviceType);

        lastCommand = null;
        lastHueChange = null;

        String label = element.getLabel();
        if (label != null) {
            name = label;
        }
    }
}
