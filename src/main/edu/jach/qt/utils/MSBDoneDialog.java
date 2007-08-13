package edu.jach.qt.utils;

import javax.swing.JDialog;

public class MSBDoneDialog extends JDialog
{
	private String userId = null;
	private String comment = null;
	private boolean accept = true;

	public boolean getAccepted()
	{
		return accept;
	}

	public String getUser()
	{
		return userId;
	}

	public String getComment()
	{
		return comment;
	}
}
