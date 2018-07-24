/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal.accessories;

import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.ColorfulLightbulb;
import com.beowulfe.hap.accessories.DimmableLightbulb;

/**
 * Implements ColorfulLightBulb using an Item that provides a On/Off and color state
 *
 * @author Felix Rotthowe
 */
class HomekitColorfulLightbulbImpl extends AbstractHomekitLightbulbImpl<ColorItem>
        implements ColorfulLightbulb, DimmableLightbulb {

    public HomekitColorfulLightbulbImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater) {
        super(taggedItem, itemRegistry, updater, ColorItem.class);
    }

    @Override
    public CompletableFuture<Double> getHue() {
        State state = getItem().getStateAs(HSBType.class);
        if (state instanceof HSBType) {
            HSBType hsb = (HSBType) state;
            return CompletableFuture.completedFuture(hsb.getHue().doubleValue());
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Double> getSaturation() {
        State state = getItem().getStateAs(HSBType.class);
        if (state instanceof HSBType) {
            HSBType hsb = (HSBType) state;
            return CompletableFuture.completedFuture(hsb.getSaturation().doubleValue());
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Integer> getBrightness() {
        State state = getItem().getStateAs(PercentType.class);
        if (state instanceof PercentType) {
            PercentType brightness = (PercentType) state;
            return CompletableFuture.completedFuture(brightness.intValue());
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> setHue(Double value) throws Exception {
        if (value == null) {
            value = 0.0;
        }
        State state = getItem().getStateAs(HSBType.class);
        if (state instanceof HSBType) {
            HSBType hsb = (HSBType) state;
            HSBType newState = new HSBType(new DecimalType(value), hsb.getSaturation(), hsb.getBrightness());
            ((ColorItem) getItem()).send(newState);
            return CompletableFuture.completedFuture(null);
        } else {
            // state is undefined (light is not connected)
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> setSaturation(Double value) throws Exception {
        if (value == null) {
            value = 0.0;
        }
        State state = getItem().getStateAs(HSBType.class);
        if (state instanceof HSBType) {
            HSBType hsb = (HSBType) state;
            HSBType newState = new HSBType(hsb.getHue(), new PercentType(value.intValue()), hsb.getBrightness());
            ((ColorItem) getItem()).send(newState);
            return CompletableFuture.completedFuture(null);
        } else {
            // state is undefined (light is not connected)
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> setBrightness(Integer value) throws Exception {
        if (value == null) {
            value = 0;
        }
        State state = getItem().getStateAs(HSBType.class);
        if (state instanceof HSBType) {
            HSBType hsb = (HSBType) state;
            HSBType newState = new HSBType(hsb.getHue(), hsb.getSaturation(), new PercentType(value));
            ((ColorItem) getItem()).send(newState);
            return CompletableFuture.completedFuture(null);
        } else {
            // state is undefined (light is not connected)
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public void subscribeHue(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), "hue", callback);
    }

    @Override
    public void subscribeSaturation(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), "saturation", callback);
    }

    @Override
    public void subscribeBrightness(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), "brightness", callback);
    }

    @Override
    public void unsubscribeHue() {
        getUpdater().unsubscribe(getItem(), "hue");
    }

    @Override
    public void unsubscribeSaturation() {
        getUpdater().unsubscribe(getItem(), "saturation");
    }

    @Override
    public void unsubscribeBrightness() {
        getUpdater().unsubscribe(getItem(), "brightness");
    }

}
