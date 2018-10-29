/**
 * AstahAPIChecker.java
 * Created at 2012/05/12
 * Copyright(c) 2012 Yoshiaki Matsuzawa All Rights Reserved
 */



//

package Slab.astah.common;

import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.project.ProjectAccessorFactory;

/**
 * @author macchan
 */
public class AstahAPIChecker {

	public static void check() {
		try {
			new AstahAPIChecker().checkImpl();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private AstahAPIChecker() {
	}

	private int indent = 0;

	private void print(IElement elm) {
		for (int i = 0; i < indent; i++) {
			System.out.print("\t");
		}
		System.out.println(elm + " (" + elm.getClass().getName() + ")"
				+ ", owner=" + elm.getOwner() + ", container="
				+ elm.getContainer());
	}

	private void checkImpl() throws Exception {
		System.out
				.println("-------------------APIChecker start----------------------");
		ProjectAccessor projectAccessor = ProjectAccessorFactory
				.getProjectAccessor();
		IModel currentProject = projectAccessor.getProject();
		printElement(currentProject);
		System.out
				.println("-------------------APIChecker end----------------------");
	}

	/**
	 * @param currentProject
	 */
	private void printElement(INamedElement model) {
		if ("java".equals(model.getName())) {
			return;
		}
		print(model);
		if (model instanceof IPackage) {
			indent++;
			for (INamedElement child : ((IPackage) model).getOwnedElements()) {
				printElement(child);
			}
			indent--;
		}
	}
}
