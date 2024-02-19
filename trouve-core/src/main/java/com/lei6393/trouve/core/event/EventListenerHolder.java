package com.lei6393.trouve.core.event;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author yulei
 * @date 2022/5/23 01:38
 */
public class EventListenerHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventListenerHolder.class);

    private static final MultiValuedMap<Class<?>, IEventListener> LISTENER_MAPPING = new HashSetValuedHashMap<>();

    public static void registerListener(IEventListener listener) {
        try {
            Class<?> clazz = listener.getClass().getDeclaredMethod("listen").getParameterTypes()[0];
            LISTENER_MAPPING.put(clazz, listener);
        } catch (Exception e) {
            LOGGER.error("listener register error!", e);
        }
    }

    public static void registerListeners(Collection<IEventListener> listeners) {
        for (IEventListener listener : listeners) {
            registerListener(listener);
        }
    }


    public static <EVENT extends IEvent> void listen(EVENT event) {
        Collection<IEventListener> listeners = LISTENER_MAPPING.get(event.getClass());
        for (IEventListener listener : listeners) {
            listener.listen(event);
        }
    }
}
