package edu.jach.qt.utils;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.Timer;
import java.io.File;
import java.io.IOException;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
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
				     options,
				     options[0]);
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
 				MsbClient.doneMSB(projId, chkSum, userId, comment);
				cancelFile.delete();
				rejectFile.delete();
				noDataFile.delete();
			    }
			    else if (value.equals(rejectString)) {
 				MsbClient.rejectMSB(projId, chkSum, userId, comment);
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
			    x.printStackTrace();
			}
			setVisible (false);
		    }
		}
	    });

	validateTree();
	pack();
	setVisible(true);
	this.getRootPane().requestFocus();
    }

}
