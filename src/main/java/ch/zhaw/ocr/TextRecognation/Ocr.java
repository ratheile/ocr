package ch.zhaw.ocr.TextRecognation;

import hu.kazocsaba.math.matrix.MatrixFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import ch.zhaw.ocr.Properties;
import ch.zhaw.ocr.BitmapParser.BitmapParser;
import ch.zhaw.ocr.BitmapParser.CharacterParser;
import ch.zhaw.ocr.BitmapParser.ContrastMatrix;
import ch.zhaw.ocr.BitmapParser.RowParser;
import ch.zhaw.ocr.BitmapParser.SimpleBitmapParser;
import ch.zhaw.ocr.BitmapParser.UnderlineRemover;
import ch.zhaw.ocr.BitmapParser.WordParser;
import ch.zhaw.ocr.CharacterRecognition.Character;
import ch.zhaw.ocr.CharacterRecognition.CharacterComperator;
import ch.zhaw.ocr.CharacterRecognition.CharacterRepresentation;
import ch.zhaw.ocr.Dictionary.Dictionary;
import ch.zhaw.ocr.knn.NeuralNetwork;
import ch.zhaw.ocr.knn.helper.MatrixHelper;

public class Ocr {
	
	private NeuralNetwork nn;
	private Dictionary dic;
	
	public Ocr(NeuralNetwork nn, Dictionary dic) {
		this.nn = nn;
		this.dic = dic;
	}
	
	public String parseImage(File f) {
		long t1 = System.currentTimeMillis();
		
		StringBuffer textBuffer = new StringBuffer();
		StringBuffer wordBuffer = new StringBuffer();

		// parse bitmap
		BitmapParser bp = new CharacterParser(new WordParser(
				new UnderlineRemover(new RowParser(new SimpleBitmapParser()))));

		try {
			//Word seperating
			List<ContrastMatrix> matrices = bp.parse(ImageIO.read(f));
			// character output
			for (ContrastMatrix cm : matrices) {
				char c = 0;
				if(cm.getFunctionalChar() != null){
					c = cm.getFunctionalChar().getCharacter();
				}else{
					c = nn.detectCharacter(MatrixFactory.createMatrix(new CharacterRepresentation(cm).getComparisonVector()));
				}
				//if the character is a functional character
				if (cm.getFunctionalChar() != null) {
					System.out.println("Dictionary Input: "+wordBuffer.toString());
					System.out.println("Dictionary Output: "+dic.correctWord(wordBuffer.toString()));
					textBuffer.append(dic.correctWord(wordBuffer.toString()));
					textBuffer.append(c);
					wordBuffer.delete(0, wordBuffer.length());
				} else {
					wordBuffer.append(c);
				}
				
			}
			//check last word in dictionary
			if (wordBuffer.length() > 0) {
				textBuffer.append(dic.correctWord(wordBuffer.toString()));
			}
			System.out.println("Einlesen des Textes abgeschlossen...("+ (System.currentTimeMillis()-t1) +"ms)");
			return textBuffer.toString().replace(Properties.unknownChar, '_');
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public Dictionary getDictionary() {
		return dic;
	}

}
