package Slab.astah.hayakawa;

import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.change_vision.jude.api.inf.editor.ClassDiagramEditor;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.model.IAssociation;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IGeneralization;
import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessorFactory;

public class InstanceDiagramGenerator {

	private static final boolean DEBUG = false;

	private static Random RANDOM = new Random();
	private static final NumberFormat FORMATTER = new DecimalFormat("00");

	public static void setRandom(Random newRANDOM) {
		RANDOM = newRANDOM;
	}

	public static IClassDiagram generate(IClassDiagram targetClassDiagram)
			throws Exception {
		return new InstanceDiagramGenerator()
				.generateInternal(targetClassDiagram);
	}

	private final ClassDiagramEditor editor; //エディタ

	private List<IClass> classes;                 //クラス配列
	private List<IAssociation> associations;      //関連配列
	private final HashMap<String, ArrayList<Myinstance>> myInstanceMap = new HashMap<String, ArrayList<Myinstance>>();
	@SuppressWarnings("rawtypes")
	
	private final List<ArrayList> recursive = new ArrayList<ArrayList>();// コンポジットパターン時、格段のインスタンスを格納したArrayListを格納しておくArrayList

	
	
	
	private InstanceDiagramGenerator() throws Exception { //const
		this.editor = ProjectAccessorFactory.getProjectAccessor()
				.getDiagramEditorFactory().getClassDiagramEditor();
	}

	
	//----------------------------------------------------------------------------------------
	
	private IClassDiagram generateInternal(IClassDiagram targetClassDiagram)
			throws Exception {
		IClassDiagram newInstanceDiagram = generateInstanceDiagram(targetClassDiagram);//インスタンス図名前
		generateInstances(targetClassDiagram, newInstanceDiagram);//中身
		checkAndRefineInstances(targetClassDiagram, newInstanceDiagram);//チェック
		return newInstanceDiagram;
	}

	private IClassDiagram generateInstanceDiagram(     //パッケージ内にインスタンス図作成（名前だけ
			IClassDiagram targetClassDiagram) throws Exception {
		IPackage pack = (IPackage) targetClassDiagram.getContainer();//クラス図が含まれるパッケージ取得
		String newInstanceDiagramName = createNewInstanceDiagramName(pack);  //インスタンス図の名前つけ
		IClassDiagram instanceDiagram = editor.createClassDiagram(pack,
				newInstanceDiagramName);
		return instanceDiagram;
	}

	private String createNewInstanceDiagramName(IPackage pack) {//名前
		int i = 1;
		while (true) {
			String name = "インスタンス図" + FORMATTER.format(i);
			if (!hasNamedDiagram(pack, name)) {//名前がかぶっていないか
				return name;
			}
			i++;
		}
	}

	private boolean hasNamedDiagram(INamedElement container, String name) { // 名前被りチェック
		for (IDiagram d : container.getDiagrams()) {
			if (d.getName().equals(name)) {
				return true;
			}

		}
		return false;
	}

	
	
	////////////////////////////////////////////////////checker
	/**
	 * @param targetClassDiagram
	 * @param newInstanceDiagram
	 */
	private void checkAndRefineInstances(IClassDiagram targetClassDiagram,
			IClassDiagram newInstanceDiagram) throws Exception {
		// nozawa作、インスタンスチェッカーによる生成したインスタンス図の検査
		// 3回チェックを通すと、大体矛盾したインスタンス図はでなくなった。
		// for (int i = 0; i < 3; i++) {
		// 3回では無理な可能性があるので，10回に変更(松)
		for (int i = 0; i < 10; i++) {
			List<RelationError> errors = RelationCheckerAdapter.check(
					targetClassDiagram, newInstanceDiagram);
			if (errors.size() <= 0) {
				break;
			}
			for (RelationError error : errors) {
				modifyInstanceForOneError(error.getClassName1(),
						error.getClassName2());
			}
		}

		// nozawa作、インスタンスチェッカーによる生成したインスタンス図の検査
		List<RelationError> errors = RelationCheckerAdapter.check(
				targetClassDiagram, newInstanceDiagram);
		if (errors.size() > 0) {
			throw new Exception("矛盾しないインスタンス図が作成出来ませんでした．(何回か試すと成功する場合があります)");
		}
	}

	// 矛盾が起きている部分の修正。実装中。
	private void modifyInstanceForOneError(String clazzname1, String clazzname2)
			throws Exception {
		if (!myInstanceMap.containsKey(clazzname1)) {
			throw new IllegalArgumentException(
					"modifyInstanceForOneError() clazzname1異常=" + clazzname1);
		}
		if (!myInstanceMap.containsKey(clazzname2)) {
			throw new IllegalArgumentException(
					"modifyInstanceForOneError() clazzname2異常=" + clazzname2);
		}
		// 矛盾の起きているインスタンスを特定して、そのインスタンスの配列内でのインデックスを取得
		ArrayList<Integer> list1 = getErrorIndexList(clazzname1, clazzname2);
		ArrayList<Integer> list2 = getErrorIndexList(clazzname2, clazzname1);
		if (list1 != null && list2 != null) {
			if (list1.size() != 0 && list2.size() != 0) {
				if (list1.size() == list2.size()) {
					modifyLink(clazzname1, clazzname2, list1, list2);
				} else if (list1.size() < list2.size()) {
					modifyLink(clazzname1, clazzname2, list1, list2);
				} else {
					modifyLink(clazzname1, clazzname2, list2, list1);
				}
			} else if (list1.size() == 0) {
				modifyInstanceAndLink(clazzname2, clazzname1, list2);
			} else {
				modifyInstanceAndLink(clazzname1, clazzname2, list1);
			}
		}
	}

	private void modifyInstanceAndLink(String sourceIns, String targetIns,
			ArrayList<Integer> list) throws Exception, InvalidEditingException {
		Myinstance source, target;
		for (int i = 0; i < list.size(); i++) {
			source = myInstanceMap.get(sourceIns).get(list.get(i));
			IClass sourceType = classes.get(getClassIndexFromString(sourceIns));
			IClass targetType = classes.get(getClassIndexFromString(targetIns));
			Point2D point = getLocation(targetType, myInstanceMap
					.get(targetIns).size() + 1);
			target = new Myinstance(targetIns
					+ myInstanceMap.get(targetIns).size(), targetType, point);
			editor.createInstanceSpecificationLink(source.getInstance(),
					target.getInstance());
			source.setLinkedList(targetType);
			target.setLinkedList(sourceType);
			myInstanceMap.get(targetIns).add(target);
		}
	}

	private void modifyLink(String souceIns, String targetIns,
			ArrayList<Integer> list1, ArrayList<Integer> list2)
			throws InvalidEditingException {
		Myinstance source;
		Myinstance target;
		for (int i = 0; i < list1.size(); i++) {
			source = myInstanceMap.get(souceIns).get(list1.get(i));
			target = myInstanceMap.get(targetIns).get(list2.get(i));
			editor.createInstanceSpecificationLink(source.getInstance(),
					target.getInstance());
			source.setLinkedList(classes
					.get(getClassIndexFromString(targetIns)));
			target.setLinkedList(classes.get(getClassIndexFromString(souceIns)));
		}
	}

	private ArrayList<Integer> getErrorIndexList(String clazzname1,
			String clazzname2) {
		int index1 = getClassIndexFromString(clazzname1);
		int index2 = getClassIndexFromString(clazzname2);
		ArrayList<Integer> errorlist;
		if (!classes.get(index1).isAbstract()
				&& !classes.get(index2).isAbstract()) {
			errorlist = (ArrayList<Integer>) checkLink(clazzname1, clazzname2);
			return errorlist;
		}
		return null;
	}

	// クラス名を渡すと、クラス図中のIClassが全て入った配列から、一致するもののインデックスを返す
	private int getClassIndexFromString(String clazzname) {
		int index = 0;
		for (int i = 0; i < classes.size(); i++) {
			String clazz = classes.get(i).toString();
			if (clazz.equals(clazzname)) {
				index = i;
			}
		}
		return index;
	}

	// 矛盾を起こしているクラスの名前を2つ渡してやると、snameのどのインスタンスがおかしいのか（配列の何番目のインスタンスがおかしいのか）特定する。
	private List<Integer> checkLink(String sName, String tName) {
		List<Integer> errorIndexes = new ArrayList<Integer>();
		List<Myinstance> instances = myInstanceMap.get(sName);
		if (instances != null) {
			for (int i = 0; i < instances.size(); i++) {
				List<String> linkedlist = instances.get(i).getLinkedList();
				if (!linkedlist.contains(tName)) {
					if (DEBUG) {
						System.out.println(sName + "[" + (i) + "]"
								+ " not link to " + tName);
					}
					errorIndexes.add(i);
				}
			}
			return errorIndexes;
		}
		return null;
	}

	
	
	
	
	
	
	///////////////////////////////////////////////
	/////////////////////////////////////////////インスタンス図の中身生成
	
	private void generateInstances(IClassDiagram inDiagram,
			IClassDiagram outDiagram) throws Exception {
		
		classes = getClasses(inDiagram);
		associations = getAssociations(inDiagram);
		getGeneralizations(inDiagram);

		editor.setDiagram(outDiagram); //editorにインスタンス図を設定
		
		// // クラスごとにインスタンス例を入力させる。
		// for (int i = 0; i < classes.size(); i++) {
		// window = new InputInstanceNameWindow(classes.get(i), this);
		// }
		// 関連の線で繋がったクラスからインスタンスを生成
		
		for (int i = 0; i < associations.size(); i++) {
			getEdge(associations.get(i));
		}
	}

	
	// 入力されたインスタンス例を保存しておくマップ。keyはインスタンスの型。valueはstringの入った配列
	HashMap<IClass, ArrayList<String>> instanceNameMap = new HashMap<IClass, ArrayList<String>>();

	
	@SuppressWarnings("unchecked")
	private <T> List<T> get(IClassDiagram diagram, Class<T> clazz)
			throws Exception {
		List<T> list = new ArrayList<T>();
		for (IPresentation p : diagram.getPresentations()) {
			IElement model = p.getModel();
			if (clazz.isInstance(model)) {
				list.add((T) model);
			}
		}
		return list;
	}

	
	private List<IClass> getClasses(IClassDiagram diagram) throws Exception {//クラス
		return get(diagram, IClass.class);
	}

	private List<IGeneralization> getGeneralizations(IClassDiagram diagram) //汎化
			throws Exception {
		return get(diagram, IGeneralization.class);
	}

	private List<IAssociation> getAssociations(IClassDiagram diagram) //関連
			throws Exception {
		return get(diagram, IAssociation.class);
	}

	
	// 関連の線から両端を取得
	private void getEdge(IAssociation association) throws Exception {
		IAttribute[] atts = association.getMemberEnds();
		IAttribute sEdge = atts[0];
		IAttribute tEdge = atts[1];
		decideSourceIns(sEdge, tEdge);
		// source,targetを入れ替えて再度実行
		sEdge = atts[1];
		tEdge = atts[0];
		decideSourceIns(sEdge, tEdge);
		// source、targetのlinkableをリセット。map内の要素を全てに行う。
		resetLinkable();
	}

	private void resetLinkable() {
		for (String key : myInstanceMap.keySet()) {
			reset(myInstanceMap.get(key));
		}
	}

	private void decideSourceIns(IAttribute sEdge, IAttribute tEdge)
			throws Exception, InvalidEditingException {
		IClass sourceType = sEdge.getType();
		List<Myinstance> sourceList;
		Myinstance source;
		if (sourceType.isAbstract()) {
			// source側が抽象クラスだったときの処理。
			sourceType = toSubType(sourceType);
		}
		sourceList = getList(sourceType);
		if (sourceList.isEmpty()) {
			makeSourceAndLinkToTarget(sEdge, tEdge, sourceType, sourceList);
		} else {
			int hoge = RANDOM.nextInt(5);
			if (hoge == 0) {
				makeSourceAndLinkToTarget(sEdge, tEdge, sourceType, sourceList);
			} else {
				for (int i = 0; i < sourceList.size(); i++) {
					source = sourceList.get(i);
					if (source.getLinkable()) {
						makeTargetAndLink(sEdge, tEdge, sourceType, source);
					}
				}
			}
		}
	}

	// sourceインスタンスを作って、target側インスタンスとリンク
	private void makeSourceAndLinkToTarget(IAttribute sEdge, IAttribute tEdge,
			IClass sourceType, List<Myinstance> sourceList) throws Exception,
			InvalidEditingException {
		Myinstance source;
		source = new Myinstance(sourceType.getName() + (sourceList.size() + 1),
				sourceType, getLocation(sourceType, sourceList.size() + 1));
		makeTargetAndLink(sEdge, tEdge, sourceType, source);
		sourceList.add(source);
		myInstanceMap.put(sourceType.getName(),
				(ArrayList<Myinstance>) sourceList);
	}

	private IClass toSubType(IClass clazz) {
		IGeneralization[] generalizations = clazz.getSpecializations();
		int decideSubType = RANDOM.nextInt(generalizations.length);
		clazz = generalizations[decideSubType].getSubType();
		return clazz;
	}

	private void makeTargetAndLink(IAttribute sEdge, IAttribute tEdge,
			IClass sourceType, Myinstance source) throws Exception,
			InvalidEditingException {
		IClass targetType = tEdge.getType();
		if (targetType.isAbstract()) { // target側が抽象クラスだったとき
			targetType = toSubType(targetType);
		}
		List<Myinstance> targetList = getList(targetType);
		int decideLink = RANDOM.nextInt(3);
		if (tEdge.getMultiplicity()[0].getLower() == 0
				|| tEdge.getMultiplicity()[0].getLower() == -1) {
			if (tEdge.getMultiplicity()[0].getUpper() == 1) {// 0..1
				if (decideLink == 0 || decideLink == 1) {
					// nothing
				} else {
					createALink(sEdge, tEdge, source, targetType, targetList);
				}
			} else if (tEdge.getMultiplicity()[0].getUpper() == -1) {// 0..*
				if (decideLink == 0) {
					// nothing
				} else if (decideLink == 1) {
					createALink(sEdge, tEdge, source, targetType, targetList);
				} else {
					createSomeLink(sEdge, tEdge, source, targetType, targetList);
				}
			}
		} else if (tEdge.getMultiplicity()[0].getLower() == 1) {
			if (tEdge.getMultiplicity()[0].getUpper() == 1) {// 1

				createALink(sEdge, tEdge, source, targetType, targetList);

			} else if (tEdge.getMultiplicity()[0].getUpper() == -1) {// 1..*
				if (targetList.isEmpty()) {
					if (decideLink == 0) {
						createSomeLink(sEdge, tEdge, source, targetType,
								targetList);
					} else {
						createALink(sEdge, tEdge, source, targetType,
								targetList);
					}
				} else {
					if (decideLink == 0) {
						appendLink(sEdge, tEdge, source, targetList);
					} else if (decideLink == 1) {
						createALink(sEdge, tEdge, source, targetType,
								targetList);
					} else {
						createSomeLink(sEdge, tEdge, source, targetType,
								targetList);
					}
				}
			}
		}
		myInstanceMap.put(targetType.getName(),
				(ArrayList<Myinstance>) targetList);
	}

	// targetの、すでに作られているインスタンスを利用してリンクするとき
	private void appendLink(IAttribute sEdge, IAttribute tEdge,
			Myinstance source, List<Myinstance> targetList)
			throws InvalidEditingException {
		for (int i = 0; i < targetList.size(); i++) {
			Myinstance target = targetList.get(i);
			paintLink(source, target, sEdge, tEdge);
		}
	}

	private void createALink(IAttribute sEdge, IAttribute tEdge,
			Myinstance source, IClass targetType, List<Myinstance> targetList)
			throws Exception, InvalidEditingException {
		Myinstance target = new Myinstance(targetType.getName()
				+ (targetList.size() + 1), targetType, getLocation(
				tEdge.getType(), targetList.size() + 1));
		paintLink(source, target, sEdge, tEdge);
		targetList.add(target);
	}

	// 多重度の上限がnのときの線引き
	private void createSomeLink(IAttribute sEdge, IAttribute tEdge,
			Myinstance source, IClass targetType, List<Myinstance> targetList)
			throws Exception, InvalidEditingException {
		for (int j = 0; j < 2; j++) {
			Myinstance target = new Myinstance(targetType.getName()
					+ (targetList.size() + 1), targetType, getLocation(
					tEdge.getType(), targetList.size() + 1));
			paintLink(source, target, sEdge, tEdge);
			targetList.add(target);
		}
	}

	private List<Myinstance> getList(IClass keyType) {
		List<Myinstance> list;
		if (myInstanceMap.containsKey(keyType.getName())) {
			list = myInstanceMap.get(keyType.getName());
		} else {
			list = new ArrayList<Myinstance>();
		}
		return list;
	}

	// source,target、互いに線が引けるかチェックして、線を描画
	private void paintLink(Myinstance source, Myinstance target,
			IAttribute sEdge, IAttribute tEdge) throws InvalidEditingException {
		if (source.getLinkable() && target.getLinkable()) {
			editor.createInstanceSpecificationLink(source.getInstance(),
					target.getInstance());
			isLinkable(tEdge, source);
			isLinkable(sEdge, target);
			// リンク相手の型を登録
			source.setLinkedList(target.getType());
			target.setLinkedList(source.getType());
		}
	}

	// インスタンスにさらにリンクが引けるかどうか
	private void isLinkable(IAttribute edge, Myinstance instance) {
		if (edge.getMultiplicity()[0].getUpper() == 1) {
			instance.setFalse();
		}
	}

	
	
	///////////////////////////////////////////////////////////////////////////////
	
	// 継承関係のクラスのインスタンス生成
	@SuppressWarnings("unused")
	private void genInstance(IGeneralization generalization) throws Exception {
		IClass superclass = generalization.getSuperType();
		IClass subclass = generalization.getSubType();

		ArrayList<Myinstance> instances = null;
		ArrayList<Myinstance> list = new ArrayList<Myinstance>();

		genSuperClassIns(superclass, subclass, instances, list);
		genSubClassIns(subclass, superclass, instances);

	}

	private void genSubClassIns(IClass subclass, IClass superclass,
			ArrayList<Myinstance> instances) throws Exception {
		instances = new ArrayList<Myinstance>();

		if (!subclass.isAbstract()) {
			if (validMap(subclass.getName())) // インスタンス既にあるかどうか。
			{
				for (int i = 0; i < 3; i++) {
					Myinstance instance = new Myinstance(subclass, getLocation(
							subclass, i + 1), i);
					instance.setSuperClass(superclass);
					instances.add(instance);
				}
				myInstanceMap.put(subclass.getName(), instances);
			} else {
				// 既にあった場合の処理。
			}
		} else {
			// 抽象クラス
			if (validMap(subclass.getName())) // インスタンス既にあるかどうか。
			{
				Myinstance instance = new Myinstance(subclass);
				instance.setSuperClass(superclass);
				instances.add(instance);
				myInstanceMap.put(subclass.getName(), instances);
			} else {
				// nothing
			}
		}
	}

	private void genSuperClassIns(IClass superclass, IClass subclass,
			ArrayList<Myinstance> instances, ArrayList<Myinstance> list)
			throws Exception {
		instances = new ArrayList<Myinstance>();
		Myinstance instance;
		if (!superclass.isAbstract()) {
			if (validMap(superclass.getName())) {
				for (int i = 0; i < 3; i++) {
					instance = new Myinstance(superclass, getLocation(
							superclass, i + 1), i);
					instance.setSubClass(subclass);
					instances.add(instance);
				}
				myInstanceMap.put(superclass.getName(), instances);
			} else {
				// すでにインスタンスが作られていた場合
				list = myInstanceMap.get(superclass.getName());
				for (int i = 0; i < list.size(); i++) {
					list.get(i).setSubClass(subclass);
				}
				myInstanceMap.put(superclass.getName(), list);
			}
		} else {
			// 抽象クラスならサブクラスを設定。プレゼンテーションはつくらない。
			if (validMap(superclass.getName())) {
				instance = new Myinstance(superclass);
				instance.setSubClass(subclass);
				instances.add(instance);
				myInstanceMap.put(superclass.getName(), instances);
			} else {
				list = myInstanceMap.get(superclass.getName());
				for (int i = 0; i < list.size(); i++) {
					list.get(i).setSubClass(subclass);
				}
				myInstanceMap.put(superclass.getName(), list);
			}
		}
	}

	private boolean validMap(String typeName) {
		if (myInstanceMap.containsKey(typeName)) {
			return false;
		} else {
			return true;
		}
	}

	private Point2D getLocation(IClass clazz, int num) throws Exception {
		Point2D point;
		INodePresentation node = (INodePresentation) clazz.getPresentations()[0];
		point = node.getLocation();
		Point2D location = new Point2D.Double(point.getX(), point.getY() + num
				* 100.0);
		return location;
	}

	// 線引き
	@SuppressWarnings("unused")
	private void genLink1(IAssociation association) throws Exception {
		// 設定
		IClass clazz1 = association.getMemberEnds()[0].getType();
		IClass clazz2 = association.getMemberEnds()[1].getType();

		List<Myinstance> instances1 = new ArrayList<Myinstance>();
		List<Myinstance> instances2 = new ArrayList<Myinstance>();
		List<Myinstance> list = new ArrayList<Myinstance>();

		if (!clazz1.isAbstract() && !clazz2.isAbstract()) // 取得してきたクラスが互いに具象クラスであるとき
		{

			// mapからインスタンスを取ってきて
			instances1 = myInstanceMap.get(clazz1.getName());
			instances2 = myInstanceMap.get(clazz2.getName());

			// 多重度の下限を取得
			int clazz1MultiMin = association.getMemberEnds()[0]
					.getMultiplicity()[0].getLower();
			int clazz2MultiMin = association.getMemberEnds()[1]
					.getMultiplicity()[0].getLower();

			if (clazz2MultiMin == 1) {
				// target側の多重度が1もしくは1..＊
				genLink2(instances1, instances2);
			} else {
				// target側の多重度が0..1、0..＊
				genLink3(instances1, instances2);
			}

			if (clazz1MultiMin == 1) {
				// target側の多重度が1もしくは1..＊
				genLink2(instances2, instances1);
			} else {
				// target側の多重度が0..1、0..＊
				genLink3(instances2, instances1);
			}

			// インスタンスに線を引けるかどうかを保存している変数をリセット
			reset(instances1);
			reset(instances2);

		} else if (clazz1.isAbstract()) { // 抽象クラスだったときの処理.未実装
			instances1 = myInstanceMap.get(clazz2.getName());
			if (instances1.get(0).getSuperClass() == clazz1.getName()) {
				// 抽象クラスを継承しているクラスの名簿を渡す
				compositeLink(clazz2, myInstanceMap.get(clazz1.getName())
						.get(0).getSubClassList());
			}

		} else if (clazz2.isAbstract()) {
			instances1 = myInstanceMap.get(clazz1.getName());
			if (instances1.get(0).getSuperClass() == clazz2.getName()) {
				compositeLink(clazz1, myInstanceMap.get(clazz2.getName())
						.get(0).getSubClassList());
			}

		}

	}

	
	
	////////////////////////////////////////////////////////////////
	// コンポジットパターンの線引きメソッド
	private void compositeLink(IClass composition, ArrayList<IClass> subClasses)
			throws Exception {

		List<Myinstance> compositionList = new ArrayList<Myinstance>();
		List<Myinstance> leafList = new ArrayList<Myinstance>();

		// leafが複数の場合、ArrayListにする。
		String leafName = null;

		for (int i = 0; i < subClasses.size(); i++) {
			// 葉の名前を取得
			if (!subClasses.get(i).getName().equals(composition.getName())) {
				leafName = subClasses.get(i).getName();
			}
		}

		compositionList = myInstanceMap.get(composition.getName());
		leafList = myInstanceMap.get(leafName);

		// コンポジットパターン、一段目の処理
		Myinstance root;
		root = compositionList.get(0);

		// 格段の要素を入れる配列。この配列はrecursiveに格納する。
		ArrayList<Myinstance> list = new ArrayList<Myinstance>();
		list.add(root);
		recursive.add(list);

		recursiveLink(composition, leafList, list);
	}

	@SuppressWarnings("rawtypes")
	private void recursiveLink(IClass composition, List<Myinstance> leafList,
			List<Myinstance> superList) throws Exception,
			InvalidEditingException {

		// 格段のインスタンスを入れるリスト
		List<Myinstance> currentList = new ArrayList<Myinstance>();

		for (int i = 0; i < superList.size(); i++) {

			Point2D rootPos = superList.get(i).getInstance().getLocation();

			Random ran = new Random();

			IClass childType;
			Point2D childPos = new Point2D.Double();
			Myinstance instance;

			for (int j = 0; j < 3; j++) {
				boolean selectNode = ran.nextBoolean(); // 子をcompositionかleafに決定する変数

				if (superList.get(i).getType() == composition) {
					// 親がcompositionなら、子を作る
					if (selectNode) { // true;composition false;leaf

						childType = composition;
						childPos.setLocation(rootPos.getX() - 200.0
								+ (200.0 * j), rootPos.getY() + 100.0);

						instance = new Myinstance(childType.getName(),
								childType, childPos);
						editor.createInstanceSpecificationLink(superList.get(i)
								.getInstance(), instance.getInstance());
						currentList.add(instance);
					} else {
						childType = leafList.get(1).getType();
						childPos.setLocation(rootPos.getX() - 200.0
								+ (200.0 * j), rootPos.getY() + 100.0);

						instance = new Myinstance(childType.getName(),
								childType, childPos);
						editor.createInstanceSpecificationLink(superList.get(i)
								.getInstance(), instance.getInstance());
						currentList.add(instance);
					}
				} else {
					// 親がleafなら、子は作らない
				}
			}
		}

		recursive.add((ArrayList) currentList);

		// 再帰
		for (int j = 0; j < currentList.size(); j++) {
			if (recursive.size() < 3
					&& currentList.get(j).getType() == composition) {
				recursiveLink(composition, leafList, currentList);
			}
		}
	}

	
	
	/////////////////////////////////////////////////////////////////////////////////
	/**
	 * target側の多重度が1、1..＊のときによばれる線引きメソッド. 工事中。
	 * */
	private void genLink2(List<Myinstance> sources, List<Myinstance> targets)
			throws Exception {
		@SuppressWarnings("unused")
		Random rnd = new Random();
		Myinstance source, target;

		for (int i = 0; i < sources.size(); i++) {
			source = sources.get(i);
			for (int j = 0; j < targets.size(); j++) {
				target = targets.get(j);
				if (source.getLinkable() && target.getLinkable()) { // target側に線が引けるならば
					editor.createInstanceSpecificationLink(
							source.getInstance(), target.getInstance());
					source.incrementAmountLink();
					target.incrementAmountLink();
					if (source.getUpper() == 1) { // source側の多重度が1のとき、targetが指すインスタンスはこれ以上リンクできない
						target.setFalse();
					}
					if (target.getUpper() == 1) { // targetへの多重度が1のとき、sourceが指すインスタンスはこれ以上リンクできない
						source.setFalse();
					}
				}
			}
			if (source.getAmountLink() == 0) { // 多重度が1以上にもかかわらず、どのインスタンスともリンクされていない場合
				IClass targetType = targets.get(0).getType();
				Point2D point = getLocation(targetType, targets.size() + 1);
				target = new Myinstance(targetType, point, targets.size() + 1,
						targets.get(0).getUpper(), targets.get(0).getLower());
				targets.add(target);

				editor.createInstanceSpecificationLink(source.getInstance(),
						target.getInstance());
				source.incrementAmountLink();
				target.incrementAmountLink();
				if (source.getUpper() == 1) { // source側の多重度が1のとき、targetが指すインスタンスはこれ以上リンクできない
					target.setFalse();
				}
				if (target.getUpper() == 1) { // targetへの多重度が1のとき、sourceが指すインスタンスはこれ以上リンクできない
					source.setFalse();
				}
			}
		}
	}

	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * target側の多重度が0..1,0..nのときによばれる線引きメソッド とりあえずは完成？想定通り稼働中
	 **/
	private void genLink3(List<Myinstance> instances1,
			List<Myinstance> instances2) throws Exception {
		Random rnd = new Random();

		for (int i = 0; i < instances1.size(); i++) {
			Myinstance source, target;
			source = instances1.get(i);

			for (int j = 0; j < instances2.size(); j++) {
				target = instances2.get(j);
				int ran = rnd.nextInt(2);

				if (target.getUpper() == 1) { // 多重度0..１のとき
					if (source.getLinkable() && target.getLinkable()
							&& ran == 1) {
						editor.createInstanceSpecificationLink(
								source.getInstance(), target.getInstance());
						source.setFalse();

						if (source.getUpper() == 1) {
							target.setFalse();
						}
					}
				} else { // 多重度nのとき
					if (source.getLinkable() && target.getLinkable()
							&& ran == 1) {
						editor.createInstanceSpecificationLink(
								source.getInstance(), target.getInstance());
						if (source.getUpper() == 1) {
							target.setFalse();
						}
					}
				}
			}
		}
	}

	private void reset(List<Myinstance> instances) {
		for (int i = 0; i < instances.size(); i++) {
			Myinstance instance = instances.get(i);
			if (instance.getLinkable() == false) {
				instance.setTrue();
			}
			if (instance.getAmountLink() != 0) {
				instance.resetAmountLink();
			}
		}
	}

	private class Myinstance {
		// いらないかもしれない
		String clazzname;
		boolean linkable = true;
		INodePresentation instance;
		int upper, lower = 0;
		// どのクラスのインスタンスなのかを保存
		IClass type = null;
		// javaは多重継承ナシなので、一個でおｋ？
		IClass superclass = null;
		// リンクされた相手クラスのリスト。矛盾解消用。
		ArrayList<String> linkedList = new ArrayList<String>();
		// サブクラスを入れておくリスト
		ArrayList<IClass> subclasses = new ArrayList<IClass>();
		// あるインスタンスに幾つリンクが繋がっているか保存しておく
		int amountLink = 0;

		// 関連からインスタンスを生成するときに用いるコンストラクタ
		Myinstance(IClass clazz, Point2D point, int num, int upper, int lower)
				throws Exception {
			clazzname = clazz.getName();
			instance = createInstance(clazzname + num, clazz, point);
			type = clazz;
			setUpper(upper);
			setLower(lower);
		}

		Myinstance(IClass clazz, Point2D point, int num) throws Exception {
			clazzname = clazz.getName();
			instance = createInstance(clazzname + num + ";" + clazzname, clazz,
					point);
			type = clazz;
		}

		Myinstance(String name, IClass clazz, Point2D point) throws Exception {
			clazzname = clazz.getName();
			instance = createInstance(name, clazz, point);
			type = clazz;
		}

		// @TODO 松澤が追加
		private INodePresentation createInstance(String name, IClass clazz,
				Point2D point) throws Exception {
			// clazz.getName() 名前で設定すると，他パッケージのものが選択される！インターフェイスが不良！
			// editor.createInstanceSpecification(clazzname + num,
			// clazz.getName(), point);

			INodePresentation newInstance = editor.createInstanceSpecification(
					name, point);
			IInstanceSpecification model = (IInstanceSpecification) newInstance
					.getModel();
			model.setClassifier(clazz);
			return newInstance;
		}

		
		
		
		
		/////////////////////////////////////////////////unused
		@SuppressWarnings("unused")
		Myinstance(IClass clazz, int upper, int lower) {
			clazzname = clazz.getName();
			setUpper(upper);
			setLower(lower);
		}

		Myinstance(IClass clazz) {
			clazzname = clazz.getName();
			type = clazz;
		}

		public ArrayList<String> getLinkedList() {
			return linkedList;
		}

		public void setLinkedList(IClass clazz) {
			linkedList.add(clazz.getName());
		}

		@SuppressWarnings("unused")
		public String getName() {
			return clazzname;
		}

		public IClass getType() {
			return type;
		}

		public boolean getLinkable() {
			return linkable;
		}

		public void setSuperClass(IClass clazz) {
			if (superclass == null) {
				superclass = clazz;
			} else {
				// nothing
			}
		}

		public void setSubClass(IClass clazz) {
			if (subclasses.isEmpty()) {
				subclasses.add(clazz);
			} else {
				if (!subclasses.contains(clazz.getName())) {
					subclasses.add(clazz);
				} else {
					// nothing
				}
			}
		}

		public String getSuperClass() {
			return superclass.getName();
		}

		public ArrayList<IClass> getSubClassList() {
			return subclasses;
		}

		public void setUpper(int num) {
			upper = num;
		}

		public void setLower(int num) {
			lower = num;
		}

		public int getUpper() {
			return upper;
		}

		public int getLower() {
			return lower;
		}

		public int getAmountLink() {
			return amountLink;
		}

		public void incrementAmountLink() {
			amountLink++;
		}

		public void resetAmountLink() {
			amountLink = 0;
		}

		public void setTrue() {
			linkable = true;
		}

		public void setFalse() {
			linkable = false;
		}

		public INodePresentation getInstance() {
			return instance;
		}
	}
}
