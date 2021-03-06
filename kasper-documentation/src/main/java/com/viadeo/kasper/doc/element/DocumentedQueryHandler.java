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
package com.viadeo.kasper.doc.element;

import com.viadeo.kasper.doc.initializer.DocumentedElementVisitor;
import com.viadeo.kasper.doc.nodes.DocumentedBean;
import com.viadeo.kasper.doc.nodes.DocumentedQueryResponse;
import com.viadeo.kasper.platform.bundle.descriptor.QueryHandlerDescriptor;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class DocumentedQueryHandler extends AbstractDomainElement {

    private final DocumentedQuery documentedQuery;
    private final DocumentedQueryResult documentedQueryResult;

    public static class DocumentedQuery extends AbstractPropertyDomainElement {

        private final DocumentedQueryHandler queryHandler;
        private final DocumentedBean response;

        public DocumentedQuery(final DocumentedDomain domain,
                               final DocumentedQueryHandler queryHandler,
                               final Class queryClass) {
            super(domain, DocumentedElementType.QUERY, queryClass);
            this.queryHandler = queryHandler;
            this.response = new DocumentedQueryResponse(queryHandler.documentedQueryResult.getReferenceClass());
        }

        public LightDocumentedElement<DocumentedQueryHandler> getQueryHandler() {
            return queryHandler.getLightDocumentedElement();
        }

        public DocumentedBean getResponse() {
            return response;
        }

        @Override
        public LightDocumentedElement<DocumentedQuery> getLightDocumentedElement() {
            return new LightDocumentedInputElement<>(this);
        }

        @Override
        public void accept(final DocumentedElementVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public boolean isPublicAccess() {
            return queryHandler.isPublicAccess();
        }

        @Override
        public DocumentedAuthorization getAuthorization() {
            return queryHandler.getAuthorization();
        }
    }

    public static class DocumentedQueryResult extends AbstractPropertyDomainElement {

        private static final LinkedMultiValueMap<Class, LightDocumentedElement> HANDLERS_BY_QUERY_RESULTS = new LinkedMultiValueMap<>();

        private DocumentedQueryResult element;

        public DocumentedQueryResult(final DocumentedDomain domain,
                                     final DocumentedQueryHandler queryHandler,
                                     final Class queryResultClass) {
            super(domain, DocumentedElementType.QUERY_RESULT, queryResultClass);

            if(null != queryHandler) {
                HANDLERS_BY_QUERY_RESULTS.add(queryResultClass, queryHandler.getLightDocumentedElement());
            }
        }

        public List<LightDocumentedElement> getQueryHandlers() {
            final List<LightDocumentedElement> queryHandlers = HANDLERS_BY_QUERY_RESULTS.get(getReferenceClass());
            return queryHandlers != null ? queryHandlers : null;
        }

        @Override
        public LightDocumentedElement<DocumentedQueryResult> getLightDocumentedElement() {
            return new LightDocumentedElement<>(this);
        }

        @Override
        public void accept(DocumentedElementVisitor visitor) {
            visitor.visit(this);
        }

        public LightDocumentedElement<DocumentedQueryResult> getElement() {
            if(null == element) {
                return null;
            }
            return element.getLightDocumentedElement();
        }

        public void setElement(DocumentedQueryResult element) {
            this.element = element;
        }
    }

    // ------------------------------------------------------------------------

    public DocumentedQueryHandler(final DocumentedDomain documentedDomain,
                                  final QueryHandlerDescriptor queryHandlerDescriptor) {
        super(
                checkNotNull(documentedDomain),
                DocumentedElementType.QUERY_HANDLER,
                queryHandlerDescriptor.getReferenceClass()
        );

        this.documentedQueryResult = new DocumentedQueryResult(
                documentedDomain,
                this,
                queryHandlerDescriptor.getQueryResultClass()
        );

        this.documentedQuery = new DocumentedQuery(
                documentedDomain,
                this,
                queryHandlerDescriptor.getQueryClass()
        );
    }

    // ------------------------------------------------------------------------

    @Override
    public void accept(final DocumentedElementVisitor visitor) {
        documentedQuery.accept(visitor);
        documentedQueryResult.accept(visitor);
        visitor.visit(this);
    }

    @Override
    public LightDocumentedElement<DocumentedQueryHandler> getLightDocumentedElement() {
        return new LightDocumentedElement<DocumentedQueryHandler>(this) {

            public String getQueryName() {
                return documentedElement.getQuery().getName();
            }

            public String getQueryResultName() {
                return documentedElement.getQueryResult().getName();
            }
        };
    }

    // ------------------------------------------------------------------------

    public LightDocumentedElement<DocumentedQuery> getQuery() {
        return documentedQuery.getLightDocumentedElement();
    }

    public LightDocumentedElement<DocumentedQueryResult> getQueryResult() {
        return documentedQueryResult.getLightDocumentedElement();
    }

}
