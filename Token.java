public class Token{

   // Some reserved words or operator symbols
   public static final int AND = 0;
   public static final int ARRAY = 1;
   public static final int BEGIN = 2;
   public static final int CHAR = 3;
   public static final int COLON = 4;
   public static final int COMMA = 5;
   public static final int CONST = 6;
   public static final int DIV = 7;
   public static final int ELSE = 8;
   public static final int ELSIF = 9;
   public static final int END = 10;
   public static final int EOF = 11;
   public static final int EQ = 12;  // "="
   public static final int ERROR = 13;
   public static final int EXIT = 14;
   public static final int EXPO = 15;
   public static final int GE = 16;
   public static final int GETS = 17;
   public static final int GT = 18;
   public static final int ID = 19;
   public static final int IF = 20;
   public static final int IN = 21;
   public static final int INT = 22;
   public static final int IS = 23;
   public static final int LE = 24;
   public static final int LT = 25;
   public static final int LOOP = 26;
   public static final int L_PAR = 27;
   public static final int MINUS = 28;
   public static final int MOD = 29;
   public static final int MUL = 30;
   public static final int NE = 31;
   public static final int NOT = 32;
   public static final int NULL = 33;
   public static final int OF = 34;
   public static final int OR = 35;
   public static final int OUT = 36;
   public static final int PLUS = 37;
   public static final int PROC = 38;
   public static final int R_PAR = 39;
   public static final int RANGE = 40;
   public static final int SEMI = 41;
   public static final int THEN = 42;
   public static final int THRU = 43;
   public static final int TYPE = 44;
   public static final int WHEN = 45;
   public static final int WHILE = 46;

   public int code;

   // for keywords or operators, these two variables have no meaning
   // integer seems to be used when this token represents a number literal
   public int integer;
   // string seems to be used when this token represents a char, an identifier or a keyword
   public String string;

   public Token(final int newCode) {
      code = newCode;
      integer = 0;
      string = "";
   }

   public String toString(){
      String s = "Code    = " + CODES[code];
      if (code == INT)
          s += "\nInteger = " + integer;
      else if (code == ID)
          s += "\nString = " + string;   
      return s;
   }

   private static final String CODES[] = {"AND", "ARRAY", "BEGIN", "CHAR", "COLON", "COMMA", "CONST", "DIV", "ELSE", "ELSIF",
                                  "END", "EOF", "EQ", "ERROR", "EXIT", "EXPO", "GE", "GETS", "GT", "ID", "IF", "IN",
                                  "INT", "IS", "LE", "LT", "LOOP", "L_PAR", "MINUS", "MOD", "MUL", "NE", "NOT", "NULL",
                                  "OF", "OR", "OUT", "PERIOD", "PLUS", "PROC", "R_PAR", "RANGE", "RECORD", "SEMI", 
                                  "THEN", "THRU", "TYPE", "WHEN", "WHILE"};
}