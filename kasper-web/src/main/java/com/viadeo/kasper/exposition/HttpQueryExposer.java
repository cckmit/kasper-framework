// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.exposition;

import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.reflect.TypeToken;
import com.viadeo.kasper.CoreReasonCode;
import com.viadeo.kasper.KasperReason;
import com.viadeo.kasper.context.Context;
import com.viadeo.kasper.context.HttpContextHeaders;
import com.viadeo.kasper.cqrs.query.Query;
import com.viadeo.kasper.cqrs.query.QueryGateway;
import com.viadeo.kasper.cqrs.query.QueryHandler;
import com.viadeo.kasper.cqrs.query.QueryResponse;
import com.viadeo.kasper.exposition.alias.AliasRegistry;
import com.viadeo.kasper.query.exposition.TypeAdapter;
import com.viadeo.kasper.query.exposition.query.QueryFactory;
import com.viadeo.kasper.query.exposition.query.QueryFactoryBuilder;
import com.viadeo.kasper.query.exposition.query.QueryParser;
import com.viadeo.kasper.tools.ObjectMapperProvider;
import org.axonframework.commandhandling.interceptors.JSR303ViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.beans.Introspector;
import java.io.IOException;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.viadeo.kasper.core.metrics.KasperMetrics.getMetricRegistry;
import static com.viadeo.kasper.core.metrics.KasperMetrics.name;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

public class HttpQueryExposer extends HttpExposer {
    private static final long serialVersionUID = 8448984922303895624L;

    protected static final transient Logger QUERY_LOGGER = LoggerFactory.getLogger(HttpQueryExposer.class);

    private static final String GLOBAL_TIMER_REQUESTS_TIME_NAME = name(HttpQueryExposer.class, "requests-time");
    private static final String GLOBAL_TIMER_REQUESTS_HANDLE_TIME_NAME = name(HttpQueryExposer.class, "requests-handle-time");
    private static final String GLOBAL_METER_REQUESTS_NAME = name(HttpQueryExposer.class, "requests");
    private static final String GLOBAL_METER_ERRORS_NAME = name(HttpQueryExposer.class, "errors");

    // ------------------------------------------------------------------------

    private static final TypeReference<ImmutableSetMultimap<String, String>> STRINGS_TYPE = new TypeReference<ImmutableSetMultimap<String, String>>() {};

    interface QueryToQueryMap {
        SetMultimap<String, String> toQueryMap(final HttpServletRequest req, final HttpServletResponse resp) throws IOException;
    }

    private static final QueryToQueryMap JSON_TO_QUERY_MAP = new QueryToQueryMap() {
        @Override
        public SetMultimap<String, String> toQueryMap(
                final HttpServletRequest req,
                final HttpServletResponse resp
        ) throws IOException {

            final ObjectMapper tmpMapper = ObjectMapperProvider.INSTANCE.mapper();
            final JsonParser parser = tmpMapper.reader().getFactory().createParser(req.getInputStream());

            return tmpMapper.reader().readValue(parser, STRINGS_TYPE);
        }
    };

    private static final QueryToQueryMap STRING_REQ_TO_RESP_MAP = new QueryToQueryMap() {
        @Override
        public SetMultimap<String, String> toQueryMap(
                final HttpServletRequest req,
                final HttpServletResponse resp
        ) throws IOException {

            final ImmutableSetMultimap.Builder<String, String> queryParams = new ImmutableSetMultimap.Builder<>();
            final Enumeration<String> keys = req.getParameterNames();

            while (keys.hasMoreElements()) {
                final String key = keys.nextElement();
                queryParams.putAll(key, Arrays.asList(req.getParameterValues(key)));
            }

            return queryParams.build();
        }
    };

    // ------------------------------------------------------------------------

    private final Map<String, Class<? extends Query>> exposedQueries = Maps.newHashMap();
    private final transient List<ExposureDescriptor<Query,QueryHandler>> descriptors;
    private final transient QueryFactory queryAdapterFactory;
    private final ObjectMapper mapper;
    private final transient QueryGateway queryGateway;
    private final transient HttpContextDeserializer contextDeserializer;
    private final AliasRegistry aliasRegistry;

    // ------------------------------------------------------------------------

    public HttpQueryExposer(final QueryGateway queryGateway, final List<ExposureDescriptor<Query,QueryHandler>> descriptors) {
        this(
                queryGateway,
                descriptors,
                new QueryFactoryBuilder().create(),
                new HttpContextDeserializer(),
                ObjectMapperProvider.INSTANCE.mapper()
        );
    }

    public HttpQueryExposer(final QueryGateway queryGateway,
                            final List<ExposureDescriptor<Query,QueryHandler>> descriptors,
                            final QueryFactory queryAdapterFactory,
                            final HttpContextDeserializer contextDeserializer,
                            final ObjectMapper mapper) {

        this.queryGateway = checkNotNull(queryGateway);
        this.descriptors = checkNotNull(descriptors);
        this.queryAdapterFactory = checkNotNull(queryAdapterFactory);
        this.contextDeserializer = checkNotNull(contextDeserializer);
        this.mapper = checkNotNull(mapper);
        this.aliasRegistry = new AliasRegistry();
    }

    // ------------------------------------------------------------------------

    @Override
    public void init() throws ServletException {
        LOGGER.info("=============== Exposing queries ===============");

        /* expose all registered queries and commands */
        for (final ExposureDescriptor<Query,QueryHandler> descriptor : descriptors) {
            expose(descriptor);
        }

        if (exposedQueries.isEmpty()) {
            LOGGER.warn("No Query has been exposed.");
        } else {
            LOGGER.info("Total exposed " + exposedQueries.size() + " queries.");
        }

        LOGGER.info("=================================================\n");
    }

    // ------------------------------------------------------------------------

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        if ( (null == req.getContentType()) || ( ! req.getContentType().startsWith("application/json"))) {
            sendError(Response.Status.NOT_ACCEPTABLE.getStatusCode(), "Accepting only application/json; charset=utf-8", req, resp, null);
        } else {
            handleQuery(JSON_TO_QUERY_MAP, req, resp);
        }
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {

        handleQuery(STRING_REQ_TO_RESP_MAP, req, resp);
    }

    // ------------------------------------------------------------------------

    protected void handleQuery(final QueryToQueryMap queryMapper, final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
         /* Start request timer */
        final Timer.Context classTimer = getMetricRegistry().timer(GLOBAL_TIMER_REQUESTS_TIME_NAME).time();

        /* Create a kasper correlation id */
        final UUID kasperCorrelationUUID = UUID.randomUUID();
        resp.addHeader("kasperCorrelationId", kasperCorrelationUUID.toString());

        resp.addHeader(HttpContextHeaders.HEADER_SERVER_NAME, serverName());

        /* extract context from request */
        final Context context = contextDeserializer.deserialize(req, kasperCorrelationUUID);
        MDC.setContextMap(context.asMap());

        /* Log starting request */
        QUERY_LOGGER.info("Processing HTTP Query '{}' '{}'", req.getMethod(), getFullRequestURI(req));

        /* always respond with a json stream (even if empty) */
        resp.setContentType(MediaType.APPLICATION_JSON + "; charset=utf-8");

        /*
         * lets be very defensive and catch every thing in order to not break
         * the contract with clients = JSON only
         */
        final String queryName = aliasRegistry.resolve(resourceName(req.getRequestURI()));
        try {

            final Query query = parseQuery(queryMapper.toQueryMap(req, resp), queryName, req, resp);

            QueryResponse response = null;
            if (!resp.isCommitted()) {
                final Timer.Context queryHandleTimer = getMetricRegistry().timer(name(query.getClass(), "requests-handle-time")).time();
                final Timer.Context classHandleTimer = getMetricRegistry().timer(GLOBAL_TIMER_REQUESTS_HANDLE_TIME_NAME).time();

                response = handleQuery(queryName, query, req, resp, context);

                queryHandleTimer.stop();
                classHandleTimer.stop();
            }

            /* need to check again as something might go wrong in handleQuery */
            if (!resp.isCommitted()) {
                sendResponse(queryName, response, req, resp);
            }

        } catch (final JSR303ViolationException validationException) {

            final List<String> errorMessages = new ArrayList<>();
            for (final ConstraintViolation<Object> violation : validationException.getViolations()) {
                errorMessages.add(violation.getPropertyPath() + " : " + violation.getMessage());
            }

            sendResponse(
                    queryName,
                    QueryResponse.error(
                            new KasperReason(
                                    CoreReasonCode.INVALID_INPUT.name(),
                                    errorMessages
                            )
                    ),
                    req, resp);

        } catch (final Throwable t) {
            sendError(
                    INTERNAL_SERVER_ERROR.getStatusCode(),
                    String.format("Could not handle query [%s] with parameters [%s]", req.getRequestURI(), req.getQueryString()),
                    req, resp, t);

        } finally {
            /* Log & metrics */
            final long time = classTimer.stop();
            QUERY_LOGGER.info("Execution Time '{}' ns",time);
            getMetricRegistry().meter(GLOBAL_METER_REQUESTS_NAME).mark();
        }

        if (!resp.isCommitted()) {
            try {
                resp.flushBuffer();
            } catch (final IOException e) {
                LOGGER.warn("Error when trying to flush output buffer", e);
            }
        }
    }

    // we can not use send error as it will send text/html response.
    protected Query parseQuery(final SetMultimap<String, String> queryMap, final String queryName, final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException {

        Query query = null;
        final Class<? extends Query> queryClass = exposedQueries.get(queryName);

        if (null == queryClass) {

            sendError(Response.Status.NOT_FOUND.getStatusCode(),
                      "No such query[" + queryName + "].",
                      req, resp, null);

        } else {

            final TypeAdapter<? extends Query> adapter = queryAdapterFactory.create(TypeToken.of(queryClass));

            try {

                query = adapter.adapt(new QueryParser(queryMap));

            } catch (final Throwable t) {
                sendError(Response.Status.BAD_REQUEST.getStatusCode(), String.format(
                        "Unable to parse Query [%s] with parameters [%s]", queryName,
                        req.getQueryString()), req, resp, t);
            }
        }

        return query;
    }

    // ------------------------------------------------------------------------

    // can not use sendError it is forcing response to text/html
    protected QueryResponse handleQuery(final String queryName, final Query query, final HttpServletRequest req,
                                         final HttpServletResponse resp, final Context context)
            throws IOException {

        QueryResponse response = null;

        /* send the query to the platform */
        try {

            response = queryGateway.retrieve(query, context);
            checkNotNull(response);

        } catch (final Throwable e) {
            /*
             * it is ok to eat all kind of exceptions as they occur at parsing
             * level so we know what approximately failed.
             */
            sendError(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                      String.format("ERROR Submiting query[%s] to Kasper platform.", queryName),
                      req, resp, e);
        }

        return response;
    }

    // ------------------------------------------------------------------------

    // can not use sendError it is forcing response to text/html
    protected void sendResponse(final String queryName, final QueryResponse response, final HttpServletRequest req,
                              final HttpServletResponse resp)
            throws IOException {

        final ObjectWriter writer = mapper.writer();

        final int status;
        if ( ! response.isOK()) {
            if (null == response.getReason()) {
                status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
            } else {
                status = CoreReasonHttpCodes.toStatus(response.getReason().getCode());
            }
        } else {
            status = Response.Status.OK.getStatusCode();
        }

        try {

            resp.setStatus(status);
            writer.writeValue(resp.getOutputStream(), response);

            /* Log the request */
            QUERY_LOGGER.info("HTTP Response {} '{}' : {}", req.getMethod(), req.getRequestURI(), status);

        } catch (final Throwable t) {
            sendError(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                      String.format("ERROR sending Response [%s] for query [%s]", response.getClass().getSimpleName(),queryName),
                      req, resp, t);
        } finally {
            try {
                resp.flushBuffer();
            } catch (final IOException e) {
                LOGGER.warn("Error when trying to flush output buffer", e);
            }
        }

    }

    // ------------------------------------------------------------------------

    @SuppressWarnings("deprecation")
    protected void sendError(final int status, final String message, final HttpServletRequest req,
                             final HttpServletResponse resp, final Throwable exception)
            throws IOException {

        if (null != exception) {
            LOGGER.error(message, exception);
        } else {
            LOGGER.error(message);
        }

        resp.setStatus(status, message);

        final ObjectWriter writer = mapper.writer();

        final KasperReason error;
        if ((null != exception) && (null != exception.getMessage())) {
            error = new KasperReason(CoreReasonCode.UNKNOWN_REASON, message, exception.getMessage());
        } else {
            error = new KasperReason(CoreReasonCode.UNKNOWN_REASON, message);
        }

        writer.writeValue(resp.getOutputStream(), new QueryResponse<>(error));

        try {
            resp.flushBuffer();
        } catch (final IOException e) {
            LOGGER.warn("Error when trying to flush output buffer", e);
        }

        /* Log the request */
        QUERY_LOGGER.info("HTTP Response {} '{}' : {} {}", req.getMethod(), req.getRequestURI(), status, message, exception);

        /* Log error metric */
        getMetricRegistry().meter(GLOBAL_METER_ERRORS_NAME).mark();
    }

    // ------------------------------------------------------------------------

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected HttpQueryExposer expose(final ExposureDescriptor<Query,QueryHandler> descriptor) {
        checkNotNull(descriptor);

        final TypeToken<? extends QueryHandler> typeToken = TypeToken.of(descriptor.getHandler());
        final Class<? super Query> queryClass = (Class<? super Query>) typeToken
                .getSupertype(QueryHandler.class)
                .resolveType(QueryHandler.class.getTypeParameters()[0])
                .getRawType();

        final String queryPath = queryToPath(queryClass);
        final List<String> aliases = AliasRegistry.aliasesFrom(descriptor.getHandler());
        final String queryName = queryClass.getSimpleName();

        LOGGER.info("-> Exposing query[{}] at path[/{}]", queryName,
                    getServletContext().getContextPath() + queryPath);

        for (final String alias : aliases) {
            LOGGER.info("-> Exposing query[{}] at path[/{}]",
                    queryName,
                    getServletContext().getContextPath() + alias);
        }

        putKey(queryPath, queryClass, exposedQueries);

        aliasRegistry.register(queryPath, aliases);

        return this;
    }

    // ------------------------------------------------------------------------

    private String queryToPath(final Class<? super Query> exposedQuery) {
        return Introspector.decapitalize(exposedQuery.getSimpleName().replaceAll("Query", ""));
    }

}
