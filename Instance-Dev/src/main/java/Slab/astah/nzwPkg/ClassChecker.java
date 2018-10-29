/**
 * InstanceCheck.java
 * Created at  2011/12/08
 */
package Slab.astah.nzwPkg;

import java.util.ArrayList;
import java.util.List;

import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.change_vision.jude.api.inf.presentation.IPresentation;

/**
 * 
 * @author Kotaro Nozawa
 */
public class ClassChecker {

	private static final boolean DEBUG = false;
	private IClassDiagram classDiagram;
	private IClassDiagram instanceDiagram;

	ClassChecker() {

	}

	public ClassChecker(IClassDiagram classDiagram,
			IClassDiagram instanceDiagram) {
		this.classDiagram = classDiagram;
		this.instanceDiagram = instanceDiagram;
	}

	public List<CheckResult> doCheck() {
		List<CheckResult> results = new ArrayList<CheckResult>();
		try {
			int memberOfInstance = 0;
			results.add(new CheckResult("ClassChecker:"
					+ instanceDiagram.getName() + "との比較"));
			for (IPresentation presentation : instanceDiagram
					.getPresentations()) {
				IElement iDElement = presentation.getModel();
				if (iDElement instanceof IInstanceSpecification) {
					IInstanceSpecification instance = (IInstanceSpecification) iDElement;
					if (DEBUG) {
						System.out.println(instance.getName() + ":"
								+ instance.getClassifier());
					}
					if (instance.getClassifier() == null) {
						results.add(new CheckResult("warning:インスタンス["
								+ instance.getName() + "]の型となるクラスが指定されていません"));
					} else {
						int INameFalseCount = 0;
						memberOfInstance++;
						for (IPresentation pres : classDiagram
								.getPresentations()) {
							IElement iCElement = pres.getModel();
							if (DEBUG) {
								System.out.println("iCElement=" + iCElement);
							}
							if (iCElement instanceof IClass) {
								IClass clazz = (IClass) iCElement;
								if (instance.getClassifier().equals(clazz)) {
									// 一致したクラスが抽象クラスだったら
									if (clazz.isAbstract()) {
										results.add(new CheckResult(clazz
												.getName() + "は抽象クラスです"));
									}

								} else {
									INameFalseCount++;
								}

							}
						}
						if (INameFalseCount == getNumberOfClass(classDiagram)) {
							results.add(new CheckResult("Error:インスタンス["
									+ instance.getName() + "]のクラス["
									+ instance.getClassifier().getName() + "]は"
									+ classDiagram.getName() + "に存在しません！"));
						}

					}

				}
			}
			if (memberOfInstance == 0) {
				results.add(new CheckResult(instanceDiagram.getName()
						+ "にはインスタンスが存在しません"));
			}
		} catch (InvalidUsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return results;
	}

	// クラス図にあるクラスの数を取得するメソッド
	private int getNumberOfClass(IClassDiagram diagram)
			throws InvalidUsingException {
		List<IClass> clazz = new ArrayList<IClass>();
		for (IPresentation presentation : diagram.getPresentations()) {
			IElement element = presentation.getModel();
			if (element instanceof IClass) {
				clazz.add((IClass) element);
			}
		}

		return clazz.size();
	}

}
