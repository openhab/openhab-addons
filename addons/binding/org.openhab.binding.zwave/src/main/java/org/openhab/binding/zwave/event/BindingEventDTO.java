package org.openhab.binding.zwave.event;

/**
 * Base BindingEvent data transfer object
 *
 * This can be extended by bindings to add binding specific information, but provides basic information to allow the UI
 * to render a message the user without having to implement any binding specific processor.
 *
 * @author Chris Jackson - initial contribution
 *
 */
public class BindingEventDTO {
    BindingEventType type;
    String message;

    public BindingEventDTO(BindingEventType type, String message) {
        this.type = type;
        this.message = message;
    }
}
