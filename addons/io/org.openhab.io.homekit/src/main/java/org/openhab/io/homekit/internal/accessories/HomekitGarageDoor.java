package org.openhab.io.homekit.internal.accessories;

import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.GarageDoor;
import com.beowulfe.hap.accessories.properties.DoorState;

public class HomekitGarageDoor extends AbstractHomekitAccessoryImpl<GroupItem> implements GarageDoor, GroupedAccessory {

    private final String groupName;
    private final HomekitSettings settings;
    private String currentDoorStateItemName;
    private String targetDoorStateItemName;
    private String obstructionDetectedItemName;

    private Logger logger = LoggerFactory.getLogger(HomekitGarageDoor.class);

    public HomekitGarageDoor(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry, HomekitAccessoryUpdater updater,
            HomekitSettings settings) {
        super(taggedItem, itemRegistry, updater, GroupItem.class);
        this.groupName = taggedItem.getItem().getName();
        this.settings = settings;
    }

    @Override
    public String getGroupName() {
        return groupName;
    }

    @Override
    public void addCharacteristic(HomekitTaggedItem item) {
        switch (item.getCharacteristicType()) {
            case CURRENT_DOOR_STATE:
                currentDoorStateItemName = item.getItem().getName();
                break;

            case TARGET_DOOR_STATE:
                targetDoorStateItemName = item.getItem().getName();
                break;

            case OBSTRUCTION_DETECTED:
                obstructionDetectedItemName = item.getItem().getName();
                break;

            default:
                logger.error("Unrecognized thermostat characteristic: " + item.getCharacteristicType().name());
                break;
        }
    }

    @Override
    public boolean isComplete() {
        return currentDoorStateItemName != null && targetDoorStateItemName != null
                && obstructionDetectedItemName != null;
    }

    @Override
    public CompletableFuture<DoorState> getCurrentDoorState() {
        Item item = getItemRegistry().get(currentDoorStateItemName);
        StringType state = (StringType) item.getStateAs(StringType.class);
        DoorState doorState;
        if (state != null) {
            String stringValue = state.toString();

            if (stringValue.equals(settings.getGarageDoorClosedState())) {
                doorState = DoorState.CLOSED;
            } else if (stringValue.equals(settings.getGarageDoorOpenState())) {
                doorState = DoorState.OPEN;
            } else if (stringValue.equals(settings.getGarageDoorOpeningState())) {
                doorState = DoorState.OPENING;
            } else if (stringValue.equals(settings.getGarageDoorClosingState())) {
                doorState = DoorState.CLOSING;
            } else if (stringValue.equals(settings.getGarageDoorStoppedState())) {
                doorState = DoorState.STOPPED;
            } else {
                logger.error("Unrecognized heating cooling target mode: " + stringValue
                        + ". Expected cool, heat, auto, or off strings in value.");
                doorState = DoorState.STOPPED;
            }
        } else {
            logger.info("Heating cooling target mode not available. Relaying value of OFF to Homekit");
            doorState = DoorState.STOPPED;
        }
        return CompletableFuture.completedFuture(doorState);
    }

    @Override
    public CompletableFuture<Boolean> getObstructionDetected() {
        Item item = getItemRegistry().get(obstructionDetectedItemName);
        OnOffType state = (OnOffType) item.getStateAs(OnOffType.class);
        if (state == null) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(state == OnOffType.ON);
    }

    @Override
    public CompletableFuture<DoorState> getTargetDoorState() {
        Item item = getItemRegistry().get(targetDoorStateItemName);
        OnOffType state = (OnOffType) item.getStateAs(OnOffType.class);
        if (state == null) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(state == OnOffType.ON ? DoorState.OPEN : DoorState.CLOSED);
    }

    @Override
    public CompletableFuture<Void> setTargetDoorState(DoorState targetDoorState) throws Exception {
        SwitchItem item = getGenericItem(targetDoorStateItemName);
        item.send(targetDoorState == DoorState.OPEN ? OnOffType.ON : OnOffType.OFF);

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeCurrentDoorState(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getGenericItem(currentDoorStateItemName), callback);
    }

    @Override
    public void subscribeObstructionDetected(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getGenericItem(obstructionDetectedItemName), callback);
    }

    @Override
    public void subscribeTargetDoorState(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getGenericItem(targetDoorStateItemName), callback);
    }

    @Override
    public void unsubscribeCurrentDoorState() {
        getUpdater().unsubscribe(getGenericItem(currentDoorStateItemName));
    }

    @Override
    public void unsubscribeObstructionDetected() {
        getUpdater().unsubscribe(getGenericItem(obstructionDetectedItemName));
    }

    @Override
    public void unsubscribeTargetDoorState() {
        getUpdater().unsubscribe(getGenericItem(targetDoorStateItemName));
    }

    @SuppressWarnings("unchecked")
    private <T extends GenericItem> T getGenericItem(String name) {
        Item item = getItemRegistry().get(name);
        if (item == null) {
            return null;
        }
        if (!(item instanceof GenericItem)) {
            throw new RuntimeException("Expected GenericItem, found " + item.getClass().getCanonicalName());
        }
        return (T) item;
    }
}
