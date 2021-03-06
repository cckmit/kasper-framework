// ----------------------------------------------------------------------------
//  This file is part of the Kasper framework.
//
//  The Kasper framework is free software: you can redistribute it and/or 
//  modify it under the terms of the GNU Lesser General Public License as 
//  published by the Free Software Foundation, either version 3 of the 
//  License, or (at your option) any later version.
//
//  Kasper framework is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public License
//  along with the framework Kasper.  
//  If not, see <http://www.gnu.org/licenses/>.
// --
//  Ce fichier fait partie du framework logiciel Kasper
//
//  Ce programme est un logiciel libre ; vous pouvez le redistribuer ou le 
//  modifier suivant les termes de la GNU Lesser General Public License telle 
//  que publiée par la Free Software Foundation ; soit la version 3 de la 
//  licence, soit (à votre gré) toute version ultérieure.
//
//  Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS 
//  AUCUNE GARANTIE ; sans même la garantie tacite de QUALITÉ MARCHANDE ou 
//  d'ADÉQUATION à UN BUT PARTICULIER. Consultez la GNU Lesser General Public 
//  License pour plus de détails.
//
//  Vous devez avoir reçu une copie de la GNU Lesser General Public License en 
//  même temps que ce programme ; si ce n'est pas le cas, consultez 
//  <http://www.gnu.org/licenses>
// ----------------------------------------------------------------------------
// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.core.interceptor;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.viadeo.kasper.api.component.query.Query;
import com.viadeo.kasper.api.component.query.QueryResult;
import com.viadeo.kasper.api.context.Context;
import com.viadeo.kasper.api.context.Contexts;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class InterceptorChainTest {

    static class DummyInterceptor implements Interceptor<Query, QueryResult> {
        @Override
        public QueryResult process(Query query, Context context, InterceptorChain chain) {
            return null;
        }
    }

    // ------------------------------------------------------------------------


    @Before
    public void setUp() throws Exception {
        MDC.clear();
    }

    @Test
    public void testIteratorToChain() {
        // Given
        final DummyInterceptor d1 = new DummyInterceptor();
        final DummyInterceptor d2 = new DummyInterceptor();
        final Iterable<DummyInterceptor> chain = Lists.newArrayList(null, d1, null, d2, null);

        // When
        final InterceptorChain<Query, QueryResult> actualChain = InterceptorChain.makeChain(chain);

        // Then
        InterceptorChain<Query, QueryResult> elt = actualChain;
        for (final DummyInterceptor e : Lists.newArrayList(d1, d2)) {
            assertEquals(e, elt.actor.get());
            elt = actualChain.next.get();
        }
    }

    @Test
    public void last_fromEmptyChain_shouldReturnAbsent(){
        // Given
        final InterceptorChain<Query, QueryResult> chain = new InterceptorChain<>();

        // When
        final Optional<Interceptor<Query,QueryResult>> optionalLastInterceptor = chain.last();

        // Then
        assertFalse(optionalLastInterceptor.isPresent());
    }

    @Test
    public void last_shouldReturnLastInterceptor(){
        // Given
        final DummyInterceptor d1 = new DummyInterceptor();
        final DummyInterceptor d2 = new DummyInterceptor();
        final InterceptorChain<Query, QueryResult> chain = InterceptorChain.makeChain(d1, d2);

        // When
        final Optional<Interceptor<Query,QueryResult>> optionalLastInterceptor = chain.last();

        // Then
        assertTrue(optionalLastInterceptor.isPresent());
        assertEquals(optionalLastInterceptor.get(), d2);
    }

    @Test
    public void next_withoutCurrentContext_shouldMDC() throws Exception {
        // Given
        final Context context = Contexts.builder(UUID.randomUUID())
                .withUserCountry("FR")
                .withUserLang("fr")
                .addTags(Sets.newHashSet("a", "b"))
                .build();
        final InterceptorChain<Query, QueryResult> chain = InterceptorChain.makeChain(new DummyInterceptor());

        // When
        chain.next(mock(Query.class), context);

        // Then
        assertEquals(context.asMap(), MDC.getCopyOfContextMap());
    }

    @Test
    public void next_withUpdatedContext_shouldSetMDC() throws Exception {
        // Given
        final Context context = Contexts.builder(UUID.randomUUID())
                .withUserCountry("FR")
                .withUserLang("fr")
                .addTags(Sets.newHashSet("a", "b"))
                .build();

        final Context newContext = Contexts.newFrom(context)
                .withFunnelName("MyFunnelRocks")
                .addTags(Sets.newHashSet("c"))
                .with("myPropertyKey", "myPropertyValue")
                .build();

        final InterceptorChain<Query, QueryResult> chain = InterceptorChain.makeChain(new DummyInterceptor());

        // When
        chain.next(mock(Query.class), newContext);

        // Then
        assertEquals(newContext.asMap(), MDC.getCopyOfContextMap());
    }

}
