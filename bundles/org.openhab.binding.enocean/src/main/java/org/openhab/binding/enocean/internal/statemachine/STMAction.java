package org.openhab.binding.enocean.internal.statemachine;

/**
 *
 * @author Sven Schad - Initial contribution
 * 
 */

// @NonNullByDefault
public enum STMAction {
    POSITION_REQUEST_UP,
    POSITION_REQUEST_DOWN,
    POSITION_DONE,
    SLATS_POS_REQUEST,
    SLATS_POS_DONE,
    CALIBRATION_REQUEST_UP,
    CALIBRATION_REQUEST_DOWN,
    CALIBRATION_DONE,
    INVALID_REQUEST
}
