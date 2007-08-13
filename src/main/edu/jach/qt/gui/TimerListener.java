package edu.jach.qt.gui;

import java.util.EventListener ;

/**
 * Describe interface <code>TimerListener</code> here.
 *
 * @author <a href="mailto:mrippa@kapili.jach.hawaii.edu">Mathew Rippa</a>
 */
public interface TimerListener extends EventListener
{
	public void timeElapsed( TimerEvent evt );
}
