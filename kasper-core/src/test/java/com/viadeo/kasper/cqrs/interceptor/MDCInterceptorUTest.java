package com.viadeo.kasper.cqrs.interceptor;

import com.viadeo.kasper.context.Context;
import com.viadeo.kasper.context.impl.DefaultContext;
import com.viadeo.kasper.core.interceptor.InterceptorChain;
import org.apache.log4j.MDC;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MDCInterceptorUTest {

    private MDCInterceptor<Object,Object> interceptor;
    private InterceptorChain<Object,Object> chain;

    @Before
    public void setUp() throws Exception {
        interceptor = new MDCInterceptor<>();
        chain = mock(InterceptorChain.class);
        when(chain.next(any(Object.class), any(Context.class))).thenReturn(42);
    }

    @Test(expected = NullPointerException.class)
    public void process_withNullAsContext_throwException() throws Exception {
        // Given
        Context context = null;

        // When
        interceptor.process(mock(Object.class), context, chain);

        // Then throw an exception
    }

    @Test
    public void process_withContext_setContextMapInMDC() throws Exception {
        // Given
        Context context = new DefaultContext();
        context.setApplicationId("myApplicationId");
        context.setProperty("myPropertyKey", "MyPropertyValue");

        // When
        interceptor.process(mock(Object.class), context, chain);

        // Then
        Assert.assertEquals("myApplicationId", MDC.get("appId"));
        Assert.assertEquals("MyPropertyValue", MDC.get("myPropertyKey"));
    }
}