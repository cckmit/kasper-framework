// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.cqrs.command.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.viadeo.kasper.KasperID;
import com.viadeo.kasper.cqrs.command.CreateCommand;

/**
 *
 * Convenient base implementation for idToUse simple management
 * 
 */
public abstract class AbstractCreateCommand implements CreateCommand {
    private static final long serialVersionUID = -432287057793281452L;

	private KasperID idToUse;

	// ------------------------------------------------------------------------

	public AbstractCreateCommand() {
		// For serialization
	}

	public AbstractCreateCommand(final KasperID providedId) {
		this.idToUse = Preconditions.checkNotNull(providedId);
	}

	// ------------------------------------------------------------------------

	@SuppressWarnings("unchecked") // To be ensured by caller
	@Override
	public <C extends CreateCommand> C  setIdToUse(final KasperID providedId) {
		this.idToUse = Preconditions.checkNotNull(providedId);
		return (C) this;
	}

	@Override
	public Optional<KasperID> getIdToUse() {
		return Optional.fromNullable(this.idToUse);
	}

}
