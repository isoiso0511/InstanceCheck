package Slab.astah.ema2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;





import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IGeneralization;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class IFmaker extends JFrame{

	  /**
	 *   if generateInstance
	 */
	private static final long serialVersionUID = 1L;

	private String[] columnNames = {"インスタンス名"};

	DefaultTableModel[] tableModel;
	
	//for instance count
	private JLabel[] labels;
	private JTextField[] texts;
	
	//for instance name
	private JPanel[] tab;
	private JTable[] table; 
	private JScrollPane[] sp;
	
	//to reset
	//private JButton[] Rbutton;
	private static final int tableSize = 15;
	public static int countnum=0;
	private static List<IClass> Classes2;
	
	private static int option = -1;
	
	//to generate
	//private static boolean endFlag = false;
	
	/**
	public static void main(String[] args){
		List<String> classes = null; 
		
		String a = new String("test1");
		
		classes.add(a);
	    IFmaker frame = new IFmaker(classes);

	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setBounds(10, 10, 500, 400);
	    frame.setTitle("インスタンス入力");
	    frame.setVisible(true);
	  }
**/
	
	  IFmaker(List<IClass> classes){
		  
		  tab = new JPanel[classes.size()];
		  labels = new JLabel[classes.size()];
		  texts = new JTextField[classes.size()];
		  table = new JTable[classes.size()];
		  sp = new JScrollPane[classes.size()];
		  tableModel  = new DefaultTableModel[classes.size()];
		 // Rbutton = new JButton[classes.size()];
		  Classes2 = classes;
		  
		  //north
		  JPanel top = new JPanel();
		  top.setLayout(new BorderLayout());
		  JLabel start= new JLabel("各クラスごとに生成するインスタンス数、インスタンス名を記入してください。");
		  	
		  JLabel start2= new JLabel("入力がない場合、そのクラスの生成されるインスタンスの数はランダムになります。");
		  
		  top.add(start,BorderLayout.NORTH);
		  top.add(start2,BorderLayout.SOUTH);
		  
	    //center//
	    JTabbedPane tabbedpane = new JTabbedPane();
	    IGeneralization[] generalizations;
	    
	    
    	//toSubType
	    for(int i =0 ; i<classes.size();i++){	
	    	if(classes.get(i).isAbstract()){
	    		Classes2.remove(i);
	    		generalizations = classes.get(i).getSpecializations();
	    		for(int j= 0;j<generalizations.length;j++){
	    			Classes2.add(generalizations[i].getSubType());
				}
			}
	    }
	    
	    
	    for(int i =0 ; i<Classes2.size();i++){	
			
			
	    	tab[i] = new JPanel();
	    	tab[i].setLayout(new BorderLayout());
	    	
	    	labels[i] = new JLabel(Classes2.get(i).getName() + "クラスのインスタンス数(0~"
	    			+ tableSize + ")：");
	    	texts[i] = new JTextField("",4);    	
	    	JPanel north = new JPanel();
	    	north.add(labels[i]);
	    	north.add(texts[i]);
	    	tab[i].add(north,BorderLayout.NORTH);
	    	
	    	
	    	//table
	    	tableModel[i] = new DefaultTableModel(columnNames,tableSize);
	    	
	    	table[i] = new JTable(tableModel[i]);
	    	
	    	reset(tableModel[i],Classes2.get(i).getName(),texts[i]);
	    	
	    	sp[i] = new JScrollPane(table[i]);
	    	sp[i].setPreferredSize(new Dimension(300, 200));

	    	
	    	
	    //	tab[i].add(table[i],BorderLayout.CENTER);
	    	tab[i].add(sp[i],BorderLayout.CENTER);
	    	
	    	
	    	JLabel warningMessage = new JLabel("インスタンス名入力後エンターキーを押すか、"
	    			+ "他のセルをクリックしてください。");
	    	tab[i].add(warningMessage,BorderLayout.SOUTH);
	    	//reset
	    	/**
	    	IFmaker.countnum=i;
	        Rbutton[i] = new JButton(classes.get(i).getName()+"のインスタンスをリセット");
		    Rbutton[i].addActionListener(new ActionListener()
		    	{
		    	public void actionPerformed(ActionEvent e){
		    		reset(tableModel[IFmaker.countnum],Classes2.get(IFmaker.countnum).getName(),texts[IFmaker.countnum]);
		    	}
		    	}
		    );	    
	    	
		    tab[i].add(Rbutton[i],BorderLayout.SOUTH);
		    **/
	    	tabbedpane.addTab(Classes2.get(i).getName(),tab[i]);
			
	    }
	
	    	    
	    
	    //south//
	    JButton AllRbutton = new JButton("すべてリセット");
	    JButton Gbutton = new JButton("インスタンス作成");
  
	    
	    //Allreset
	    AllRbutton.addActionListener(new ActionListener()
	    	{
	    		public void actionPerformed(ActionEvent e){
	    			for(int i=0;i<Classes2.size();i++)
	    			reset(tableModel[i],Classes2.get(i).getName(),texts[i]);
	    		}
	    	}
	    );	    
	    //generate
	    Gbutton.addActionListener(new ActionListener()
			{
	    	public void actionPerformed(ActionEvent e){
	    		generate();
				}
			}
	    );
	    
	    JPanel Bpane = new JPanel();
	    Bpane.add(AllRbutton);
	   // Bpane.add(Gbutton); 
	    
	    
	    //set
	    JPanel p = new JPanel();
	    p.setLayout(new BorderLayout());

	    p.add(top,BorderLayout.NORTH);
	    p.add(tabbedpane, BorderLayout.CENTER);
	    p.add(Bpane, BorderLayout.SOUTH);

	   //getContentPane().add(p, BorderLayout.CENTER);
	   String select[] = {"キャンセル","インスタンス生成"};
	     option = JOptionPane.showOptionDialog(this, p, "インスタンス入力画面", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
	    		null, select, null);

	    if(option==1)generate();
	    else ;
	    
	  }
	  
	  

	  
	  private void reset(DefaultTableModel tableModel,String className,JTextField text){
		//初期化  
		  text.setText("");
		  
		  int num=tableSize;
		  for(int i=0;i<tableSize;i++){
			  num = i+1;
			  tableModel.setValueAt((className+num),i,0);
		  }
	  }

	  private void generate(){
		//インスタンス作成 subtypeも考慮
		  HashMap<String, ArrayList<String>> in =new HashMap<String,ArrayList<String>>();
		  HashMap<String,Integer> inNum = new HashMap<String,Integer>();
		  
		  ArrayList<String> list;// = new ArrayList<String>();
		  String text ;//= new String();
		  int instanceNum;
		  int nullcheck;
		  for(int i =0 ;i<Classes2.size();i++){

			  instanceNum=-1;
			  nullcheck=0;
			  text = texts[i].getText();
			  if(text.equals(""))nullcheck=-1;
			  
			  if(nullcheck==0)
				  instanceNum = Integer.parseInt(text);//all null error
			  
			  if(instanceNum<0 || instanceNum>tableSize)
				 instanceNum =-1;
			  inNum.put(Classes2.get(i).getName(),instanceNum);
			  
			  list= new ArrayList<String>();
			  for(int j=0;j<tableSize;j++){
				  list.add((String) table[i].getValueAt(j,0));
				
			  }
			in.put(Classes2.get(i).getName(), list);
		  }
		  
		  

		  
		 InstanceGenerator.setInput(in);
		 InstanceGenerator.setInputNum(inNum);
		 
	  }
	  

	  
	  
	  //図を作るかどうか
	  public static int getEndFlag(){
		 
		  return option;
	  }
	  
	}