package org.openhab.binding.enocean.internal.statemachine;

/**
 *
 * @author Sven Schad - Initial contribution
 * 
 */

public enum STMState {
    IDLE,
    MOVEMENT_POSITION_UP,
    MOVEMENT_POSITION_DOWN,
    POSITION_REACHED,
    MOVEMENT_SLATS,
    MOVEMENT_CALIBRATION_UP,
    MOVEMENT_CALIBRATION_DOWN,
    INVALID
}