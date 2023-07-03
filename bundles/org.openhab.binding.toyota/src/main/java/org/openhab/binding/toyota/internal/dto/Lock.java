package org.openhab.binding.toyota.internal.dto;

import java.util.ArrayList;

public class Lock {
    public String lockState;
    public String source;
    public ArrayList<String> failedUnlockPreconditions;
}
