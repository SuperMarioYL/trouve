package com.lei6393.trouve.core.event;

/**
 * @author yulei
 * @date 2022/5/23 01:27
 */
public interface IEventListener<EVENT extends IEvent> {

    void listen(EVENT event);
}
