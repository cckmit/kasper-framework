// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.er.impl;

import com.viadeo.kasper.KasperID;
import com.viadeo.kasper.KasperRelationID;
import com.viadeo.kasper.ddd.impl.AbstractAggregateRoot;
import com.viadeo.kasper.er.RootConcept;
import com.viadeo.kasper.er.RootRelation;
import com.viadeo.kasper.er.annotation.XBidirectional;

/**
 *
 * Base Kasper Relation Aggregate Root implementation
 *
 * @param <S> Source concept of the relation
 * @param <T> Target concept of the relation
 * 
 * @see com.viadeo.kasper.er.Relation
 * @see com.viadeo.kasper.er.RootRelation
 * @see com.viadeo.kasper.ddd.AggregateRoot
 */
public abstract class AbstractRootRelation<S extends RootConcept, T extends RootConcept>
		extends AbstractAggregateRoot<KasperRelationID>
		implements RootRelation<S, T> {

	private static final long serialVersionUID = 4719442806097449770L;

	// ------------------------------------------------------------------------

	/**
	 * @see com.viadeo.kasper.er.Relation#getSourceIdentifier()
	 */
	@Override
	public KasperID getSourceIdentifier() {
		return this.getEntityId().getSourceId();
	}

	/**
	 * @see com.viadeo.kasper.er.Relation#getTargetIdentifier()
	 */
	@Override
	public KasperID getTargetIdentifier() {
 		return this.getEntityId().getTargetId();
	}

	/**
	 * @see com.viadeo.kasper.er.Relation#isBidirectional()
	 */
	@Override
	public boolean isBidirectional() {
        return (null != this.getClass().getAnnotation(XBidirectional.class));
	}

}
