package org.openhab.binding.mynice.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public enum CommandType {
    PAIR(false,
            "Authentication username=\"%s\" cc=\"null\" CType=\"phone\" OSType=\"Android\" OSVer=\"6.0.1\" desc=\"%s\""),
    VERIFY(false, "User username=\"%s\""),
    CONNECT(false, "Authentication username=\"%s\" cc=\"%s\""),
    INFO(true, ""),
    STATUS(true, "");

    public final boolean signNeeded;
    private final String body;

    CommandType(boolean signNeeded, String body) {
        this.signNeeded = signNeeded;
        this.body = body;
    }

    public String getBody(Object... bodyParms) {
        return body.length() == 0 ? body : String.format("<" + body + "/>", bodyParms);
    }
}
