// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================

package com.viadeo.kasper.ddd.specification.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * A basic implementation 
 *
 */
public class DefaultSpecificationErrorMessage implements com.viadeo.kasper.ddd.specification.SpecificationErrorMessage {

	private String message;
	
	// ------------------------------------------------------------------------
	
	/**
	 * @see com.viadeo.kasper.ddd.specification.SpecificationErrorMessage#getMessage()
	 */
	@Override
	public Optional<String> getMessage() {
		return Optional.fromNullable(this.message);
	}

	// ------------------------------------------------------------------------
	
	/**
	 * @see com.viadeo.kasper.ddd.specification.SpecificationErrorMessage#setMessage(java.lang.String)
	 */
	@Override
	public void setMessage(final String message) {
		this.message = Preconditions.checkNotNull(message);
	}
	
}