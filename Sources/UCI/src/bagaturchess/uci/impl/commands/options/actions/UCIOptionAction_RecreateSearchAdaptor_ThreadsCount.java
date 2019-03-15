package bagaturchess.uci.impl.commands.options.actions;


import java.io.FileNotFoundException;

import bagaturchess.uci.api.IUCIOptionAction;
import bagaturchess.uci.impl.StateManager;


public class UCIOptionAction_RecreateSearchAdaptor_ThreadsCount implements IUCIOptionAction {
	
	
	private StateManager stateManager;
	
	
	public UCIOptionAction_RecreateSearchAdaptor_ThreadsCount(StateManager _stateManager) {
		stateManager = _stateManager;
	}
	
	@Override
	public void execute() throws FileNotFoundException {
		if (stateManager.destroySearchAdaptor()) {
			stateManager.createSearchAdaptor();
		}
	}
	
	
	@Override
	public String getOptionName() {
		return "SMP Threads";
	}
}
