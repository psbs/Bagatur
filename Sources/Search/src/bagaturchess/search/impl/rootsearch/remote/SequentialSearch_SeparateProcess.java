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
package bagaturchess.search.impl.rootsearch.remote;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import bagaturchess.bitboard.api.IBitBoard;
import bagaturchess.bitboard.common.Utils;
import bagaturchess.bitboard.impl.movegen.MoveInt;
import bagaturchess.search.api.IFinishCallback;
import bagaturchess.search.api.IRootSearchConfig;
import bagaturchess.search.api.internal.ISearchInfo;
import bagaturchess.search.api.internal.ISearchMediator;
import bagaturchess.search.impl.info.SearchInfoFactory;
import bagaturchess.search.impl.rootsearch.RootSearch_BaseImpl;
import bagaturchess.search.impl.utils.DEBUGSearch;
import bagaturchess.uci.api.ChannelManager;
import bagaturchess.uci.engine.EngineProcess;
import bagaturchess.uci.engine.EngineProcess.LineCallBack;
import bagaturchess.uci.engine.EngineProcess_BagaturImpl_DistributionImpl;
import bagaturchess.uci.engine.EngineProcess_BagaturImpl_WorkspaceImpl;
import bagaturchess.uci.engine.UCIEnginesManager;
import bagaturchess.uci.impl.commands.Go;
import bagaturchess.uci.impl.commands.info.Info;


public class SequentialSearch_SeparateProcess extends RootSearch_BaseImpl {
	
	
	private ExecutorService executor;
	
	private UCIEnginesManager runner;
	
	private int hashfull;
	
	
	public SequentialSearch_SeparateProcess(Object[] args) {
		
		super(args);
		
		executor = Executors.newFixedThreadPool(2);
		
		
		runner = new UCIEnginesManager();
		
		
		String workdir = new File(".").getAbsolutePath();//"C:/DATA/OWN/chess/software/ARENA/arena_3.5.1/Engines/BagaturEngine_DEV/",
		ChannelManager.getChannel().dump("SequentialSearch_SeparateProcess: Starting Java process of engine in workdir '" + workdir + "'");
		
		/*EngineProcess engine = new EngineProcess_BagaturImpl_WorkspaceImpl("BagaturEngineClient",
				"C:/DATA/OWN/chess/GIT_REPO/Bagatur-Chess-Engine-And-Tools/Sources/",
				"",
				256);*/
		
		EngineProcess engine = new EngineProcess_BagaturImpl_DistributionImpl("BagaturEngineClient",
				workdir + File.separatorChar,
				"",
				256);
		
		
		runner.addEngine(engine);
		
		try {
			
			runner.startEngines();
			
			runner.uciOK();
			
			runner.isReady();
			
			runner.disable();
			
		} catch (Throwable t) {
			ChannelManager.getChannel().dump(t);
		}
	}
	
	
	public IRootSearchConfig getRootSearchConfig() {
		return (IRootSearchConfig) super.getRootSearchConfig();
	}
	
	
	@Override
	public void newGame(IBitBoard _bitboardForSetup) {
		
		super.newGame(_bitboardForSetup);
		
		try {
			
			runner.newGame();
			
			setUpEnginePosition(_bitboardForSetup);
			
		} catch (Throwable t) {
			ChannelManager.getChannel().dump(t);
		}
	}


	private void setUpEnginePosition(IBitBoard _bitboardForSetup)
			throws IOException {
		
		//Initialize engine by FEN and moves
		
		int movesCount = _bitboardForSetup.getPlayedMovesCount();
		int[] moves = Utils.copy(_bitboardForSetup.getPlayedMoves());
		
		_bitboardForSetup.revert();
		
		String initialFEN = _bitboardForSetup.toEPD();
		
		for (int i=0; i<movesCount; i++) {
			_bitboardForSetup.makeMoveForward(moves[i]);
		}
		
		String allMovesStr = MoveInt.getMovesUCI(_bitboardForSetup);
		
		runner.setupPosition("fen " + initialFEN + " moves " + allMovesStr);
		
		//runner.setupPosition("startpos");
	}
	
	
	@Override
	public void shutDown() {
		try {
			
			runner.stopEngines();
			
			runner.destroyEngines();
			
			//runner.enable();
			
			executor.shutdownNow();
			
		} catch(Throwable t) {
			//Do nothing
		}
	}
	
	
	@Override
	public void negamax(IBitBoard _bitboardForSetup, ISearchMediator mediator,
			int startIteration, int maxIterations, final boolean useMateDistancePrunning, final IFinishCallback multiPVCallback, final int[] prevPV) {
		
		negamax(_bitboardForSetup, mediator, multiPVCallback, new Go(ChannelManager.getChannel(), "go infinite"));
	}
	
	
	@Override
	public void negamax(IBitBoard bitboardForSetup, ISearchMediator mediator, final IFinishCallback multiPVCallback, Go go) {
			
		
		if (stopper != null) {
			throw new IllegalStateException("MTDSequentialSearch started whithout beeing stopped");
		}
		stopper = new Stopper();
		
		
		setupBoard(bitboardForSetup);
		
		
		String allMovesStr = MoveInt.getMovesUCI(getBitboardForSetup());
		
		if (DEBUGSearch.DEBUG_MODE) ChannelManager.getChannel().dump("SequentialSearch_SeparateProcess: allMovesStr=" + allMovesStr);
		
		//StringBuilder message = new StringBuilder(32);
		//MoveInt.moveToStringUCI(cur_move, message);
		//String moveStr = message.toString();
		
		try {
			
			//runner.setupPosition("startpos moves " + allMovesStr);
			runner.setupPosition("moves " + allMovesStr);
			
			runner.go(go);
			
			runner.disable();
			
			
			final ISearchMediator final_mediator = mediator;
			
			//OutboundQueueProcessor
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						
						if (DEBUGSearch.DEBUG_MODE) ChannelManager.getChannel().dump("SequentialSearch_SeparateProcess: OutboundQueueProcessor before loop");
						
						while (!final_mediator.getStopper().isStopped() //Condition for normal play
								&& stopper != null && !stopper.isStopped()) {
							
							Thread.sleep(15);
						}
						
						if (DEBUGSearch.DEBUG_MODE) ChannelManager.getChannel().dump("SequentialSearch_SeparateProcess: OutboundQueueProcessor after loop stopped="
								+ final_mediator.getStopper().isStopped());
						
						
						//runner.stopEngines();
						
						
						//runner.enable();
						
						
					} catch(Throwable t) {
						ChannelManager.getChannel().dump(t);
						ChannelManager.getChannel().dump(t.getMessage());
					}
				}
			});
			
			
			//InboundQueueProcessor
			executor.execute(new Runnable() {
				
				@Override
				public void run() {
					try {
						
						if (DEBUGSearch.DEBUG_MODE) ChannelManager.getChannel().dump("SequentialSearch_SeparateProcess: InboundQueueProcessor: before getInfoLines");
						
						LineCallBack callback = new LineCallBack() {
							
							
							private List<String> lines = new ArrayList<String>();
							private String exitLine = null; 
							
							
							@Override
							public void newLine(String line) {
								
								if (DEBUGSearch.DEBUG_MODE) ChannelManager.getChannel().dump("SequentialSearch_SeparateProcess: getInfoLine '" + line + "'");
								
								if (line.contains("LOG")) {
									if (DEBUGSearch.DEBUG_MODE) ChannelManager.getChannel().dump("SequentialSearch_SeparateProcess: getInfoLine SKIPED, contains LOG");
									return;
								}
								
								lines.add(line);
								
								if (line.contains("bestmove")) {
									
									for (int i=lines.size() - 1; i >=0; i--) {
										if (lines.get(i).contains("info ") && lines.get(i).contains(" pv ")) {
											exitLine = lines.get(i);
											return;
										}
									}
									
									throw new IllegalStateException("No pv: " + lines);
									
								} else if (line.contains("info ")) {
									if (line.contains(" pv ")) {
										
										Info info = new Info(line);
										//System.out.println("MAJOR: " + info);
										
										ISearchInfo searchInfo = SearchInfoFactory.getFactory().createSearchInfo(info, getBitboardForSetup());
										if (searchInfo.getPV() != null && searchInfo.getPV().length > 0) {
											final_mediator.changedMajor(searchInfo);
										}
									} else {
										//System.out.println("MINOR: " + line);
										if (DEBUGSearch.DEBUG_MODE) ChannelManager.getChannel().dump("SequentialSearch_SeparateProcess: getInfoLine minor line");
										
										Info info = new Info(line);
										//System.out.println("MAJOR: " + info);
										
										hashfull = info.getHashfull() / 10;
										
										ISearchInfo searchInfo = SearchInfoFactory.getFactory().createSearchInfo_Minor(info, getBitboardForSetup());
										final_mediator.changedMinor(searchInfo);
									}
								}
							}
							
							
							@Override
							public String exitLine() {
								return exitLine;
							}	
						};
						
						List<String> infos = runner.getInfoLines(callback);
						
						if (infos.size() > 1) {
							throw new IllegalStateException("Only one engine is supported");
						}
						
						if (infos.size() == 0 || infos.get(0) == null) {
							throw new IllegalStateException("infos.size() == 0 || infos.get(0) == null");
						}
						//System.out.println("depth " + depth);
						
						//runner.stopEngines();
						
						if (stopper == null) {
							throw new IllegalStateException();
						}
						stopper.markStopped();
						stopper = null;
						
						if (multiPVCallback == null) {//Non multiPV search
							final_mediator.getBestMoveSender().sendBestMove();
						} else {
							//MultiPV search
							multiPVCallback.ready();
						}
						
						if (DEBUGSearch.DEBUG_MODE) ChannelManager.getChannel().dump("SequentialSearch_SeparateProcess: InboundQueueProcessor after loop stopped="
								+ final_mediator.getStopper().isStopped());
						
						//runner.enable();
						
					} catch(Throwable t) {
						ChannelManager.getChannel().dump(t);
						ChannelManager.getChannel().dump(t.getMessage());
					}
				}
			});
			
		} catch (Throwable t) {
			ChannelManager.getChannel().dump(t);
		}
	}


	@Override
	public int getTPTUsagePercent() {
		return hashfull;
	}


	@Override
	public void decreaseTPTDepths(int reduction) {
		//Do nothing
		//As UCI doesn't support such options, this have to be implemented "on top" or "in addition" of UCI communication between this object and currently running engine process(es)
	}
}