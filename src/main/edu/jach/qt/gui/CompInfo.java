package edu.jach.qt.gui;

import java.util.LinkedList;


/**
 * Describe class <code>CompInfo</code> here.
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * $Id$
 */
class CompInfo {
  private String title = "";
  private int view = -1;
  private LinkedList list;

  public CompInfo() {
    list = new LinkedList();
  }

  public void addElem(Object obj) {
    list.add(obj);
  }

  public Object getElem(int i) {
    return list.get(i);
  }

  public LinkedList getList() {
    return list;
  }

  public int getSize() {
    return list.size();
  }

  public void setView(int view) {
    this.view = view;
  }

  public  int getView() {
    return view;
  }

  public  void setTitle(String title) {
    this.title = title;
  }
  
  /**
   * Get the value of title.
   * @return value of title.
   */
  public  String getTitle() {
    return title;
  }

}
