package org.openhab.binding.sedif.internal.dto;

import java.util.Hashtable;

import org.eclipse.jdt.annotation.Nullable;

public class Action {

    public class ReturnValue {
        public @Nullable Value returnValue;
    };

    public @Nullable String id;
    public @Nullable String descriptor;
    public @Nullable String callingDescriptor;
    public @Nullable String state;
    public Hashtable<String, Object> params = new Hashtable<String, Object>();
    public @Nullable ReturnValue returnValue;
}
