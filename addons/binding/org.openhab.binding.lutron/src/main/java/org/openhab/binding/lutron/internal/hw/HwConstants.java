package org.openhab.binding.lutron.internal.hw;

import static org.openhab.binding.lutron.LutronBindingConstants.BINDING_ID;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * Defines common constants, which are used across the whole binding.
 *
 * @author Andrew Shilliday
 */
public class HwConstants {
    public static final ThingTypeUID THING_TYPE_HWSERIALBRIDGE = new ThingTypeUID(BINDING_ID, "hwserialbridge");
    public static final ThingTypeUID THING_TYPE_HWDIMMER = new ThingTypeUID(BINDING_ID, "hwdimmer");
}
