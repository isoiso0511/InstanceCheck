/**
 * ExtraTabView.java
 * create at 2011/11/30
 */
package Slab.astah.nzwPkg;

/**
 * @author cs07071
 *
 */
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import Slab.astah.common.AstahRecorder;

import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.project.ProjectAccessorFactory;
import com.change_vision.jude.api.inf.project.ProjectEvent;
import com.change_vision.jude.api.inf.project.ProjectEventListener;
import com.change_vision.jude.api.inf.ui.IPluginExtraTabView;
import com.change_vision.jude.api.inf.ui.ISelectionListener;

public class ExtraTabView extends JPanel implements IPluginExtraTabView,
		ProjectEventListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExtraTabView() {
		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		add(createLabelPane(), BorderLayout.CENTER);
		addProjectEventListener();
	}

	private void addProjectEventListener() {
		try {
			ProjectAccessor projectAccessor = ProjectAccessorFactory
					.getProjectAccessor();
			projectAccessor.addProjectEventListener(this);
		} catch (ClassNotFoundException e) {
		}
	}

	private final JPanel panel = new JPanel();
	private final JPanel bigpanel = new JPanel();
	// private final JLabel label = new JLabel("チェックするクラス図");
	private final JButton checkButton = new JButton("Check!");
	private final JTextArea textarea = new JTextArea();

	private Container createLabelPane() {
		BorderLayout borderLayout = new BorderLayout();
		borderLayout.setVgap(5);

		panel.setLayout(new FlowLayout());
		panel.add(checkButton);
		bigpanel.setLayout(borderLayout);
		bigpanel.add(panel, BorderLayout.NORTH);

		final JScrollPane scrollpane = new JScrollPane(textarea);
		scrollpane.setPreferredSize(new Dimension(100, 100));
		bigpanel.add(scrollpane, BorderLayout.CENTER);

		textarea.setSize(300, 400);
		textarea.setText("ここに出力");
		textarea.setEditable(false);
		// panel2.add(textarea);

		checkButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame _astahFrame = null;
				try {
					_astahFrame = ProjectAccessorFactory.getProjectAccessor()
							.getViewManager().getMainFrame();
					AstahRecorder.getInstance().record("check");
					InstanceChecker iCheck = new InstanceChecker();
					List<CheckResult> results = iCheck.action();
					showResult(results);
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(_astahFrame, ex.getMessage(),
							"Exception was caught", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		return bigpanel;
	}

	private void showResult(List<CheckResult> results) throws Exception {
		String text = new String();
		if (results.isEmpty() == true) {
			text = "Check is OK!\n";
		} else {

			/*
			 * if (result.size() % 2 == 0) { for (int i = 0; i <= (result.size()
			 * + 8) / 2; i++) { result.remove(0); } } else { for (int i = 0; i
			 * <= (result.size() + 5) / 2; i++) { result.remove(0); } }
			 */

			// 2倍になっているので，半分切り取り（adhocな解決）
			/*
			 * int half = results.size() / 2; List<CheckResult> newResults = new
			 * ArrayList<CheckResult>(); for (int i = 0; i < half; i++) {
			 * newResults.add(results.get(i)); } results = newResults;
			 */
			for (CheckResult result : results) {
				text += result + "\n";
			}

		}
		textarea.setText(text);
		// } catch (Exception e) {
		// textarea.setText("エラーが発生しました。クラス図を選択した状態でチェックボタンを押してください\n");
		// }
	}

	/*
	 * public void instanceCheck() throws Exception { InstanceChecker iCheck =
	 * new InstanceChecker();
	 * 
	 * }
	 */

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
		return "Show Hello World here";
	}

	@Override
	public String getTitle() {
		return "InstanceChecker";
	}

	@Override
	public void activated() {
	}

	@Override
	public void deactivated() {
	}

	// private JPanel getJPanel() {
	// GridBagConstraints gbc = new GridBagConstraints();
	// GridBagConstraints gbc2 = new GridBagConstraints();
	// GridBagConstraints gbc3 = new GridBagConstraints();
	//
	// gbc.gridx = 1;
	// gbc.gridy = 0;
	// gbc.anchor = GridBagConstraints.WEST;
	// gbc.fill = GridBagConstraints.BOTH;
	// gbc.gridwidth = 100;
	// gbc.weightx = 1.0;
	// gbc.insets = new Insets(3, 5, 0, 0);
	// gbc2.gridx = 0;
	// gbc2.gridy = 0;
	// gbc3.gridwidth = 1;
	// gbc3.insets = new Insets(0, 10, 0, 1000);
	//
	// JPanel jPanel = new JPanel();
	// jPanel.setLayout(new GridBagLayout());
	// jPanel.add(label, gbc2);
	// jPanel.add(new JTextField(), gbc);
	// jPanel.add(checkButton, gbc3);
	//
	// return jPanel;
	// }

}
