import java.util.*;

public class SymbolTable extends Object{
   // Level 0 is the outmost scope when you start the program.
   private int level;
   private Stack<Map<String, SymbolEntry>> stack;
   private Chario chario;

   private static final SymbolEntry EMPTY_SYMBOL = new SymbolEntry("");

   public SymbolTable(Chario c){
      chario = c;
      reset();
   }

   // Clear the SymbolTable and set the stack empty.
   public void reset(){
      level = -1;
      stack = new Stack<Map<String, SymbolEntry>>();
   }

   // Add a new table onto the top of the stack.
   public void enterScope(){
      stack.push(new HashMap<String, SymbolEntry>());
      level++;
   }

   // Pop out the topmost table.
   public void exitScope(int mode){
      Map<String, SymbolEntry> table = stack.pop();
      printTable(table, mode);
      level--;
   }

   // Enter a new identifier to the topmost table and return the symbol entry.
   // If it already exists in the table, it would print en error message but continues.
   public SymbolEntry enterSymbol(String id){
      Map<String, SymbolEntry> table = stack.peek();
      if (table.containsKey(id)){
         chario.putError("identifier already declared in this block");
         return EMPTY_SYMBOL;
      }
      else{
         SymbolEntry s = new SymbolEntry(id);
         table.put(id, s);
         return s;
      } 
   }

   // Find the symbol in the table (progressively going further into the stack) and return it.
   // If it does not exist, return an empty symbole entry.
   public SymbolEntry findSymbol(String id){
      for (int i = stack.size() - 1; i >= 0; i--){
         Map<String, SymbolEntry> table = stack.get(i);
         SymbolEntry s = table.get(id);
         if (s != null)
             return s;
      }
      chario.putError("undeclared identifier");
      return EMPTY_SYMBOL;
   }
         
   private void printTable(Map<String, SymbolEntry> table, int mode){
      chario.println("\nLevel " + level);
      chario.println("---------");
      for (SymbolEntry s : table.values())
         chario.println(s.toString(mode));
   }

}