package edu.jach.qt.gui;

import edu.jach.qt.utils.*;

/**
 * MyResultSet.java
 *
 *
 * Created: Wed May  2 13:46:40 2001
 *
 * @author <a href="mailto: "Mathew Rippa</a>
 * @version
 */

public class MyResultSet {

   String[][] results;
   TextReader tr;
   int cursor = 0;

   /**
    * Creates a new <code>MyResultSet</code> instance.
    *
    * @param file a <code>String</code> value
    */
   public MyResultSet (String file){
      tr = new TextReader(file);
      results = new String[25][11];
      parse();
      print();

      System.out.println("Row cursor: "+ getString(3));
  
   }

   /**
    * Describe <code>parse</code> method here.
    *
    */
   public void parse() {
      int row = 0;
      int col = 0;
      String word = "";

      //read lines until end of file
      while (tr.ready()) {

	 //skip over comments
	 while(tr.peek() == '#')
	    tr.readLine();

	 //read word until end of line
	 while(true) {
	    if ((tr.peek() == '\n') || 
		(tr.peek() == -1) ||
		(tr.peek() == 10)) {
	       break;
	    }
	    word = tr.readWord();
	    results[row][col] = word+" ";
	    col++;
	 }
	 tr.read();
	 col = 0;
	 row++;
      }
   }

   /**
    * Describe <code>getString</code> method here.
    *
    * @param col an <code>int</code> value
    * @return a <code>String</code> value
    */
   public String getString(int col) {
      String str = "";
      str += results[cursor][col];
      return str;
   }

   /**
    * Describe <code>next</code> method here.
    *
    * @return a <code>boolean</code> value
    */
   public boolean next() {
      cursor++;
      return (cursor < 25);
   }

   /**
    * Describe <code>print</code> method here.
    *
    */
   public void print() {
      for(int i=0; i<25; i++) {
	 for(int j=0; j<11; j++)
	    System.out.print(results[i][j]);
	 System.out.println();
      }
      System.out.println("DONE");
   }
   
   /**
    * Describe <code>main</code> method here.
    *
    * @param args a <code>String[]</code> value
    */
   public static void main(String[] args) {
      MyResultSet rs = new MyResultSet("/home/mrippa/root/src/omp/src/gui/querySet.txt");
   }
}// MyResultSet
