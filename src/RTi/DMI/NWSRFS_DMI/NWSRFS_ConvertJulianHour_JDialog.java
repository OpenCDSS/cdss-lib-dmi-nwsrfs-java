// ----------------------------------------------------------------------------
// NWSRFS_ConvertJulianHour_JDialog - convert DateTime to/from Julian Hour
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History: 
//
// 2003-12-02	Steven A. Malers, RTi	Initial version (copy TSTool
//					setOutputPeriod_JDialog and modify).
// 2003-12-04	SAM, RTi		For output, always display the
//					Date, Julian hour, and Julian Day/hour.
// 2004-09-01	SAM, RTi		Change from RTi.DMI.NWSRFS package to
//					this RTi.DMI.NWSRFS_DMI package and
//					remove references to old package code.
// ----------------------------------------------------------------------------

package RTi.DMI.NWSRFS_DMI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

public class NWSRFS_ConvertJulianHour_JDialog extends JDialog
implements ActionListener, KeyListener, WindowListener
{
private SimpleJButton	__cancel_JButton = null,// Cancel Button
			__convert_JButton = null;// Convert Button
//private JFrame		__parent_JFrame = null;	// parent Frame GUI class
private JTextField	__input_JTextField = null,// Input to convert
			__datetime_JTextField = null,// Results as Date/Time
			__julday_JTextField = null,// Results as Julian day,hour
			__julhour_JTextField = null;// Results as Julian hour
private boolean		__error_wait = false;	// Is there an error that we
						// are waiting to be cleared up
						// or Cancel?

/**
NWSRFS_ConvertJulianHour_JDialog constructor.
@param parent JFrame class instantiating this class.
*/
public NWSRFS_ConvertJulianHour_JDialog ( JFrame parent )
{	super(parent, false);
	initialize ( parent );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event )
{	Object o = event.getSource();

	if ( o == __cancel_JButton ) {
		setVisible ( false );
		dispose();
	}
	else if ( o == __convert_JButton ) {
		checkInput();
		if ( !__error_wait ) {
			convert();
		}
	}
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.
*/
private void checkInput ()
{	String input = __input_JTextField.getText().trim();
	Vector tokens = StringUtil.breakStringList ( input, " ",
				StringUtil.DELIM_SKIP_BLANKS );
	String routine = "NWSRFS_ConvertJulianHour_JDialog.checkInput";
	String warning = "";

	// Make sure that the input is appropriate...

	boolean parse_error = false;
	DateTime date = null;
	if (	(tokens != null) && (tokens.size() == 2) &&
		StringUtil.isInteger((String)tokens.elementAt(0)) &&
		StringUtil.isInteger((String)tokens.elementAt(1)) ) {
		// Input is OK as julday and inthr...
	}
	else if ( StringUtil.isInteger(input) ) {
		// Input is OK as julhour...
	}
	else {	try {	date = DateTime.parse ( input );
		}
		catch (	Exception e ) {
			parse_error = true;
		}
	}
		
	if ( parse_error ) {
		warning +=
			"\nThe input \"" + input + "\" is not a valid " +
			"date/time, Julian hour integer,\n" +
			"or Julian Day and hour pair.";
	}
	if (	!parse_error && (date != null) &&
		(date.getPrecision() != DateTime.PRECISION_HOUR)) {
		warning +=
			"\nThe date/time \"" + input + "\" must include hours.";
	}
	if ( warning.length() > 0 ) {
		__error_wait = true;
		warning += 
			"\nSpecify appropriate input or Cancel.";
		Message.printWarning ( 1, routine, warning );
	}
}

/**
Convert the input to output.  It is assumed that checkInput() has already
been called.
*/
private void convert ()
{	String input = __input_JTextField.getText().trim();
	Vector tokens = StringUtil.breakStringList ( input, " ",
				StringUtil.DELIM_SKIP_BLANKS );
	if (	(tokens != null) && (tokens.size() == 2) &&
		StringUtil.isInteger((String)tokens.elementAt(0)) &&
		StringUtil.isInteger((String)tokens.elementAt(1)) ) {
		// Convert Julian day and hour...
		int jday = StringUtil.atoi((String)tokens.elementAt(0));
		int inthr = StringUtil.atoi((String)tokens.elementAt(1));
		DateTime datetime = NWSRFS_Util.mdyh1 ( jday, inthr );
		__datetime_JTextField.setText ( datetime.toString() );
		__julday_JTextField.setText("" + jday + " " +
			StringUtil.formatString(inthr,"%02d"));
		int jhour = (jday - 1)*24 + inthr;
		__julhour_JTextField.setText ( "" + jhour );
	}
	else if ( StringUtil.isInteger(input) ) {
		// Convert the Julian hour...
		int jhour = StringUtil.atoi(input);
		int inthr = jhour%24;
		int jday = (jhour - inthr)/24 + 1;
		DateTime datetime = NWSRFS_Util.mdyh1 ( jday, inthr );
		__datetime_JTextField.setText ( datetime.toString() );
		__julday_JTextField.setText("" + jday + " " +
			StringUtil.formatString(inthr,"%02d"));
		__julhour_JTextField.setText ( "" + jhour );
	}	
	else {	// Convert the DateTime...
		DateTime date = null;
		try {	date = DateTime.parse ( input );
			__datetime_JTextField.setText ( input );
		}
		catch ( Exception e ) {
			// Should not happen because checked in checkInput().
		}
		try {	//int jhour = NWSRFS_Util.getJulianHour1900FromDate (
			//		date.getMonth(), date.getDay(),
			//		date.getYear(), date.getHour() );
			int [] jul = NWSRFS_Util.julda (
					date.getMonth(), date.getDay(),
					date.getYear(), date.getHour() );
			__julday_JTextField.setText("" + jul[0] + " " +
				StringUtil.formatString(jul[1],"%02d"));
			int jhour = (jul[0] - 1)*24 + jul[1];
			__julhour_JTextField.setText ( "" + jhour );
		}
		catch ( Exception e ) {
			__datetime_JTextField.setText ( "Error" );
		}
	}
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__cancel_JButton = null;
	__input_JTextField = null;
	__datetime_JTextField = null;
	__julday_JTextField = null;
	__julhour_JTextField = null;
	__convert_JButton = null;
	super.finalize ();
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
*/
private void initialize ( JFrame parent )
{	//__parent_JFrame = parent;
	String app_name = JGUIUtil.getAppNameForWindows();
	if ( (app_name != null) && !app_name.equals("") ) {
		setTitle ( app_name + " - Convert Julian Hour" );
	}
	else {
		setTitle ( "Convert Julian Hour" );
	}

	addWindowListener( this );

        Insets insetsTLBR = new Insets(7,2,7,2);
        Insets insetsMin = new Insets(0,2,0,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = 0;

	// Main contents...

        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Convert a date/time to/from an NWSRFS" +
		" Julian day and hour."),
		0, y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Julian hour 0 = 1900-01-01 00 or " +
		"(1899-12-31 24 in NWS clock)." ),
		0, ++y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Julian hour 1 = 1900-01-01 01." ),
		0, ++y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Julian day 1 = 1900-01-01." ),
		0, ++y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Julian Hour = (Julian Day - 1)/24 + Hour of Julian Day" ),
		0, ++y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Input can be:" ),
		0, ++y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"  * a Julian hour (HHHHHH)," ),
		0, ++y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"  * a Julian day and hour in day (DDDDD HH)," ),
		0, ++y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"  * or a date/hour (YYYY-MM-DD HH)" ),
		0, ++y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel ( "Input:"),
		0, ++y, 1, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__input_JTextField = new JTextField ( 20 );
	__input_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __input_JTextField,
		1, y, 2, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel ( "Date/Time:" ), 
		0, ++y, 1, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__datetime_JTextField = new JTextField ( 20 );
	__datetime_JTextField.setEditable ( false );
        JGUIUtil.addComponent(main_JPanel, __datetime_JTextField,
		1, y, 2, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel ( "Julian day:" ), 
		0, ++y, 1, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__julday_JTextField = new JTextField ( 20 );
	__julday_JTextField.setEditable ( false );
        JGUIUtil.addComponent(main_JPanel, __julday_JTextField,
		1, y, 2, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel ( "Julian hour:" ), 
		0, ++y, 1, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__julhour_JTextField = new JTextField ( 20 );
	__julhour_JTextField.setEditable ( false );
        JGUIUtil.addComponent(main_JPanel, __julhour_JTextField,
		1, y, 2, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);

	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	__convert_JButton = new SimpleJButton("Convert", this);
	button_JPanel.add ( __convert_JButton );
	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add ( __cancel_JButton );

	// Dialogs do not need to be resizable...
	setResizable ( false );
        pack();
        JGUIUtil.center( this );
        super.setVisible( true );
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event )
{	int code = event.getKeyCode();

	if ( (code == KeyEvent.VK_ENTER) || (code == KeyEvent.VK_TAB) ) {
		convert ();
	}
}

public void keyReleased ( KeyEvent event )
{	
}

public void keyTyped ( KeyEvent event ) {;}

/**
Responds to WindowEvents.
@param event WindowEvent object
*/
public void windowClosing( WindowEvent event )
{
}

public void windowActivated( WindowEvent evt )
{
}

public void windowClosed( WindowEvent evt )
{
}

public void windowDeactivated( WindowEvent evt )
{
}

public void windowDeiconified( WindowEvent evt )
{
}

public void windowIconified( WindowEvent evt )
{
}

public void windowOpened( WindowEvent evt )
{
}

} // end NWSRFS_ConvertJulianHour_JDialog
