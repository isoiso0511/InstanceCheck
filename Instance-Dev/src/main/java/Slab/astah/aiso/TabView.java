package Slab.astah.aiso;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.change_vision.jude.api.inf.AstahAPI;
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

    public TabView() {
    	initComponents();
    }

  	private void initComponents() {
		setLayout(new BorderLayout());
		add(createLabelPane(),BorderLayout.WEST);
		addProjectEventListener();
  	}

  	private Container createLabelPane(){
	  panel.setLayout(new FlowLayout());
	  panel.add(checkButton);
	  bigpanel.add(panel, BorderLayout.NORTH);

	  final JScrollPane scrollpane1 = new JScrollPane(textarea1);
	  final JScrollPane scrollpane2 = new JScrollPane(textarea2);
	  scrollpane1.setPreferredSize(new Dimension(400,200));
	  scrollpane2.setPreferredSize(new Dimension(400,200));
	  bigpanel.add(scrollpane1,BorderLayout.WEST);
	  bigpanel.add(scrollpane2,BorderLayout.EAST);

	  textarea1.setSize(300,400);
	  //textarea.setText("input")???
	  textarea1.setEditable(true);

	  textarea2.setSize(300,400);
	  //textarea.setText("input")???
	  textarea2.setEditable(false);

	  checkButton.addActionListener(new ActionListener(){
		  public void actionPerformed(ActionEvent e){

		  }
	  });
	  return bigpanel;
  }

  private void addProjectEventListener() {
	  try {
	    AstahAPI api = AstahAPI.getAstahAPI();
	    ProjectAccessor projectAccessor = api.getProjectAccessor();
	    projectAccessor.addProjectEventListener(this);
	  } catch (ClassNotFoundException e) {
	    e.getMessage();
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
    return "tabtabtab";
  }

  @Override
  public String getTitle() {
    return "TabTest";
  }

  public void activated() {
  }

  public void deactivated() {
  }
}