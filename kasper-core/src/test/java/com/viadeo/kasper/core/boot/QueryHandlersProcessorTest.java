// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.core.boot;

import com.viadeo.kasper.core.annotation.XKasperUnregistered;
import com.viadeo.kasper.core.locators.impl.DefaultQueryHandlersLocator;
import com.viadeo.kasper.cqrs.query.Query;
import com.viadeo.kasper.cqrs.query.QueryHandler;
import com.viadeo.kasper.cqrs.query.QueryHandlerAdapter;
import com.viadeo.kasper.cqrs.query.QueryResult;
import com.viadeo.kasper.cqrs.query.annotation.XKasperQueryHandler;
import com.viadeo.kasper.ddd.Domain;
import org.junit.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class QueryHandlersProcessorTest {

    private static final String HANDLER_NAME = "TestHandler";

    // ------------------------------------------------------------------------

    final DefaultQueryHandlersLocator locator = spy(new DefaultQueryHandlersLocator());
    final QueryHandlersProcessor processor = new QueryHandlersProcessor();

    /* init */ {
        processor.setQueryHandlersLocator(locator);
    }

    // ------------------------------------------------------------------------

    @XKasperUnregistered
    public class TestDomain implements Domain { }

    @XKasperUnregistered
    public class TestQuery implements Query { }

    @XKasperUnregistered
    public class TestAdapter implements QueryHandlerAdapter { }

    @XKasperUnregistered
    public class TestAdapter2 implements QueryHandlerAdapter { }

    public class TestResult implements QueryResult { }

    // ------------------------------------------------------------------------

    @XKasperUnregistered
    @XKasperQueryHandler( name = HANDLER_NAME, domain = TestDomain.class )
    public static class TestHandler extends QueryHandler<TestQuery, TestResult> { }

    @XKasperUnregistered
    @XKasperQueryHandler( domain = TestDomain.class )
    public static class TestHandlerNoName extends QueryHandler<TestQuery, TestResult> { }

    @XKasperUnregistered
    @XKasperQueryHandler( domain = TestDomain.class )
    public static class TestHandlerOneFilter extends QueryHandler<TestQuery, TestResult> { }

    @XKasperUnregistered
    @XKasperQueryHandler( domain = TestDomain.class )
    public static class TestHandlerMultipleFilters extends QueryHandler<TestQuery, TestResult> { }

    // ------------------------------------------------------------------------

    @Test
    public void processorShouldRegisterHandlerWithName() {

        // Given
        final TestHandler handler = new TestHandler();

        // When
        processor.process(handler.getClass(), handler);

        // Then
        verify(locator).registerHandler(eq(HANDLER_NAME), eq(handler), eq(TestDomain.class));

    }

    @Test
    public void processorShouldRegisterHandlerWithoutName() {

        // Given
        final TestHandlerNoName handler = new TestHandlerNoName();

        // When
        processor.process(handler.getClass(), handler);

        // Then
        verify(locator).registerHandler(eq(handler.getClass().getSimpleName()), eq(handler), eq(TestDomain.class));

    }

}
