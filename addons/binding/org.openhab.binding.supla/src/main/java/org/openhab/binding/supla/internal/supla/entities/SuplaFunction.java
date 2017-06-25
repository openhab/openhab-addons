package org.openhab.binding.supla.internal.supla.entities;

public final class SuplaFunction {
    private final long id;
    private final String name;

    public SuplaFunction(long id, String name) {
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
        if (!(o instanceof SuplaFunction)) return false;

        SuplaFunction that = (SuplaFunction) o;

        if (id != that.id) return false;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "SuplaFunction{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
