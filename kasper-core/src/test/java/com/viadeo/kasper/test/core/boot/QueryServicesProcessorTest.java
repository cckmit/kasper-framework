// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.test.core.boot;

import com.viadeo.kasper.core.boot.QueryServicesProcessor;
import com.viadeo.kasper.core.locators.QueryServicesLocator;
import com.viadeo.kasper.core.locators.impl.DefaultQueryServicesLocator;
import com.viadeo.kasper.cqrs.query.*;
import com.viadeo.kasper.cqrs.query.annotation.XKasperQueryService;
import com.viadeo.kasper.ddd.Domain;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class QueryServicesProcessorTest {

    private static final String SERVICE_NAME = "TestService";

    // ------------------------------------------------------------------------

    final QueryServicesLocator locator = spy(new DefaultQueryServicesLocator());
    final QueryServicesProcessor processor = new QueryServicesProcessor();
    { processor.setQueryServicesLocator(locator); }

    // ------------------------------------------------------------------------

    private class TestDomain implements Domain { }
    private class TestQuery implements Query { }
    private class TestDTO implements QueryDTO { }
    private class TestFilter implements ServiceFilter { }
    private class TestFilter2 implements ServiceFilter { }

    // ------------------------------------------------------------------------

    @XKasperQueryService( name = SERVICE_NAME, domain = TestDomain.class )
    private class TestService implements QueryService<TestQuery, TestDTO> {
        @Override
        public TestDTO retrieve(final QueryMessage message) throws Exception { return null; }
    }

    @XKasperQueryService( domain = TestDomain.class )
    private class TestServiceNoName implements QueryService<TestQuery, TestDTO> {
        @Override
        public TestDTO retrieve(final QueryMessage message) throws Exception { return null; }
    }

    @XKasperQueryService( domain = TestDomain.class, filters = TestFilter.class )
    private class TestServiceOneFilter implements QueryService<TestQuery, TestDTO> {
        @Override
        public TestDTO retrieve(final QueryMessage message) throws Exception { return null; }
    }

    @XKasperQueryService( domain = TestDomain.class, filters = { TestFilter.class, TestFilter2.class } )
    private class TestServiceMultipleFilters implements QueryService<TestQuery, TestDTO> {
        @Override
        public TestDTO retrieve(final QueryMessage message) throws Exception { return null; }
    }

    // ------------------------------------------------------------------------

    @Test
    public void processorShouldRegisterServiceWithName() {

        // Given
        final TestService service = new TestService();

        // When
        processor.process(service.getClass(), service);

        // Then
        verify(locator).registerService(eq(SERVICE_NAME), eq(service));

    }

    @Test
    public void processorShouldRegisterServiceWithoutName() {

        // Given
        final TestServiceNoName service = new TestServiceNoName();

        // When
        processor.process(service.getClass(), service);

        // Then
        verify(locator).registerService(eq(service.getClass().getSimpleName()), eq(service));

    }

    @Test
    public void processorShouldRegisterServiceWithOneFilter() {

         // Given
        final TestServiceOneFilter service = new TestServiceOneFilter();

        // When
        processor.process(service.getClass(), service);

        // Then
        verify(locator).registerService(any(String.class), any(QueryService.class));
        verify(locator).registerFilteredService(eq(service.getClass()), eq(TestFilter.class));

    }

    @Test
    public void processorShouldRegisterServiceWithMultipleFilters() {

         // Given
        final TestServiceMultipleFilters service = new TestServiceMultipleFilters();

        // When
        processor.process(service.getClass(), service);

        // Then
        verify(locator).registerService(any(String.class), any(QueryService.class));
        verify(locator).registerFilteredService(eq(service.getClass()), eq(TestFilter.class));
        verify(locator).registerFilteredService(eq(service.getClass()), eq(TestFilter2.class));
    }

}