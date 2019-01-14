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
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileFilter;

import java.io.File;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.change_vision.jude.api.inf.model.ILink;
import com.change_vision.jude.api.inf.model.ILifelineLink;
import com.change_vision.jude.api.inf.model.ILifeline;
import com.change_vision.jude.api.inf.model.ILinkEnd;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
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
    
    private int instMaxNum;
    private int linkMaxNum;

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
			    JFileChooser filechooser = new JFileChooser("c:/WorkSpace/InstChecker/Instance-Dev/");
			    FileFilter filter = new FileNameExtensionFilter("XMLファイル", "xml");
			    filechooser.addChoosableFileFilter(filter);
			    filechooser.setAcceptAllFileFilterUsed(false);
			    int selected = filechooser.showOpenDialog(null);
			    if (selected == JFileChooser.APPROVE_OPTION){
			      File file = filechooser.getSelectedFile();
			      System.out.println(file.getName());
			      xml = new XmlReader(file.getPath());
			      //showObject(xml.Object);
			      textarea1.setText(xml.getObject().getScenario());
			    }else if (selected == JFileChooser.CANCEL_OPTION){
			    	System.out.println("キャンセルされました");
			    }else if (selected == JFileChooser.ERROR_OPTION){
			    	System.out.println("エラー又は取消しがありました");
			    }
			    
			}
		});

		checkButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				//診断のイベント
				if(xml != null){//xmlが選択されている場合
					getDiagram();//作成したオブジェクト図を同じモデルに格納
					
					//System.out.println("-----------------------");
			    	//showLinks(xml.getObject());
			    	//System.out.println("-----------------------");
			    	showLinks(createObject);
			    	
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
    	List<InstModel> cInstList = new ArrayList<InstModel>();//学習者の作ったリストの取得
    	cInstList = createObject.getInstList();
    	
    	List<LinkModel> cLinkList = new ArrayList<LinkModel>();
    	cLinkList = createObject.getLinkList();
    	
    	//インスタンスの診断
    	for(InstModel inst : cInstList){
    		checkInstList(inst);
    	}
    	
    	//リンクの診断
    	for(LinkModel link : cLinkList){
    		checkLinkList(link);
    	}
    }
    
    private void checkLinkList(LinkModel link){//引数は学習者が作成したリンク
    	//リンクの診断メソッド
    	List<LinkModel> xList = new ArrayList<LinkModel>();
    	LinkModel matchlink = new LinkModel();
    	InstModel cinst1 = new InstModel();
    	InstModel cinst2 = new InstModel();
    	InstModel xinst1 = new InstModel();
    	InstModel xinst2 = new InstModel();
    	xList =  xml.getObject().getLinkList();
		if(link != null){
			//リンク間のidに対応するインスタンスをcinst1,cinst2に取り出す
			for(int linkPoint:link.getLinkPointList()){
    			if(cinst1 != null)cinst2 = searchIdCreateInst(linkPoint);
    			else cinst1 = searchIdCreateInst(linkPoint);
			}
		}
    	
		for(LinkModel xLink : xList){
			if(xLink != null){
				//リンク間のidに対応するインスタンスをcinst1,cinst2に取り出す
				for(int linkPoint:link.getLinkPointList()){
	    			if(xinst1 == null)xinst1 = searchIdXmlInst(linkPoint);
	    			else xinst2 = searchIdXmlInst(linkPoint);
				}
				
				if(cinst1 != null && cinst2 != null){
			    	if((cinst1.getName().equals(xinst1.getName())&&cinst2.getName().equals(xinst2.getName()))
			    	||(cinst1.getName().equals(xinst2.getName())&&cinst2.getName().equals(xinst1.getName()))){
			    		matchlink = link;
			    		str = str + "インスタンス名："+cinst1.getName()+ "　インスタンス名:"+cinst2.getName()+"　のリンクは一致しています。"+"\n";
			    		return;
			    	}
				}
			}
		}
		str = str + "インスタンス名："+cinst1.getName()+ "　インスタンス名:"+cinst2.getName()+"　間のリンクは不要な可能性があります。"+"\n";
		return ;
    }
    
    //idでインスタンスを探す
    private InstModel searchIdCreateInst(int id) {
    	InstModel matchInst = new InstModel();
    	for(InstModel inst:createObject.getInstList()){
    		if(inst.getInstId()==id) matchInst = inst;
    	}
    	if(matchInst != null)return matchInst;
    	return null;
    }
    
    private InstModel searchIdXmlInst(int id) {
    	InstModel matchInst = new InstModel();
    	for(InstModel xinst:xml.getObject().getInstList()){
    		if(xinst.getInstId() == id){
    			matchInst = xinst;
    		}
    	}
    	if(matchInst != null)return matchInst;
    	return null;
    }
    
    private void checkInstList(InstModel inst){
    	InstModel xInst = new InstModel();
    	int instNum = 0;
    	List<InstModel> xmlList = new ArrayList<InstModel>();//xmlでの記述したListの取得
    	InstModel matchInst = new InstModel();
    	xmlList = xml.getObject().getInstList();
    	for(InstModel xinst : xmlList){
    		if(xinst != null){
		    	if(inst.getName().equals(xinst.getName())){
		    		instNum++;
		    		matchInst = xinst;
		    	}
    		}
    	}
    	//診断出力部
		if(instNum == 1){
    		//診断には必要ないので実際に動かすときには消す
    		str = str + "インスタンス名:"+inst.getName()+" は存在しています。"+"\n";
    		checkAttribute(inst,matchInst);//属性名,属性値を調べる
		}else if(instNum == 0){
    		str = str+ "インスタンス名:"+ inst.getName() +" インスタンス名が正しくない、または必要のないインスタンスの可能性\n";
    	}else if(instNum > 1){
			str = str + "インスタンス名:"+ inst.getName() +" 同名インスタンスが複数存在している\n";
		}
    }
    
    
    private InstModel checkOneInst(InstModel inst){
    	List<InstModel> xmlList = new ArrayList<InstModel>();//xmlでの記述したListの取得
    	InstModel matchInst = new InstModel();
    	xmlList = xml.getObject().getInstList();
    	
    	for(InstModel xinst : xmlList){
    		if(xinst != null){
		    	if(inst.getName().equals(xinst.getName())){
		    		matchInst = xinst;
		    	}
    		}
    	}
    	return matchInst;
    }
    
    private void checkAttribute(InstModel inst,InstModel xinst){
    	//インスタンス同士の属性名、属性値チェックを行う
    	//属性名の学習者ごとの命名→チェックのアルゴリズムについても考える（xmlに複数記述するなど）
    	//xinstはxml上のインスタンス
    	List<AttributeModel> cList = new ArrayList<AttributeModel>();
    	List<AttributeModel> xList = new ArrayList<AttributeModel>();
    	
    	cList = inst.getAttriList();//学習者の作成したオブジェクト図のインスタンスの属性リスト
    	xList = xinst.getAttriList();//xml
    	boolean findFlag;
    	
    	for(AttributeModel cAttri : cList){
    		//チェック用あとで削除
    		//System.out.println("インスタンス名："+inst.getName()+" 属性名:"+cAttri.getName()+cAttri.getValue());
    		if(cAttri != null){
	    		if(!isNullOrEmpty(cAttri.getName()) && !isNullOrEmpty(cAttri.getValue())){
	    			int listNum = 0;
	    			findFlag = false;
	    			for(AttributeModel xAttri : xList){
	    				
		    			//今の所完全一致の体で作っている変更するかも
				    	if(cAttri.getName().equals(xAttri.getName()) && 
				    						cAttri.getValue().equals(xAttri.getValue())&&findFlag == false){
				    		
				    		//診断には必要ないので実際に動かすときには消す
				    		str = str + "インスタンス名："+inst.getName()+ "属性名:"+cAttri.getName()+"　は一致しています。"+"\n";
				    		findFlag = true;
				    	}else if(listNum == xList.size() - 1 &&findFlag == false){
				    		str = str + "インスタンス名："+inst.getName()+ "属性名:"+cAttri.getName()+"　は見つかりませんでした。"+"\n";
				    	}
				    	listNum++;
	    			}
	    		}else if(!isNullOrEmpty(cAttri.getName()) && isNullOrEmpty(cAttri.getValue())){
	    			str = str + "インスタンス名："+inst.getName()+" 属性名:"+cAttri.getName()+"　の属性値が入力されていません"+"\n";
	    		}
    		}
    	}	
    }
    
    private void showLinks(ObjectModel object){
    	List<LinkModel> linkList = new ArrayList<LinkModel>();
    	
    	linkList = object.getLinkList();
    	for(LinkModel link:linkList){
    		if(link != null){
    			for(int i : link.getLinkPointList()){
				    //診断には必要ないので実際に動かすときには消す
				    System.out.println("linkpoint:"+i);
    			}
    		}
    	}
    }
    
    private void showObject(ObjectModel object){
    	List<InstModel> instList = new ArrayList<InstModel>();
    	List<AttributeModel> attriList = new ArrayList<AttributeModel>();
    	
    	
    	instList = object.getInstList();
    	for(InstModel inst:instList){
    		if(inst != null){
    			attriList = inst.getAttriList();
    			//System.out.println(inst.getName());
    			for(AttributeModel attri : attriList){
    				if(attri != null){
    					if(!(isNullOrEmpty(attri.getName()) && isNullOrEmpty(attri.getValue()))){
	    		
				    		//診断には必要ないので実際に動かすときには消す
				    		//System.out.println(attri.getName()+":"+attri.getValue());
						}
    				}
    			}
    		}
    	}
    }
    
    private boolean isNullOrEmpty(String str){//nullか空の文字列であればtrue
    	return str == null || str.isEmpty();
    }

    private void getDiagram() {
    	try {
			project = prjAccessor.getProject();
			createObject = new ObjectModel();
			
			instMaxNum = 0;
			linkMaxNum = 0;

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
		if(model instanceof ILink){//linkの取得
			ILink link = ILink.class.cast(model);
			saveLink(link);
		}
		
		if (model instanceof INamedElement) {//インスタンス以外の図
			INamedElement namedElement = INamedElement.class.cast(model);
			return;

		}
	}
	
	private void saveLink(ILink _link){//linkの取得
		System.out.println(_link.getName());//linkの名前（メッセージ部）の取得
		ILinkEnd[] linkEnds = _link.getMemberEnds();
		int instId;
		
		LinkModel link = new LinkModel();
		link.setLinkName(_link.getName());
		linkMaxNum++;//全体のリンクの数追加
		if(linkEnds != null){
			for(ILinkEnd linkEnd:linkEnds){
				link = new LinkModel();//LinkModel
				IInstanceSpecification inst = linkEnd.getType();
				instId = searchNameToIdCreateInst(inst.getName());
				link.addLinkPoint(instId);
			}
		}
		
		if(link != null){
			createObject.addLinkList(link);
		}
	}
	
    private int searchNameToIdCreateInst(String name) {
    	InstModel matchInst = new InstModel();
    	for(InstModel inst:createObject.getInstList()){
    		if(inst.getName().equals(name)) matchInst = inst;
    	}
    	return matchInst.getInstId();
    }

	private void saveInstanceSpecificationInfo(IInstanceSpecification instanceSpecification) {
		//図の属性名、属性値の取得
		
	    InstModel inst;//格納するための仮のinst
	    AttributeModel attri;
		
		inst = new InstModel();//一時的なインスタンス、後にaddをして追加

		//str = str + "instanceSpecification name : " + instanceSpecification.getName()+"\n";
		inst.setName(instanceSpecification.getName());
		
		ISlot[] slots = instanceSpecification.getAllSlots();//インスタンスの属性情報
		IClass c  = instanceSpecification.getClassifier();//インスタンスのクラスの情報
		inst.setInstId(instMaxNum);
		instMaxNum++;
		if(c != null){
			inst.setClassName(c.getName());//インスタンスのクラス名をセット
		}

		if(slots != null){
			for (ISlot slot : slots) {
				String s = slot.getDefiningAttribute()+"";//String変換方法がわからないので強引に作成
				if(!isNullOrEmpty(s)){
					attri = new AttributeModel();//一時的な属性名、属性値後にaddして追加
					attri.setName(s);//属性名の格納
					attri.setValue(slot.getValue());//属性値の格納
					inst.addAttriList(attri);
					//出力結果に出力（後に削除）
					//str = str + "attribute : " + slot.getDefiningAttribute() + ", value : " + slot.getValue() + "\n";
				}
			}
		}
		createObject.addInstList(inst);

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
    return "ObjectCheck";
  }

  public void activated() {
  }

  public void deactivated() {
  }
}