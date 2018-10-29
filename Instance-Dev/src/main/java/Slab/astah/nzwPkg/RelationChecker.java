/**
 * RelationChecker.java
 * Created at  2011/12/13
 */
package Slab.astah.nzwPkg;

import static Slab.astah.common.AstahClassUtilities.getAllElements;
import static Slab.astah.common.AstahClassUtilities.getAllSubClasses;
import static Slab.astah.common.AstahClassUtilities.getSuperClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IAssociation;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.change_vision.jude.api.inf.model.ILink;
import com.change_vision.jude.api.inf.model.ILinkEnd;
import com.change_vision.jude.api.inf.model.IMultiplicityRange;
import com.change_vision.jude.api.inf.presentation.IPresentation;

/**
 * 
 * @author Kotaro Nozawa
 */
public class RelationChecker {
	private static final boolean DEBUG = false;
	private final IClassDiagram classDiagram;
	private IClassDiagram instanceDiagram;
	private final Map<String, Relation> relationMap = new HashMap<String, Relation>();

	public RelationChecker(IClassDiagram classdiagram,
			IClassDiagram instancediagram) {
		this.classDiagram = classdiagram;
		this.instanceDiagram = instancediagram;
	}

	public RelationChecker(IClassDiagram classdiagram) {
		this.classDiagram = classdiagram;
	}

	List<CheckResult> results = new ArrayList<CheckResult>();

	public List<CheckResult> doCheck() {

		// リンク取得してMapに格納
		makeLinks(instanceDiagram);
		try {
			makeRelation(classDiagram, relationMap);

			// インスタンス図中の要素を取得
			for (IPresentation presentation : instanceDiagram
					.getPresentations()) {
				IElement element = presentation.getModel();
				// リンクであれば
				if (element instanceof ILink) {
					ILink link = (ILink) element;
					ILinkEnd[] linkend = link.getMemberEnds();
					IClass source = linkend[0].getType().getClassifier();
					IClass target = linkend[1].getType().getClassifier();

					if (source == null || target == null) {
						results.add(new CheckResult(
								"Error:型（クラス）が設定されていないインスタンスと繋がっているリンクが存在しています"));
					} else if (relationMap.containsKey(source.getName() + "to"
							+ target.getName())
							|| relationMap.containsKey(target.getName() + "to"
									+ source.getName())) {
					} else {
						results.add(new CheckResult(
								"Error:クラス図に関連のないリンクが存在しています　@「"
										+ linkend[0].getType().getName()
										+ "」と「"
										+ linkend[1].getType().getName()
										+ "」のリンク"));
					}
				}
			}
		} catch (InvalidUsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		multiplicityCheck();
		doCheckMultiplicity();
		return results;
	}

	Map<IInstanceSpecification, List<ILink>> links = new HashMap<IInstanceSpecification, List<ILink>>();

	List<ILink> getLinks(IInstanceSpecification instance) {
		if (!links.containsKey(instance)) {
			links.put(instance, new ArrayList<ILink>());
		}
		return links.get(instance);
	}

	public void makeRelation(IClassDiagram diagram,
			Map<String, Relation> relationmap) {
		try {
			for (IPresentation presentation : classDiagram.getPresentations()) {
				IElement element = presentation.getModel();
				if (element instanceof IAssociation) {
					IAssociation association = (IAssociation) element;
					IAttribute[] attribute = association.getMemberEnds();
					setRelation(attribute, relationmap);
					for (int i = 0; i < attribute.length; i++) {
						IAttribute att = attribute[i];
						int another = 0;
						List<IClass> subclasses = new ArrayList<IClass>();
						if (att.getType().getSpecializations().length != 0) {
							if (i == 0) {
								another = 1;
							} else {
								another = 0;
							}
							List<IClass> targetclasses = new ArrayList<IClass>();
							targetclasses.add(attribute[another].getType());
							getAllSubClasses(attribute[another].getType(),
									targetclasses);
							for (IClass clazz : getAllSubClasses(att.getType(),
									subclasses)) {
								setSubRelation(clazz, targetclasses, attribute,
										i, another, relationmap);
							}
						}
					}
				}
			}
		} catch (InvalidUsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void setRelation(IAttribute[] attribute, Map<String, Relation> relationmap) {

		// 関連の情報をRelationクラスのオブジェクトに格納
		Relation relation = new Relation(attribute[0].getType(),
				attribute[1].getType(),
				attribute[1].getMultiplicity()[0].getLower(),
				attribute[1].getMultiplicity()[0].getUpper());

		// 逆側から見た関連を仮想的に作りRelationクラスのオブジェクトに格納
		Relation anotherRelation = new Relation(attribute[1].getType(),
				attribute[0].getType(),
				attribute[0].getMultiplicity()[0].getLower(),
				attribute[0].getMultiplicity()[0].getUpper());

		// それぞれをハッシュマップに格納
		relationmap.put(attribute[0].getType().getName() + "to"
				+ attribute[1].getType().getName(), relation);
		relationmap.put(attribute[1].getType().getName() + "to"
				+ attribute[0].getType().getName(), anotherRelation);

	}

	void setSubRelation(IClass source, List<IClass> targetList,
			IAttribute[] attribute, int i, int j,
			Map<String, Relation> relationmap) {
		for (IClass target : targetList) {
			Relation relation = new Relation(source, target,
					attribute[j].getMultiplicity()[0].getLower(),
					attribute[j].getMultiplicity()[0].getUpper());
			String key = source.getName() + "to" + target.getName();

			Relation anotherRelation = new Relation(target, source,
					attribute[i].getMultiplicity()[0].getLower(),
					attribute[i].getMultiplicity()[0].getUpper());
			String key2 = target.getName() + "to" + source.getName();

			if (relationmap.containsKey(key) || relationmap.containsKey(key2)) {

			} else {
				relationmap.put(key, relation);
				relationmap.put(key2, anotherRelation);
			}

		}

	}

	void makeLinks(IClassDiagram instanceDiagram) {
		for (ILink link : getAllElements(instanceDiagram, ILink.class)) {
			for (ILinkEnd linkend : link.getMemberEnds()) {
				getLinks(linkend.getType()).add(link);
			}
		}
	}

	void multiplicityCheck() {
		ILinkEnd linkendS;
		ILinkEnd linkendT;

		for (IInstanceSpecification instance : getAllElements(instanceDiagram,
				IInstanceSpecification.class)) {
			
			
			
			// リンクの取得
			for (ILink link : getLinks(instance)) {
				

				
				if (link.getMemberEnds()[0].getType().getClassifier() != null
						&& link.getMemberEnds()[1].getType().getClassifier() != null) {
					if (!instance.equals(link.getMemberEnds()[0].getType())) {
						linkendS = link.getMemberEnds()[1];
						linkendT = link.getMemberEnds()[0];
					} else {
						linkendS = link.getMemberEnds()[0];
						linkendT = link.getMemberEnds()[1];
					}
					String key = linkendS.getType().getClassifier().getName()
							+ "to"
							+ linkendT.getType().getClassifier().getName();
					if (relationMap.containsKey(key)) {
						Relation relation = relationMap.get(key);
						// updateMultiplicity(relation, instance);
						int count = 0;
						count = countConnection(relation, instance, count);
						updateBound(relation, instance, count);

						if (relation.target.getGeneralizations().length != 0) {
							IClass superclass = getSuperClass(relation.target);
							Relation superrelation = relationMap.get(instance
									.getClassifier().getName()
									+ "to"
									+ superclass.getName());
							if (superclass != null && superrelation != null) {
								superrelation.connectionCount = superrelation.connectionCount
										+ count;

								/*
								 * relation.determinedLowerBound = 0;
								 * relation.determinedUpperBound = 0;
								 * relation.lowerBound = 0; relation.upperBound
								 * = 0;
								 */
							}
						}

						/*
						 * if (relation.source.getGeneralizations().length != 0)
						 * { IClass superclass = getSuperClass(relation.source);
						 * Relation superrelation = relationMap.get(superclass
						 * .getName() + "to" + relation.target); if (superclass
						 * != null && superrelation != null) {
						 * superrelation.connectionCount =
						 * superrelation.connectionCount + count;
						 * 
						 * relation.determinedLowerBound = 0;
						 * relation.determinedUpperBound = 0;
						 * relation.lowerBound = 0; relation.upperBound = 0; } }
						 */

					}
				}

			}
			
			
			// リンクとして繋がっていないがクラス図には関連があるものを探す
			//add ema (リンクがない場合の処理なのにリンクのループ内に置かれていたのを修正)
			if (instance.getClassifier() != null) {
				if (DEBUG) {
					System.out.println(instance.getName());
				}
				

				
				for (Map.Entry<String, Relation> relations : relationMap
						.entrySet()) {
					if (relations.getValue().source.equals(instance
							.getClassifier())) {
						

						
						Relation relation = relations.getValue();
						if (!isLinkExisted(instance, relation)) {
							
							
	
							
							relation.exsistingLink = true;
							relation.lowerBound = 0;
						}
					}
				}
			}
			
			
			updateRelationBound();
		}
	}

	/*
	 * 現在未使用。CountConnection()を使用中 void updateMultiplicity(Relation relation,
	 * IInstanceSpecification instance) { //
	 * 以前に型が同じ組み合わせのリンクがあったかの判定と、同じインスタンスにいくつ同一のクラスのインスタンスが繋がっているかをカウント if
	 * (relation.sourceInstance == null) { relation.sourceInstance = instance;
	 * relation.connectionCount++; } else if
	 * (!relation.sourceInstance.equals(instance)) { relation.exsistingLink =
	 * true; relation.sourceInstance = instance; relation.connectionCount = 1; }
	 * else { relation.connectionCount++; }
	 * 
	 * // カウントしたもので関係の最小、最大関係数を更新 if (relation.exsistingLink == false) {
	 * relation.lowerBound = relation.connectionCount; relation.upperBound =
	 * relation.connectionCount; } else { if (relation.lowerBound >
	 * relation.connectionCount) { relation.lowerBound =
	 * relation.connectionCount; } else if (relation.upperBound <
	 * relation.connectionCount) { relation.upperBound =
	 * relation.connectionCount; } }
	 * 
	 * }　
	 */
	private void updateRelationBound() {
		for (Map.Entry<String, Relation> relationmap : relationMap.entrySet()) {
			Relation relation = relationmap.getValue();
			if (relation.connectionCount == 0) {
			} else if (relation.connectionCount != 0
					&& relation.upperBound == 0 && relation.lowerBound == 0) {
				relation.upperBound = relation.connectionCount;
				relation.lowerBound = relation.connectionCount;
			} else {
				if (relation.connectionCount > relation.upperBound) {
					relation.upperBound = relation.connectionCount;
				}
				if (relation.connectionCount < relation.lowerBound) {
					relation.lowerBound = relation.connectionCount;
				}
			}
			relation.connectionCount = 0;
		}
	}

	// 関連のターゲットが他クラスのサブクラスの時、親クラスとの関連を取ってくる（相当する関連がなければnullのRelationを返す)
	/*
	 * private Relation getSuperRelation(Relation relation, Map<String,
	 * Relation> relationmap) { Relation superRelation =
	 * relationmap.get(relation.source.getName() + "to" +
	 * relation.target.getGeneralizations()[0].getSuperType() .getName());
	 * 
	 * return superRelation; }
	 */

	// 関連のターゲットが他クラスの親の時、子クラスとの関連を取得し、多重度をリセットする（チェックにひっかからないようにするため）
	private void resetSubRelations(Relation relation) {
		if (relation.target.getSpecializations().length != 0) {
			List<IClass> subsets = new ArrayList<IClass>();
			getAllSubClasses(relation.target, subsets);
			for (IClass clazz : subsets) {
				Relation subrelation = relationMap.get(relation.source
						.getName() + "to" + clazz.getName());
				if (subrelation != null) {
					if (relation.determinedUpperBound == -1) {
						if (relation.determinedLowerBound > relation.lowerBound) {
							results.add(new CheckResult("Error:インスタンス図において"
									+ relation.source.getName() + "のインスタンスから"
									+ relation.target.getName()
									+ "のインスタンスへのリンク数が多重度の範囲を逸脱しています"));
						} else {
							subrelation.determinedLowerBound = 0;
							subrelation.determinedUpperBound = 0;
							subrelation.lowerBound = 0;
							subrelation.upperBound = 0;
						}
					} else {

						if (relation.determinedLowerBound > relation.lowerBound
								|| relation.determinedUpperBound < relation.upperBound) {
							results.add(new CheckResult("Error:インスタンス図において"
									+ relation.source.getName() + "のインスタンスから"
									+ relation.target.getName()
									+ "のインスタンスへのリンク数が多重度の範囲を逸脱しています"));
						} else {
							subrelation.determinedLowerBound = 0;
							subrelation.determinedUpperBound = 0;
							subrelation.lowerBound = 0;
							subrelation.upperBound = 0;
						}
					}
				} else {

				}
			}
		}
	}

	/*
	 * void checkGeneralization(Relation relation, Map<String, Relation>
	 * relationmap) { // 継承に関わるリンクを束ねる方法を模索中
	 * 
	 * if (relation.target.getGeneralizations().length != 0) { Relation
	 * superRelation = getSuperRelation(relation, relationmap);
	 * 
	 * if (superRelation != null) { superRelation.lowerBound =
	 * superRelation.lowerBound + relation.lowerBound; superRelation.upperBound
	 * = superRelation.upperBound + superRelation.upperBound;
	 * 
	 * relation.determinedLowerBound = 0; relation.determinedUpperBound = 0;
	 * relation.lowerBound = 0; relation.upperBound = 0;
	 * 
	 * } }
	 * 
	 * if (relation.source.getGeneralizations().length != 0) { Relation
	 * anotherRelation = relationmap.get(relation.source
	 * .getGeneralizations()[0].getName() + "to" + relation.target.getName());
	 * 
	 * if (anotherRelation != null) { anotherRelation.lowerBound =
	 * anotherRelation.lowerBound + relation.lowerBound;
	 * anotherRelation.upperBound = anotherRelation.upperBound +
	 * anotherRelation.upperBound;
	 * 
	 * relation.lowerBound = 0; relation.upperBound = 0; } } }
	 */

	// リンク数をカウントするメソッド
	private int countConnection(Relation relation,
			IInstanceSpecification instance, int count) {
		/*
		 * for (Entry<IInstanceSpecification, List<ILink>> linklist : links
		 * .entrySet()) { if (linklist.getKey().equals(instance)) { for (ILink
		 * link : linklist.getValue()) { if
		 * (link.getMemberEnds()[0].getType().getClassifier()
		 * .equals(relation.target) || link.getMemberEnds()[1].getType()
		 * .getClassifier().equals(relation.target)) {
		 */
		count++;
		return count;
	}

	private void updateBound(Relation relation,
			IInstanceSpecification instance, int count) {
		if (relation.exsistingLink == false) {
			relation.lowerBound = count;
			relation.upperBound = count;
			relation.exsistingLink = true;
		} else if (relation.lowerBound > count) {
			relation.lowerBound = count;
		} else if (relation.upperBound < count) {
			relation.upperBound = count;
		}
	}

	// 対象のクラス関係と対になるリンク関係がインスタンスからひかれているか
	boolean isLinkExisted(IInstanceSpecification instance, Relation relation) {
		List<ILink> linklist = links.get(instance);
		
		

		
		for (ILink link : linklist) {
			if (relation.target.isAbstract()) {
				return true;
			}
			if (link.getMemberEnds()[1].getType().getClassifier() != null
					&& link.getMemberEnds()[0].getType().getClassifier() != null) {
				if (link.getMemberEnds()[1].getType().getClassifier()
						.equals(relation.target)
						|| link.getMemberEnds()[0].getType().getClassifier()
								.equals(relation.target)) {
					return true;
				}
			}
		}
		return false;
	}

	// 多重度比較
	void doCheckMultiplicity() {
		for (Map.Entry<String, Relation> relationmap : relationMap.entrySet()) {
			Relation relation = relationmap.getValue();

			resetSubRelations(relation);
		}
		for (Map.Entry<String, Relation> relationmap : relationMap.entrySet()) {
			Relation relation = relationmap.getValue();

			ArrayList<IInstanceSpecification> instancelist = new ArrayList<IInstanceSpecification>();
			instancelist = (ArrayList<IInstanceSpecification>) getAllElements(
					instanceDiagram, IInstanceSpecification.class);
			int existInstanceCount = 0;
			for (IInstanceSpecification instance : instancelist) {
				if (relation.source.equals(instance.getClassifier())) {
					existInstanceCount++;
				}
			}

			if (existInstanceCount == 0) {

			} else {
				if (!relation.source.isAbstract()
						&& !relation.target.isAbstract()) {

					// 対象の関連のターゲットが他クラスのサブクラスかどうかを調べて、そうならばリンク数を追加する（良くない方法）
					// ここでやるよりリンク数をカウントする時点でやらないとダメ
					// checkGeneralization(relation, relationMap);

					if (relation.determinedLowerBound == -100
							|| relation.determinedUpperBound == -100) {
						results.add(new CheckResult("Warning:"
								+ relation.source.getName() + "と"
								+ relation.target.getName()
								+ "の関連に多重度をつけてみましょう"));
					} else if (relation.determinedUpperBound == -1) {
						
						
				
						
						
						
						
						if (relation.lowerBound < relation.determinedLowerBound) {
							results.add(new CheckResult("Error:インスタンス図において"
									+ relation.source.getName() + "のインスタンスから"
									+ relation.target.getName()
									+ "のインスタンスへのリンク数が多重度の範囲を逸脱しています"));
							relation.isMultiplicityError = false;
						}
					} else if (relation.lowerBound < relation.determinedLowerBound
							|| relation.upperBound > relation.determinedUpperBound) {
						results.add(new CheckResult("Error:インスタンス図において"
								+ relation.source.getName() + "のインスタンスから"
								+ relation.target.getName()
								+ "のインスタンスへのリンク数が多重度の範囲を逸脱しています"));
						relation.isMultiplicityError = false;
					}
				}
				if (DEBUG) {
					System.out.println("lowerBound=" + relation.lowerBound);
					System.out.println("upperBound=" + relation.upperBound);
				}
			}
		}
	}

	// 多重度の例がちゃんとあるかチェックするメソッド
	void doCheckSampleNumber(Relation relation) {
		if (!relation.source.isAbstract() && !relation.target.isAbstract()) {
			if (relation.determinedLowerBound != -1
					&& relation.determinedUpperBound != -1) {
				if (relation.lowerBound != relation.determinedLowerBound) {
					results.add(new CheckResult("Warning:"
							+ relation.source.getName() + "から"
							+ relation.target.getName() + "への多重度の下限の例がありません。"));
				}
				if (relation.upperBound != relation.determinedUpperBound) {
					results.add(new CheckResult("Warning:"
							+ relation.source.getName() + "から"
							+ relation.target.getName() + "への多重度の上限の例がありません。"));
				}
			}
		}
	}

	public void doCheckTotalBounds(Map<String, Relation> totalrelationmap) {
		for (Map.Entry<String, Relation> e : totalrelationmap.entrySet()) {
			if (relationMap.containsKey(e.getKey())) {
				Relation relation = relationMap.get(e.getKey());
				Relation totalrelation = e.getValue();

				if (totalrelation.totalLowerBound == -2
						&& totalrelation.totalUpperBound == -2) {
					totalrelation.totalLowerBound = relation.lowerBound;
					totalrelation.totalUpperBound = relation.upperBound;
				}
				if (totalrelation.totalLowerBound > relation.lowerBound) {
					totalrelation.totalLowerBound = relation.lowerBound;
				}
				if (totalrelation.totalUpperBound < relation.upperBound) {
					totalrelation.totalUpperBound = relation.upperBound;

				}

			}
		}
	}

	boolean isExistMultiplicity(IAttribute[] attribute) {
		for (IAttribute att : attribute) {
			for (IMultiplicityRange mRange : att.getMultiplicity()) {
				if (mRange.getLower() == IMultiplicityRange.UNDEFINED
						|| mRange.getUpper() == IMultiplicityRange.UNDEFINED) {
					return false;
				}
			}
		}
		return true;
	}

}
