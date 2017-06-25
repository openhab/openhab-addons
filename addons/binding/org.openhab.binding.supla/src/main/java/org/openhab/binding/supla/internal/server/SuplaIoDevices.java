package org.openhab.binding.supla.internal.server;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class SuplaIoDevices implements List<SuplaIoDevice>{
    private final List<SuplaIoDevice> suplaIoDevices;

    public SuplaIoDevices(List<SuplaIoDevice> suplaIoDevices) {
        this.suplaIoDevices = ImmutableList.copyOf(suplaIoDevices);
    }

    @Override
    public int size() {
        return suplaIoDevices.size();
    }

    @Override
    public boolean isEmpty() {
        return suplaIoDevices.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return suplaIoDevices.contains(o);
    }

    @Override
    public Iterator<SuplaIoDevice> iterator() {
        return suplaIoDevices.iterator();
    }

    @Override
    public Object[] toArray() {
        return suplaIoDevices.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return suplaIoDevices.toArray(a);
    }

    @Override
    public boolean add(SuplaIoDevice suplaIoDevice) {
        return suplaIoDevices.add(suplaIoDevice);
    }

    @Override
    public boolean remove(Object o) {
        return suplaIoDevices.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return suplaIoDevices.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends SuplaIoDevice> c) {
        return suplaIoDevices.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends SuplaIoDevice> c) {
        return suplaIoDevices.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return suplaIoDevices.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return suplaIoDevices.retainAll(c);
    }

    @Override
    public void clear() {
        suplaIoDevices.clear();
    }

    @Override
    public SuplaIoDevice get(int index) {
        return suplaIoDevices.get(index);
    }

    @Override
    public SuplaIoDevice set(int index, SuplaIoDevice element) {
        return suplaIoDevices.set(index, element);
    }

    @Override
    public void add(int index, SuplaIoDevice element) {
        suplaIoDevices.add(index, element);
    }

    @Override
    public SuplaIoDevice remove(int index) {
        return suplaIoDevices.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return suplaIoDevices.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return suplaIoDevices.lastIndexOf(o);
    }

    @Override
    public ListIterator<SuplaIoDevice> listIterator() {
        return suplaIoDevices.listIterator();
    }

    @Override
    public ListIterator<SuplaIoDevice> listIterator(int index) {
        return suplaIoDevices.listIterator(index);
    }

    @Override
    public List<SuplaIoDevice> subList(int fromIndex, int toIndex) {
        return suplaIoDevices.subList(fromIndex, toIndex);
    }
}
