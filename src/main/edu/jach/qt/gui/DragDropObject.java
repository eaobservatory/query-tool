// Copyright 1997 Association for Universities for Research in Astronomy, Inc.,
// Observatory Control System, Gemini Telescopes Project.
// See the file COPYRIGHT for complete details.
//
// $Id$
//
package edu.jach.qt.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import gemini.sp.SpItem;
import java.util.Vector;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * This class ties an SpItem to the tree widget in which its associated
 * tree node lives.  This is used by drop targets when an item is being
 * moved or deleted in order to remove dragged objects from their tree.
 */
public final class DragDropObject implements Transferable {

   /** Identifies the object being dragged and dropped */
   public final static DataFlavor DATA_FLAVOR = new DataFlavor(DragDropObject.class, "DragDropObject");

   // The item(s) being dragged.

   //private SpItem[] _spItemA;

   private Vector	      children	= null;
   private DragDropObject     parent	= null;
   private String	      myName;
   private SpItem	      myItem;
   /**
    * This constructor should be used when dragging a newly created object
    * that hasn't been inserted in any tree.
    */
   public DragDropObject(SpItem spItem) {
      children = new Vector();
      myItem = spItem;
      myName = spItem.name();
   }

   public SpItem getSpItem() {
      return myItem;
   }

   public String getName() {
      return myName;
   }

   public void add(DragDropObject info) {
      info.setParent(this);
      children.add(info);
   }

   public void remove(DragDropObject info) {
      info.setParent(null);
      children.remove(info);
   }

   public void setParent(DragDropObject someParent) {
      parent = someParent;
   }

   public Vector getChildren() {
      return children;
   }

   public DragDropObject getParent() {
      return parent;
   }

//     /** Is more than one item being dragged? */
//     public boolean isMultiDrag() {
//        return (_spItemA.length > 1);
//     }

//     /** Get the first SpItem. */
//     public SpItem getSpItem() {
//        return getSpItem(0);
//     }

//     /** Get the nth SpItem. */
//     SpItem getSpItem(int i) {
//        return _spItemA[i];
//     }

//     /** Get the set of SpItems. */
//     public SpItem[] getSpItems() {
//        return _spItemA;
//     }

   // Implementation of the Transferable interface
   public DataFlavor[] getTransferDataFlavors() {
      // MFO: DataFlavor.stringFlavor is only added because dropping nodes would not work under Windows (NT) otherwise.
      //      The same trick is used in the Gemini OT (from ot-0.6, in jsky.app.ot.viewer.SPDragDropObject)
      return new DataFlavor[] { DATA_FLAVOR, DataFlavor.stringFlavor };
   }

   public boolean isDataFlavorSupported(DataFlavor fl) {
      return fl.equals(DATA_FLAVOR);
   }

   public Object getTransferData(DataFlavor fl) throws UnsupportedFlavorException, IOException {
      if (fl.equals(DATA_FLAVOR)) {
	 return this;
      }
      else throw new UnsupportedFlavorException(fl);
   }
}
