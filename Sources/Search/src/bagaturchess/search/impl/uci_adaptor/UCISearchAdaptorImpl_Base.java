/*
 *  BagaturChess (UCI chess engine and tools)
 *  Copyright (C) 2005 Krasimir I. Topchiyski (k_topchiyski@yahoo.com)
 *  
 *  Open Source project location: http://sourceforge.net/projects/bagaturchess/develop
 *  SVN repository https://bagaturchess.svn.sourceforge.net/svnroot/bagaturchess
 *
 *  This file is part of BagaturChess program.
 * 
 *  BagaturChess is open software: you can redistribute it and/or modify
 *  it under the terms of the Eclipse Public License version 1.0 as published by
 *  the Eclipse Foundation.
 *
 *  BagaturChess is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  Eclipse Public License for more details.
 *
 *  You should have received a copy of the Eclipse Public License version 1.0
 *  along with BagaturChess. If not, see <http://www.eclipse.org/legal/epl-v10.html/>.
 *
 */
package bagaturchess.search.impl.uci_adaptor;


import bagaturchess.bitboard.api.IBitBoard;
import bagaturchess.bitboard.impl.utils.ReflectionUtils;
import bagaturchess.search.api.IRootSearch;
import bagaturchess.search.api.IRootSearchConfig;
import bagaturchess.search.api.internal.ISearch;
import bagaturchess.search.api.internal.ISearchInfo;
import bagaturchess.search.api.internal.ISearchMediator;
import bagaturchess.search.impl.env.MemoryConsumers;
import bagaturchess.search.impl.env.SharedData;
import bagaturchess.search.impl.rootsearch.multipv.MultiPVRootSearch;
import bagaturchess.search.impl.uci_adaptor.timemanagement.ITimeController;
import bagaturchess.uci.api.BestMoveSender;
import bagaturchess.uci.api.ChannelManager;
import bagaturchess.uci.api.ISearchAdaptorConfig;
import bagaturchess.uci.api.IUCISearchAdaptor;
import bagaturchess.uci.impl.commands.Go;


public abstract class UCISearchAdaptorImpl_Base implements IUCISearchAdaptor {
	
	
	protected static final int RETRY_COUNT_FOR_GETTING_INFO = 11;
	
	
	protected ISearchAdaptorConfig searchAdaptorCfg;
	protected IRootSearchConfig rootSearchCfg;
	protected IBitBoard boardForSetup;
	
	protected BestMoveSender bestMoveSender;
	protected ITimeController timeController;
	protected ISearchMediator currentMediator;
	protected Go currentGoCommand;

	private SharedData sharedData;
	private IRootSearch searcherNormal;
	private IRootSearch searcherNormalMultiPV;
	private IRootSearch searcherPonder;
	private TimeSaver saver;
	
	
	public UCISearchAdaptorImpl_Base(Object[] args) {
		
		searchAdaptorCfg = (ISearchAdaptorConfig) args[0];
		boardForSetup = (IBitBoard) args[1];
		rootSearchCfg = (IRootSearchConfig) searchAdaptorCfg.getRootSearchConfig();
		
		sharedData = new SharedData(rootSearchCfg, null);
		
		searcherNormal = createRootSearcher();
		searcherNormalMultiPV = new MultiPVRootSearch(rootSearchCfg, searcherNormal);
		searcherPonder = searcherNormal;//createRootSearcher();
		
		//Should be created always in the beginning of the game because later the initial board position (fen) is not available.
		searcherNormal.newGame(boardForSetup);
		searcherNormalMultiPV.newGame(boardForSetup);
		searcherPonder.newGame(boardForSetup);
		
		
		//Should be initialized after the searchers to handle memory in a correct way.
		sharedData.setMemoryConsumers(new MemoryConsumers(ChannelManager.getChannel(), rootSearchCfg, searchAdaptorCfg.isOwnBookEnabled()));
		saver = new TimeSaver(sharedData, rootSearchCfg, boardForSetup);
	}
	
	
	public IRootSearch getSearcherNormal() {
		return searcherNormal;
	}


	@Override
	public void shutDown() {
		
		try {
			
			sharedData.clear();
			
			if (currentMediator != null) {
				currentMediator.dump("Shutdown IRootSearch searchers");
			}
			
			if (searcherNormal != null) {
				searcherNormal.shutDown();
			}
			
			if (searcherNormalMultiPV != null) {
				searcherNormalMultiPV.shutDown();
			}
			
			if (searcherPonder != null) {
				searcherPonder.shutDown();
			}
			
		} catch(Exception e) {
			if (currentMediator != null) {
				currentMediator.dump(e);
			}
		}
	}
	
	protected SharedData getSharedData() {
		return sharedData;
	}
	
	
	private IRootSearch createRootSearcher() {
		String rootSearchClassName = searchAdaptorCfg.getRootSearchClassName();
		IRootSearch _searcher = (IRootSearch)
			ReflectionUtils.createObjectByClassName_ObjectsConstructor(rootSearchClassName, new Object[] {rootSearchCfg, sharedData});
		//searcherNormal = new MTDSequentialSearch(_engineBootCfg);
		//searcherNormal = new MTDParallelSearch(_engineBootCfg);
		return _searcher;
	}
	
	
	protected void goSearch(boolean ponderSearch) {
		
		if (ponderSearch) {
			getSearcherNormal().decreaseTPTDepths(1);
		} else {
			getSearcherNormal().decreaseTPTDepths(2);
		}
		
		/*if (timeController == null || timeController.hasTime(1000)) {
			System.gc();
		}*/
		
		
		IRootSearch searcher = getSearcher(ponderSearch);
		
		currentMediator.dump("ROOT SEARCHER: " + searcher);
		currentMediator.dump("Ponder: " + ponderSearch);
		
		boolean isEndlessSearch = isEndlessSearch(currentGoCommand);
		
		currentMediator.dump("IsEndlessSearch: " + isEndlessSearch);
		
		if (!isEndlessSearch) {
			currentMediator.dump("Using TimeSaver ...");
			
			boolean moveSent = saver.beforeMove(boardForSetup, sharedData, currentMediator, searchAdaptorCfg.isOwnBookEnabled());
			
			if (!moveSent) {
				
				int start_iteration = 1;
				
				currentMediator.dump("Normal search started");
				
				int maxIterations = currentGoCommand.getDepth();
				if (maxIterations > ISearch.MAX_DEPTH) {
					maxIterations = ISearch.MAX_DEPTH;
				} else {
					if (maxIterations < maxIterations + rootSearchCfg.getHiddenDepth()) {//Type overflow
						maxIterations += rootSearchCfg.getHiddenDepth();
					}
				}
				
				searcher.negamax(boardForSetup, currentMediator, start_iteration, maxIterations, true, null);
			}
		} else {
			currentMediator.dump("Endless search started");
			searcher.negamax(boardForSetup, currentMediator, false, null);
		}
		
		/*if (ponderSearch) {
			System.gc();
		}*/
	}


	protected IRootSearch getSearcher(boolean ponderSearch) {
		IRootSearch searcher = null;
		if (ponderSearch) {
			searcher = searcherPonder;
		} else {
			if (rootSearchCfg.getMultiPVsCount() > 1) {
				searcher = searcherNormalMultiPV;
			} else {
				searcher = searcherNormal;
			}
		}
		return searcher;
	}
	
	
	@Override
	public synchronized int[] stopSearch() {
		
		if (currentMediator == null) return new int[] {0, 0}; 
		
		int retryCount = RETRY_COUNT_FOR_GETTING_INFO;
		ISearchInfo info = currentMediator.getLastInfo();
		while (info == null && retryCount > 0) {
			try {
				Thread.sleep(33);
			} catch (InterruptedException e) {
				currentMediator.dump(e);
			}
			currentMediator.dump("Retry " + (RETRY_COUNT_FOR_GETTING_INFO - retryCount + 1) + " for getting info");
			info = currentMediator.getLastInfo();
			retryCount--;
		}
		
		if (info == null) {
			currentMediator.dump("Info is still null. Return empty move (=0).");
			
			currentMediator.getStopper().markStopped();
			
			IRootSearch searcher = getSearcher(isPonderSearch(currentGoCommand));
			searcher.stopSearchAndWait();
			
			currentMediator = null;
			currentGoCommand = null;
			
			return new int[] {0, 0}; 
		}
		
		
		int move = info.getBestMove();
		int ponder = 0;
		
		//boolean endlessSearch = isEndlessSearch(currentGoCommand);
		//if (isPonderSearch(currentGoCommand)) {
			if (info.getPV().length >= 2) {
				ponder = info.getPV()[1];
			}
		//}
			
		
		currentMediator.getStopper().markStopped();
		
		IRootSearch searcher = getSearcher(isPonderSearch(currentGoCommand));
		searcher.stopSearchAndWait();
		
		currentMediator = null;
		currentGoCommand = null;
		
		return new int[] {move, ponder};
	}
	
	
	protected boolean isEndlessSearch(Go go) {
		return isPonderSearch(go)
				|| searchAdaptorCfg.isAnalyzeMode() || go.isAnalyzingMode();
	}
	
	
	protected boolean isPonderSearch(Go go) {
		return searchAdaptorCfg.isPonderingEnabled() && go.isPonder();
	}
}
