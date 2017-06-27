/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.robonect.model;

/**
 * @author Marco Meyer - Initial contribution
 */
public class Timer {
    
    public enum TimerMode{
        INACTIVE(0), ACTIVE(1), STANDBY(2);
        
        private int code;
        
        TimerMode(int code){
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static TimerMode fromCode(int code) {
            for (TimerMode status : TimerMode.values()) {
                if (status.code == code) {
                    return status;
                }
            }
            return INACTIVE;
        }
    }
    
    private TimerMode status;

    private NextTimer next;
    
    public TimerMode getStatus() {
        return status;
    }

    public NextTimer getNext() {
        return next;
    }

    public void setStatus(TimerMode status) {
        this.status = status;
    }

    public void setNext(NextTimer next) {
        this.next = next;
    }
}
