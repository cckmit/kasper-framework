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
package com.viadeo.kasper.core.interceptor.tags;

import com.viadeo.kasper.core.TestDomain;
import com.viadeo.kasper.core.component.annotation.XKasperCommandHandler;
import com.viadeo.kasper.core.component.annotation.XKasperEventListener;
import com.viadeo.kasper.core.component.annotation.XKasperUnregistered;
import com.viadeo.kasper.core.component.command.AutowiredCommandHandler;
import com.viadeo.kasper.core.component.event.listener.AutowiredEventListener;
import com.viadeo.kasper.core.component.query.AutowiredQueryHandler;
import com.viadeo.kasper.core.component.query.annotation.XKasperQueryHandler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.viadeo.kasper.core.TestDomain.*;
import static org.junit.Assert.*;

public class TagsHolderUTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public ResetTagsCache resetTagsCache = new ResetTagsCache();

    //// getTags

    @Test
    public void getTags_withNull_shouldThrowNPE() {
        // Expect
        thrown.expect(NullPointerException.class);

        // When
        TagsHolder.getTags(null);
    }

    // --- queries

    @Test
    public void getTags_fromQueryHandler_withoutTags_shouldReturnEmpty() {
        // Given
        @XKasperUnregistered
        @XKasperQueryHandler(domain = TestDomain.class)
        class TestQueryHandler extends AutowiredQueryHandler<TestQuery, TestQueryResult> {
        }

        final Class<?> handlerClass = TestQueryHandler.class;

        // When
        Set<String> tags = TagsHolder.getTags(handlerClass);

        // Then
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
        assertEquals(tags, TagsHolder.CACHE_TAGS.get(handlerClass));
    }

    @Test
    public void getTags_fromQueryHandler_withOneTag_shouldReturnTheSingletonSet() {
        // Given
        @XKasperUnregistered
        @XKasperQueryHandler(domain = TestDomain.class, tags = "this-is-a-tag")
        class TestQueryHandler extends AutowiredQueryHandler<TestQuery, TestQueryResult> {
        }
        final Class<?> handlerClass = TestQueryHandler.class;

        // When
        final Set<String> tags = TagsHolder.getTags(handlerClass);

        // Then
        final Set<String> expectedTags = newHashSet("this-is-a-tag");
        assertNotNull(tags);
        assertEquals(expectedTags, tags);
        assertEquals(expectedTags, TagsHolder.CACHE_TAGS.get(handlerClass));
    }

    @Test
    public void getTags_fromQueryHandler_withSeveralTags_shouldReturnTheSet() {
        // Given
        @XKasperUnregistered
        @XKasperQueryHandler(domain = TestDomain.class, tags = {"this-is-a-tag", "this-is-another-tag"})
        class TestQueryHandler extends AutowiredQueryHandler<TestQuery, TestQueryResult> {
        }
        final Class<?> handlerClass = TestQueryHandler.class;

        // When
        final Set<String> tags = TagsHolder.getTags(handlerClass);

        // Then
        final Set<String> expectedTags = newHashSet("this-is-a-tag", "this-is-another-tag");
        assertNotNull(tags);
        assertEquals(expectedTags, tags);
        assertEquals(expectedTags, TagsHolder.CACHE_TAGS.get(handlerClass));
    }

    @Test
    public void getTags_fromQueryHandler_withoutAnnotations_shouldReturnEmpty() {
        // Given
        @XKasperUnregistered
        class TestQueryHandler extends AutowiredQueryHandler<TestQuery, TestQueryResult> {
        }
        final Class<?> handlerClass = TestQueryHandler.class;

        // When
        final Set<String> tags = TagsHolder.getTags(handlerClass);

        // Then
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
        assertEquals(tags, TagsHolder.CACHE_TAGS.get(handlerClass));
    }

    // --- commands

    @Test
    public void getTags_fromCommandHandler_withoutTags_shouldReturnEmpty() {
        // Given
        @XKasperUnregistered
        @XKasperCommandHandler(domain = TestDomain.class)
        class TestCommandHandler extends AutowiredCommandHandler<TestCommand> {
        }
        final Class<?> handlerClass = TestCommandHandler.class;

        // When
        Set<String> tags = TagsHolder.getTags(handlerClass);

        // Then
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
        assertEquals(tags, TagsHolder.CACHE_TAGS.get(handlerClass));
    }

    @Test
    public void getTags_fromCommandHandler_withOneTag_shouldReturnTheSingletonSet() {
        // Given
        @XKasperUnregistered
        @XKasperCommandHandler(domain = TestDomain.class, tags = "this-is-a-tag")
        class TestCommandHandler extends AutowiredCommandHandler<TestCommand> {
        }
        final Class<?> handlerClass = TestCommandHandler.class;

        // When
        final Set<String> tags = TagsHolder.getTags(handlerClass);

        // Then
        final Set<String> expectedTags = newHashSet("this-is-a-tag");
        assertNotNull(tags);
        assertEquals(expectedTags, tags);
        assertEquals(expectedTags, TagsHolder.CACHE_TAGS.get(handlerClass));
    }

    @Test
    public void getTags_fromCommandHandler_withSeveralTags_shouldReturnTheSet() {
        // Given
        @XKasperUnregistered
        @XKasperCommandHandler(domain = TestDomain.class, tags = {"this-is-a-tag", "this-is-another-tag"})
        class TestCommandHandler extends AutowiredCommandHandler<TestCommand> {
        }
        final Class<?> handlerClass = TestCommandHandler.class;

        // When
        final Set<String> tags = TagsHolder.getTags(handlerClass);

        // Then
        final Set<String> expectedTags = newHashSet("this-is-a-tag", "this-is-another-tag");
        assertNotNull(tags);
        assertEquals(expectedTags, tags);
        assertEquals(expectedTags, TagsHolder.CACHE_TAGS.get(handlerClass));
    }

    @Test
    public void getTags_fromCommandHandler_withoutAnnotations_shouldReturnEmpty() {
        // Given
        @XKasperUnregistered
        class TestCommandHandler extends AutowiredCommandHandler<TestCommand> {
        }
        final Class<?> handlerClass = TestCommandHandler.class;

        // When
        final Set<String> tags = TagsHolder.getTags(handlerClass);

        // Then
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
        assertEquals(tags, TagsHolder.CACHE_TAGS.get(handlerClass));
    }

    // --- events

    @Test
    public void getTags_fromEventListener_withoutTags_shouldReturnEmpty() {
        // Given
        @XKasperUnregistered
        @XKasperEventListener(domain = TestDomain.class)
        class TestEventListener extends AutowiredEventListener<TestEvent> {
        }
        final Class<?> handlerClass = TestEventListener.class;

        // When
        Set<String> tags = TagsHolder.getTags(handlerClass);

        // Then
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
        assertEquals(tags, TagsHolder.CACHE_TAGS.get(handlerClass));
    }

    @Test
    public void getTags_fromEventListener_withOneTag_shouldReturnTheSingletonSet() {
        // Given
        @XKasperUnregistered
        @XKasperEventListener(domain = TestDomain.class, tags = "this-is-a-tag")
        class TestEventListener extends AutowiredEventListener<TestEvent> {
        }
        final Class<?> handlerClass = TestEventListener.class;

        // When
        final Set<String> tags = TagsHolder.getTags(handlerClass);

        // Then
        final Set<String> expectedTags = newHashSet("this-is-a-tag");
        assertNotNull(tags);
        assertEquals(expectedTags, tags);
        assertEquals(expectedTags, TagsHolder.CACHE_TAGS.get(handlerClass));
    }

    @Test
    public void getTags_fromEventListener_withSeveralTags_shouldReturnTheSet() {
        // Given
        @XKasperUnregistered
        @XKasperEventListener(domain = TestDomain.class, tags = {"this-is-a-tag", "this-is-another-tag"})
        class TestEventListener extends AutowiredEventListener<TestEvent> {
        }
        final Class<?> handlerClass = TestEventListener.class;

        // When
        final Set<String> tags = TagsHolder.getTags(handlerClass);

        // Then
        final Set<String> expectedTags = newHashSet("this-is-a-tag", "this-is-another-tag");
        assertNotNull(tags);
        assertEquals(expectedTags, tags);
        assertEquals(expectedTags, TagsHolder.CACHE_TAGS.get(handlerClass));
    }

    @Test
    public void getTags_fromEventListener_withoutAnnotations_shouldReturnEmpty() {
        // Given
        @XKasperUnregistered
        class TestEventListener extends AutowiredEventListener<TestEvent> {
        }
        final Class<?> handlerClass = TestEventListener.class;

        // When
        final Set<String> tags = TagsHolder.getTags(handlerClass);

        // Then
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
        assertEquals(tags, TagsHolder.CACHE_TAGS.get(handlerClass));
    }

}
