package edu.jach.qt.gui;

import javax.swing.tree.*;

/** This class forces "male" nodes to have leaf icons and
forbids male childbaring ability */
public class MsbNode extends DefaultMutableTreeNode  {

  public MsbNode(DragDropObject info) {
    super(info);
  }

  /** Override a few methods... */
  public boolean isLeaf() {
    //Note: Male == true;
     //return ((DragDropObject) getUserObject()).isMale();
     return false;
  }

  public boolean getAllowsChildren() {
    //Note: Male == true;
     //return !((DragDropObject) getUserObject()).isMale();
     return true;
  }

  public void add(DefaultMutableTreeNode child) {
    super.add(child);
    //System.out.println(child + " added to " + this);

    DragDropObject childPI = (DragDropObject) ((MsbNode) child).getUserObject();

    DragDropObject oldParent = childPI.getParent();
    //if (parent != null) oldParent.remove(childPI);

    DragDropObject newParent = (DragDropObject) getUserObject();

    newParent.add(childPI);
  }

  public void remove(DefaultMutableTreeNode child) {
    super.remove(child);
    //System.out.println(child + " removed from " + this);

    DragDropObject childDDO = (DragDropObject) ((MsbNode) child).getUserObject();

    DragDropObject parentDDO = (DragDropObject) getUserObject();
    if (parent != null) parentDDO.remove(childDDO);
  }
}
