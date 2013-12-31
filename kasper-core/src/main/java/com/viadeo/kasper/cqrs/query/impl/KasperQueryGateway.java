// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.cqrs.query.impl;

import com.codahale.metrics.Timer;
import com.google.common.base.Optional;
import com.viadeo.kasper.context.Context;
import com.viadeo.kasper.core.context.CurrentContext;
import com.viadeo.kasper.core.interceptor.InterceptorChain;
import com.viadeo.kasper.core.interceptor.InterceptorChainRepository;
import com.viadeo.kasper.core.interceptor.QueryInterceptorFactory;
import com.viadeo.kasper.core.locators.QueryHandlersLocator;
import com.viadeo.kasper.core.locators.impl.DefaultQueryHandlersLocator;
import com.viadeo.kasper.cqrs.query.*;
import com.viadeo.kasper.cqrs.query.annotation.XKasperQueryHandler;
import com.viadeo.kasper.cqrs.query.interceptor.QueryHandlerInterceptor;
import com.viadeo.kasper.exception.KasperException;
import com.viadeo.kasper.security.SecurityConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.viadeo.kasper.core.metrics.KasperMetrics.getMetricRegistry;
import static com.viadeo.kasper.core.metrics.KasperMetrics.name;

/**
 * The Kasper gateway base implementation
 */
public class KasperQueryGateway implements QueryGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(KasperQueryGateway.class);

    private static final String GLOBAL_TIMER_REQUESTS_TIME_NAME = name(QueryGateway.class, "requests-time");
    private static final String GLOBAL_HISTO_REQUESTS_TIMES_NAME = name(QueryGateway.class, "requests-times");
    private static final String GLOBAL_METER_REQUESTS_NAME = name(QueryGateway.class, "requests");
    private static final String GLOBAL_METER_ERRORS_NAME = name(QueryGateway.class, "errors");

    private final QueryHandlersLocator queryHandlersLocator;
    private final InterceptorChainRepository<Query, QueryResponse<QueryResult>> interceptorChainRepository;

    // -----------------------------------------------------------------------

    public KasperQueryGateway() {
        this(new DefaultQueryHandlersLocator());
    }

    public KasperQueryGateway(final QueryHandlersLocator queryHandlersLocator) {
        this(queryHandlersLocator, new InterceptorChainRepository<Query, QueryResponse<QueryResult>>());
    }

    public KasperQueryGateway(final QueryHandlersLocator queryHandlersLocator,
                              final InterceptorChainRepository<Query, QueryResponse<QueryResult>> interceptorChainRepository) {
        this.queryHandlersLocator = checkNotNull(queryHandlersLocator);
        this.interceptorChainRepository = interceptorChainRepository;
    }

    // ------------------------------------------------------------------------

    @Override
    @SuppressWarnings("unchecked")
    public <RESULT extends QueryResult> QueryResponse<RESULT> retrieve(final Query query, final Context context)
            throws Exception {

        checkNotNull(context);
        checkNotNull(query);

        final Class<? extends Query> queryClass = query.getClass();

        /* Start request timer */
        final Timer.Context classTimer = getMetricRegistry().timer(GLOBAL_TIMER_REQUESTS_TIME_NAME).time();
        final Timer.Context timer = getMetricRegistry().timer(name(queryClass, "requests-time")).time();

        /* Sets current thread context */
        CurrentContext.set(context);

        // Search for associated handler --------------------------------------
        LOGGER.debug("Retrieve request processor chain for query " + queryClass.getSimpleName());
        Optional<InterceptorChain<Query, QueryResponse<QueryResult>>> optionalRequestChain =
                getInterceptorChain(queryClass);

        if (!optionalRequestChain.isPresent()) {
            timer.close();
            classTimer.close();
            throw new KasperException("Unable to find the handler implementing query class " + queryClass);
        }

        Exception exception = null;
        QueryResponse<RESULT> ret = null;

        try {
            LOGGER.info("Call actor chain for query " + queryClass.getSimpleName());
            ret = (QueryResponse<RESULT>) optionalRequestChain.get().next(query, context);
        } catch (final RuntimeException e) {
            exception = e;
        } catch (final Exception e) {
            exception = e;
        }

        /* Monitor the request calls */
        timer.stop();
        final long time = classTimer.stop();
        getMetricRegistry().histogram(GLOBAL_HISTO_REQUESTS_TIMES_NAME).update(time);
        getMetricRegistry().meter(GLOBAL_METER_REQUESTS_NAME).mark();

        getMetricRegistry().histogram(name(queryClass, "requests-times")).update(time);
        getMetricRegistry().meter(name(queryClass, "requests")).mark();

        if ((null != exception) || ! ret.isOK()) {
            getMetricRegistry().meter(GLOBAL_METER_ERRORS_NAME).mark();
            getMetricRegistry().meter(name(queryClass, "errors")).mark();
        }

        if (null != exception) {
            throw exception;
        }

        return ret;
    }

    // ------------------------------------------------------------------------

    /**
     * Register a query handler adapter to the gateway
     *
     * @param name the name of the adapter
     * @param adapter the query handler adapter to register
     * @param global the kind of the adapter. If true then the adapter will be applied to every query handler component.
     *               Otherwise the  adapter will be applied only on the component whose reference it
     */
    @Deprecated
    public void register(final String name, final  QueryHandlerAdapter adapter, final boolean global) {
        queryHandlersLocator.registerAdapter(name, adapter, global);
    }

    public void configureSecurity(SecurityConfiguration securityConfiguration) {
        queryHandlersLocator.configureSecurity(securityConfiguration);
    }

    /**
     * Register an interceptor factory to the gateway
     *
     * @param interceptorFactory the query interceptor factory to register
     */
    public void register(final QueryInterceptorFactory interceptorFactory) {
        checkNotNull(interceptorFactory);

        LOGGER.info("Registering the query interceptor factory : " + interceptorFactory.getClass().getSimpleName());

        interceptorChainRepository.register(interceptorFactory);
    }

    /**
     * Register a query handler to the gateway
     *
     * @param queryHandler the query handler to register
     */
    @SuppressWarnings("unchecked")
    public void register(final QueryHandler queryHandler) {
        checkNotNull(queryHandler);

        final Class<? extends QueryHandler> queryHandlerClass = queryHandler.getClass();
        LOGGER.info("Registering the query handler : " + queryHandlerClass.getName());

        final XKasperQueryHandler annotation = queryHandlerClass.getAnnotation(XKasperQueryHandler.class);

        final String handlerName;
        if (annotation.name().isEmpty()) {
            handlerName = queryHandlerClass.getSimpleName();
        } else {
            handlerName = annotation.name();
        }

        queryHandlersLocator.registerHandler(handlerName, queryHandler, annotation.domain());

        queryHandler.setQueryGateway(this);

        // create immediately the chain instead of lazy mode
        interceptorChainRepository.create(queryHandlerClass, new QueryHandlerInterceptor(queryHandler));
    }

    protected Optional<InterceptorChain<Query, QueryResponse<QueryResult>>> getInterceptorChain(final Class<? extends Query> queryClass) {
        final Optional<QueryHandler<Query, QueryResult>> queryHandlerOptional = queryHandlersLocator.getHandlerFromQueryClass(queryClass);

        if (!queryHandlerOptional.isPresent()) {
            return Optional.absent();
        }

        Class<? extends QueryHandler> queryHandlerClass = queryHandlerOptional.get().getClass();

        final Optional<InterceptorChain<Query, QueryResponse<QueryResult>>> chainOptional = interceptorChainRepository.get(queryHandlerClass);

        if (chainOptional.isPresent()) {
            return chainOptional;
        }

        return interceptorChainRepository.create(queryHandlerClass, new QueryHandlerInterceptor<>(queryHandlerOptional.get()));
    }
}
