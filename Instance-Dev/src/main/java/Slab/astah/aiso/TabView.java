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
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.change_vision.jude.api.inf.model.ILinkEnd;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.ISlot;
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
	private final JButton xmlButton = new JButton("scenario");
	private final JButton checkButton = new JButton("Check!");
    private final JTextArea textarea1 = new JTextArea();
    private final JTextArea textarea2 = new JTextArea();

    private AstahAPI api;
    private ProjectAccessor prjAccessor;
    private IModel project;
    private String str;
    private XmlReader xml;//xmlから取得したオブジェクト図、情報の保存
    private ObjectModel createObject;//学習者の作成したオブジェクト図を保存
    private InstModel inst;//格納するための仮のinst
    private AttributeModel attri;

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

		xmlButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				//xmlの選択するイベント
				xml = new XmlReader();
				textarea1.setText(xml.getObject().getScenario());
			}
		});

		checkButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				//診断のイベント
				if(xml != null){//xmlが選択されている場合
					getDiagram();//作成したオブジェクト図を同じモデルに格納
					diagnoseObject();//診断
					textarea2.setText(str);
					str = "";
				}else {
					//xmlが選択されていないことを示す
				}
			}
		});

		panel.add(xmlButton);
		panel.add(checkButton);
		bigpanel.add(panel, BorderLayout.NORTH);
		bigpanel.add(scrollpane1,BorderLayout.WEST);
		bigpanel.add(scrollpane2,BorderLayout.EAST);
		return bigpanel;
	}

    private void diagnoseObject() {
    	//オブジェクト図とxmlの照らし合わせる診断を予定


    }

    private void getDiagram() {
    	try {
			project = prjAccessor.getProject();
			createObject = new ObjectModel();

			List<IPresentation> presentations = new ArrayList<IPresentation>();
			IDiagram[] diagrams = project.getDiagrams();
			for (IDiagram diagram : diagrams) {//図を取得し、リストに格納
	            presentations.addAll(Arrays.asList(diagram.getPresentations()));
	        }

			for(IPresentation presentation : presentations) {//インスタンス含んだ図の取得
				getPresentationInfo(presentation);
			}
		} catch (ProjectNotFoundException e) {
			e.printStackTrace();
		} catch(InvalidUsingException e) {
			e.printStackTrace();
		}
    }


	private void getPresentationInfo(IPresentation presentation) {//presentationの中身取り出し
		IElement model = presentation.getModel();
		if (model instanceof IInstanceSpecification) {//インスタンス
			IInstanceSpecification instanceSpecification = IInstanceSpecification.class.cast(model);
			saveInstanceSpecificationInfo(instanceSpecification);
			return;

		}
		if (model instanceof INamedElement) {//インスタンス以外の図
			INamedElement namedElement = INamedElement.class.cast(model);
			return;

		}
	}

	private void saveInstanceSpecificationInfo(IInstanceSpecification instanceSpecification) {
		//図の属性名、属性値の取得
		inst = new InstModel();//一時的なインスタンス、後にaddをして追加
		attri = new AttributeModel();//一時的な属性名、属性値後にaddして追加

		str = str + "instanceSpecification name : " + instanceSpecification.getName()+"\n";
		inst.setName(instanceSpecification.getName());

		ISlot[] slots = instanceSpecification.getAllSlots();//インスタンスの属性情報
		IClass c  = instanceSpecification.getClassifier();//インスタンスのクラスの情報
		ILinkEnd[] links = instanceSpecification.getLinkEnds();//インスタンスのリンク端の情報

		inst.setClassName(c.getName());//インスタンスのクラス名をセット

		for (ISlot slot : slots) {
			String s = slot.getDefiningAttribute()+"";//String変換方法がわからないので強引に作成
			attri.setName(s);//属性名の格納
			attri.setValue(slot.getValue());//属性値の格納

			//出力結果に出力（後に削除）
			str = str + "attribute : " + slot.getDefiningAttribute() + ", value : " + slot.getValue() + "\n";
		}
		inst.addAttriList(attri);
		createObject.addInstList(inst);

		//リンクのモデルへの格納とapiの理解がまだ・・・

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