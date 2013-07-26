// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.ddd;

import com.viadeo.kasper.er.RootConcept;
import org.axonframework.eventsourcing.EventSourcedEntity;

/**
 *
 * Using Kasper, a component entity is an entity which is not an aggregate
 * This kind of entity is used as a component of a parent aggregate
 * 
 * @see Entity
 *
 * @param <R> the parent concept root
 */
public interface ComponentEntity<R extends RootConcept>
		extends Entity, EventSourcedEntity {

	/**
	 * The position of the parent aggregate in generic parameters
	 */
	int PARENT_ARGUMENT_POSITION = 0;
	
}