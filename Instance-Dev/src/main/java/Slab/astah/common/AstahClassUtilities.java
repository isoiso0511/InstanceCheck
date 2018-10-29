/**
 * ClassData.java
 * Created at  2011/12/07
 */
package Slab.astah.common;

/**
 * 
 * @author Kotaro Nozawa
 */

import java.util.ArrayList;
import java.util.List;

import com.change_vision.jude.api.inf.model.IAssociation;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IGeneralization;
import com.change_vision.jude.api.inf.presentation.IPresentation;

public class AstahClassUtilities {

	private AstahClassUtilities() {

	}

	// 引数のクラスについてる関連のリストを取得するメソッド
	public static List<IAssociation> getAssociations(IClass clazz) {
		List<IAssociation> associations = new ArrayList<IAssociation>();
		for (IAttribute attribute : clazz.getAttributes()) {
			if (attribute.getAssociation() != null) {
				associations.add(attribute.getAssociation());
			}
		}
		return associations;
	}

	// 引数のクラスのサブクラスのリストを取得するメソッド
	public static List<IClass> getSubClasses(IClass clazz) {
		List<IClass> subclasses = new ArrayList<IClass>();
		for (IGeneralization generalization : clazz.getSpecializations()) {
			subclasses.add(generalization.getSubType());
		}
		return subclasses;
	}

	// 引数のクラスを継承したすべてのサブクラスを取得するメソッド
	public static List<IClass> getAllSubClasses(IClass clazz,
			List<IClass> subclassesList) {
		for (IGeneralization generalization : clazz.getSpecializations()) {
			subclassesList.add(generalization.getSubType());
			if (generalization.getSubType().getSpecializations().length != 0) {
				getAllSubClasses(generalization.getSubType(), subclassesList);
			}
		}
		return subclassesList;
	}

	// 引数のクラスのトップにくるスーパークラスを取得するメソッド
	public static IClass getSuperClass(IClass clazz) {
		IClass superclass = null;
		if (clazz.getGeneralizations().length != 0) {
			superclass = clazz.getGeneralizations()[0].getSuperType();
		}
		return superclass;
	}

	@SuppressWarnings("unchecked")
	// 指定した図から指定した要素のリストを取得するメソッド
	public static <T> List<T> getAllElements(IClassDiagram diagram,
			Class<T> clazz) {
		try {
			List<T> elements = new ArrayList<T>();
			for (IPresentation presentation : diagram.getPresentations()) {
				IElement element = presentation.getModel();
				if (clazz.isInstance(element)) {
					elements.add((T) element);
				}
			}
			return elements;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	// // 属性の型を取得するメソッド
	// public static List<IClass> getAttribute(IClass clazz) {
	// List<IClass> attributes = new ArrayList<IClass>();
	// for (IAttribute attribute : clazz.getAttributes()) {
	// attributes.add(attribute.getType());
	// }
	//
	// return attributes;
	// }

}
