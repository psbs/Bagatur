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
package bagaturchess.search.impl.rootsearch.sequential.mtd;


import java.util.ArrayList;
import java.util.List;

import bagaturchess.bitboard.api.IBitBoard;
import bagaturchess.bitboard.impl.movegen.MoveInt;
import bagaturchess.search.api.internal.ISearch;
import bagaturchess.search.api.internal.ISearchInfo;
import bagaturchess.search.api.internal.ISearchMediator;
import bagaturchess.search.api.internal.SearchInterruptedException;
import bagaturchess.search.impl.info.SearchInfoFactory;
import bagaturchess.search.impl.pv.PVHistoryEntry;
import bagaturchess.search.impl.pv.PVNode;
import bagaturchess.search.impl.tpt.TPTEntry;
import bagaturchess.search.impl.utils.DEBUGSearch;
import bagaturchess.uci.api.ChannelManager;


public class NullwinSearchTask implements Runnable {
	
	
	private ISearch searcher;
	private SearchManager distribution;
	private IBitBoard bitboard;
	private ISearchMediator mediator;
	private boolean useMateDistancePrunning;
	private int[] prevPV;
	
	private List<Integer> pv_buffer = new ArrayList<Integer>();
	
	
	public NullwinSearchTask(ISearch _searcher, SearchManager _distribution,
			IBitBoard _bitboard, ISearchMediator _mediator, boolean _useMateDistancePrunning, int[] _prevPV) {
		searcher = _searcher;
		distribution = _distribution;
		bitboard = _bitboard;
		mediator = _mediator;
		useMateDistancePrunning = _useMateDistancePrunning;
		prevPV = _prevPV;
	}
	
	
	public void run() {
		
		//boolean unlock = false;
		
		try {
			
			//distribution.writeLock();
			//unlock = true;
			
			
			//if (mediator.getStopper().isStopped()) return;
			
			//System.out.println(sharedData);
			
			ISearchInfo info = SearchInfoFactory.getFactory().createSearchInfo();
			mediator.registerInfoObject(info);
			
			//System.out.println("working");
			
			//System.out.println("run: in " + Thread.currentThread());
			
			//System.out.println("run: not stopped");
			
			//mediator.dump(Thread.currentThread().getName() + "B	" + distribution.toString());
			
			int maxdepth = distribution.getCurrentDepth();
			if (maxdepth > ISearch.MAX_DEPTH) {
				//distribution.writeUnlock();
				//Unlocked in the finally block
				//return;
				throw new IllegalStateException("maxdepth > ISearch.MAX_DEPTH");
			}
			
			//System.out.println("win");
			if (maxdepth > distribution.getMaxIterations()) {
				//distribution.writeUnlock();
				//Unlocked in the finally block
				//System.out.println("return");
				//return;
				throw new IllegalStateException("maxdepth > ISearch.MAX_DEPTH");
			}
			
			int beta = distribution.nextBeta();
			
			info.setDepth(maxdepth);
			info.setSelDepth(maxdepth);
			
			//Runnable task = new NullwinSearchTask(tpool, searchers, distribution, bitboard, mediator, sharedData, useMateDistancePrunning);
			
			//IBitBoard new_bitboard = new Board(bitboard.toEPD());
			//tpool.execute(new WorkPortion(tpool, searchers, distribution, new_bitboard, mediator, sharedData));
			
			//System.out.println("run: forked");
			//tpool.execute(task);
			
			
			//searchEnv = new SearchEnv(bitboard, sharedData);
			//Search search = new Search(searchEnv);
			
			//searcher = searchers.getSearcher(bitboard, sharedData);
			
			if (prevPV == null) {
				PVHistoryEntry historyEntry = searcher.getEnv().getPVs().getPV(bitboard.getHashKey());
				if (historyEntry != null) {
					prevPV = searcher.getEnv().getPVs().getPV(searcher.getEnv().getBitboard().getHashKey()).getPv();
				}
			}
			
			/*
			if (sharedData.getTPT() != null) {
				sharedData.getTPT().lock();
				TPTEntry entry = sharedData.getTPT().get(hashkey);
				if (entry != null && entry.getBestMove_lower() != 0) {
					initialVal = entry.getLowerBound();
					//if (sharedData.getEngineConfiguration().getSearchConfig().isOther_UseTPTInRoot()) {
						//prevIterationEval = initialVal;
					//}
				}
				sharedData.getTPT().unlock();
			}
			*/
			
			//RootSearchMTDImpl rootWin = new RootSearchMTDImpl(bitboard.getColourToMove(), distribution.getBetasGen());
			
			if (DEBUGSearch.DEBUG_MODE) mediator.dump(Thread.currentThread().getName() + ":	start search with depth=" + maxdepth + ", beta=" + beta +" and distribution is " + distribution.toString());
			
			//distribution.writeUnlock();
			//unlock = false;
			
			
			//((SearchMTD_PV)searcher).analyze(maxdepth);
			
			//((SearchMTD_PV)searcher).setTempoless(0);
			//((SearchMTD_PV)searcher).pv_search(mediator, mediator.getStopper(), rootWin, info,
				//	ISearch.PLY * maxdepth, ISearch.PLY * maxdepth, 0, beta - 1, beta, 0, 0, prevPV, false, 0, bitboard.getColourToMove());
			
			//searcher.getEnv().getTPT().obsoleteAll();
			
			//((SearchMTD_PV)searcher).setTempoless(SearchMTD_PV.MAX_tempoless);
			
			/*int eval = searcher.pv_search(mediator, rootWin, info,
					ISearch.PLY * maxdepth, ISearch.PLY * maxdepth, 0, beta - 1, beta, 0, 0, prevPV, false, 0,
					bitboard.getColourToMove(), 0, 0, false, 0, useMateDistancePrunning);*/
			int eval = searcher.nullwin_search(mediator, info, ISearch.PLY * (maxdepth - 0), ISearch.PLY * (maxdepth - 0),
					0, beta, false, 0, 0, prevPV, searcher.getEnv().getBitboard().getColourToMove(), 0, 0, false, 0, useMateDistancePrunning);
			
			if (eval >= beta) {
			//if (eval >= distribution.getLowerBound() && eval <= distribution.getUpperBound()) {
				eval = searcher.pv_search(mediator, null, info, ISearch.PLY * maxdepth, ISearch.PLY * maxdepth,
					0, beta - 1, beta, 0, 0, prevPV, false, 0, searcher.getEnv().getBitboard().getColourToMove(), 0, 0, false, 0, useMateDistancePrunning);
			}
			
			
			//distribution.writeLock();
			//unlock = true;
			
			//distribution.addNodes(info.getSearchedNodes());
			
			if (maxdepth != info.getDepth()) {
				//distribution.writeUnlock();
				throw new IllegalStateException("maxdepth=" + maxdepth + " info.getDepth()=" + info.getDepth());
			}
			
			
			/**
			 * Critical section for distribution, be careful with the usage of other locking methods inside
			 */
			if (maxdepth == distribution.getCurrentDepth()) {
				
				pv_buffer.clear();
				
				if (eval >= beta) {
					//eval is lower bound
					
					//int[] pv = getPVfromTPT();
					int[] pv = PVNode.convertPV(searcher.getPvman().load(0), pv_buffer);
					//validatePV(pv);
					info.setPV(pv);
					if (info.getPV().length > 0) {
						info.setBestMove(info.getPV()[0]);
					}
					info.setEval(eval);
					info.setLowerBound(true);
					
					if (DEBUGSearch.DEBUG_MODE) mediator.dump(Thread.currentThread().getName() + ":	stop search (increaseLowerBound) with eval " + eval);
					
					distribution.increaseLowerBound(eval, info);
					
				} else {
					//eval is upper bound
					//eval < beta <=> eval <= beta - 1 <=> eval <= alpha
					
					//int[] pv = getPVfromTPT();
					int[] pv = PVNode.convertPV(searcher.getPvman().load(0), pv_buffer);
					//validatePV(pv);
					info.setPV(pv);
					if (info.getPV().length > 0) {
						info.setBestMove(info.getPV()[0]);
					}
					info.setEval(eval);
					info.setUpperBound(true);
					
					if (DEBUGSearch.DEBUG_MODE) mediator.dump(Thread.currentThread().getName() + ":	stop search (decreaseUpperBound) with eval " + eval);
					
					distribution.decreaseUpperBound(eval, info);
				}
			} else if (maxdepth > distribution.getCurrentDepth()) {
				throw new IllegalStateException("maxdepth=" + maxdepth + " distribution.getMaxdepth()=" + distribution.getCurrentDepth());
			} else {
				if (DEBUGSearch.DEBUG_MODE) mediator.dump(Thread.currentThread().getName() + ":	stop search -> depth is less");
				//System.out.println("PINKO");
				//Do nothing
			}
			
		} catch(Throwable t) {
			if (t instanceof SearchInterruptedException) {
				//distribution.addNodes(info.getSearchedNodes());	
			} else {
				ChannelManager.getChannel().dump(t);
			}
		} finally {
			//if (searcher != null) searchers.releaseSearcher(searcher);
			//if (unlock) distribution.writeUnlock();
		}
	}
	
	
	private int[] getPVfromTPT() {
		
		List<Integer> moves = new ArrayList<Integer>();
		
		TPTEntry entry = null;
		while ((entry = searcher.getEnv().getTPT().get(bitboard.getHashKey())) != null) {
			
			int move = entry.getBestMove_lower();
			if (move == 0) {
				move = entry.getBestMove_upper();
			}
			
			bitboard.makeMoveForward(move);
			
			moves.add(move);
			
			if (bitboard.getStateRepetition() >= 3) {
				break;
			}
		}
		
		for (int i = moves.size() - 1; i >= 0; i--) {
			bitboard.makeMoveBackward(moves.get(i));
		}
		
		int[] result = new int[moves.size()];
		for (int i=0; i<moves.size(); i++) {
			result[i] = moves.get(i);
		}
		
		return result;
	}
	
	
	private void validatePV(int[] pv) {
		
		TPTEntry entry = searcher.getEnv().getTPT().get(bitboard.getHashKey());
		if (entry != null) {
			if (pv != null && pv.length > 0) {
				if (pv[0] != entry.getBestMove_lower() && pv[0] != entry.getBestMove_upper()) {
					mediator.dump(Thread.currentThread().getName() + ":	validatePV -> diff moves "
								+ MoveInt.moveToString(pv[0]) + " "
								+ MoveInt.moveToString(entry.getBestMove_lower()) + " "
								+ MoveInt.moveToString(entry.getBestMove_upper()) + " ");
				}
			}
		}
	}
}
