// Parsing shell partially completed

// Note that EBNF rules are provided in comments
// Just add new methods below rules without them

// Task for scope analysis:
// entering information into a symbol table when identifier declarations are encountered,
// and looking up this information in the symbol table when identifier references are encountered.

// ! Detailed instruction:
// 1. Before parsing begins, a new table of level 0 is pushed onto the stack
//    and the predefined identifiers are loaded in.
// 2. When a scope is exited, at the end of a procedure declaration, the table at the top is popped.
// 3. Whenever an identifier reference is encountered, the parser searches for that info in the stack.
// 4. The parser can detect two scope errors: redeclaration error and undeclaration error.

// ! The parser does not halt when a scope error (one of the two above) occurs
import java.util.*;

public class Parser extends Object{

   private Chario chario;
   private Scanner scanner;
   private SymbolTable table;
   // next token waiting to be processes
   private Token token;

   // these sets include some of TinyAda's operator symbols
   // and the tokens that begin various declarations and statements in the language
   // see initHandles for details
   private Set<Integer> addingOperator, multiplyingOperator, relationalOperator, basicDeclarationHandles,
         statementHandles;

   public Parser(Chario c, Scanner s) {
      // save for reference later
      chario = c;
      scanner = s;
      initHandles();
      initTable();
      // initial token
      token = scanner.nextToken();
   }

   public void reset() {
      scanner.reset();
      initTable();
      token = scanner.nextToken();
   }

   private void initHandles() {
      addingOperator = new HashSet<Integer>();
      addingOperator.add(Token.PLUS);
      addingOperator.add(Token.MINUS);
      multiplyingOperator = new HashSet<Integer>();
      multiplyingOperator.add(Token.MUL);
      multiplyingOperator.add(Token.DIV);
      multiplyingOperator.add(Token.MOD);
      relationalOperator = new HashSet<Integer>();
      relationalOperator.add(Token.EQ);
      relationalOperator.add(Token.NE);
      relationalOperator.add(Token.LE);
      relationalOperator.add(Token.GE);
      relationalOperator.add(Token.LT);
      relationalOperator.add(Token.GT);
      basicDeclarationHandles = new HashSet<Integer>();
      basicDeclarationHandles.add(Token.TYPE);
      basicDeclarationHandles.add(Token.ID);
      basicDeclarationHandles.add(Token.PROC);
      statementHandles = new HashSet<Integer>();
      statementHandles.add(Token.EXIT);
      statementHandles.add(Token.ID);
      statementHandles.add(Token.IF);
      statementHandles.add(Token.LOOP);
      statementHandles.add(Token.NULL);
      statementHandles.add(Token.WHILE);
   }

   // accept() and fatalError() are two utility methods
   // accept() only compares their code
   private void accept(int expected, String errorMessage) {
      if (token.code != expected)
         fatalError(errorMessage);
      token = scanner.nextToken();
   }

   // The call to this method would trigger a runtime exception.
   private void fatalError(String errorMessage) {
      // print an error message before throwing the exception
      chario.putError(errorMessage);
      throw new RuntimeException("Fatal error");
   }

   /*
   Three new routines for scope analysis.
   */

  private void initTable(){
   // When it's newly created, its level is -1 and its stack is empty
   table = new SymbolTable(chario);
   table.enterScope();
   // There are five predefined identifiers
   // TODO: what is the definition of an identifier?
   table.enterSymbol("BOOLEAN");
   table.enterSymbol("CHAR");
   table.enterSymbol("INTEGER");
   table.enterSymbol("TRUE");
   table.enterSymbol("FALSE");
}      

// first check the token's code to determine that it is an identifier
// If it is, register this identifier in the table and return the entry
private SymbolEntry enterId(){
   SymbolEntry entry = null;
   if (token.code == Token.ID)
      entry = table.enterSymbol(token.string);
   else
      fatalError("identifier expected");
   token = scanner.nextToken();
   return entry;
}

// consume the next token (representing an identifier)
// and find the corresponding entry in the table
private SymbolEntry findId(){
   SymbolEntry entry = null;
   if (token.code == Token.ID)
      entry = table.findSymbol(token.string);
   else
      fatalError("identifier expected");
   token = scanner.nextToken();
   return entry;
}

   // the beginning of parsing process
   // this method will either return normally or throw an exception
   // if the first syntax error is encountered
   public void parse(){
      subprogramBody();
      accept(Token.EOF, "extra symbols after logical end of program");
      table.exitScope();
   }

   // each parsing method assumes the parser's token variable
   // refers to the first token waiting to be processed.
   // Each parsing method leaves behind the next token following the phrase
   // usually using accept() to accomplish this goal
   // the parsing methods do not share any information except for token

   /*
   subprogramBody =
         subprogramSpecification "is"
         declarativePart
         "begin" sequenceOfStatements
         "end" [ <procedure>identifier ] ";"
   */
   private void subprogramBody(){
      subprogramSpecification();
      accept(Token.IS, "'is' expected");
      declarativePart();
      accept(Token.BEGIN, "'begin' expected");
      sequenceOfStatements();
      accept(Token.END, "'end' expected");
      table.exitScope();
      // the identifier here is optional, so we need to check first
      if (token.code == Token.ID)
         findId();
      accept(Token.SEMI, "';' expected");
   }

   /*
   subprogramSpecification = "procedure" identifier [ formalPart ]
   */
   // wrote by xizma
   private void subprogramSpecification(){
      // "procedure" is a keyword, not an identifier
      accept(Token.PROC, "'procedure' expected");
      enterId();
      table.enterScope();
      if(token.code == Token.L_PAR){
         formalPart();
      }
   }
   /*
   formalPart = "(" parameterSpecification { ";" parameterSpecification } ")"
   */
   // wrote by xizma
   private void formalPart(){
      accept(Token.L_PAR, "left parenthesis expected");
      parameterSpecification();
      while(token.code == Token.SEMI){
         token = scanner.nextToken();
         parameterSpecification();
      }
      accept(Token.R_PAR, "right parenthesis expected");
   }
   /*
   parameterSpecification = identifierList ":" mode <type>name
   */
   // wrote by xizma
   private void parameterSpecification(){
      identifierList();
      accept(Token.COLON, "':' expected");
      mode();
      // warning: we ignore <type> now
      name();
   }

   /*
   mode = [ "in" ] | "in" "out" | "out"
   */
   // wrote by xizma
   private void mode(){
      // it could be "in", "in out", "out" or nothing
      if(token.code == Token.IN){
         token = scanner.nextToken();
         if(token.code == Token.OUT){
            token = scanner.nextToken();
         }
      }
      else if(token.code == Token.OUT){
         token = scanner.nextToken();
      }
   }
   /*
   declarativePart = { basicDeclaration }
   */
   private void declarativePart(){
      // recall that basicDeclarationHandles is a set
      while (basicDeclarationHandles.contains(token.code))
         basicDeclaration();
   }

   /*
   basicDeclaration = objectDeclaration | numberDeclaration
                    | typeDeclaration | subprogramBody   
   */
   private void basicDeclaration(){
      // Divide the case into several non-overlapping subcases
      // Since first(objectDeclaration) overlaps with first(numberDeclaration),
      // we collect them into a single procedure.
      switch (token.code){
         case Token.ID:
            numberOrObjectDeclaration();
            break;
         case Token.TYPE:
            typeDeclaration();
            break;
         case Token.PROC:
            subprogramBody();
            break;
         default: fatalError("error in declaration part");
      }
   }

   /*
   objectDeclaration =
         identifierList ":" typeDefinition ";"

   numberDeclaration =
         identifierList ":" "constant" ":=" <static>expression ";"
   */
   private void numberOrObjectDeclaration(){
      identifierList();
      accept(Token.COLON, "':' expected");
      if (token.code == Token.CONST){
         token = scanner.nextToken();
         accept(Token.GETS, "':=' expected");
         expression();
      }
      else
         typeDefinition();
      accept(Token.SEMI, "';' expected");
   }

   /*
   typeDeclaration = "type" identifier "is" typeDefinition ";"
   */
   // wrote by xizma
   private void typeDeclaration(){
      accept(Token.TYPE, "'type' expected");
      enterId();
      accept(Token.IS, "'is' expected");
      typeDefinition();
      accept(Token.SEMI, "';' expected");
   }
   /*
   typeDefinition = enumerationTypeDefinition | arrayTypeDefinition
                  | range | <type>name
   */
   // wrote by xizma
   private void typeDefinition(){
      // enumerationTypeDefinition starts with "("
      // arrayTypeDefinition starts with "array"
      // range starts with "range "
      // name starts with identifier
      // they don't overlap with each other
      switch (token.code) {
         case Token.L_PAR:
            enumerationTypeDefinition();
            break;
         case Token.ARRAY:
            arrayTypeDefinition();
            break;
         case Token.RANGE:
            range();
            break;
         case Token.ID:
            name();
            break;
         default:
            fatalError("error in type definition");
            break;
      }
   }

   /*
   enumerationTypeDefinition = "(" identifierList ")"
   */
   // wrote by xizma
   private void enumerationTypeDefinition(){
      accept(Token.L_PAR, "left parenthesis expected");
      identifierList();
      accept(Token.R_PAR, "right parenthesis expected");
   }

   /*
   arrayTypeDefinition = "array" "(" index { "," index } ")" "of" <type>name
   */
   // wrote by xizma
   private void arrayTypeDefinition(){
      accept(Token.ARRAY, "'array' expected");
      accept(Token.L_PAR, "left parenthesis expected");
      index();
      while(token.code == Token.COMMA){
         token = scanner.nextToken();
         index();
      }
      accept(Token.R_PAR, "right parenthesis expected");
      accept(Token.OF, "'of' expected");
      // warning: we ignored <type>
      name();
   }

   /*
   index = range | <type>name
   */
   // wrote by xizma
   private void index(){
      // range starts with "range "
      // name starts with identifier
      // their token codes are different
      if(token.code == Token.RANGE){
         range();
      }
      else if(token.code == Token.ID){
         // warning: we ignored <type>
         name();
      }
      else{
         fatalError("error in index");
      }
   }

   /*
   range = "range " simpleExpression ".." simpleExpression
   */
   // wrote by xizma
   private void range(){
      accept(Token.RANGE, "'range' expected");
      simpleExpression();
      accept(Token.THRU, "'..' expected");
      simpleExpression();
   }

   /*
   identifierList = identifier { "," identifer }
   */
  // wrote by xizma
   private void identifierList(){
      // this method gets called every time a kind of declaration happens
      enterId();
      while(token.code == Token.COMMA){
         token = scanner.nextToken();
         enterId();
      }
   }

   /*
   sequenceOfStatements = statement { statement }
   */
   private void sequenceOfStatements(){
      statement();
      while (statementHandles.contains(token.code))
         statement();
   }

   /*
   statement = simpleStatement | compoundStatement

   simpleStatement = nullStatement | assignmentStatement
                   | procedureCallStatement | exitStatement

   compoundStatement = ifStatement | loopStatement
   */
   private void statement(){
      switch (token.code){
         case Token.ID:
            assignmentOrCallStatement();
            break;
         case Token.EXIT:
            exitStatement();
            break;
         case Token.IF:
            ifStatement();
            break;
         case Token.NULL:
            nullStatement();
            break;
         case Token.WHILE:
         case Token.LOOP:
            loopStatement();
            break;
         default: fatalError("error in statement");
      }
   }

   /*
   nullStatement = "null" ";"
   */
  // wrote by xizma
   private void nullStatement(){
      accept(Token.NULL, "'null' expected");
      accept(Token.SEMI, "';' expected");
   }

   /*
   loopStatement =
         [ iterationScheme ] "loop" sequenceOfStatements "end" "loop" ";"

   iterationScheme = "while" condition
   */
   // wrote by xizma
   private void loopStatement(){
      // iterationScheme begins with "while"
      if(token.code == Token.WHILE){
         iterationScheme();
      }

      accept(Token.LOOP, "'loop' expected");
      sequenceOfStatements();
      accept(Token.END, "'end' expected");
      accept(Token.LOOP, "'loop' expected");
      accept(Token.SEMI, "';' expected");
   }

   // wrote by xizma
   private void iterationScheme(){
      accept(Token.WHILE, "'while' expected");
      condition();
   }
   /*
   ifStatement =
         "if" condition "then" sequenceOfStatements
         { "elsif" condition "then" sequenceOfStatements }
         [ "else" sequenceOfStatements ]
         "end" "if" ";"
   */
   // wrote by xizma
   private void ifStatement(){
      accept(Token.IF, "'if' expected");
      condition();
      accept(Token.THEN, "'then' expected");
      sequenceOfStatements();
      while(token.code == Token.ELSIF){
         token = scanner.nextToken();
         condition();
         accept(Token.THEN, "'then' expected");
         sequenceOfStatements();
      }

      if(token.code == Token.ELSE){
         token = scanner.nextToken();
         sequenceOfStatements();
      }

      accept(Token.END, "'end' expected");
      accept(Token.IF, "'if' expected");
      accept(Token.SEMI, "';' expected");
   }
   /*
   exitStatement = "exit" [ "when" condition ] ";"
   */
   // wrote by xizma
   private void exitStatement(){
      accept(Token.EXIT, "'exit' expected");
      if(token.code == Token.WHEN){
         token = scanner.nextToken();
         condition();
      }
      accept(Token.SEMI, "';' expected");
   }

   /*
   assignmentStatement = <variable>name ":=" expression ";"

   previous: procedureCallStatement = <procedure>name [ actualParameterPart ] ";"
   modified as demanded: procedureCallStatement = <procedure>name ";"
   */
   // modified by xizma
   private void assignmentOrCallStatement(){
      name();
      if (token.code == Token.GETS){
         // it is an assignmentStatement
         token = scanner.nextToken();
         expression();
      }
      accept(Token.SEMI, "';' expected");
   }

   /*
   actualParameterPart = "(" expression { "," expression } ")"
   */
   // wrote by xizma
   // private void actualParameterPart(){
   //    accept(Token.L_PAR, "left parenthesis expected");
   //    expression();
   //    while(token.code == Token.COMMA){
   //       token = scanner.nextToken();
   //       expression();
   //    }
   //    accept(Token.R_PAR, "right parenthesis expected");
   // }

   /*
   condition = <boolean>expression
   */
   private void condition(){
      expression();
   }

   /*
   expression = relation [{ "and" relation } | { "or" relation }]
   */
   private void expression(){
      relation();
      if(token.code == Token.AND){
         while(token.code == Token.AND){
            token = scanner.nextToken();
            relation();
         }
      }
      else if(token.code == Token.OR){
         while(token.code == Token.OR){
            token = scanner.nextToken();
            relation();
         }
      }
   }

   /*
   relation = simpleExpression [ relationalOperator simpleExpression ]
   */
   // wrote by xizma
   private void relation(){
      simpleExpression();
      if(relationalOperator.contains(token.code)){
         token = scanner.nextToken();
         simpleExpression();
      }
   }

   /*
   simpleExpression =
         [ unaryAddingOperator ] term { binaryAddingOperator term }
   */
   private void simpleExpression(){
      if (addingOperator.contains(token.code))
         token = scanner.nextToken();
      term();
      while (addingOperator.contains(token.code)){
         token = scanner.nextToken();
         term();
      }
   }

   /*
   term = factor { multiplyingOperator factor }
   */
   // wrote by xizma
   private void term(){
      factor();
      while(multiplyingOperator.contains(token.code)){
         token = scanner.nextToken();
         factor();
      }
   }

   /*
   factor = primary [ "**" primary ] | "not" primary
   */
   // wrote by xizma
   private void factor(){
      primary();
      // there are three possibilities: nothing, "**" and "not"
      if(token.code == Token.EXPO){
         token = scanner.nextToken();
         primary();
      }
      else if(token.code == Token.NOT){
         token = scanner.nextToken();
         primary();
      }
   }

   /*
   primary = numericLiteral | stringLiteral | name | "(" expression ")"
   */
   void primary(){
      switch (token.code){
         case Token.INT:
         case Token.CHAR:
            token = scanner.nextToken();
            break;
         case Token.ID:
            name();
            break;
         case Token.L_PAR:
            token = scanner.nextToken();
            expression();
            accept(Token.R_PAR, "')' expected");
            break;
         default: fatalError("error in primary");
      }
   }

   /*
   name = identifier [ indexedComponent ]
   */
   private void name(){
      findId();
      if (token.code == Token.L_PAR)
         indexedComponent();
   }

   /*
   indexedComponent = "(" expression  { "," expression } ")"
   */
   private void indexedComponent(){
      accept(Token.L_PAR, "left parenthesis expected");
      expression();
      while(token.code == Token.COMMA){
         token = scanner.nextToken();
         expression();
      }
      accept(Token.R_PAR, "right parenthesis expected");
   }

}
