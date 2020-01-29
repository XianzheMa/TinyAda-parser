public class SymbolEntry extends Object{

   public static final int NONE = 0;
   public static final int CONST = 1;
   public static final int PARAM = 2;
   public static final int PROC = 3;
   public static final int TYPE = 4;
   public static final int VAR = 5;

   // name is just the identifier name
   private String name;
   public int role;
   //TODO: like a linkedlist? I don't know what it is
   public SymbolEntry next;

   public SymbolEntry(String id){
      name = id;
      role = NONE;
      next = null;
   }

   public String toString(){
      return "Name: " + name + "\n" + "Role: " + roleToString();
   }

   // "role" is an integer defined above
   public void setRole(int r){
      role = r;
      if (next != null)
         next.setRole(r);
   }

   public void append(SymbolEntry entry){
      if (next == null)
         next = entry;
      else
         next.append(entry);
   }  

   private String roleToString(){
      String s = "";
      switch (role){
         case NONE:  s = "None";      break;
         case CONST: s = "CONSTANT";  break;
         case PARAM: s = "PARAMETER"; break;
         case PROC:  s = "PROCEDURE"; break;
         case TYPE:  s = "TYPE";      break;
         case VAR:   s = "VARIABLE";  break;
         default:    s = "None";
      }
      return s;
   }

}