/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal.accessories;

import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.WindowCovering;
import com.beowulfe.hap.accessories.properties.WindowCoveringPositionState;

/**
 * Abstract class implementing a Homekit Lightbulb using a SwitchItem
 *
 * @author Andy Lintner
 */
public class HomekitWindowCoveringImpl extends AbstractHomekitAccessoryImpl<RollershutterItem>
        implements WindowCovering {

    public HomekitWindowCoveringImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater) {
        super(taggedItem, itemRegistry, updater, RollershutterItem.class);
    }

    @Override
    public CompletableFuture<Integer> getCurrentPosition() {
        PercentType state = (PercentType) getItem().getStateAs(PercentType.class);
        return CompletableFuture.completedFuture(state.intValue());
    }

	
	@Override
    public void subscribeCurrentPosition(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), callback);
    }

    @Override
    public void unsubscribeCurrentPosition() {
        getUpdater().unsubscribe(getItem());
    }


    @Override
    public CompletableFuture<Integer> getTargetPosition() {
        PercentType state = (PercentType) getItem().getStateAs(PercentType.class);
        return CompletableFuture.completedFuture(state.intValue());
    }

    @Override
    public CompletableFuture<Void> setTargetPosition(int value) throws Exception {
        GenericItem item = getItem();
        if (item instanceof RollershutterItem) {
            ((RollershutterItem) item).send(new PercentType(value));
        } else if (item instanceof GroupItem) {
            ((GroupItem) item).send(new PercentType(value));
        }
        return CompletableFuture.completedFuture(null);
    }
	
	@Override
    public void subscribeTargetPosition(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), callback);
    }

    @Override
    public void unsubscribeTargetPosition() {
        getUpdater().unsubscribe(getItem());
    }
	
	
	@Override
    public CompletableFuture<WindowCoveringPositionState> getPositionState() {
        return CompletableFuture.completedFuture(WindowCoveringPositionState.STOPPED);
    }
	
	@Override
    public void subscribePositionState(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), callback);
    }

    @Override
    public void unsubscribePositionState() {
        getUpdater().unsubscribe(getItem());
    }
	
	
	@Override
    public CompletableFuture<Void> setHoldPosition(boolean hold) throws Exception {
		GenericItem item = getItem();
        if (item instanceof RollershutterItem) {
            ((RollershutterItem) item).send(hold ? StopMoveType.STOP : StopMoveType.MOVE);
        } else if (item instanceof GroupItem) {
            ((GroupItem) item).send(hold ? StopMoveType.STOP : StopMoveType.MOVE);
        }
        return CompletableFuture.completedFuture(null);
	}
	
	
	@Override
    public CompletableFuture<Boolean> getObstructionDetected() {
		return CompletableFuture.completedFuture(false);
	}
	
	@Override
    public void subscribeObstructionDetected(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), callback);
    }

    @Override
    public void unsubscribeObstructionDetected() {
        getUpdater().unsubscribe(getItem());
    }

}