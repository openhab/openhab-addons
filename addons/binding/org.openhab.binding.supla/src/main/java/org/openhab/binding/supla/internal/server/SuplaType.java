package org.openhab.binding.supla.internal.server;

public final class SuplaType {
    private final long id;
    private final String name;

    public SuplaType(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SuplaType)) return false;

        SuplaType suplaType = (SuplaType) o;

        if (id != suplaType.id) return false;
        return name != null ? name.equals(suplaType.name) : suplaType.name == null;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "SuplaType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
