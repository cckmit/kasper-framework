// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.cqrs.query;

/**
 *
 * This is a convenient class
 *
 * Extend it instead of implementing IQueryHandler will allow you to
 * override at your convenience retrieve(message) or simply retrieve(query)
 * if you are not interested by the message
 *
 * The query gateway is aware of this internal convenience and will deal with it
 *
 * @param <Q> the query
 * @param <RESULT> the Response
 */
public abstract class QueryHandler<Q extends Query, RESULT extends QueryResult> {

    /**
     * Generic parameter position for Data Query Object
     */
    public static int PARAMETER_QUERY_POSITION = 0;

    /**
     * Generic parameter position for Data Transfer Object
     */
    public static int PARAMETER_RESULT_POSITION = 1;

    protected QueryHandler() { }

    // ------------------------------------------------------------------------

    public QueryResponse<RESULT> retrieve(final QueryMessage<Q> message) throws Exception {
        return retrieve(message.getQuery());
    }

    public QueryResponse<RESULT> retrieve(final Q query) throws Exception {
        throw new UnsupportedOperationException();
    }

}

