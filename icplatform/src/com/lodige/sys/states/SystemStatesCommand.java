/**
 * icplatform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.sys.states;

import github.javaappplatform.platform.console.ICommand;
import github.javaappplatform.platform.extension.ExtensionRegistry;

import java.io.PrintStream;

/**
 * TODO javadoc
 * @author renken
 */
public class SystemStatesCommand implements ICommand
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(String[] args, PrintStream out) throws Exception
	{
		for (ISystemState state : ExtensionRegistry.<ISystemState>getServices(ISystemState.EXTPOINT_SYSTEM_STATE))
		{
			out.println(state.name() + ": " + state.state()); //$NON-NLS-1$
		}
	}

}
