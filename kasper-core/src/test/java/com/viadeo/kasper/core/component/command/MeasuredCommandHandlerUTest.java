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
package com.viadeo.kasper.core.component.command;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.viadeo.kasper.api.annotation.XKasperCommand;
import com.viadeo.kasper.api.annotation.XKasperDomain;
import com.viadeo.kasper.api.component.Domain;
import com.viadeo.kasper.api.component.command.Command;
import com.viadeo.kasper.api.component.command.CommandResponse;
import com.viadeo.kasper.api.context.Context;
import com.viadeo.kasper.api.context.Contexts;
import com.viadeo.kasper.api.response.CoreReasonCode;
import com.viadeo.kasper.api.response.KasperResponse;
import com.viadeo.kasper.core.component.annotation.XKasperCommandHandler;
import com.viadeo.kasper.core.component.annotation.XKasperUnregistered;
import com.viadeo.kasper.core.metrics.KasperMetrics;
import com.viadeo.kasper.core.resolvers.CommandHandlerResolver;
import com.viadeo.kasper.core.resolvers.CommandResolver;
import com.viadeo.kasper.core.resolvers.DomainResolver;
import com.viadeo.kasper.core.resolvers.ResolverFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MeasuredCommandHandlerUTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private MetricRegistry metricRegistry;

    @Before
    public void setUp() throws Exception {
        when(metricRegistry.meter(anyString())).thenReturn(mock(Meter.class));
        Timer timer = mock(Timer.class);
        when(timer.time()).thenReturn(mock(Timer.Context.class));
        when(metricRegistry.timer(anyString())).thenReturn(timer);

        DomainResolver domainResolver = new DomainResolver();

        CommandResolver commandResolver = new CommandResolver();
        commandResolver.setDomainResolver(domainResolver);

        CommandHandlerResolver commandHandlerResolver = new CommandHandlerResolver();
        commandHandlerResolver.setDomainResolver(domainResolver);

        ResolverFactory resolverFactory = new ResolverFactory();
        resolverFactory.setCommandResolver(commandResolver);
        resolverFactory.setCommandHandlerResolver(commandHandlerResolver);

        KasperMetrics.setResolverFactory(resolverFactory);
    }

    @Test
    public void measure_an_accepted_command() throws Exception {
        // Given
        MeasuredCommandHandler handler = new MeasuredCommandHandler(
                metricRegistry,
                new TestCommandHandler(KasperResponse.Status.ACCEPTED)
        );

        // When
        CommandResponse response = handler.handle(new CommandMessage<Command>(Contexts.empty(), mock(TestCommand.class)));

        // Then
        assertNotNull(response);
        verify(metricRegistry).timer("unknown.command.testcommand.requests-handle-time");
        verify(metricRegistry).timer("unknown.command.requests-handle-time");
        verify(metricRegistry).timer("com.viadeo.kasper.core.component.command.gateway.commandgateway.requests-handle-time");
        verify(metricRegistry).meter("client.unknown.command.requests");

    }

    @Test
    public void measure_an_ok_command() throws Exception {
        // Given
        MeasuredCommandHandler handler = new MeasuredCommandHandler(
                metricRegistry,
                new TestCommandHandler(KasperResponse.Status.OK)
        );

        // When
        CommandResponse response = handler.handle(new CommandMessage<Command>(Contexts.empty(), mock(TestCommand.class)));

        // Then
        assertNotNull(response);
        verify(metricRegistry).timer("unknown.command.testcommand.requests-handle-time");
        verify(metricRegistry).timer("unknown.command.requests-handle-time");
        verify(metricRegistry).timer("com.viadeo.kasper.core.component.command.gateway.commandgateway.requests-handle-time");
        verify(metricRegistry).meter("client.unknown.command.requests");
    }

    @Test
    public void measure_a_refused_command() throws Exception {
        // Given
        MeasuredCommandHandler handler = new MeasuredCommandHandler(
                metricRegistry,
                new TestCommandHandler(KasperResponse.Status.REFUSED)
        );

        // When
        CommandResponse response = handler.handle(new CommandMessage<Command>(Contexts.empty(), mock(TestCommand.class)));

        // Then
        assertNotNull(response);
        verify(metricRegistry).timer("unknown.command.testcommand.requests-handle-time");
        verify(metricRegistry).timer("unknown.command.requests-handle-time");
        verify(metricRegistry).timer("com.viadeo.kasper.core.component.command.gateway.commandgateway.requests-handle-time");
        verify(metricRegistry).meter("client.unknown.command.requests");
    }

    @Test
    public void measure_an_error_command() throws Exception {
        // Given
        MeasuredCommandHandler handler = new MeasuredCommandHandler(
                metricRegistry,
                new TestCommandHandler(KasperResponse.Status.ERROR)
        );

        // When
        CommandResponse response = handler.handle(new CommandMessage<Command>(Contexts.empty(), mock(TestCommand.class)));

        // Then
        assertNotNull(response);
        verify(metricRegistry).timer("unknown.command.testcommand.requests-handle-time");
        verify(metricRegistry).timer("unknown.command.requests-handle-time");
        verify(metricRegistry).timer("com.viadeo.kasper.core.component.command.gateway.commandgateway.requests-handle-time");
        verify(metricRegistry).meter("client.unknown.command.requests");
        verify(metricRegistry).meter("unknown.command.testcommand.errors");
        verify(metricRegistry).meter("unknown.command.errors");
        verify(metricRegistry).meter("client.unknown.command.errors");
    }

    @Test
    public void measure_a_command_throwing_an_unexpected_exception() throws Exception {
        // Given
        MeasuredCommandHandler handler = new MeasuredCommandHandler(
                metricRegistry,
                new TestCommandHandler(KasperResponse.Status.ERROR) {
                    @Override
                    public CommandResponse handle(Context context, TestCommand command) {
                        throw new RuntimeException("Fake exception");
                    }
                }
        );

        // Then
        exception.expect(RuntimeException.class);
        exception.expectMessage("Fake exception");

        // When
        try {
            handler.handle(new CommandMessage<Command>(Contexts.empty(), mock(TestCommand.class)));
        } finally {
            // Then
            verify(metricRegistry).timer("unknown.command.testcommand.requests-handle-time");
            verify(metricRegistry).timer("unknown.command.requests-handle-time");
            verify(metricRegistry).timer("com.viadeo.kasper.core.component.command.gateway.commandgateway.requests-handle-time");
            verify(metricRegistry).meter("client.unknown.command.requests");
            verify(metricRegistry).meter("unknown.command.testcommand.errors");
            verify(metricRegistry).meter("unknown.command.errors");
            verify(metricRegistry).meter("client.unknown.command.errors");
        }
    }

    @XKasperUnregistered
    @XKasperDomain(prefix = "test", label = "test")
    public static class TestDomain implements Domain { }

    @XKasperUnregistered
    @XKasperCommand()
    private static class TestCommand implements Command {}

    @XKasperUnregistered
    @XKasperCommandHandler(domain = TestDomain.class)
    private static class TestCommandHandler extends AutowiredCommandHandler<TestCommand> {

        private final KasperResponse.Status status;

        public TestCommandHandler(KasperResponse.Status status) {
            this.status = status;
        }

        @Override
        public CommandResponse handle(Context context, TestCommand command) {
            switch (status) {
                case OK:
                    return CommandResponse.ok();
                case ACCEPTED:
                    return CommandResponse.accepted();
                case REFUSED:
                    return CommandResponse.refused(CoreReasonCode.UNKNOWN_REASON);
                case ERROR:
                    return CommandResponse.error(CoreReasonCode.UNKNOWN_REASON);
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }
}
