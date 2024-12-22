package org.openhab.binding.enocean.internal.statemachine;

/**
 *
 * @author Sven Schad - Initial contribution
 * 
 */

public class STMTransition {
    STMState from;
    STMAction action;
    STMState to;

    public STMTransition(STMState from, STMAction action, STMState to) {
        this.from = from;
        this.action = action;
        this.to = to;
    }
}
