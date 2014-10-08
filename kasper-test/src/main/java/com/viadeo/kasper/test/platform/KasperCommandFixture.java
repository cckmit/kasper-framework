// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.test.platform;

import com.viadeo.kasper.context.Context;
import com.viadeo.kasper.cqrs.command.Command;

import java.util.List;

public interface KasperCommandFixture<EXECUTOR extends KasperFixtureCommandExecutor<VALIDATOR>, VALIDATOR extends KasperFixtureCommandResultValidator>
        extends KasperFixture<EXECUTOR> {

    EXECUTOR givenCommands(final Command... commands);

    EXECUTOR givenCommands(final List<Command> commands);

    EXECUTOR givenCommands(final Context context, final Command... commands);

    EXECUTOR givenCommands(final Context context, final List<Command> commands);

}