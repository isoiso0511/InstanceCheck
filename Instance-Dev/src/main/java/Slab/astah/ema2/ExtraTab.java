package Slab.astah.ema2;




/*
 * パッケージ名は、生成したプラグインのパッケージ名よりも下に移してください。
 * プラグインのパッケージ名=> com.example
 *   com.change_vision.astah.extension.plugin => X
 *   com.example                              => O
 *   com.example.internal                     => O
 *   learning                                 => X
 */


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
//import javax.swing.JScrollPane;

import Slab.astah.common.AstahAPIUtils;
import Slab.astah.common.AstahAPIUtilsInstance;
import Slab.astah.common.AstahRecorder;

import com.change_vision.jude.api.inf.editor.ClassDiagramEditor;
import com.change_vision.jude.api.inf.editor.TransactionManager;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.presentation.IPresentation;
//import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.project.ProjectAccessorFactory;
import com.change_vision.jude.api.inf.project.ProjectEvent;
import com.change_vision.jude.api.inf.project.ProjectEventListener;
import com.change_vision.jude.api.inf.ui.IPluginExtraTabView;
import com.change_vision.jude.api.inf.ui.ISelectionListener;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;

public class ExtraTab extends JPanel  implements IPluginExtraTabView, ProjectEventListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExtraTab() {
    initComponents();
  }

  private void initComponents() {
    setLayout(new BorderLayout());
    add(createButtonPane(), BorderLayout.CENTER);
   // addProjectEventListener();
  }
/**
  private void addProjectEventListener() {
  try {
    AstahAPI api = AstahAPI.getAstahAPI();
    ProjectAccessor projectAccessor = api.getProjectAccessor();
    projectAccessor.addProjectEventListener(this);
  } catch (ClassNotFoundException e) {
    e.getMessage();
  }
  }
**/
  
  
  //生成ボタン作成
  private Container createButtonPane() {
    JButton button = new JButton("インスタンス数指定");
    
    JLabel iLabel = new JLabel("　クラスごとにインスタンス数、インスタンス名を指定してインスタンスを生成します(クラス図を選択した状態で押してください)" +
    		"");
    
    JButton Gbutton = new JButton("インスタンス図生成");//
    
    JLabel lLabel = new JLabel("　インスタンス間にリンクの線を張りインスタンス図を完成させます（リンクを張りたいインスタンス図or中間インスタンス図を選択した状態で押してください）");
    
    JButton Cbutton = new JButton("インスタンス図チェック");
    
    JLabel cLabel = new JLabel("　インスタンス図がクラス図と矛盾がないかチェックすることができます(チェックするインスタンス図を選択した状態で押してください)" +
    		"");
    
    
    JPanel pane = new JPanel();
    pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
   // JPanel pane1 = new JPanel();
   // JPanel pane2 = new JPanel();
    
   // pane.setLayout(new BorderLayout());
   
    
    
    //インスタンス数指定
    button.addActionListener(new ActionListener()
    	{
    		public void actionPerformed(ActionEvent e){ 			   			  			
    			doInstanceGenerate();
    		}
    	}
    );
    
    //リンク生成
    Gbutton.addActionListener(new ActionListener()
		{
    	public void actionPerformed(ActionEvent e){
    		doLinkGenerate();
			}
		}
    );
    
    //インスタンス図チェック
    Cbutton.addActionListener(new ActionListener()
		{
    	public void actionPerformed(ActionEvent e){
    		doDiagramCheck();
			}
		}
    );
    
    
    
    JLabel space = new JLabel(" ");
    
    pane.add(space);
    pane.add(button);
    pane.add(iLabel);
    pane.add(space);
    pane.add(Gbutton);
    pane.add(lLabel);
    pane.add(space);
    pane.add(Cbutton);
    pane.add(cLabel);
    
    //pane.add(pane1);
    //pane.add(pane2);
    return pane;
  }

  //add ema
  private void doDiagramCheck(){
	  
		JFrame _astahFrame = null;
		try {
			AstahRecorder.getInstance().record("check");
			_astahFrame = ProjectAccessorFactory.getProjectAccessor()
					.getViewManager().getMainFrame();

			// インスタンス図の作成
			TransactionManager.beginTransaction();
			ProjectAccessor projectAccessor = ProjectAccessorFactory
					.getProjectAccessor();
			IDiagramViewManager diagramViewManager = projectAccessor
					.getViewManager().getDiagramViewManager();
			IClassDiagram targetInstanceDiagram = AstahAPIUtilsInstance   //インスタンス図を取得、判定
					.getTargetClassDiagram(diagramViewManager);
			
			//add ema
			IPackage pack = (IPackage) targetInstanceDiagram.getContainer();
			IClassDiagram targetClassDiagram = getPackedClassDiagram(pack);
			
			checkAndRefineInstances(targetClassDiagram, targetInstanceDiagram);//チェック

			TransactionManager.endTransaction();

			/**
			// インスタンス図をビューに配置。クラス図と左右分割で表示する。
			diagramViewManager.closeAll();
			diagramViewManager.open(targetClassDiagram);
			diagramViewManager.open(targetInstanceDiagram);


			// diagramViewManager.open(targetClassDiagram);// selection
			doVerticalAlign(projectAccessor);                //左右に配置
			 **/
			
			
		} catch (Exception ex) { //エラー時
			if (TransactionManager.isInTransaction()) {
				TransactionManager.abortTransaction(); //
			}
			ex.printStackTrace();
			JOptionPane.showMessageDialog(_astahFrame, ex.getMessage(),
					"Exception was caught", JOptionPane.ERROR_MESSAGE);
		}
	  
	  
	  
  }
  
	private void checkAndRefineInstances(IClassDiagram ClassDiagram,
			IClassDiagram InstanceDiagram) throws Exception {
		// nozawa作、インスタンスチェッカーによる生成したインスタンス図の検査

		List<RelationError> errors = RelationCheckerAdapter.check(
				ClassDiagram, InstanceDiagram);
		if (errors.size() > 0) {
			throw new Exception("インスタンス図「" +
					InstanceDiagram.getName() +
					"」に矛盾している箇所があります．(Checker Error)");
		}
		
		JLabel message = new JLabel( "インスタンス図「" +
					InstanceDiagram.getName() +
					"」に矛盾している箇所はありません．(Checker OK)");
		JOptionPane.showMessageDialog(null, message);
	}
	
	
	
  //GenerateInstance
  private void doInstanceGenerate(){
	  
		JFrame _astahFrame = null;
		try {
			AstahRecorder.getInstance().record("generateInstance");
			_astahFrame = ProjectAccessorFactory.getProjectAccessor()
					.getViewManager().getMainFrame();

			// インスタンス図の作成
			TransactionManager.beginTransaction();
			ProjectAccessor projectAccessor = ProjectAccessorFactory
					.getProjectAccessor();
			IDiagramViewManager diagramViewManager = projectAccessor
					.getViewManager().getDiagramViewManager();
			IClassDiagram targetClassDiagram = AstahAPIUtils   //クラス図を取得、判定
					.getTargetClassDiagram(diagramViewManager);
			IClassDiagram newInstanceDiagram = InstanceGenerator
					.generate(targetClassDiagram);      //インスタンス図を作成
			
			ClassDiagramEditor cde = projectAccessor.getDiagramEditorFactory().getClassDiagramEditor();
			if(IFmaker.getEndFlag()!=1) cde.delete(newInstanceDiagram);
			
			TransactionManager.endTransaction();

			if(IFmaker.getEndFlag()==1){
			// インスタンス図をビューに配置。クラス図と左右分割で表示する。
			diagramViewManager.closeAll();
			diagramViewManager.open(targetClassDiagram);
			diagramViewManager.open(newInstanceDiagram);
			// diagramViewManager.open(targetClassDiagram);// selection
			doVerticalAlign(projectAccessor);                //左右に配置
			}
		} catch (Exception ex) { //エラー時
			if (TransactionManager.isInTransaction()) {
				TransactionManager.abortTransaction(); //
			}
			ex.printStackTrace();
			JOptionPane.showMessageDialog(_astahFrame, ex.getMessage(),
					"Exception was caught", JOptionPane.ERROR_MESSAGE);
		}
	  
	  
  }
  
  
  //GenerateLink
  private void doLinkGenerate(){
	    
		JFrame _astahFrame = null;
		try {
			AstahRecorder.getInstance().record("generateLink");
			_astahFrame = ProjectAccessorFactory.getProjectAccessor()
					.getViewManager().getMainFrame();

			// インスタンス図の作成
			TransactionManager.beginTransaction();
			ProjectAccessor projectAccessor = ProjectAccessorFactory
					.getProjectAccessor();
			IDiagramViewManager diagramViewManager = projectAccessor
					.getViewManager().getDiagramViewManager();
			IClassDiagram targetInstanceDiagram = AstahAPIUtilsInstance   //クラス図を取得、判定
					.getTargetClassDiagram(diagramViewManager);
			
			//add ema
			IPackage pack = (IPackage) targetInstanceDiagram.getContainer();
			IClassDiagram targetClassDiagram = getPackedClassDiagram(pack);
			
			
			IClassDiagram newInstanceDiagram = LinkGenerator
					.generate(targetClassDiagram,targetInstanceDiagram);      //インスタンス図を作成
			TransactionManager.endTransaction();

			// インスタンス図をビューに配置。クラス図と左右分割で表示する。
			diagramViewManager.closeAll();
			diagramViewManager.open(targetClassDiagram);
			diagramViewManager.open(targetInstanceDiagram);

			diagramViewManager.open(newInstanceDiagram);
			// diagramViewManager.open(targetClassDiagram);// selection
			doVerticalAlign(projectAccessor);                //左右に配置

		} catch (Exception ex) { //エラー時
			if (TransactionManager.isInTransaction()) {
				TransactionManager.abortTransaction(); //
			}
			ex.printStackTrace();
			JOptionPane.showMessageDialog(_astahFrame, ex.getMessage(),
					"Exception was caught", JOptionPane.ERROR_MESSAGE);
		}
	  
	  
  }
  
	//add ema
	private IClassDiagram getPackedClassDiagram(INamedElement container) throws Exception{
		IClassDiagram ClassD;
		//String name = "クラス図";
		for (IDiagram d : container.getDiagrams()) {
			if (checkClassDiagram(d)) {//hennkou
				ClassD = (IClassDiagram)d;
				return ClassD;
			}
		}
		throw new Exception("1つ以上のクラスを含むクラス図がありません");
	}
	
	//add ema
	private boolean checkClassDiagram(IDiagram d) throws InvalidUsingException{
		IClassDiagram classDiagram = (IClassDiagram) d;
		for (IPresentation p : classDiagram.getPresentations()) {
			if (p.getModel() instanceof IClass) {
				return true;
			}
		}
		return false;
	}
  
  
  //クラス図とオブジェクト図を横に並べる
  private void doVerticalAlign(ProjectAccessor projectAccessor) throws Exception{
	  
	  
		// astahのメインフレームからastahのメニューバーを取得
		JMenuBar myMenuBar = projectAccessor.getViewManager().getMainFrame()
				.getJMenuBar();
		for (int i = 0; i < myMenuBar.getComponents().length; i++) {
			String menuWindow = "managementview.menu.window";
			if (menuWindow.equals(myMenuBar.getComponent(i).getName())) {
				// メニューバーのウィンドウだったとき。
				JMenu myWindowMenu = (JMenu) myMenuBar.getComponent(i);
				// ウィンドウメニューのポップアップメニューをさらに取得。
				JPopupMenu myPopupMenu = myWindowMenu.getPopupMenu();
				String popupWindowMenu = "managementview.menu.window.alignment";
				for (int j = 0; j < myPopupMenu.getComponents().length; j++) {
					if (popupWindowMenu.equals(myPopupMenu.getComponent(j)
							.getName())) {
						// 整列のポップアップメニューを取得してきたとき。
						JMenu alignMenu = (JMenu) myPopupMenu.getComponent(j);
						for (int k = 0; k < alignMenu.getPopupMenu()
								.getComponents().length; k++) {
							String alignName = "managementview.menu.window.vertical";
							if (alignName.equals(alignMenu.getPopupMenu()
									.getComponent(k).getName())) {
								JMenuItem horizontal = (JMenuItem) alignMenu
										.getPopupMenu().getComponent(k);
								horizontal.doClick();
							}
						}
					}
				}
			}
		}
	  
	  
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
    return "AutoInstanceDiagramGenerator2";
  }

  @Override
  public String getTitle() {
    return "インスタンス図自動生成";
  }

  public void activated() {
  }

  public void deactivated() {
  }
}
