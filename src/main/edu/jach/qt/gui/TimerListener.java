package edu.jach.qt.gui;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
/**
 * Describe interface <code>TimerListener</code> here.
 *
 * @author <a href="mailto:mrippa@kapili.jach.hawaii.edu">Mathew Rippa</a>
 */
public interface TimerListener extends EventListener{
   public void timeElapsed(TimerEvent evt);
}
