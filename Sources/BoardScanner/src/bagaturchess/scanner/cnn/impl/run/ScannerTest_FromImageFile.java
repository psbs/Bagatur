/**
 *  BagaturChess (UCI chess engine and tools)
 *  Copyright (C) 2005 Krasimir I. Topchiyski (k_topchiyski@yahoo.com)
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
 *  along with BagaturChess. If not, see http://www.eclipse.org/legal/epl-v10.html
 *
 */
package bagaturchess.scanner.cnn.impl.run;


import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import bagaturchess.scanner.cnn.impl.BoardScanner;
import bagaturchess.scanner.cnn.impl.BoardScanner_RGB;
import bagaturchess.scanner.cnn.impl.model.NetworkModel;
import bagaturchess.scanner.cnn.impl.model.NetworkModel_RGB;
import bagaturchess.scanner.cnn.impl.utils.ScannerUtils;
import bagaturchess.scanner.common.BoardProperties;


public class ScannerTest_FromImageFile {
	
	
	private static final String NET_FILE = "scanner.bin";
	
	
	public static void main(String[] args) {
		
		try {
			
			BoardProperties boardProperties = new BoardProperties(192);
			
			NetworkModel netmodel = new NetworkModel_RGB(NET_FILE, boardProperties);
			
			BufferedImage boardImage = ImageIO.read(new File("./data/tests/lichess.org/test1.png"));
			//BufferedImage boardImage = ImageIO.read(new File("./data/tests/test7.png"));
			boardImage = ScannerUtils.resizeImage(boardImage, boardProperties.getImageSize());
			
			BoardScanner scanner = new BoardScanner_RGB(netmodel);
			
			String fen = scanner.scan(ScannerUtils.convertToRGBMatrix(boardImage));
			
			System.out.println(fen);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
