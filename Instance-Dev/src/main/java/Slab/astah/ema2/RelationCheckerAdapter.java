/**
 * RelationCheckerAdapter.java
 * Created at 2012/05/14
 * Copyright(c) 2012 Yoshiaki Matsuzawa All Rights Reserved
 */
package Slab.astah.ema2;

import java.util.ArrayList;
import java.util.List;

import Slab.astah.nzwPkg.CheckResult;
import Slab.astah.nzwPkg.RelationChecker;

import com.change_vision.jude.api.inf.model.IClassDiagram;

/**
 * @author macchan
 * 
 */
public class RelationCheckerAdapter {

	public static List<RelationError> check(IClassDiagram targetClassDiagram,
			IClassDiagram newInstanceDiagram) {
		return new RelationCheckerAdapter().checkInternal(targetClassDiagram,
				newInstanceDiagram);
	}

	private List<RelationError> checkInternal(IClassDiagram targetClassDiagram,
			IClassDiagram newInstanceDiagram) {
		List<RelationError> errors = new ArrayList<RelationError>();
		RelationChecker rChecker = new RelationChecker(targetClassDiagram,
				newInstanceDiagram);
		List<CheckResult> results = rChecker.doCheck();
		for (CheckResult result : results) {
			RelationError error = readResult(result.toString());
			if (error != null) {
				errors.add(error);
			}
		}
		return errors;
	}

	private RelationError readResult(String result) {
		// 2012/05/11実験でうまくインスタンス図が出力できなかった原因
		// ->この応急処置が適切に行われていなかった（文字分割方法が修正されていなかった＆テストされていなかった）ことによる
		if (result.startsWith("Error")) {// 応急処置
			// Error:インスタンス図においてBのインスタンスからAのインスタンスへのリンク数が多重度の範囲を逸脱しています
			String clazzname1, clazzname2;
			int start1 = result.indexOf("において") + 4;
			int end1 = result.indexOf("のインスタンス");
			int start2 = result.indexOf("から") + 2;
			int end2 = result.lastIndexOf("のインスタンス");
			if (start1 < 0 || end1 < 0 || start2 < 0 || end2 < 0) {
				throw new RuntimeException("readResult() 解析エラー:"
						+ result.toString());
			}
			// 矛盾が起きているクラス名を取得
			clazzname1 = result.substring(start1, end1);
			clazzname2 = result.substring(start2, end2);
			return new RelationError(clazzname1, clazzname2);
		} else {
			return null;
		}
	}

	// // インスタンスチェッカーの結果から、矛盾が起きているクラスを特定するメソッド
	// // ただし、エラーメッセージの表記を変更した場合、こちらも変更の必要あり。
	// private void readResultMessageAndModifyInstanceDiagram(String result)
	// throws Exception {
	// String clazzname1, clazzname2;
	// if (!result.contains("Error")) {// 応急処置
	// int index1 = result.indexOf("から");
	// int index2 = result.indexOf("への");
	// // 矛盾が起きているクラス名を取得
	// clazzname1 = result.substring(0, index1);
	// clazzname2 = result.substring(index1 + 2, index2);
	// modifyInstanceDiagram(clazzname1, clazzname2);
	// }
	// }
}

class RelationError {
	private final String className1;
	private final String className2;

	/**
	 * @param className1
	 * @param className2
	 */
	public RelationError(String className1, String className2) {
		super();
		this.className1 = className1;
		this.className2 = className2;
	}

	/**
	 * @return the className1
	 */
	public String getClassName1() {
		return className1;
	}

	/**
	 * @return the className2
	 */
	public String getClassName2() {
		return className2;
	}

}