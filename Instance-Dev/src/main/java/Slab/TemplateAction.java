package Slab;



import java.awt.BorderLayout;


import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.project.ProjectAccessorFactory;
import com.change_vision.jude.api.inf.ui.IPluginActionDelegate;
import com.change_vision.jude.api.inf.ui.IWindow;

public class TemplateAction implements IPluginActionDelegate {

	public Object run(IWindow window) throws UnExpectedException {
	    try {
	        ProjectAccessor projectAccessor = ProjectAccessorFactory.getProjectAccessor();
	        projectAccessor.getProject();
	       // JOptionPane.showMessageDialog(window.getParent(),"Hello");
	        
	        
 ///////////////////////////////
	        
            JFrame frame = new JFrame();
            frame.setSize( 600, 500 );
            frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

            // 場所
            frame.setLocationByPlatform( true );

            // タイトル
            frame.setTitle( "constraints" );

            
            JLabel label = new JLabel("ラベル");

            frame.getContentPane().add(label, BorderLayout.NORTH);

            frame.setVisible(true);
	       
            
            
///////////////////////////////////            
	        
	    } catch (ProjectNotFoundException e) {
	        String message = "Project is not opened.Please open the project or create new project.";
			JOptionPane.showMessageDialog(window.getParent(), message, "Warning", JOptionPane.WARNING_MESSAGE); 
	    } catch (Exception e) {
	    	JOptionPane.showMessageDialog(window.getParent(), "Unexpected error has occurred.", "Alert", JOptionPane.ERROR_MESSAGE); 
	        throw new UnExpectedException();
	    }
	    return null;
	}

////////////////////////////////////////////
	
	
	
	
	
	
}
