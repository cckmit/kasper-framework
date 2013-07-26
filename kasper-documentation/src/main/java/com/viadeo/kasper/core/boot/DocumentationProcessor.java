// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.core.boot;

import com.google.common.base.Preconditions;
import com.viadeo.kasper.test.doc.KasperLibrary;

import java.lang.annotation.Annotation;

public abstract class DocumentationProcessor<T extends Annotation, I> implements AnnotationProcessor<T, I> {

	private KasperLibrary kasperLibrary;
	
	public void setKasperLibrary(final KasperLibrary kasperLibrary) {
		this.kasperLibrary = Preconditions.checkNotNull(kasperLibrary);
	}
	
	public KasperLibrary getKasperLibrary() {
		return this.kasperLibrary;
	}
	
}