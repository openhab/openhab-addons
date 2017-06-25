package org.openhab.binding.supla.internal.server;

public final class SuplaChannel {
    private final long id;
    private final int channelNumber;
    private final String caption; // Nullable
    private final SuplaType type;
    private final SuplaFunction function;

    public SuplaChannel(long id, int channelNumber, String caption, SuplaType type, SuplaFunction function) {
        this.id = id;
        this.channelNumber = channelNumber;
        this.caption = caption;
        this.type = type;
        this.function = function;
    }

    public long getId() {
        return id;
    }

    public int getChannelNumber() {
        return channelNumber;
    }

    public String getCaption() {
        return caption;
    }

    public SuplaType getType() {
        return type;
    }

    public SuplaFunction getFunction() {
        return function;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SuplaChannel)) return false;

        SuplaChannel that = (SuplaChannel) o;

        if (id != that.id) return false;
        if (channelNumber != that.channelNumber) return false;
        if (caption != null ? !caption.equals(that.caption) : that.caption != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        return function != null ? function.equals(that.function) : that.function == null;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "SuplaChannel{" +
                "id=" + id +
                ", channelNumber=" + channelNumber +
                ", caption='" + caption + '\'' +
                ", type=" + type +
                ", function=" + function +
                '}';
    }
}
