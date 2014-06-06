package com.viadeo.kasper.eventhandling.amqp;

import org.axonframework.eventhandling.EventListener;

import java.util.List;

public interface RoutingKeysResolver {

    /**
     * For a given event listener, this resolver will give out
     * a list of routing keys to bind queue on.
     *
     * @see @com.viadeo.kasper.eventhandling.amqp.AMQPCluster#setupTopology(com.viadeo.kasper.event.EventListener)
     * @param listener the event listener
     * @return a list of routing keys
     */
    public List<String> resolve(EventListener listener);
}
