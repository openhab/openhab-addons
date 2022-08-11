package org.openhab.binding.liquidcheck.internal.json;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class Header {
    public String namespace = "";
    public String name = "";
    public String messageId = "";
    public String payloadVersion = "";
    public String authorization = "";
}
