package org.openhab.binding.mynice.internal.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Error")
public class Error {
    @XStreamAlias("Code")
    private int code;
    @XStreamAlias("Info")
    private String info;
}
