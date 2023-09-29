package boggle;
/**
 * class used to store some data to make the boggle
 * @author guill
 *
 */
public class Node {
	private char letter;
	private boolean isVisited = false;
	
	public Node(char letter) {
		this.letter=letter;
	}

	public char getLetter() {
		return letter;
	}

	public void setLetter(char letter) {
		this.letter = letter;
	}

	public boolean isVisited() {
		return isVisited;
	}

	public void setVisited(boolean isVisited) {
		this.isVisited = isVisited;
	}
	
	
}
