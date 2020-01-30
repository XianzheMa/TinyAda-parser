/*
You can test Chario, Scanner, or Parser separately by adding and removing comments
in the last few lines of the TerminalApp constructor.
*/

import java.io.*;

public class TerminalApp{

   // Data model
   private Chario chario;
   private Scanner scanner;
   private Parser parser;

   public TerminalApp(String args[]){
      int mode = getMode(args);
      if(mode == -1){
         return;
      }
      String filename = args[0];
      FileInputStream stream;
      try{
         stream = new FileInputStream(filename);
      }catch(IOException e){
         System.out.println("Error opening file.");
         return;
      }      
      chario = new Chario(stream);
      //testChario();
      scanner = new Scanner(chario);
      //testScanner();
      parser = new Parser(chario, scanner, mode);
      testParser();
   }

   // -1 is returned if invalid args are given
   private int getMode(String args[]){
      int mode;
      if (args.length == 0){
         System.out.println("USAGE: java -jar hw2.jar <src file> ([options])");
         System.out.println("options are zero or more of:");
         System.out.println("-s scope analysis");
         System.out.println("-r role analysis");
         mode = -1;
      }
      else if (args.length == 1){
         // only a source filename
         mode = Parser.NONE;
      }
      else if (args.length == 2){
         // a filename and an option specification
         if (args[1].equals("-s")){
            mode = Parser.SCOPE;
         }
         else if (args[1].equals("-r")){
            mode = Parser.ROLE;
         }
         else{
            System.out.println("Invalid option: " + args[1]);
            mode = -1;
         }
      }
      else if (args.length == 3){
         // the two options specified must be "-r" or "-s"
         boolean condition1 = args[1].equals("-r") && args[2].equals("-s");
         boolean condition2 = args[1].equals("-s") && args[2].equals("-r");
         if (condition1 || condition2){
            mode = Parser.ROLE;
         }
         else{
            System.out.print("Invalid option combination:");
            for(int i = 1; i < args.length; i ++){
               System.out.print(" " + args[i]);
            }
            System.out.print('\n');
            mode = -1;
         }
      }
      else{
            System.out.print("Invalid option combination:");
            for(int i = 1; i < args.length; i ++){
               System.out.print(" " + args[i]);
            }
            System.out.print('\n');
            mode = -1;
      }
      return mode;
   }


   private void testChario(){
      char ch = chario.getChar();
      while (ch != Chario.EF)
         ch = chario.getChar();
      chario.reportErrors();
   }

   private void testScanner(){
      Token token = scanner.nextToken();
      while (token.code != Token.EOF){
         chario.println(token.toString());
         token = scanner.nextToken();
      }
      chario.reportErrors();
   }

   private void testParser(){
      try{
         parser.parse();
      }
      catch(Exception e){}
      chario.reportErrors();
   }


   public static void main(String args[]){
      new TerminalApp(args);
   }
}