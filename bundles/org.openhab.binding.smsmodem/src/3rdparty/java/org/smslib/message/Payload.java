package org.smslib.message;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Extracted from SMSLib
 */
@NonNullByDefault
public class Payload {
    public enum Type {
        Text,
        Binary
    }

    private @Nullable String textData;

    private byte @Nullable [] binaryData;

    private Type type;

    public Payload(String data) {
        this.type = Type.Text;
        this.textData = data;
    }

    public Payload(byte[] data) {
        this.type = Type.Binary;
        this.binaryData = data.clone();
    }

    public Payload(Payload p) {
        this.type = p.getType();
        this.textData = (this.type == Type.Text ? p.getText() : "");
        byte[] bytes = p.getBytes();
        this.binaryData = (this.type == Type.Binary && bytes != null ? bytes.clone() : null);
    }

    public Type getType() {
        return this.type;
    }

    public @Nullable String getText() {
        return (this.type == Type.Text ? this.textData : null);
    }

    public byte @Nullable [] getBytes() {
        return (this.type == Type.Binary ? this.binaryData : null);
    }

    public boolean isMultipart() {
        return false;
    }
}
