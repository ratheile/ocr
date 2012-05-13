package ch.zhaw.ocr.Dictionary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.zhaw.ocr.Property;

public class Dictionary {
	// HashMap representing the dictionary
	// Key: Word (String)
	// Value: Emphasis (Integer)
	private HashMap<String, Integer> dictionary;
	private final char[] alphabet = new String("abcdefghijklmnopqrstuvwxyzäöü")
			.toCharArray();

	/**
	 * 
	 * @param dictFile
	 * @throws IOException
	 */
	public Dictionary(File dictFolder) throws IOException {
		dictionary = new HashMap<String, Integer>();
		buildDictionaryFromFile(dictFolder);
	}

	/**
	 * Try to deserialize the map from our configured filepath. If no serialized
	 * map is available, create an empty one
	 */
	public Dictionary() {
		File f = new File(Property.dictionaryMapSerializiationPath);
		if (f.exists()) {
			try {
				buildDictionaryFromSerializedMap(f);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			dictionary = new HashMap<String, Integer>();
		}
	}

	/**
	 * This method
	 * 
	 * @param word
	 * @return
	 */
	public String correctWord(String word) {
		String rv = null;

		char firstSignChar = 0;
		char lastSignChar = 0;

		/*
		 * Step 1: Pre-processing: Remove "special" chars at start / end. Remove
		 * capital letters
		 */
		// Check if first character is != digit or letter. If first char !=
		// digit or letter => remove character and store it
		if (!Character.isLetterOrDigit(word.charAt(0))
				&& word.charAt(0) != Property.unknownChar) {
			firstSignChar = word.charAt(0);
			word = word.substring(1);
		}

		// Check if last character is != digit or letter. If last char != digit
		// or letter => remove character and store it
		if (!Character.isLetterOrDigit(word.charAt(word.length() - 1))
				&& word.charAt(word.length() - 1) != Property.unknownChar) {
			lastSignChar = word.charAt(word.length() - 1);
			word = word.substring(word.length() - 2);
		}

		// Check if word has a capital letter. If => write a flag and remove
		// capital letter
		boolean hasCapitalLetter = Character.isUpperCase(word.charAt(0));
		word = word.toLowerCase();

		/*
		 * Step 2: Find word in dictionary / correct it if necessary
		 */
		// is the given word in our dictionary?
		// if yes return the word
		if (dictionary.containsKey(word)) {
			// adjust probability in dictionary
			dictionary.put(word, dictionary.get(word) + 1);
			return restoreOriginalWordSetting(word, hasCapitalLetter,
					firstSignChar, lastSignChar);
		}

		// build a list of all words with replaced unkown char
		// search these words
		Set<String> unknownCharReplaced = replaceUnknownChar(word);
		Set<String> unkownCharReplacedInDict = findDictionaryEntries(unknownCharReplaced);
		rv = getBestWord(unkownCharReplacedInDict);
		if (rv != null) {
			return restoreOriginalWordSetting(rv, hasCapitalLetter,
					firstSignChar, lastSignChar);
		}

		// build a list of all words with distance 1
		// search these words
		Set<String> distance1 = editDistance1(unknownCharReplaced);
		Set<String> distance1InDict = findDictionaryEntries(distance1);

		rv = getBestWord(distance1InDict);
		if (rv != null) {
			return restoreOriginalWordSetting(rv, hasCapitalLetter,
					firstSignChar, lastSignChar);
		}

		// build a list of all words with distance 2
		// search these words
		Set<String> distance2 = editDistance2(distance1);
		Set<String> distance2InDict = findDictionaryEntries(distance2);

		rv = getBestWord(distance2InDict);
		if (rv != null) {
			return restoreOriginalWordSetting(rv, hasCapitalLetter,
					firstSignChar, lastSignChar);
		}

		// if no correction was found, just return the input word
		return restoreOriginalWordSetting(word, hasCapitalLetter,
				firstSignChar, lastSignChar);
	}

	private String getBestWord(Set<String> wordList) {
		int max = 0;
		String bestWord = null;

		for (String c : wordList) {
			if (dictionary.get(c) > max) {
				max = dictionary.get(c);
				bestWord = c;
			}
		}

		return bestWord;
	}

	/**
	 * 
	 * @param input
	 * @param hasCapitalLetter
	 * @param firstSignChar
	 * @param lastSignChar
	 * @return
	 */
	private String restoreOriginalWordSetting(String input,
			boolean hasCapitalLetter, char firstSignChar, char lastSignChar) {
		/*
		 * Step 3: Post-processing. Add capital letter / special characters at
		 * start / end if needed
		 */
		if (hasCapitalLetter) {
			input = Character.toUpperCase(input.charAt(0)) + input.substring(1);
		}
		if (firstSignChar != 0) {
			input = firstSignChar + input;
		}
		if (lastSignChar != 0) {
			input = input + lastSignChar;
		}
		return input;
	}

	private Set<String> replaceUnknownChar(String word) {
		Set<String> rv = new HashSet<String>();

		char[] wArray = word.toCharArray();

		// replace each character of the word with each character of our given
		// alphabet
		for (int i = 0; i < wArray.length; i++) {
			if (wArray[i] == Property.unknownChar) {
				for (char c : alphabet) {
					char[] copy = wArray.clone();
					copy[i] = c;
					rv.add(String.valueOf(copy));
				}
			}
		}

		if (rv.isEmpty()) {
			rv.add(word);
		}

		return rv;
	}

	private Set<String> editDistance1(Set<String> unknownCharReplaced) {
		Set<String> rv = new HashSet<String>();

		for (String word : unknownCharReplaced) {
			char[] wArray = word.toCharArray();

			// replace each character of the word with each character of our
			// given alphabet
			for (int i = 0; i < wArray.length; i++) {
				for (char c : alphabet) {
					char[] copy = wArray.clone();
					copy[i] = c;
					rv.add(String.valueOf(copy));
				}
			}
		}

		return rv;
	}

	/**
	 * Builds a list of
	 * 
	 * @param distance1
	 * @return
	 */
	private Set<String> editDistance2(Set<String> distance1) {
		return editDistance1(distance1);
	}

	/**
	 * Searches the dictionary for all words in the input set. Return a set of
	 * words contained in the dictionary.
	 * 
	 * @param searchList
	 *            list of word variants
	 * @return subset of the input set. Contains only words which are in the
	 *         "dictionary" set
	 */
	private Set<String> findDictionaryEntries(Set<String> searchList) {
		Set<String> rv = new HashSet<String>();

		for (String word : searchList) {
			if (dictionary.containsKey(word)) {
				rv.add(word);
			}
		}

		return rv;
	}

	/**
	 * Adds a given list of words to the dictionary
	 * 
	 * @param input
	 *            list of words to be added
	 */
	public void addToDicitionary(List<String> input) {
		for (String word : input) {
			word = word.toLowerCase();
			// word = word.toLowerCase();
			if (dictionary.containsKey(word)) {
				dictionary.put(word, dictionary.get(word) + 1);
			} else {
				dictionary.put(word, 1);
			}
		}
	}

	/**
	 * Builds a dictionary using all textfiles in a directory
	 * 
	 * @param folder
	 *            a "file" representing a folder which contains textfiles to add
	 * @throws IOException
	 */
	private void buildDictionaryFromFile(File folder) throws IOException {
		System.out.println("Build dictionary from resource folder...");
		long t1 = System.currentTimeMillis();
		TextFileParser tfp = new TextFileParser();

		String textfileContent = "";

		for (File f : folder.listFiles()) {
			textfileContent = tfp.parseFile(f);
			addToDicitionary(Arrays.asList(textfileContent.split(" ")));
		}
		System.out.println("Dictionary built... ("
				+ (System.currentTimeMillis() - t1) + "ms)");
	}

	public void serializeMap() {
		System.out.println("Serialize map ("+dictionary.size()+" entries...");
		
		long t1 = System.currentTimeMillis();
		File f = new File(Property.dictionaryMapSerializiationPath);

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(f), 100*1024);

			for (String key : dictionary.keySet()) {
				bw.write(key + "," + dictionary.get(key) + ";");
			}
			
			bw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Dictionary serialized... ("
				+ (System.currentTimeMillis() - t1) + "ms)");
	}

	@SuppressWarnings("unchecked")
	public void buildDictionaryFromSerializedMap(File serializedMap)
			throws IOException {
		System.out.println("Build dictionary from serialized map...");
		dictionary = new HashMap<String, Integer>();

		long t1 = System.currentTimeMillis();

		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(serializedMap));
			String line = null; // not declared within while loop
			while ((line = input.readLine()) != null) {
				for (String entry : line.split(";")) {
					if (entry != null && !entry.trim().equals("")) {
						String[] pair = entry.split(",");
						dictionary.put(pair[0], Integer.parseInt(pair[1]));
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			input.close();
		}

		System.out.println("Dictionary built... ("
				+ (System.currentTimeMillis() - t1) + "ms; "+dictionary.size()+" entries)");
	}

}
