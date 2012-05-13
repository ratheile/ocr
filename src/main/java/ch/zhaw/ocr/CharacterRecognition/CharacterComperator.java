package ch.zhaw.ocr.CharacterRecognition;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import ch.zhaw.ocr.BitmapParser.BitmapParser;
import ch.zhaw.ocr.BitmapParser.CharacterParser;
import ch.zhaw.ocr.BitmapParser.ContrastMatrix;
import ch.zhaw.ocr.BitmapParser.RowParser;
import ch.zhaw.ocr.BitmapParser.SimpleBitmapParser;
import ch.zhaw.ocr.BitmapParser.UnderlineRemover;
import ch.zhaw.ocr.BitmapParser.WordParser;
import ch.zhaw.ocr.Dictionary.Dictionary;
import ch.zhaw.ocr.NeuronalNetwork.Neuron;
import ch.zhaw.ocr.NeuronalNetwork.NeuronalNetwork;

public class CharacterComperator {
	private NeuronalNetwork<Character, String> characterRecognitionNetwork;
	private Dictionary dict;
	
	public CharacterComperator(Dictionary dict) {
		this.dict = dict;
		characterRecognitionNetwork = new NeuronalNetwork<Character, String>();
	}

	public void learn(List<Character> input, List<String> chars, double emphasis) {
		if (input.size() != chars.size()) {
			throw new IllegalArgumentException(
					"List dimensions must agree! input: " + input.size()
							+ ", chars: " + chars.size());
		}

		for (int i = 0; i < input.size(); i++) {
			characterRecognitionNetwork.addNeuron(input.get(i), chars.get(i),
					emphasis);
		}
	}

	public String detectCharacter(Character c) {
		if (c.getMatrix().getFunctionalChar() != null) {
			return c.getMatrix().getFunctionalChar().toString();
		} else {

			// System.out.println(c.getMatrix());

			double highestEmph = 0;
			String rv = "";
			Set<Neuron<Character, String>> subset = characterRecognitionNetwork
					.getNeuronsFromSource(c);
			if (!subset.isEmpty()) {
				for (Neuron<Character, String> n : subset) {
					// System.out.println(n.getTarget() + " - " +
					// n.getEmphasis());
					if (n.getEmphasis() > highestEmph) {
						rv = n.getTarget();
						highestEmph = n.getEmphasis();
					}
				}
			} else {
				// System.out.println("---");
				// System.out.println(c.getMatrix());
				// for(int i = 0;i < c.getComparisonVector().length;i++){
				// System.out.println("["+i+"]: " + c.getComparisonVector()[i]);
				// }
				//
				//
				// System.out.println("not found");
				// Set<Neuron<Character, String>> nset =
				// characterRecognitionNetwork.getNeuronsFromTarget("i");
				// for(Neuron<Character, String> n : nset){
				// for(int i = 0;i <
				// n.getSource().getComparisonVector().length;i++){
				// System.out.println("["+i+"]: " +
				// n.getSource().getComparisonVector()[i]);
				// }
				// System.out.println(n.getSource().getMatrix());
				// }

			}
			return rv;
		}
	}
}
