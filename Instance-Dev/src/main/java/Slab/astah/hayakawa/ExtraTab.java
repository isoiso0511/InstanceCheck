package Slab.astah.hayakawa;




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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import Slab.astah.common.AstahAPIUtils;
import Slab.astah.common.AstahRecorder;

import com.change_vision.jude.api.inf.editor.TransactionManager;
import com.change_vision.jude.api.inf.model.IClassDiagram;
//import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.project.ProjectAccessorFactory;
import com.change_vision.jude.api.inf.project.ProjectEvent;
import com.change_vision.jude.api.inf.project.ProjectEventListener;
import com.change_vision.jude.api.inf.ui.IPluginExtraTabView;
import com.change_vision.jude.api.inf.ui.ISelectionListener;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;

public class ExtraTab extends JPanel  implements IPluginExtraTabView, ProjectEventListener {

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
    JButton button = new JButton("Generate.");
    
   // JButton Cbutton = new JButton("条件指定へ");//未完
    
    JPanel pane = new JPanel();
    
    //条件なし
    button.addActionListener(new ActionListener()
    	{
    		public void actionPerformed(ActionEvent e){
    			doGenerate();
    		}
    	}
    );
  /*  
    //条件付き
    Cbutton.addActionListener(new ActionListener()
		{
    	public void actionPerformed(ActionEvent e){
    		doGenerate();
			}
		}
    );
    */
    pane.add(button);
  //  pane.add(Cbutton);
    return pane;
  }

  
  //Generate
  private void doGenerate(){
	  
	  
	  
	  
	  //未完
		JFrame _astahFrame = null;
		try {
			AstahRecorder.getInstance().record("generate");
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
			IClassDiagram newInstanceDiagram = InstanceDiagramGenerator
					.generate(targetClassDiagram);      //インスタンス図を作成
			TransactionManager.endTransaction();

			// インスタンス図をビューに配置。クラス図と左右分割で表示する。
			diagramViewManager.closeAll();
			diagramViewManager.open(targetClassDiagram);
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
    return "AutoInstanceDiagramGenerator";
  }

  @Override
  public String getTitle() {
    return "AutoIDG";
  }

  public void activated() {
  }

  public void deactivated() {
  }
}
