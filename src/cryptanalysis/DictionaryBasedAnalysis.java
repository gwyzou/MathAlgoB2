package cryptanalysis;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import tree.LexicographicTree;

public class DictionaryBasedAnalysis {
	
	private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String DICTIONARY = "mots/dictionnaire_FR_sans_accents.txt";
	
	private static final String CRYPTOGRAM_FILE = "txt/Plus fort que Sherlock Holmes (cryptogram).txt";
	private static final String DECODING_ALPHABET = "VNSTBIQLWOZUEJMRYGCPDKHXAF"; // Sherlock

	private LexicographicTree dict;
	private String cryptogram;
	private List<String> words;
	private Set<String> founds;
	
	private Comparator<String> stringLengthComparator = new Comparator<String>()
    {
        @Override
        public int compare(String o1, String o2)
        {
            return Integer.compare(o2.length(), o1.length());
        }
    };
	/*
	 * CONSTRUCTOR
	 */
	public DictionaryBasedAnalysis(String cryptogram, LexicographicTree dict) {
		if(cryptogram == null || dict == null) {
			throw new NullPointerException("Both params need not to be null");
		}
		if(cryptogram.isBlank()) {
			throw new IllegalArgumentException("Nothing to treat");
		}
		this.dict = dict;
		this.cryptogram = cryptogram;	
		this.words= getHiddenList();
		this.founds = new HashSet<>();
;
	}
	
	/*
	 * PUBLIC METHODS
	 */

	/**
	 * Performs a dictionary-based analysis of the cryptogram and returns an approximated decoding alphabet.
	 * @param alphabet The decoding alphabet from which the analysis starts
	 * @return The decoding alphabet at the end of the analysis process
	 */
	public String guessApproximatedAlphabet(String alphabet) {
		checkAlphabet(alphabet);
		
		String currentAlphabet = alphabet.toUpperCase();
		int previousScore = getStatsAndRemoveFoundsIfBiggerScore(currentAlphabet , 0);
		List<String> possibilities = null;
		//loop over all elements
		for (int i=0 ; i<words.size();i++) {
			String word = words.get(i);
			//go to next if word was found previously
			if( this.founds.contains(word)) {
				continue;
			}
			var positions = analyseWord(word);
			if(!hasDup(positions)) {
				continue;
			}
			possibilities = reloadPossibilities(possibilities, word);
			String found = GuessWord(positions,possibilities );
			if(found!=null) {
				var newAlphabet = getNewAlphabet(currentAlphabet,word,found);
				int newScore = getStatsAndRemoveFoundsIfBiggerScore(newAlphabet , previousScore);
				if(newScore > previousScore) {
					previousScore = newScore;
					currentAlphabet=newAlphabet;
				}
			}
		}
		return currentAlphabet.toUpperCase();
	}
	
	


	/**
	 * Applies an alphabet-specified substitution to a text.
	 * @param text A text
	 * @param alphabet A substitution alphabet
	 * @throws IllegalArgumentException if param are not good
	 * @return The substituted text
	 */
	public static String applySubstitution(String text, String alphabet) {
		checkAlphabet(alphabet);
		if(text == null || text.isBlank()) {
			throw new IllegalArgumentException();
		}
		StringBuilder sb = new StringBuilder(text);
		for(int i=0; i<sb.length();i++) {
			char value = sb.charAt(i);
			if(value>='A' && value<='Z') {
				sb.setCharAt(i, alphabet.charAt(value-'A'));
			}
		}
		return sb.toString();
	}
	
	/*
	 * PRIVATE METHODS
	 */
	
	/**
	 * Check if the given alphabet is valide if not throw illegalArgumentException
	 * @param alphabet
	 */
	private static void checkAlphabet(String alphabet) {
		if(alphabet == null || alphabet.isBlank()) {
			throw new IllegalArgumentException("Alphabet need not to be null nor blank");
		}
		if(alphabet.length() != 26) {
			throw new IllegalArgumentException("need a size of 26");
		}
	}
	
	
	/**
	 * check if the given possibilities is instantiated or if the size of the words is the same as the given word
	 * else reload with wanted word
	 * @param possibilities
	 * @param word
	 * @return
	 */
	private List<String> reloadPossibilities(List<String> possibilities, String word) {
		if(possibilities == null ||(possibilities.size()>0 && possibilities.get(0).length() != word.length())) {
			return dict.getWordsOfLength(word.length());
		}
		return possibilities;
	}
	
	/**
	 *  split the cryptogram given during instantiating the class into each word within the cryptogram
	 * @param cryptogram
	 * @return the list of each unique words within the cryptogram
	 */
	private List<String> getHiddenList() {
		List<String> words = Arrays.asList(cryptogram.split("\\s+"));
		words.sort(stringLengthComparator);
		 Set<String> uniqueWords = new LinkedHashSet<>(words);
		 return new ArrayList<>(uniqueWords);
		
	}
	
	/**
	 * get the new alphabet after changing the one given in param by changing the letter based of cryptedWord and plainWord
	 * @param alphabet
	 * @param cryptedWord
	 * @param plainWord
	 * @return
	 */
	private String getNewAlphabet (String alphabet , String cryptedWord, String plainWord ) {
		String alphabetCopy = new String(alphabet);
		if(cryptedWord.length()== plainWord.length()) {
			String plainUpper =plainWord.toUpperCase();
			StringBuilder alphabetModifier = new StringBuilder(alphabetCopy);
			for(int i=0; i<plainUpper.length();i++) {
				
				changeLetter(alphabetModifier,cryptedWord.charAt(i),plainUpper.charAt(i));
				
			}
			
			return alphabetModifier.toString();
		}
		return null;
	}
	
	/**
	 * swap two letters into the alphabet
	 * @param alphabet
	 * @param cryptedChar
	 * @param plainChar
	 */
	private void changeLetter(StringBuilder alphabet , char cryptedChar, char plainChar) {
		if(plainChar>='A' && plainChar<='Z') {
			int position = cryptedChar - 'A'; 
			char previousLetter = alphabet.charAt(position);
			int previousPosition = alphabet.indexOf(""+plainChar);

			alphabet.setCharAt(previousPosition, previousLetter);
			alphabet.setCharAt(position, plainChar);
		}
		
	}
	
	
	/**
	 * try to find a word with the same structure as @param within the dictionary 
	 * @param word
	 * @return the word found or null if none is found
	 */
	private String GuessWord(Collection< List<Integer>> wordPositions , List<String> poss) {
			var possibilities = poss;
			for(String current : possibilities) {
				if(isAPossibility(current,wordPositions)) {
					return current;
				}
			}
		return null;
	}	
	/**
	 * find if there is at least one list with a size > 1
	 * @param wordPositions
	 * @return
	 */
	private boolean hasDup(Collection< List<Integer>> wordPositions) {
		for(var position : wordPositions) {
			if(position.size()>1) {
				return true;
			}
		}
		return false;
	}
	/**
	 * divide a string into a list containing each char positions as value in a list of integer 
	 * @param word
	 * @return the map
	 */
	private List<List<Integer>> analyseWord(String word) {
	    Map<Character, List<Integer>> charPositions = new HashMap<>();
	    for (int i = 0; i < word.length(); i++) {
	        char value = word.charAt(i);
	        charPositions.computeIfAbsent(value, k -> new ArrayList<>()).add(i);
	    }
	    return new ArrayList<>(charPositions.values());
	}
	
	/**
	 * check if a word have the same structure as the one contained into positions
	 * @param wordOfDict
	 * @param positions
	 * @return if has same structure
	 */
	private boolean isAPossibility(String wordOfDict,Collection< List<Integer>> positions ) {
		for(var current : positions) {
			if(current.size() > 1) {
				if(!checkSameChar(wordOfDict,current)) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Check if each char at the positions contained into positions are the same
	 * @param str
	 * @param positions
	 * @return true if all char are the same else false
	 */
	private boolean checkSameChar(String str, List<Integer> positions) {
		char firstChar = str.charAt(positions.get(0));
	    for (int i = 1; i < positions.size(); i++) {
	        if (str.charAt( positions.get(i)) != firstChar) {
	            return false;
	        }
	    }
	    return true;
	}

	/**
	 * loop over each word of the text, apply the substitution and look if it is a word in this case add it to a collection
	 * at the end of the loop if the size of the collection is bigger than the score provided in the param , 
	 * add to founds all elements of the collection built during the loop and return the biggest score
	 * @param words
	 * @param alphabet
	 * @return number of words found
	 */
	private int getStatsAndRemoveFoundsIfBiggerScore(String alphabet , int previousValue) {
		

		List<String> foundStrings = new ArrayList<>(); // to store the found strings
		foundStrings = words.stream()
		    .filter(s -> dict.containsWord(applySubstitution(s, alphabet).toLowerCase()))
		    .filter(foundStrings::add) // add to the foundStrings list while filtering
		    .collect(Collectors.toList());

		int foundValue = foundStrings.size();
		if (foundValue > previousValue) {
		    previousValue = foundValue;
		    founds.addAll(foundStrings); // remove the found strings from the original list
		    return foundValue;
		}
		return previousValue; //Nothing is removed here
		
		
	}
	/**
	 * Compares two substitution alphabets.
	 * @param a First substitution alphabet
	 * @param b Second substitution alphabet
	 * @return A string where differing positions are indicated with an 'x'
	 */
	private static String compareAlphabets(String a, String b) {
		String result = "";
		for (int i = 0; i < a.length(); i++) {
			result += (a.charAt(i) == b.charAt(i)) ? " " : "x";
		}
		return result;
	}
	
	/**
	 * Load the text file pointed to by pathname into a String.
	 * @param pathname A path to text file.
	 * @param encoding Character set used by the text file.
	 * @return A String containing the text in the file.
	 * @throws IOException
	 */
	private static String readFile(String pathname, Charset encoding) {
		String data = "";
		try {
			data = Files.readString(Paths.get(pathname), encoding);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}
	
    /*
	 * MAIN PROGRAM
	 */
	
	public static void main(String[] args) {
		/*
		 * Load dictionary
		 */
		System.out.print("Loading dictionary... ");
		LexicographicTree dict = new LexicographicTree(DICTIONARY);
		System.out.println("done.");
		System.out.println();
		
		/*
		 * Load cryptogram
		 */
		String cryptogram = readFile(CRYPTOGRAM_FILE, StandardCharsets.UTF_8);
//		System.out.println("*** CRYPTOGRAM ***\n" + cryptogram.substring(0, 100));
//		System.out.println();

		/*
		 *  Decode cryptogram
		 */
		long startDecipherTimer = System.currentTimeMillis();
		DictionaryBasedAnalysis dba = new DictionaryBasedAnalysis(cryptogram, dict);
		
		String startAlphabet = LETTERS;
//		String startAlphabet = "ZISHNFOBMAVQLPEUGWXTDYRJKC"; // Random alphabet
		String finalAlphabet = dba.guessApproximatedAlphabet(startAlphabet);
		
		// Display final results
		System.out.println();
		System.out.println("Decoding     alphabet : " + DECODING_ALPHABET);
		System.out.println("Approximated alphabet : " + finalAlphabet);
		System.out.println("Remaining differences : " + compareAlphabets(DECODING_ALPHABET, finalAlphabet));
		System.out.println();
		
		// Display decoded text
		System.out.println("*** DECODED TEXT ***\n" + applySubstitution(cryptogram, finalAlphabet).substring(0, 200));
		System.out.println();
		
		//Time Value
		long EndDecipherTimer = System.currentTimeMillis();
		System.out.println("Duration : " + (EndDecipherTimer - startDecipherTimer)/1000.0);
	}
}

