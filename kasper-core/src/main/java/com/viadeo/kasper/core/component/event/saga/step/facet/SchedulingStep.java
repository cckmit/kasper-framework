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
package com.viadeo.kasper.core.component.event.saga.step.facet;

import com.google.common.base.Optional;
import com.viadeo.kasper.api.component.event.Event;
import com.viadeo.kasper.api.component.event.SchedulableSagaMethod;
import com.viadeo.kasper.api.context.Context;
import com.viadeo.kasper.core.component.annotation.XKasperSaga;
import com.viadeo.kasper.core.component.event.saga.Saga;
import com.viadeo.kasper.core.component.event.saga.step.Scheduler;
import com.viadeo.kasper.core.component.event.saga.step.Step;
import com.viadeo.kasper.core.component.event.saga.step.StepInvocationException;
import org.joda.time.Duration;

import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public class SchedulingStep extends DecorateStep {

    public enum OperationType {
        CANCEL {
            @Override
            public String action(String methodName) {
                return String.format("Cancel(methodName=%s)", methodName);
            }
        },
        SCHEDULE {
            @Override
            public String action(String methodName) {
                return String.format("Schedule(methodName=%s)", methodName);
            }
        },
        SCHEDULE_BY_EVENT {
            @Override
            public String action(String methodName) {
                return String.format("SchedulingByEvent(methodName=%s)", methodName);
            }
        };

        public abstract String action(final String methodName);
    }

    // ------------------------------------------------------------------------

    protected interface Operation {
        void execute(Event event, Object identifier);
        void clean(Object identifier);
        String getAction();
        OperationType getType();
    }

    // ------------------------------------------------------------------------

    public static abstract class OperationAdapter implements Operation {

        protected final Scheduler scheduler;
        protected final String methodName;
        protected final Class<? extends Saga> sagaClass;
        private final String action;
        private final OperationType operationType;

        public OperationAdapter(final Scheduler scheduler, final Class<? extends Saga> sagaClass, final String methodName, final OperationType operationType) {
            this.sagaClass = checkNotNull(sagaClass);
            this.scheduler = checkNotNull(scheduler);
            this.methodName = checkNotNull(methodName);
            this.operationType = checkNotNull(operationType);
            this.action = operationType.action(methodName);
        }

        @Override
        public String getAction() {
            return action;
        }

        @Override
        public OperationType getType() {
            return operationType;
        }

        @Override
        public void execute(final Event event, final Object identifier) { }

        @Override
        public void clean(final Object identifier) { }
    }

    // ------------------------------------------------------------------------

    public static class CancelOperation extends OperationAdapter {

        public CancelOperation(final Scheduler scheduler, final Class<? extends Saga> sagaClass, final String methodName) {
            super(scheduler, sagaClass, methodName, OperationType.CANCEL);
        }

        @Override
        public void execute(final Event event, final Object identifier) {
            scheduler.cancelSchedule(sagaClass, methodName, identifier);
        }
    }

    // ------------------------------------------------------------------------

    public static class ScheduleOperation extends OperationAdapter {

        private final Duration duration;
        private final boolean endAfterExecution;

        public ScheduleOperation(final Scheduler scheduler, final Class<? extends Saga> sagaClass, final String methodName, final Long delay, final TimeUnit unit, final boolean endAfterExecution) {
            super(scheduler, sagaClass, methodName, OperationType.SCHEDULE);
            checkNotNull(delay);
            checkNotNull(unit);
            this.endAfterExecution = endAfterExecution;
            this.duration = new Duration(TimeUnit.MILLISECONDS.convert(delay, unit));
        }

        @Override
        public void execute(final Event event, final Object identifier) {
            scheduler.schedule(sagaClass, methodName, identifier, duration, endAfterExecution);
        }

        @Override
        public void clean(final Object identifier) {
            scheduler.cancelSchedule(sagaClass, methodName, identifier);
        }
    }

    // ------------------------------------------------------------------------

    public static class ScheduleByEventOperation extends OperationAdapter {

        private final boolean endAfterExecution;

        public ScheduleByEventOperation(final Scheduler scheduler, final Class<? extends Saga> sagaClass, final String methodName, final boolean endAfterExecution) {
            super(scheduler, sagaClass, methodName, OperationType.SCHEDULE_BY_EVENT);
            this.endAfterExecution = endAfterExecution;
        }

        @Override
        public void execute(final Event event, final Object identifier) {
            if (SchedulableSagaMethod.class.isAssignableFrom(event.getClass())) {
                scheduler.schedule(sagaClass, methodName, identifier, ((SchedulableSagaMethod) event).getScheduledDate(), endAfterExecution);
            }
        }

        @Override
        public void clean(final Object identifier) {
            scheduler.cancelSchedule(sagaClass, methodName, identifier);
        }
    }

    // ------------------------------------------------------------------------

    private final Operation operation;

    public SchedulingStep(
            final Scheduler scheduler,
            final Step delegateStep,
            final XKasperSaga.CancelSchedule annotation
    ) {
        this(delegateStep, new CancelOperation(
                scheduler,
                delegateStep.getSagaClass(),
                annotation.methodName()
        ));
    }

    public SchedulingStep(
            final Scheduler scheduler,
            final Step delegateStep,
            final XKasperSaga.Schedule annotation
    ) {
        this(delegateStep, new ScheduleOperation(
                scheduler,
                delegateStep.getSagaClass(),
                annotation.methodName(),
                annotation.delay(),
                annotation.unit(),
                annotation.end()
        ));
    }

    public SchedulingStep(
            final Scheduler scheduler,
            final Step delegateStep,
            final XKasperSaga.ScheduledByEvent annotation
    ) {
        this(delegateStep, new ScheduleByEventOperation(
                scheduler,
                delegateStep.getSagaClass(),
                annotation.methodName(),
                annotation.end()
        ));
    }

    public SchedulingStep(final Step delegateStep,
                          final Operation operation) {
        super(delegateStep);
        this.operation = checkNotNull(operation);
    }

    @Override
    public void invoke(final Saga saga, final Context context, final Event event) throws StepInvocationException {
        getDelegateStep().invoke(saga, context, event);

        final Optional<Object> identifier = getSagaIdentifierFrom(event);
        if (identifier.isPresent()) {
            operation.execute(event, identifier.get());
        }
    }

    @Override
    protected String getAction() {
        return operation.getAction();
    }

    @Override
    public void clean(Object identifier) {
        operation.clean(identifier);
    }

    public OperationType getOperationType() {
        return operation.getType();
    }
}
