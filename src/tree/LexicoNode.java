package tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LexicoNode {

	private char letter;
	private int order; // number of the node in the tree (number of the letter to make the word)
	private boolean isFinal = false;
	private LexicoNode[] children = null;

	/**
	 * contructor of the root node
	 */
	public LexicoNode() {
		this.order = 0;
	}

	/*
	 * PUBLIC METHODS
	 */
	
	/**
	 * constructor of a normal node
	 * @param letters
	 * @param order
	 */
	public LexicoNode(char[] letters, int order) {
		this.letter = letters[order - 1];
		this.order = order;
		continueWord(letters);
	}

	/**
	 * Method used to add words in the Structure.
	 * Supposed to be used on root only
	 * @param letters
	 */
	public void addWord(char[] letters) {
		if (this.order == 0) {
			continueWord(letters);
		}
	}


	
	/**
	 * look within the object structure if the given char Array if a existing sequence 
	 * within the object and if the last letter is a terminal letter
	 * @param letters
	 * @return
	 */
	public boolean containsWord(char[] letters) {
		if (this.order < letters.length) {
			var node = findNode(letters[this.order]);
			if (node == null) {
				return false;
			} else {
				return node.containsWord(letters);
			}
		} else {
			return this.order == letters.length && this.isFinal;
		}
	}

	/**
	 * retrieve all word of a given size an return them in a list
	 * this algorithm uses recursive implementation
	 * @param size
	 * @return
	 */
	public List<String> getWordSize(int size) {
		var temp = new ArrayList<String>();
		if (children == null || size == 0) {
			return temp;
		}
		if (size == this.order + 1) {
			return getLastNodeLetter();
		}
		for (LexicoNode child : children) {
			var childTemp = child.getWordSize(size);
			for (String word : childTemp) {
				if(this.order==0) {
					temp.add(word);
				}else {
					temp.add(this.letter + word);
				}
				
			}

		}
		return temp;

	}
	/**
	 * return weather the given char[] is a sequence contained in the object
	 * @param letters
	 * @return
	 */
	public boolean isAPossibleWord(char[] letters) {
		if (this.order < letters.length) {
			var node = findNode(letters[this.order]);
			if (node == null) {
				return false;
			} else {
				return node.isAPossibleWord(letters);
			}
		} 
		return true;
	}
	
	/**
	 * return all words that start with a given char[]
	 * @param prefix
	 * @return
	 */
    public List<String> getWordsOfPrefix(char[] prefix){
        if(prefix.length==0){
            return findAllChild();
        }
        LexicoNode finalNode = this;
        for(char letter : prefix){
            finalNode = finalNode.findNode(letter);
            if(finalNode==null){
                return new ArrayList<>();
            }
        }
        var temp = new ArrayList<String>();
        var list = finalNode.findAllChild();
        String editedPrefix = String.valueOf(prefix);
        if(finalNode.isFinal) {
        	editedPrefix = String.valueOf(prefix).substring(0,prefix.length-1);
        }
        for (var str : list){
            temp.add(editedPrefix+str);
        }
        
        return temp;
        //Final node here is the last node of the prefix
    }
    
	
	/*
	 * PRIVATE METHODS
	 */
	
	
	/**
	 * if is the end of the char[] make the node as a terminal node
	 * else make the next node and add it to it's children
	 * @param letters
	 */
	private void continueWord(char[] letters) {
		if (letters.length == this.order) {
			this.isFinal = true;
		} else {
			makeNextNode(letters);
		}
	}
	/**
	 * if it is the first child that we want to add
	 * create the array , create the node and add it to the array (the node create has a order +=1 than the node that created it)
	 * 
	 * else check if the node already exist in the child and add it if not
	 * @param letters
	 */
	private void makeNextNode(char[] letters) {
		var nextOrder = getNextGoodOrder(letters);
		if(nextOrder == -1) {
			this.isFinal=true;
			return;
		}
		if (children == null) {
			children = new LexicoNode[] { new LexicoNode(letters, nextOrder + 1) };
		} else {
			findIfNodeExist(letters); // TODO sort to test
		}
	}

	/**
	 * method used to find the next char index within 'a' and 'z' or equals to '-' or '\''
	 * @param letters
	 * @return
	 */
	private int getNextGoodOrder(char[] letters) {
		int nextIndex = this.order;
		while (nextIndex < letters.length && !((letters[nextIndex] >= 'a') && (letters[nextIndex] <= 'z')) && letters[nextIndex] != '\'' && letters[nextIndex] != '-') {
		    nextIndex++;
		}
		if (nextIndex < letters.length) {
		    return nextIndex;
		    // Perform desired operations with the nextChar
		} 
		return -1;
	}

	/**
	 * find if the node is already a children and if not create it and add it to children
	 * then take it and call methods to continue the word
	 * @param letters
	 */
	private void findIfNodeExist(char[] letters) {
		var node = findNode(letters[this.order]);
		if (node == null) {
			children = Arrays.copyOf(children, children.length + 1);
			children[children.length - 1] = new LexicoNode(letters, this.order + 1);
		} else {
			node.continueWord(letters);
		}
	}

	/**
	 * look for a node with the given letter in it's attribute and return it
	 * @param letter
	 * @return node if found else null
	 */
	private LexicoNode findNode(char letter) {
		if (children == null) {
			return null;
		}
		for (LexicoNode child : children) {
			if (child.letter == letter) {
				return child;
			}
		}
		return null;
	}

	/**
	 * retrieve all words contained in the lexicographicTree
	 *  with a recursive method
	 * @return all words stored in the object
	 */
    private List<String> findAllChild(){
        var temp = new ArrayList<String>();
        if(this.isFinal) {
        	temp.add(""+this.letter);
        }
        if(children==null) {
        	return temp;
        }
   
        
        for (LexicoNode child : children) {
            var childTemp = child.findAllChild();
            for(String word : childTemp){
            	if(this.order==0) {
					temp.add(word);
				}else {
					temp.add(this.letter + word);
				}
            }

        }
        return temp;
    }

	/**
	 * return the list of all child that are final as a string this.letter+child.letter
	 * 
	 * @return return the list of all child's letter that are final as a string this.letter+child.letter
	 */
	private List<String> getLastNodeLetter() {
		var temp = new ArrayList<String>();
		if (children != null) {
			for (LexicoNode child : children) {
				if (child.isFinal) {
					temp.add(""+this.letter + child.letter);
				}
			}
		}
		return temp;
	}

}
