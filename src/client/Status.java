package client;

/*Through Status Class achieving Status Pattern.
 * Encapsule Status of people.
 * Player change image and orientation of bullet when Status change
 */

public class Status {
	private char orient='s';
	public void setOrient(char orient) {
		this.orient=orient;
	}
	public char getOrient() {
		return this.orient;
	}
}
