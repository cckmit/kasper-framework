// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.test.doc.web;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.viadeo.kasper.client.platform.domain.descriptor.*;
import com.viadeo.kasper.doc.element.DocumentedPlatform;
import com.viadeo.kasper.doc.initializer.DefaultDocumentedElementInitializer;
import com.viadeo.kasper.doc.web.KasperDocResource;
import com.viadeo.kasper.doc.web.ObjectMapperKasperResolver;
import com.viadeo.kasper.event.IEvent;
import com.viadeo.kasper.test.applications.Applications;
import com.viadeo.kasper.test.root.Facebook;
import com.viadeo.kasper.test.root.commands.AddConnectionToMemberCommand;
import com.viadeo.kasper.test.root.entities.Member;
import com.viadeo.kasper.test.root.entities.Member_connectedTo_Member;
import com.viadeo.kasper.test.root.events.FacebookEvent;
import com.viadeo.kasper.test.root.events.FacebookMemberEvent;
import com.viadeo.kasper.test.root.events.MemberCreatedEvent;
import com.viadeo.kasper.test.root.events.NewMemberConnectionEvent;
import com.viadeo.kasper.test.root.handlers.AddConnectionToMemberHandler;
import com.viadeo.kasper.test.root.listeners.MemberCreatedEventListener;
import com.viadeo.kasper.test.root.queries.GetAllMemberQueryHandler;
import com.viadeo.kasper.test.root.queries.GetMemberQueryHandler;
import com.viadeo.kasper.test.root.queries.GetMembersQueryHandler;
import com.viadeo.kasper.test.root.repositories.MemberConnectionsRepository;
import com.viadeo.kasper.test.root.repositories.MemberRepository;
import com.viadeo.kasper.test.timelines.Timelines;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;

import java.io.IOException;

public class KasperDocStandalone {

    public static void main(final String [] args) throws IOException, InterruptedException {
        final String baseUri = "http://localhost:9988/";

        final DomainDescriptor domainDescriptor = new DomainDescriptor(
                Facebook.NAME,
                Facebook.class,
                ImmutableList.<QueryHandlerDescriptor>of(
                        new QueryHandlerDescriptor(
                                GetMembersQueryHandler.class,
                                GetMembersQueryHandler.GetMembersQuery.class,
                                GetMembersQueryHandler.MembersResult.class),
                        new QueryHandlerDescriptor(
                                GetAllMemberQueryHandler.class,
                                GetAllMemberQueryHandler.GetAllMemberQuery.class,
                                GetAllMemberQueryHandler.AllMemberResult.class),
                        new QueryHandlerDescriptor(
                                GetMemberQueryHandler.class,
                                GetMemberQueryHandler.GetMemberQuery.class,
                                GetAllMemberQueryHandler.MemberResult.class)
                ),
                ImmutableList.<CommandHandlerDescriptor>of(new CommandHandlerDescriptor(
                        AddConnectionToMemberHandler.class,
                        AddConnectionToMemberCommand.class)
                ),
                ImmutableList.<RepositoryDescriptor>of(
                        new RepositoryDescriptor(
                                MemberRepository.class,
                                DomainDescriptorFactory.toAggregateDescriptor(Member.class)
                        ),
                        new RepositoryDescriptor(
                                MemberConnectionsRepository.class,
                                DomainDescriptorFactory.toAggregateDescriptor(Member_connectedTo_Member.class)
                        )
                ),
                ImmutableList.<EventListenerDescriptor>of(new EventListenerDescriptor(
                        MemberCreatedEventListener.class,
                        MemberCreatedEvent.class)
                ),
                ImmutableList.<Class<? extends IEvent>>of(
                        FacebookEvent.class,
                        FacebookMemberEvent.class,
                        MemberCreatedEvent.class,
                        NewMemberConnectionEvent.class
                )
        );

        final DocumentedPlatform documentedPlatform = new DocumentedPlatform();
        documentedPlatform.registerDomain(Facebook.NAME, domainDescriptor);
        documentedPlatform.registerDomain(Applications.NAME, new DomainDescriptor(
                Applications.NAME,
                Applications.class,
                Lists.<QueryHandlerDescriptor>newArrayList(),
                Lists.<CommandHandlerDescriptor>newArrayList(),
                Lists.<RepositoryDescriptor>newArrayList(),
                Lists.<EventListenerDescriptor>newArrayList(),
                Lists.<Class<? extends IEvent>>newArrayList()
                )
        );
        documentedPlatform.registerDomain(Timelines.NAME, new DomainDescriptor(
                Timelines.NAME,
                Timelines.class,
                Lists.<QueryHandlerDescriptor>newArrayList(),
                Lists.<CommandHandlerDescriptor>newArrayList(),
                Lists.<RepositoryDescriptor>newArrayList(),
                Lists.<EventListenerDescriptor>newArrayList(),
                Lists.<Class<? extends IEvent>>newArrayList()
        ));
        documentedPlatform.accept(new DefaultDocumentedElementInitializer(documentedPlatform));

        final KasperDocResource res = new KasperDocResource(documentedPlatform);

        final ResourceConfig rc = new PackagesResourceConfig("com.viadeo.kasper.test.doc.web");
        rc.getSingletons().add(res);
        rc.getProviderClasses().add(ObjectMapperKasperResolver.class);

        System.out.println("Starting grizzly...");

        final HttpServer server = GrizzlyServerFactory.createHttpServer(baseUri, rc);

        server.getServerConfiguration().addHttpHandler(new StaticHttpHandler("src/main/resources/META-INF/resources/doc/"),"/doc");
        server.getServerConfiguration().addHttpHandler(new StaticHttpHandler("src/main/resources/META-INF/resources/ndoc/"),"/ndoc");

        System.out.println(String.format("Try out %skasper/doc/domains \nAccess UI at %sdoc/index.htm", baseUri, baseUri));

        System.in.read();

        server.stop();
        System.exit(0);
    }

}
