/**
 * CheckResult.java
 * Created at  2011/12/08
 */
package Slab.astah.nzwPkg;

/**
 * 
 * @author Kotaro Nozawa
 */
public class CheckResult {

	private final String text;

	public CheckResult(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return this.text;
	}

}
