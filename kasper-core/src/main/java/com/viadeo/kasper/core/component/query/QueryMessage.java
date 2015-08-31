// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.core.component.query;

import com.viadeo.kasper.api.component.query.Query;
import com.viadeo.kasper.api.context.Context;
import com.viadeo.kasper.core.component.KasperMessage;

/**
 * The kasper query message base implementation
 *
 * @param <Q> the enclosed query type
 */
public class QueryMessage<Q extends Query> extends KasperMessage<Q> {

	private static final long serialVersionUID = 8648752933168387124L;

	// -----------------------------------------------------------------------

	public QueryMessage(final Context context, final Q query) {
        super(context, query);
	}

	// -----------------------------------------------------------------------

	public Q getQuery() {
		return getInput();
	}

}