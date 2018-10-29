/**
 * InstanceChecker.java
 * create at 2011/11/29
 */
package Slab.astah.nzwPkg;

/**
 * @author cs07071
 *
 */

import static Slab.astah.common.AstahClassUtilities.getAllElements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.project.ProjectAccessorFactory;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;

public class InstanceChecker {
	private static final boolean DEBUG = false;

	/**
	 * @param pane
	 */

	public InstanceChecker() {
		// TODO Auto-generated constructor stub
	}

	// public InstanceChecker(JTextArea textarea) {
	// action(textarea);
	// }

	public List<CheckResult> action() throws Exception {

		ProjectAccessor projectAccessor = ProjectAccessorFactory
				.getProjectAccessor();

		IDiagramViewManager diagramViewManager = projectAccessor
				.getViewManager().getDiagramViewManager();
		IDiagram diagram = diagramViewManager.getCurrentDiagram();
		if (diagram == null) {
			throw new Exception("クラス図，またはインスタンス図が選択されていません");
		}
		IElement container = diagram.getContainer();
		if (!(container instanceof IPackage)) {
			throw new Exception("パッケージ直下の図が選択されていません");
		}
		IPackage pack = (IPackage) container;
		List<IClassDiagram> classDiagrams = getClassDiagrams(projectAccessor,
				pack);
		if (classDiagrams.size() <= 0) {
			throw new Exception("パッケージにクラス図がありません．");
		}
		if (classDiagrams.size() >= 2) {
			throw new Exception("現在，チェックできるクラス図はパッケージあたり一つだけです．");
		}
		IClassDiagram targetClassDiagram = classDiagrams.get(0);

		if (DEBUG) {
			System.out.println("hoge");
		}

		assert isClassDiagram(targetClassDiagram); // 選択されている図にクラス要素がそんざいしているか

		List<CheckResult> results = new ArrayList<CheckResult>();// チェック結果の文章を格納しておくためのリスト
		Map<String, Relation> totalrelationMap = new HashMap<String, Relation>();

		RelationChecker relationchecker = new RelationChecker(
				targetClassDiagram);
		relationchecker.makeRelation(targetClassDiagram, totalrelationMap);

		// プロジェクトから比較インスタンス図を取得
		List<IClassDiagram> instanceDiagrams = getInstanceDiagrams(
				projectAccessor, pack);
		for (IClassDiagram instanceDiagram : instanceDiagrams) {
			if (DEBUG) {
				System.out.println("Check:" + instanceDiagram.getName());
			}
			int errorCount = 0; // 各インスタンス図で検出されたエラー数

			// クラスチェック
			ClassChecker iCheck = new ClassChecker(targetClassDiagram,
					instanceDiagram); // インスタンス図リストを渡す又はここでループさせる
			List<CheckResult> ccResult = new ArrayList<CheckResult>();
			ccResult = iCheck.doCheck();
			results.addAll(ccResult);
			errorCount = errorCount + ccResult.size();

			// 関連チェック
			RelationChecker rCheck = new RelationChecker(targetClassDiagram,
					instanceDiagram);
			List<CheckResult> rcResult = new ArrayList<CheckResult>();
			rcResult = rCheck.doCheck();
			results.addAll(rcResult);
			rCheck.doCheckTotalBounds(totalrelationMap);
			errorCount = errorCount + rcResult.size();

			if (errorCount == 1) {
				results.add(new CheckResult("クラス図と" + instanceDiagram.getName()
						+ "の間に不整合は見つかりませんでした"));
			}
		}
		results.addAll(doCheckExistSample(totalrelationMap));

		return results;
	}

	/**
	 * @param projectAccessor
	 * @param container
	 * @return
	 */
	private List<IClassDiagram> getInstanceDiagrams(
			ProjectAccessor projectAccessor, IPackage container) {
		List<IClassDiagram> diagrams = new ArrayList<IClassDiagram>();
		for (IDiagram diagram : container.getDiagrams()) {
			if (diagram instanceof IClassDiagram) {
				IClassDiagram classDiagram = (IClassDiagram) diagram;
				if (!isClassDiagram(classDiagram)) {
					diagrams.add(classDiagram);
				}
			}
		}
		return diagrams;
	}

	/**
	 * @param projectAccessor
	 * @param container
	 * @return
	 */
	private List<IClassDiagram> getClassDiagrams(
			ProjectAccessor projectAccessor, IPackage container) {
		List<IClassDiagram> diagrams = new ArrayList<IClassDiagram>();
		for (IDiagram diagram : container.getDiagrams()) {
			if (diagram instanceof IClassDiagram) {
				IClassDiagram classDiagram = (IClassDiagram) diagram;
				if (isClassDiagram(classDiagram)) {
					diagrams.add(classDiagram);
				}
			}
		}
		return diagrams;
	}

	// 選択中のクラス図に「クラス」が存在しているか、すなわちクラス図として使われているか
	private boolean isClassDiagram(IClassDiagram diagram) {
		if (getAllElements(diagram, IClass.class).isEmpty()) {
			return false;
		}
		return true;
	}

	private List<CheckResult> doCheckExistSample(
			Map<String, Relation> totalrelationmap) {
		List<CheckResult> results = new ArrayList<CheckResult>();
		results.add(new CheckResult(""));
		for (Map.Entry<String, Relation> e : totalrelationmap.entrySet()) {
			Relation relation = e.getValue();
			if (!relation.source.isAbstract() && !relation.target.isAbstract()) {
				// 多重度に*が使われていたら
				if (relation.upperBound != 0 || relation.lowerBound != 0) {

					if (relation.determinedUpperBound == -1) {
						// 存在するインスタンス例の最低数が多重度の下限と一致してたら
						if (relation.totalLowerBound == relation.determinedLowerBound) {
							// インスタンス例が一つしか無かったら
							if (relation.totalLowerBound == relation.totalUpperBound) {
								results.add(new CheckResult("Warning:"
										+ relation.source.getName() + "から"
										+ relation.target.getName() + "へのリンク数が"
										+ relation.connectionCount
										+ "以外のインスタンス例も書いてみましょう"));
							}
						} else if (relation.totalLowerBound != relation.determinedLowerBound
								&& relation.totalUpperBound != relation.determinedLowerBound) {
							if (relation.isMultiplicityError) {
								results.add(new CheckResult("Warning:"
										+ relation.source.getName() + "から"
										+ relation.target.getName()
										+ "への多重度の下限の例がありません。"));
							}
						}
					}
					if (relation.determinedLowerBound != -1
							&& relation.determinedUpperBound != -1) {
						if (relation.totalUpperBound != relation.determinedLowerBound
								&& relation.totalLowerBound != relation.determinedLowerBound
								&& relation.isMultiplicityError == true) {
							results.add(new CheckResult("Warning:"
									+ relation.source.getName() + "から"
									+ relation.target.getName()
									+ "への多重度の下限の例がありません。"));
						}
						if (relation.totalUpperBound != relation.determinedUpperBound
								&& relation.isMultiplicityError == true) {
							results.add(new CheckResult("Warning:"
									+ relation.source.getName() + "から"
									+ relation.target.getName()
									+ "への多重度の上限の例がありません。"));
						}
					}
				}
			}
		}
		return results;
	}

	// プロジェクト内から現在選択中以外のクラス図を取得するメソッド
	// private List<IClassDiagram> getProjectClassDiagram(
	// ProjectAccessor projectAccessor, IClassDiagram iCurrentDiagram)
	// throws ProjectNotFoundException {
	//
	// List<IClassDiagram> classDiagrams = new ArrayList<IClassDiagram>();
	// for (INamedElement element : projectAccessor
	// .findElements(IClassDiagram.class)) {
	// if (DEBUG) {
	// System.out.println("getProjectClassDiagram:list:" + element);
	// }
	// if (element.equals(iCurrentDiagram)) {
	// if (DEBUG) {
	// System.out
	// .println("getProjectClassDiagram:isCurrentDiagram");
	// }
	// } else {
	// // *ここで図にインスタンス仕様が存在しているかチェックする
	// if (getAllElements((IClassDiagram) element,
	// IInstanceSpecification.class).isEmpty()) {
	// if (DEBUG) {
	// System.out.println("getProjectClassDiagram:isEmpty");
	// }
	// } else {
	// classDiagrams.add((IClassDiagram) element);
	// if (DEBUG) {
	// System.out
	// .println("getProjectClassDiagram:isInstanceDiagram");
	// }
	// }
	// }
	//
	// }
	// return classDiagrams;
	// }

}
