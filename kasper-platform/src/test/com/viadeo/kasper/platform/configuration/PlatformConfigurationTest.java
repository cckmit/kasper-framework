// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.platform.configuration;

import com.viadeo.kasper.core.boot.*;
import com.viadeo.kasper.core.locators.DomainLocator;
import com.viadeo.kasper.core.locators.QueryServicesLocator;
import com.viadeo.kasper.cqrs.command.CommandGateway;
import com.viadeo.kasper.cqrs.query.QueryGateway;
import com.viadeo.kasper.exception.KasperException;
import com.viadeo.kasper.platform.Platform;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.eventhandling.EventBus;
import org.junit.Test;

import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.fail;

public class PlatformConfigurationTest {

    @Test
    public void testDefaultPlatformConfiguration() throws Exception {
        this.testPlatformConfiguration(new DefaultPlatformConfiguration());
    }

    @Test
    public void testDefaultSpringPlatformConfiguration() throws Exception {
        this.testPlatformConfiguration(new DefaultPlatformSpringConfiguration());
    }

    // ------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private void testPlatformConfiguration(final PlatformConfiguration platformConfiguration) throws Exception {

        final CommandBus commandBus =
                this.testCommandBus(platformConfiguration);

        final CommandGateway commandGateway =
                this.testCommandGateway(platformConfiguration, commandBus);

        final QueryServicesLocator queryServicesLocator=
                this.testQueryServicesLocator(platformConfiguration);

        final QueryGateway queryGateway =
                this.testQueryGateway(platformConfiguration, queryServicesLocator);

        final EventBus eventBus =
                this.testEventBus(platformConfiguration);

        final ComponentsInstanceManager componentsInstanceManager =
                this.testComponentsInstanceManager(platformConfiguration);

        final AnnotationRootProcessor annotationRootProcessor =
                this.testAnnotationRootProcessor(platformConfiguration, componentsInstanceManager);

        final DomainLocator domainLocator =
                this.testDomainLocator(platformConfiguration);

        final CommandHandlersProcessor commandHandlersProcessor =
                this.testCommandHandlersProcessor(platformConfiguration, commandBus, domainLocator);

        final DomainsProcessor domainsProcessor =
                this.testDomainsProcessor(platformConfiguration, domainLocator);

        final EventListenersProcessor eventListenersProcessor =
                this.testEventListenersProcessor(platformConfiguration, eventBus);

        final QueryServicesProcessor queryServicesProcessor =
                this.testQueryServicesProcessor(platformConfiguration, queryServicesLocator);

        final RepositoriesProcessor repositoriesProcessor=
                this.testRepositoriesProcessor(platformConfiguration, domainLocator, eventBus);

        final ServiceFiltersProcessor serviceFiltersProcessor=
                this.testServiceFiltersProcessor(platformConfiguration, queryServicesLocator);

        final Platform platform =
                this.testPlatform(platformConfiguration,
                                  commandGateway, queryGateway,
                                  eventBus, annotationRootProcessor);
    }

    // ------------------------------------------------------------------------

    private CommandBus testCommandBus(final PlatformConfiguration platformConfiguration) {
        final CommandBus commandBus = platformConfiguration.commandBus();
        assertSame(commandBus, platformConfiguration.commandBus());
        return commandBus;
    }

    // ------------------------------------------------------------------------

    private CommandGateway testCommandGateway(final PlatformConfiguration platformConfiguration,
                                              final CommandBus commandBus) {

        try {
            platformConfiguration.commandGateway();
            fail();
        } catch (final KasperException e) {
            // Ignore
        }

        final CommandGateway commandGateway = platformConfiguration.commandGateway(commandBus);
        assertSame(commandGateway, platformConfiguration.commandGateway());

        try {
            platformConfiguration.commandGateway(commandBus);
            fail();
        } catch (final KasperException e) {
            // Ignore
        }

        return commandGateway;
    }

    // ------------------------------------------------------------------------

    private QueryServicesLocator testQueryServicesLocator(final PlatformConfiguration platformConfiguration) {
        final QueryServicesLocator queryServicesLocator = platformConfiguration.queryServicesLocator();
        assertSame(queryServicesLocator, platformConfiguration.queryServicesLocator());

        return queryServicesLocator;
    }

    // ------------------------------------------------------------------------

    private QueryGateway testQueryGateway(final PlatformConfiguration platformConfiguration,
                                          final QueryServicesLocator queryServicesLocator) {
        try {
            platformConfiguration.queryGateway();
            fail();
        } catch (final KasperException e) {
            // Ignore
        }

        final QueryGateway queryGateway = platformConfiguration.queryGateway(queryServicesLocator);
        assertSame(queryGateway, platformConfiguration.queryGateway());

        try {
            platformConfiguration.queryGateway(queryServicesLocator);
            fail();
        } catch (final KasperException e) {
            // Ignore
        }

        return queryGateway;
    }

    // ------------------------------------------------------------------------

    private EventBus testEventBus(final PlatformConfiguration platformConfiguration) {
        final EventBus eventBus = platformConfiguration.eventBus();
        assertSame(eventBus, platformConfiguration.eventBus());

        return eventBus;
    }

    // ------------------------------------------------------------------------

    private ComponentsInstanceManager testComponentsInstanceManager(final PlatformConfiguration platformConfiguration) {
        final ComponentsInstanceManager componentsInstanceManager = platformConfiguration.getComponentsInstanceManager();
        assertSame(componentsInstanceManager, platformConfiguration.getComponentsInstanceManager());

        return componentsInstanceManager;
    }

    // --------------------------------------------------------------------

    private AnnotationRootProcessor testAnnotationRootProcessor(final PlatformConfiguration platformConfiguration,
                                                                final ComponentsInstanceManager componentsInstanceManager) {
        try {
            platformConfiguration.annotationRootProcessor();
            fail();
        } catch (final KasperException e) {
            // Ignore
        }

        final AnnotationRootProcessor annotationRootProcessor = platformConfiguration.annotationRootProcessor(componentsInstanceManager);
        assertSame(annotationRootProcessor, platformConfiguration.annotationRootProcessor());

        try {
            platformConfiguration.annotationRootProcessor(componentsInstanceManager);
            fail();
        } catch (final KasperException e) {
            // Ignore
        }

        return annotationRootProcessor;
    }

    // ------------------------------------------------------------------------

    private DomainLocator testDomainLocator(final PlatformConfiguration platformConfiguration) {
        final DomainLocator domainLocator = platformConfiguration.domainLocator();
        assertSame(domainLocator, platformConfiguration.domainLocator());

        return domainLocator;
    }

    // ------------------------------------------------------------------------

    private CommandHandlersProcessor testCommandHandlersProcessor(final PlatformConfiguration platformConfiguration,
                                                                  final CommandBus commandBus,
                                                                  final DomainLocator domainLocator) {
        try {
            platformConfiguration.commandHandlersProcessor();
            fail();
        } catch (final KasperException e) {
            // Ignore
        }

        final CommandHandlersProcessor commandHandlersProcessor = platformConfiguration.commandHandlersProcessor(commandBus, domainLocator);
        assertSame(commandHandlersProcessor, platformConfiguration.commandHandlersProcessor());

        try {
            platformConfiguration.commandHandlersProcessor(commandBus, domainLocator);
            fail();
        } catch (final KasperException e) {
            // Ignore
        }

        return commandHandlersProcessor;
    }

    // ------------------------------------------------------------------------

    private DomainsProcessor testDomainsProcessor(final PlatformConfiguration platformConfiguration,
                                                  final DomainLocator domainLocator) {
        try {
            platformConfiguration.domainsProcessor();
            fail();
        } catch (final KasperException e) {
            // Ignore
        }

        final DomainsProcessor domainsProcessor = platformConfiguration.domainsProcessor(domainLocator);
        assertSame(domainsProcessor, platformConfiguration.domainsProcessor());

        try {
            platformConfiguration.domainsProcessor(domainLocator);
            fail();
        } catch (final KasperException e) {
            // Ignore
        }

        return domainsProcessor;
    }

    // ------------------------------------------------------------------------

    private EventListenersProcessor testEventListenersProcessor(final PlatformConfiguration platformConfiguration,
                                                                final EventBus eventBus) {
        try {
            platformConfiguration.eventListenersProcessor();
            fail();
        } catch (final KasperException e) {
            // Ignore
        }

        final EventListenersProcessor eventListenersProcessor = platformConfiguration.eventListenersProcessor(eventBus);
        assertSame(eventListenersProcessor, platformConfiguration.eventListenersProcessor());

        try {
            platformConfiguration.eventListenersProcessor(eventBus);
            fail();
        } catch (final KasperException e) {
            // Ignore
        }

        return eventListenersProcessor;
    }

    // ------------------------------------------------------------------------

    private QueryServicesProcessor testQueryServicesProcessor(final PlatformConfiguration platformConfiguration,
                                                              final QueryServicesLocator queryServicesLocator){
        try {
            platformConfiguration.queryServicesProcessor();
            fail();
        } catch (final KasperException e) {
            // Ignore
        }

        final QueryServicesProcessor queryServicesProcessor = platformConfiguration.queryServicesProcessor(queryServicesLocator);
        assertSame(queryServicesProcessor, platformConfiguration.queryServicesProcessor());

        try {
            platformConfiguration.queryServicesProcessor(queryServicesLocator);
            fail();
        } catch (final KasperException e) {
            // Ignore
        }

        return queryServicesProcessor;
    }

    // ------------------------------------------------------------------------

    private RepositoriesProcessor testRepositoriesProcessor(final PlatformConfiguration platformConfiguration,
                                                            final DomainLocator domainLocator,
                                                            final EventBus eventBus) {
        try {
            platformConfiguration.repositoriesProcessor();
            fail();
        } catch (final KasperException e) {
            // Ignore
        }

        final RepositoriesProcessor repositoriesProcessor = platformConfiguration.repositoriesProcessor(domainLocator, eventBus);
        assertSame(repositoriesProcessor, platformConfiguration.repositoriesProcessor());

        try {
            platformConfiguration.repositoriesProcessor(domainLocator, eventBus);
            fail();
        } catch (final KasperException e) {
            // Ignore
        }

        return repositoriesProcessor;
    }

    // ------------------------------------------------------------------------

    private ServiceFiltersProcessor testServiceFiltersProcessor(final PlatformConfiguration platformConfiguration,
                                                                final QueryServicesLocator queryServicesLocator) {
        try {
            platformConfiguration.serviceFiltersProcessor();
            fail();
        } catch (final KasperException e) {
            // Ignore
        }

        final ServiceFiltersProcessor serviceFiltersProcessor = platformConfiguration.serviceFiltersProcessor(queryServicesLocator);
        assertSame(serviceFiltersProcessor, platformConfiguration.serviceFiltersProcessor());

        try {
            platformConfiguration.serviceFiltersProcessor(queryServicesLocator);
            fail();
        } catch (final KasperException e) {
            // Ignore
        }

        return serviceFiltersProcessor;
    }

    // ------------------------------------------------------------------------

    private Platform testPlatform(final PlatformConfiguration platformConfiguration,
                                  final CommandGateway commandGateway,
                                  final QueryGateway queryGateway,
                                  final EventBus eventBus,
                                  final AnnotationRootProcessor annotationRootProcessor) {
        try {
            platformConfiguration.kasperPlatform();
            fail();
        } catch (final KasperException e) {
            // Ignore
        }

        final Platform platform = platformConfiguration.kasperPlatform(commandGateway, queryGateway, eventBus, annotationRootProcessor);
        assertSame(platform, platformConfiguration.kasperPlatform());

        try {
            platformConfiguration.kasperPlatform(commandGateway, queryGateway, eventBus, annotationRootProcessor);
            fail();
        } catch (final KasperException e) {
            // Ignore
        }

        return platform;
    }

}