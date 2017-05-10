package org.openhab.binding.robonect.model;

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
