package org.openhab.binding.robonect.model;

public enum MowerStatus {

    DETECTING_STATUS(0),
    PARKING(1),
    MOWING(2),
    SEARCH_CHARGING_STATION(3),
    CHARGING(4),
    SEARCHING(5),
    UNKNOWN_6(6),
    ERROR_STATUS(7),
    LOST_SIGNAL(8),
    UNKNOWN_9(9),
    UNKNOWN_10(10),
    UNKNOWN_11(11),
    UNKNOWN_12(12),
    UNKNOWN_13(13),
    UNKNOWN_14(14),
    UNKNOWN_15(15),
    OFF(16),
    SLEEPING(17),
    OFFLINE(98),
    UNKNOWN(99);
    

    private int statusCode;
    
    // this is just a workaround to support future, undocumented or unknown codes in rules.
    private Integer unknownCode;

    MowerStatus(int statusCode) {
        this.statusCode = statusCode;
    }
    

    public static MowerStatus fromCode(int code) {
        for (MowerStatus status : MowerStatus.values()) {
            if (status.statusCode == code) {
                return status;
            }
        }
        UNKNOWN.unknownCode = new Integer(code);
        return UNKNOWN;
    }

    public int getStatusCode() {
        if(unknownCode == null){
            return statusCode;
        }else {
            return unknownCode;
        }
    }
    
    /*
     0: Status wird ermittelt
    1: Automower parkt
    2: Automower mäht
    3: Automower sucht die Ladestation
    4: Automower lädt
    5: Automower sucht (wartet auf das Umsetzen im manuellen Modus)
    7: Fehlerstatus
    8: Schleifensignal verloren
    16: Automower abgeschaltet
    17: Automower schläft*/
}
