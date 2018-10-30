package Slab.astah.aiso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.ISlot;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.ui.IPluginActionDelegate;
import com.change_vision.jude.api.inf.ui.IWindow;

public class ShowInstance implements IPluginActionDelegate {

	public Object run(IWindow window) {
		try {
			AstahAPI api = AstahAPI.getAstahAPI();
			ProjectAccessor prjAccessor = api.getProjectAccessor();
			IModel project = prjAccessor.getProject();

			List<IPresentation> presentations = new ArrayList<IPresentation>();
			IDiagram[] diagrams = project.getDiagrams();
			for (IDiagram diagram : diagrams) {//図を取得し、リストに格納
                presentations.addAll(Arrays.asList(diagram.getPresentations()));
            }

			System.out.println("Printing the InstanceSpecification");
			System.out.println("---");

			for(IPresentation presentation : presentations) {//インスタンスの表示
				printPresentationInfo(presentation);
				System.out.println("---");
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (ProjectNotFoundException e) {
			e.printStackTrace();
		} catch(InvalidUsingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void printPresentationInfo(IPresentation presentation) {//presentationの中身取り出し
		IElement model = presentation.getModel();
		if (model instanceof IInstanceSpecification) {//インスタンス
			IInstanceSpecification instanceSpecification = IInstanceSpecification.class.cast(model);
			printInstanceSpecificationInfo(instanceSpecification);
			return;
		}
		if (model instanceof INamedElement) {//インスタンス以外の図
			INamedElement namedElement = INamedElement.class.cast(model);
			System.out.println(namedElement.getName() + " is Not InstanceSpecification.");
			return;
		}
		System.out.println("This Presentation is Not InstanceSpecification.");
	}

	private static void printInstanceSpecificationInfo(IInstanceSpecification instanceSpecification) {
		System.out.println("instanceSpecification name : " + instanceSpecification.getName());
		ISlot[] slots = instanceSpecification.getAllSlots();//インスタンスの情報
		for (ISlot slot : slots) {
			IAttribute attribute = slot.getDefiningAttribute();
			String value = slot.getValue();
			System.out.println("attribute : " + attribute + ", value : " + value);
		}
	}

	private void test() {

	}

}