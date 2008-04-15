//-----------------------------------------------------------------------------
// JPanel to plug-in generic applications that contains tabbed panes
// with a JTree in each pane. 
//-----------------------------------------------------------------------------
/*
History
2004-07-09	A. Morgan Love		Initial Implementation.
*/
//-----------------------------------------------------------------------------
package RTi.DMI.NWSRFS_DMI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;

import RTi.Util.Message.Message;

/**
The NWSRFS_Tree_JPanel class creates a JPanel with Tabbed Panes to
add JTrees to.
*/
public class NWSRFS_Tree_JPanel extends JPanel {

//panel
private JPanel __panel;

//tabbed pane
private JTabbedPane __panes;

/**
Constructor for NWSRFS_Tree_JPanel.  Sets up basic panel with 
JTabbedPanes.  No trees are added by default.
@param parent JFrame parent 
*/
public NWSRFS_Tree_JPanel ( JFrame parent )
{

	//Make panel
	__panel = new JPanel();
	__panel.setLayout( new GridBagLayout() );
	__panel.setBackground( Color.white );

	//make TabbedPane
	__panes = new JTabbedPane();

	//initialize GUI strings (translate if needed)
	//initialize_gui_strings();

} //end constructor

/**
Adds a JTree to a pane, using the title passed in for the tab name. 
A JScrollPane is added to the tab.
@param title Name to appear on tab.
@param tree JTree to appear in tab.
*/
protected void addTreeTab( String title, JTree tree ) {
	String routine = "NWSRFS_Tree_JPanel.addTreeTab";

	if ( tree == null ) {
		Message.printWarning( 2, routine, "JTree null. Will not " +
		"add to panel." );
		return;
	}
	
	//make scroll pane
	JScrollPane scroll = new JScrollPane( tree ); 
	scroll.setVerticalScrollBarPolicy(
	JScrollPane.VERTICAL_SCROLLBAR_ALWAYS ); 
	scroll.setPreferredSize( new Dimension( 250, 500 ) ); 
	scroll.setBorder( BorderFactory.createCompoundBorder( 
	( BorderFactory.createEmptyBorder( 5, 5, 5, 5 )), 
	scroll.getBorder() )); 

	//add tab
	__panes.setBorder( BorderFactory.createLineBorder( Color.black) );
	__panes.addTab( title, null, scroll, title );

}

/**
Returns the assembled JPanel that contains the tabbed panes with JTrees.
@return JPanel containig JTabbedPanes with JTrees.
*/
protected JPanel getPanel() {
	return __panel;
}


/**
This method is used to get the strings needed for labelling all the GUI components only if a translation table is used for the application, ie, if
there is a translation file.  If the String cannot be located in the
translation file, the original string will be utilized in non-translated form.
*/
/*
public void initialize_gui_strings()  {
        String routine = "NWSRFS_Tree_JPanel.initialize_gui_strings"; 
	LanguageTranslator translator = null; 
	translator = LanguageTranslator.getTranslator();
        if ( translator != null ) {
		_string = translator.translate(
			"string", _string );
	}
} //end initialize_gui_strings()  
*/


} // end NWSRFS_Tree_JPanel
