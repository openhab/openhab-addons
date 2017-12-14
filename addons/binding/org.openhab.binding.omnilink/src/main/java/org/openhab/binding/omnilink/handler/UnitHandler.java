package org.openhab.binding.omnilink.handler;

import org.eclipse.smarthome.core.thing.binding.ThingHandler;

import com.digitaldan.jomnilinkII.MessageTypes.statuses.UnitStatus;

/**
 *
 * @author Craig Hamilton
 *
 */
public interface UnitHandler extends ThingHandler {

    public static final int UNIT_OFF = 0;
    public static final int UNIT_ON = 1;
    public static final int UNIT_SCENE_A = 2;
    public static final int UNIT_SCENE_L = 13;
    public static final int UNIT_DIM_1 = 17;
    public static final int UNIT_DIM_9 = 25;
    public static final int UNIT_BRIGHTEN_1 = 33;
    public static final int UNIT_BRIGHTEN_9 = 41;
    public static final int UNIT_LEVEL_0 = 100;
    public static final int UNIT_LEVEL_100 = 200;

    public void handleUnitStatus(UnitStatus unitStatus);

}
