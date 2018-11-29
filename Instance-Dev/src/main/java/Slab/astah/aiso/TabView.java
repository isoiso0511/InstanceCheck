package Slab.astah.aiso;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.project.ProjectEvent;
import com.change_vision.jude.api.inf.project.ProjectEventListener;
import com.change_vision.jude.api.inf.ui.IPluginExtraTabView;
import com.change_vision.jude.api.inf.ui.ISelectionListener;

public class TabView extends JPanel
      implements IPluginExtraTabView, ProjectEventListener {
	private final JPanel panel = new JPanel();
	private final JPanel bigpanel = new JPanel();
	private final JButton checkButton = new JButton("Check!");
    private final JTextArea textarea1 = new JTextArea();
    private final JTextArea textarea2 = new JTextArea();

    private AstahAPI api;
    private ProjectAccessor prjAccessor;
    private IModel project;
    private String str;
    private XmlReader xml;

    public TabView() {
    	try {
    		api = AstahAPI.getAstahAPI();
    		prjAccessor = api.getProjectAccessor();
    		prjAccessor.addProjectEventListener(this);
    	} catch (ClassNotFoundException e) {
    		e.getMessage();
    	}
    	initComponents();
    }

    private void initComponents() {
		setLayout(new BorderLayout());
		add(createLabelPane(),BorderLayout.WEST);
		//addProjectEventListener();
	}

    private Container createLabelPane(){
    	panel.setLayout(new FlowLayout());
    	final JScrollPane scrollpane1 = new JScrollPane(textarea1);
    	final JScrollPane scrollpane2 = new JScrollPane(textarea2);
    	scrollpane1.setPreferredSize(new Dimension(600,200));
    	scrollpane2.setPreferredSize(new Dimension(600,200));


		textarea1.setSize(300,400);
		textarea1.setText("scenario");
		textarea1.setEditable(false);

		textarea2.setSize(300,400);
		textarea2.setText("output");
		textarea2.setEditable(false);

		checkButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				//ボタンクリック時のイベント

				showDiagram();
				xml = new XmlReader();
				textarea1.setText(xml.getObject().getScenario());
				textarea2.setText(str);
				str = "";
			}
		});

		panel.add(checkButton);
		bigpanel.add(panel, BorderLayout.NORTH);
		bigpanel.add(scrollpane1,BorderLayout.WEST);
		bigpanel.add(scrollpane2,BorderLayout.EAST);
		return bigpanel;
	}

    private void showDiagram() {
    	try {
			api = AstahAPI.getAstahAPI();
			prjAccessor = api.getProjectAccessor();
			project = prjAccessor.getProject();

			List<IPresentation> presentations = new ArrayList<IPresentation>();
			IDiagram[] diagrams = project.getDiagrams();
			for (IDiagram diagram : diagrams) {//図を取得し、リストに格納
	            presentations.addAll(Arrays.asList(diagram.getPresentations()));
	        }
			//str = str + "Printing the InstanceSpecification"+ "\n";
			//str = str + "---"+"\n";

			for(IPresentation presentation : presentations) {//インスタンスの表示
				printPresentationInfo(presentation);
				//str = str +"---"+"\n";
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (ProjectNotFoundException e) {
			e.printStackTrace();
		} catch(InvalidUsingException e) {
			e.printStackTrace();
		}
    }


	private void printPresentationInfo(IPresentation presentation) {//presentationの中身取り出し
		IElement model = presentation.getModel();
		if (model instanceof IInstanceSpecification) {//インスタンス
			IInstanceSpecification instanceSpecification = IInstanceSpecification.class.cast(model);
			printInstanceSpecificationInfo(instanceSpecification);
			return;
		}
		if (model instanceof INamedElement) {//インスタンス以外の図
			INamedElement namedElement = INamedElement.class.cast(model);
			//str = str+" is Not InstanceSpecification.\n";
			return;
		}
		//str = str + "This Presentation is Not InstanceSpecification.\n";
	}

	private void printInstanceSpecificationInfo(IInstanceSpecification instanceSpecification) {
		/*
		str = str + "instanceSpecification name : " + instanceSpecification.getName()+"\n";
		ISlot[] slots = instanceSpecification.getAllSlots();//インスタンスの情報
		for (ISlot slot : slots) {
			IAttribute attribute = slot.getDefiningAttribute();
			String value = slot.getValue();
			str = str + "attribute : " + attribute + ", value : " + value + "\n";
		}
		*/
		System.out.println(xml.getObject().getInst(1).getName());
		/*
		for(int i = 0;i < xml.getObject().getInstList().size();i++) {
			if(instanceSpecification.getName().equals(xml.getObject().getInst(i).getName())) {
				str = xml.getObject().getInst(i).getName()+"が見つかりました\n";
			}else {
				str = xml.getObject().getInst(i).getName()+"が見つかりません\n";
			}
		}
		*/
	}

  @Override
  public void projectChanged(ProjectEvent e) {
  }

  @Override
  public void projectClosed(ProjectEvent e) {
  }

   @Override
  public void projectOpened(ProjectEvent e) {
  }

  @Override
  public void addSelectionListener(ISelectionListener listener) {
  }

  @Override
  public Component getComponent() {
    return this;
  }

  @Override
  public String getDescription() {
    return "tabtabtab";
  }

  @Override
  public String getTitle() {
    return "InstanceCheck";
  }

  public void activated() {
  }

  public void deactivated() {
  }
}