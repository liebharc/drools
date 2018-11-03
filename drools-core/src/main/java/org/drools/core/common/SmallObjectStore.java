package org.drools.core.common;

import org.kie.api.runtime.ObjectFilter;

import java.util.*;

public class SmallObjectStore implements ObjectStore {

    private final List<InternalFactHandle> handles = new ArrayList<>();

    private final Map<Integer, InternalFactHandle> index = new HashMap<>();

    @Override
    public int size() {
        return handles.size();
    }

    @Override
    public boolean isEmpty() {
        return handles.isEmpty();
    }

    @Override
    public void clear() {
        handles.clear();
        index.clear();
    }

    @Override
    public Object getObjectForHandle(InternalFactHandle handle) {
        if (!index.containsKey(System.identityHashCode(handle.getObject()))) {
            return null;
        }

        return handle.getObject();
    }

    @Override
    public InternalFactHandle reconnect(InternalFactHandle factHandle) {
        Optional<InternalFactHandle> matchingValue = findByHandle(factHandle);
        if (matchingValue.isPresent()) {
            return matchingValue.get();
        }

        addHandle(factHandle, factHandle.getObject());
        return factHandle;
    }

    @Override
    public InternalFactHandle getHandleForObject(Object object) {
        return index.get(System.identityHashCode(object));
    }

    @Override
    public InternalFactHandle getHandleForObjectIdentity(Object object) {
        return this.getHandleForObject(object);
    }

    @Override
    public void updateHandle(InternalFactHandle handle, Object object) {
        index.remove(System.identityHashCode(handle.getObject()));
        handle.setObject(object);
        index.put(System.identityHashCode(object), handle);
    }

    @Override
    public void addHandle(InternalFactHandle handle, Object object) {
        handle.setObject(object);
        handles.add(handle);
        index.put(System.identityHashCode(object), handle);
    }

    @Override
    public void removeHandle(InternalFactHandle handle) {
        if (!handles.remove(handle)) {
            return;
        }

        if (index.remove(System.identityHashCode(handle.getObject())) == null) {
            Optional<InternalFactHandle> matchingValue = findByHandle(handle);
            if (matchingValue.isPresent()) {
                index.remove(System.identityHashCode(matchingValue.get().getObject()));
            }
        }
    }

    private Optional<InternalFactHandle> findByHandle(InternalFactHandle handle) {
        return handles.stream()
                .filter(entry -> entry.equals(handle))
                .findAny();
    }

    @Override
    public Iterator<Object> iterateObjects() {
        return handles.stream().map(h -> h.getObject()).iterator();
    }

    @Override
    public Iterator<Object> iterateObjects(ObjectFilter filter) {
        return handles.stream().map(h -> h.getObject()).filter(h -> filter.accept(h)).iterator();
    }

    public Iterator<Object> iterateObjects(Class<?> clazz) {
        return handles.stream().map(h -> h.getObject()).filter(h -> clazz.isInstance(h)).iterator();
    }

    @Override
    public Iterator<InternalFactHandle> iterateFactHandles() {
        return handles.stream().iterator();
    }

    @Override
    public Iterator<InternalFactHandle> iterateFactHandles(ObjectFilter filter) {
        return handles.stream().filter(h -> filter.accept(h.getObject())).iterator();
    }

    @Override
    public Iterator<InternalFactHandle> iterateFactHandles(Class<?> clazz) {
        return handles.stream().filter(h -> clazz.isInstance(h.getObject())).iterator();
    }


    @Override
    public Iterator<Object> iterateNegObjects(ObjectFilter filter) {
        return Collections.emptyIterator();
    }

    @Override
    public Iterator<InternalFactHandle> iterateNegFactHandles(ObjectFilter filter) {
        return Collections.emptyIterator();
    }
}
