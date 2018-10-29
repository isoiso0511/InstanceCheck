/**
 * Relation.java
 * Created at  2011/12/13
 */
package Slab.astah.nzwPkg;

import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IInstanceSpecification;

/**
 * 
 * @author Kotaro Nozawa
 */
public class Relation {
	public final IClass source;
	public final IClass target;
	public int determinedLowerBound;
	public int determinedUpperBound;
	public int lowerBound = 0;
	public int upperBound = 0;
	public int totalLowerBound = 0;
	public int totalUpperBound = 0;
	public int connectionCount = 0;
	public IInstanceSpecification sourceInstance = null;
	public boolean exsistingLink = false;
	public boolean isMultiplicityError = true;

	public Relation(IClass source, IClass target, int lowerbound, int upperbound) {
		this.source = source;
		this.target = target;
		this.determinedLowerBound = lowerbound;
		this.determinedUpperBound = upperbound;
	}

	/**
	 * 
	 */

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Relation) {
			Relation relation = (Relation) obj;
			if (this.source.equals(relation.source)
					&& this.target.equals(relation.target)
					&& this.determinedLowerBound == relation.determinedLowerBound
					&& this.determinedUpperBound == relation.determinedUpperBound) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
