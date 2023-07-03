package org.openhab.binding.toyota.internal.dto;

public class ProtectionState {
    public String overallStatus;
    public String timestamp;
    public Doors doors;
    public Hood hood;
    public Lamps lamps;
    public Windows windows;
    public Key key;
    public Lock lock;
}
