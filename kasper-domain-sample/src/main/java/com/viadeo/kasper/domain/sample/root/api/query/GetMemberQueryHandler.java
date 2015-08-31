// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.domain.sample.root.api.query;

import com.viadeo.kasper.api.component.query.Query;
import com.viadeo.kasper.core.component.query.AutowiredQueryHandler;
import com.viadeo.kasper.core.component.query.annotation.XKasperQueryHandler;
import com.viadeo.kasper.domain.sample.root.api.Facebook;

@XKasperQueryHandler(domain=Facebook.class)
public class GetMemberQueryHandler
        extends AutowiredQueryHandler<GetMemberQueryHandler.GetMemberQuery, GetAllMemberQueryHandler.MemberResult>
{

    public static class GetMemberQuery implements Query {
        private static final long serialVersionUID = -2974536117783571730L;
    }
}