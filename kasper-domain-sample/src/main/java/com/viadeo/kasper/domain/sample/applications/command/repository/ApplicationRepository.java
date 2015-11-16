// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.domain.sample.applications.command.repository;

import com.google.common.base.Optional;
import com.viadeo.kasper.api.id.KasperID;
import com.viadeo.kasper.core.component.annotation.XKasperRepository;
import com.viadeo.kasper.core.component.command.repository.AutowiredRepository;
import com.viadeo.kasper.domain.sample.applications.command.model.entity.Application;

@XKasperRepository(description = ApplicationRepository.DESCRIPTION)
public class ApplicationRepository extends AutowiredRepository<KasperID,Application> {

	public static final String DESCRIPTION = "The applications repository";

    @Override
    protected Optional<Application> doLoad(KasperID aggregateIdentifier, Long expectedVersion) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doSave(Application aggregate) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doDelete(Application aggregate) {
        throw new UnsupportedOperationException();
    }
}
