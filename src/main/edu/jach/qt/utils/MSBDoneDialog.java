package edu.jach.qt.utils;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.io.File;
import java.io.IOException;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.*;


public class MSBDoneDialog extends JDialog {
    private String userId  = null;
    private String comment = null;
    private boolean accept = true;
    private JOptionPane optionPane;
    private String defaultUserDir = "/tmp/last_user";

    private String getDefaultUserId() {
	String _usr=null;

	if (System.getProperty("userid") == null) {
	    File userDir = new File(defaultUserDir);
	    if (userDir.exists() && userDir.isDirectory()) {
		String [] fNames = userDir.list();
		for (int i=0; i<fNames.length; i++) {
		    if (fNames[i].startsWith(".")) {
			continue;
		    }
		    else {
			_usr = fNames[i];
			break;
		    }
		}
	    }
	    if (_usr == null || _usr.equals("") ) {
		_usr = System.getProperty("user.name");
	    }
	    System.setProperty("userid", _usr);
	}
	
	return System.getProperty("userid");
    }

    private void setDefaultUserId(String usr) {
	File userDir = new File(defaultUserDir);
	if (!(userDir.exists())) {
	    userDir.mkdir();
	}

	// Get a list of all the current files
	String [] fName = userDir.list();
	for (int i=0; i<fName.length; i++) {
	    if (fName[i].startsWith(".")) {
		continue;
	    }
	    else {
		if (fName[i].equals(usr)) {
		    break;
		}
		else {
		    File f = new File (defaultUserDir+"/"+fName[i]);
		    f.delete();
		}
	    }
	}

	File userName = new File(defaultUserDir+"/"+usr);
	try {
	    userName.createNewFile();
	}
	catch (IOException ioe) {
	}

	System.setProperty("userid", usr);
    }
	    
    public boolean getAccepted() {
	return accept;
    }

    public String getUser() {
	return userId;
    }

    public String getComment() {
	return comment;
    }
	

    public MSBDoneDialog (Frame parent, 
			  String projectID,
			  String title,
			  String checksum) {
	super(parent, true);
	
	String msg = "Mark MSB with \n"+ 
	    "Project Id "+projectID+ "\n" +
	    "and title "+ title+ "\n" +
	    "as complete?";

	final String projId = projectID;
	final String chkSum = checksum;
	final JLabel userIdLabel     = new JLabel ("User ID");
	final JTextField userIdField = new JTextField(10);
	userIdField.setText(getDefaultUserId());
	final JLabel commentLabel    = new JLabel ("Comment");
	final JTextArea commentArea  = new JTextArea(10,5);
	commentArea.setLineWrap(true);
	commentArea.setWrapStyleWord(true);
	Object [] inputArray  = { msg, userIdLabel, userIdField,
				  commentLabel, commentArea};

	final String acceptString = "Accept";
	final String rejectString = "Reject";
	final String cancelString = "Decide Later";
	final String noDataString = "Took No Data";
	Object [] options = {acceptString, rejectString, cancelString, noDataString};

	optionPane = new JOptionPane(inputArray, 
				     JOptionPane.QUESTION_MESSAGE,
				     JOptionPane.DEFAULT_OPTION,
				     null,
				     options);
	setContentPane(optionPane);
// 	setSize(330,370);
	validateTree();
	pack();
	setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
		    /*
		     * Instead of directly closing the window,
		     * we're going to change the JOptionPane's
		     * value property.
		     */
                    optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
		}
	    });

	// Walk throught the elements looking for JButtons, and add tooltips
        findButtons (optionPane);

	optionPane.addPropertyChangeListener( new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
		    String prop = e.getPropertyName();

		    if (isVisible() &&
			(e.getSource() == optionPane) &&
			( prop.equals(JOptionPane.VALUE_PROPERTY) ||
			  prop.equals(JOptionPane.INPUT_VALUE_PROPERTY)
			  )
			) {
			Object value = optionPane.getValue();
			
			if (value == JOptionPane.UNINITIALIZED_VALUE) {
			    return;
			}

			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

			userId = userIdField.getText();
			setDefaultUserId(userId);
			comment = commentArea.getText();
			try {
			    File cancelFile = new File ("/tmp/cancel");
			    File acceptFile = new File ("/tmp/accept");
			    File rejectFile = new File ("/tmp/reject");
			    File noDataFile = new File ("/tmp/noData");
			    if (value.equals(acceptString)) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				MsbClient.doneMSB(projId, chkSum, userId, comment);
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				cancelFile.delete();
				rejectFile.delete();
				noDataFile.delete();
			    }
			    else if (value.equals(rejectString)) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				MsbClient.rejectMSB(projId, chkSum, userId, comment);
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				cancelFile.delete();
				acceptFile.delete();
				noDataFile.delete();
			    }
			    else if (value.equals(noDataString)) {
				acceptFile.delete();
				rejectFile.delete();
				cancelFile.delete();
			    }
			    else {
				acceptFile.delete();
				rejectFile.delete();
				noDataFile.delete();
			    }
			}
			catch (InvalidUserException iue) {
			    JOptionPane.showMessageDialog(null,
							  iue.getMessage(),
							  null,
							  JOptionPane.ERROR_MESSAGE
							  );
			    return;
			}
			catch (Exception x) {
			}
			setVisible (false);
			try {
			    dispose();
			}
			catch (Exception ex) {};
		    }
		}
	    });

	validateTree();
	pack();
	setVisible(true);
	this.getRootPane().requestFocus();
    }

    private void findButtons( Container c ) {
	// Get all the children
	Component [] components = c.getComponents();
	for ( int i=0; i<components.length; i++ ) {
	    if ( components[i] instanceof JButton ) {
		String current = ( (JButton)components[i] ).getText();
		if ( current.equalsIgnoreCase("ACCEPT" ) ) {
			( (JButton)components[i] ).setToolTipText(
								   "Reduce the MSB's database count by 1 and charge the program for it. Dothis only when you're sure the MSB is, or will, be, completed." );
		}
		else if ( current.equalsIgnoreCase( "REJECT" ) ) {
		    ( (JButton)components[i] ).setToolTipText( "Don't reduce the MSB count, don't charge the project, log the fact that the MSB was attempted and allow you to provide a comment as to why the MSB was not completed.");
		}
		else if ( current.equalsIgnoreCase( "DECIDE LATER" ) ) {
		    ( (JButton)components[i] ).setToolTipText( "Click this if you want to change exposure time on the last observation of an MSB, or execute a new query while an observation is underway. The dialogue box will reappear later to accept the current MSB.");
		}
		else if ( current.equalsIgnoreCase( "TOOK NO DATA" ) ) {
		    ( (JButton)components[i] ).setToolTipText( "Don't reduce the MSB count, don't charge the program anything. The MSB's observations are removed from the staging area.");
		}
	    }
	    else if ( components[i] instanceof Container ) {
		findButtons ( (Container)components[i] );
	    }
	}
    }
}
