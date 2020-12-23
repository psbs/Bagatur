package bagaturchess.scanner.run;


import bagaturchess.scanner.visitors.ScannerLearningVisitor_V1;
import bagaturchess.scanner.visitors.ScannerLearningVisitor_V2;
import bagaturchess.scanner.visitors.ScannerLearningVisitor_V3;
import bagaturchess.ucitracker.api.PositionsTraverser;
import bagaturchess.ucitracker.api.PositionsVisitor;


public class ScannerLearningMain {
	
	public static void main(String[] args) {
		
		long startTime = System.currentTimeMillis();
		try {
			
			String filePath = "./stockfish-12.cg";
			
			PositionsVisitor visitor = new ScannerLearningVisitor_V3();
			
			System.out.println("Reading games ... ");
			while (true) {
				PositionsTraverser.traverseAll(filePath, visitor, 999999999, null, null);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		System.out.println("OK " + ((endTime - startTime) / 1000) + "sec");		
	}
}