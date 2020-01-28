import java.awt.*;
import javax.swing.*;
import java.io.*;

public class Chario extends Object{

   public static final char EL = '\n';
   public static final char EF = (char) 26;
   public static final char TAB = '\t';
   private JTextArea input, output;
   private String sourceProgram, line;
   private int totalErrors, column, lineNumber;
   private boolean terminalBased;

   // Constructor for use with GUI-based app
   public Chario(JTextArea sourceArea, JTextArea utilityArea){
      terminalBased = false;
      input = sourceArea;
      output = utilityArea;
      reset();
   }

   // Constructor for use with terminal-based app
   public Chario(FileInputStream stream){
      terminalBased = true;
      sourceProgram = "";
      readFile(stream);
      reset();
   }

   public void reset(){
      if (! terminalBased){
         sourceProgram = input.getText();
         output.setText("");
      }
      totalErrors = 0;
      lineNumber = 0;
      column = 0;
      line = "";
   }      

   public void println(String s){
      if (terminalBased)
         System.out.println(s);
      else
         output.append(s + "\n");
   }
   
  private String makeSpaces(int number){
      String s = "";
      for (int i = 1; i <= number; i++)
          s += " ";
      return s;
   }

   public void putError(String message){
      totalErrors++;
      String spaces = makeSpaces(column);
      if (terminalBased)
         System.out.println(spaces + "ERROR > " + message);
      else{
         output.append(spaces);
         println("ERROR > " + message);
      }
   }

   public void reportErrors(){
      println("\nCompilation complete.");
      if (totalErrors == 0)
         println("No errors reported.");
      else if (totalErrors == 1) 
         println("1 error reported.");
      else
         println(totalErrors + " errors reported.");
   }

   public char getChar(){
      char ch;
      if (column >= line.length())
         nextLine();
      ch = line.charAt(column);
      column++;
      return ch;
   }

   private void nextLine(){
      column = 0;
      line = getLine();
      if (line.charAt(0) != EF){
         lineNumber++;
         if (terminalBased)
            System.out.print(lineNumber + " > " + line);
         else{
            output.append(lineNumber + " > ");
            output.append(line);
         }
      }
   }

   private String getLine(){
      String ln;
      int first, last;
      if (sourceProgram.equals(""))
         ln = "" + EF;
      else{
         first = sourceProgram.indexOf(EL);
         last = sourceProgram.length();
         if (first == -1){
            ln = sourceProgram + EL;
            sourceProgram = "";
         }
         else{
            ln = sourceProgram.substring(0, first + 1);
            sourceProgram = sourceProgram.substring(first + 1, last);
         }
      }
      return ln;
   }

   public void openFile(){
      try{
         FileDialog fileDialog = new FileDialog(new Frame(), "Input file", FileDialog.LOAD);
         fileDialog.setSize(450, 300);
         fileDialog.setVisible(true);
         String fileName = fileDialog.getFile();
         String dirName = fileDialog.getDirectory();
         // Check for cancellation.
         if (fileName != null && dirName != null){
            File file = new File(dirName, fileName);
            FileInputStream inputStream = new FileInputStream(file);
            readFile(inputStream);
            inputStream.close();
         }
      }
      catch(IOException e){
         System.out.println("Error in opening input file" + e.toString());
      }
   }

   private void readFile(FileInputStream stream){
      try{
         BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
         if (! terminalBased)
            input.setText("");
         String data = reader.readLine();
         while (data != null){
            if (terminalBased)
               sourceProgram += data + "\n";
            else
               input.append(data + "\n");
            data = reader.readLine();
         }
      }
      catch(IOException e){
         System.out.println("Error in file input" + e.toString());
      }
   }

   public void saveFile(JTextArea input){
      try{            
         FileDialog fileDialog = new FileDialog(new Frame(), "Output file", FileDialog.SAVE);
         fileDialog.setSize(100, 100);
         fileDialog.setVisible(true);
         String dirName = fileDialog.getDirectory();
         String fileName = fileDialog.getFile();
         // Check for cancellation.
         if (dirName != null && fileName != null){
            File file = new File(dirName, fileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            writeFile(outputStream, input);
            outputStream.close();
         }
      }
      catch(IOException e){
         System.out.println("Error opening file: " + e.toString());
      }
   }

   private void writeFile(FileOutputStream stream, JTextArea input){
      String text = input.getText();
      PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(stream)));
      writer.print(text);
      writer.flush();
   }

}
