/**
 * AstahAPIUtils.java
 * Created at 2012/05/14
 * Copyright(c) 2012 Yoshiaki Matsuzawa All Rights Reserved
 */
package Slab.astah.common;

import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;

/**
 * @author macchan
 * 
 */
public class AstahAPIUtils {
	private AstahAPIUtils() {
	}

	// クラス図の定義
	// 1) IClassDiagram
	// 2) classが一つ以上ある　（それ以外はインスタンス図と見なす）
	public static IClassDiagram getTargetClassDiagram(
			IDiagramViewManager diagramViewManager) throws Exception {
		IDiagram diagram = diagramViewManager.getCurrentDiagram();

		if (!(diagram instanceof IClassDiagram)) {
			throw new Exception("クラス図が選択されていません");
		}

		IClassDiagram classDiagram = (IClassDiagram) diagram;
		for (IPresentation p : classDiagram.getPresentations()) {
			if (p.getModel() instanceof IClass) {
				return classDiagram;
			}
		}

		for (IPresentation p : classDiagram.getPresentations()) {
			if (p.getModel() instanceof IInstanceSpecification) {
				throw new Exception("クラス図が選択されていません(インスタンス図が選択されています)");
			}
		}

		throw new Exception("1つ以上のクラスを含むクラス図が選択されていません");
	}
}
