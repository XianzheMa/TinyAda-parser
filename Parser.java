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
   // some constants deciding parsing mode
   // only reports syntax error
   static public final int NONE = 0;
   // includes scope analysis
   static public final int SCOPE = 1;
   // includes both scope analysis and role analysis
   static public final int ROLE = 2;


   private Chario chario;
   private Scanner scanner;
   // next token waiting to be processes
   private Token token;
   private SymbolTable table;
   private final int mode;
   // these sets include some of TinyAda's operator symbols
   // and the tokens that begin various declarations and statements in the language
   // see initHandles for details
   private Set<Integer> addingOperator,
                        multiplyingOperator,
                        relationalOperator,
                        basicDeclarationHandles,
                        statementHandles,
                        leftNames, // Sets of roles for names (see below)
                        rightNames;

   public Parser(Chario c, Scanner s, int mode) {
      // save for reference later
      chario = c;
      scanner = s;
      this.mode = mode;
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
      leftNames = new HashSet<Integer>();                 // Name roles for targets of assignment statement
      leftNames.add(SymbolEntry.PARAM);
      leftNames.add(SymbolEntry.VAR);
      rightNames = new HashSet<Integer>(leftNames);       // Name roles for names in expressions
      rightNames.add(SymbolEntry.CONST);
   }

   /*
   Two new routines for role analysis.
   */

  private void acceptRole(SymbolEntry s, int expected, String errorMessage){
      if (this.mode == Parser.ROLE){
         if (s.role != SymbolEntry.NONE && s.role != expected){
            chario.putError(errorMessage);
         }
      }
   }

   private void acceptRole(SymbolEntry s, Set<Integer> expected, String errorMessage){
      if (this.mode == Parser.ROLE){
         if (s.role != SymbolEntry.NONE && ! (expected.contains(s.role))){
            chario.putError(errorMessage);
         }
      }
   }

   // provide an adapter for dealing with cases when we don't want to do scope or role analysis
   private void setRole(SymbolEntry s, final int role){
      if (this.mode == Parser.ROLE){
         s.setRole(role);
      }
   }

   private void appendEntry(SymbolEntry head, SymbolEntry tail){
      if (this.mode == Parser.SCOPE || this.mode == Parser.ROLE){
         head.append(tail);
      }
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
      if (this.mode == Parser.ROLE || this.mode == Parser.SCOPE){
         table = new SymbolTable(chario);
         this.enterScope();
         // There are five predefined identifiers
         // TODO: what is the definition of an identifier?
         SymbolEntry entry = table.enterSymbol("BOOLEAN");
         this.setRole(entry, SymbolEntry.TYPE);
         entry.setRole(SymbolEntry.TYPE);
         this.setRole(entry, SymbolEntry.TYPE);
         entry = table.enterSymbol("CHAR");
         this.setRole(entry, SymbolEntry.TYPE);
         entry = table.enterSymbol("INTEGER");
         this.setRole(entry, SymbolEntry.TYPE);
         entry = table.enterSymbol("TRUE");
         this.setRole(entry, SymbolEntry.TYPE);
         entry = table.enterSymbol("FALSE");
         this.setRole(entry, SymbolEntry.TYPE);
      }
   }      

   private void enterScope(){
      if (this.mode == Parser.ROLE || this.mode == Parser.SCOPE){
         table.enterScope();
      }
   }

   private void exitScope(){
      if (this.mode == Parser.ROLE || this.mode == Parser.SCOPE){
         table.exitScope(this.mode);
      }
   }

// first check the token's code to determine that it is an identifier
// If it is, register this identifier in the table and return the entry
private SymbolEntry enterId(){
   SymbolEntry entry = null;
   if (token.code == Token.ID){
      if (this.mode == Parser.SCOPE || this.mode == Parser.ROLE){
         entry = table.enterSymbol(token.string);
      }
   }
   else{
      fatalError("identifier expected");
   } 
   token = scanner.nextToken();
   return entry;
}

// consume the next token (representing an identifier)
// and find the corresponding entry in the table
private SymbolEntry findId(){
   SymbolEntry entry = null;
   if (token.code == Token.ID){
      if (this.mode == Parser.SCOPE || this.mode == Parser.ROLE){
         entry = table.findSymbol(token.string);
      }
   }
   else{
      fatalError("identifier expected");
   }
   token = scanner.nextToken();
   return entry;
}

   // the beginning of parsing process
   // this method will either return normally or throw an exception
   // if the first syntax error is encountered
   public void parse(){
      subprogramBody();
      accept(Token.EOF, "extra symbols after logical end of program");
      this.exitScope();
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
      this.exitScope();
      // the identifier here is optional, so we need to check first
      if (token.code == Token.ID){
         SymbolEntry entry = findId();
         acceptRole(entry, SymbolEntry.PROC, "must be a procedure name");
      }
      accept(Token.SEMI, "';' expected");
   }

   /*
   subprogramSpecification = "procedure" identifier [ formalPart ]
   */
   // wrote by xizma
   private void subprogramSpecification(){
      // "procedure" is a keyword, not an identifier
      accept(Token.PROC, "'procedure' expected");
      SymbolEntry entry = enterId();
      this.setRole(entry, SymbolEntry.PROC);
      this.enterScope();
      if(token.code == Token.L_PAR){
         formalPart();
      }
   }
   /*
   formalPart = "(" parameterSpecification { ";" parameterSpecification } ")"
   */
   // wrote by xizma
   private void formalPart(){
      accept(Token.L_PAR, "'(' expected");
      parameterSpecification();
      while(token.code == Token.SEMI){
         token = scanner.nextToken();
         parameterSpecification();
      }
      accept(Token.R_PAR, "')' expected");
   }
   /*
   parameterSpecification = identifierList ":" mode <type>name
   */
   // wrote by xizma
   private void parameterSpecification(){
      SymbolEntry list = identifierList();
      // TODO: what is the difference between parameters and variables?
      this.setRole(list, SymbolEntry.PARAM);
      accept(Token.COLON, "':' expected");
      mode();
      SymbolEntry entry = name();
      acceptRole(entry, SymbolEntry.TYPE, "must be a type name");
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
      SymbolEntry list = identifierList();
      accept(Token.COLON, "':' expected");
      if (token.code == Token.CONST){
         this.setRole(list, SymbolEntry.CONST);
         token = scanner.nextToken();
         accept(Token.GETS, "':=' expected");
         expression();
      }
      else{
         this.setRole(list, SymbolEntry.VAR);
         typeDefinition();
      }
      accept(Token.SEMI, "';' expected");
   }

   /*
   typeDeclaration = "type" identifier "is" typeDefinition ";"
   */
   // wrote by xizma
   private void typeDeclaration(){
      accept(Token.TYPE, "'type' expected");
      SymbolEntry entry = enterId();
      this.setRole(entry, SymbolEntry.TYPE);
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
            SymbolEntry entry = name();
            acceptRole(entry, SymbolEntry.TYPE, "must be a type name");
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
      accept(Token.L_PAR, "'(' expected");
      SymbolEntry list = identifierList();
      // All of the IDs here should be constants
      this.setRole(list, SymbolEntry.CONST);
      accept(Token.R_PAR, "')' expected");
   }

   /*
   arrayTypeDefinition = "array" "(" index { "," index } ")" "of" <type>name
   */
   // wrote by xizma
   private void arrayTypeDefinition(){
      accept(Token.ARRAY, "'array' expected");
      accept(Token.L_PAR, "'(' expected");
      index();
      while(token.code == Token.COMMA){
         token = scanner.nextToken();
         index();
      }
      accept(Token.R_PAR, "')' expected");
      accept(Token.OF, "'of' expected");
      SymbolEntry entry = name();
      acceptRole(entry, SymbolEntry.TYPE, "must be a type name");
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
         SymbolEntry entry = name();
         acceptRole(entry, SymbolEntry.TYPE, "must be a type name");
      }
      else{
         fatalError("error in index type");
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
   private SymbolEntry identifierList(){
      // this method gets called every time a kind of declaration happens
      SymbolEntry list = enterId();
      while(token.code == Token.COMMA){
         token = scanner.nextToken();
         this.appendEntry(list, enterId());
      }
      return list;
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
      SymbolEntry entry = name();
      if (token.code == Token.GETS){
         // it is an assignmentStatement
         // TODO: check later if there is the desired behavior
         acceptRole(entry, this.leftNames, "must be a parameter or variable name");
         token = scanner.nextToken();
         expression();
      }
      else{
         // it is a procedureCallStatement
         acceptRole(entry, SymbolEntry.PROC, "must be a procedure name");
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
   // TODO: check how to interpret this rule
   private void factor(){
      if (token.code == Token.NOT){
         token = scanner.nextToken();
         primary();
      }
      else{
         primary();
         if(token.code == Token.EXPO){
            token = scanner.nextToken();
            primary();
         }
      }
      // primary();
      // there are three possibilities: nothing, "**" and "not"
      // if(token.code == Token.EXPO){
      //    token = scanner.nextToken();
      //    primary();
      // }
      // else if(token.code == Token.NOT){
      //    token = scanner.nextToken();
      //    primary();
      // }
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
            SymbolEntry entry = name();
            acceptRole(entry, this.rightNames, "must be a parameter, variable or constant name");
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
   private SymbolEntry name(){
      SymbolEntry entry = findId();
      if (token.code == Token.L_PAR)
         indexedComponent();
      return entry;
   }

   /*
   indexedComponent = "(" expression  { "," expression } ")"
   */
   private void indexedComponent(){
      accept(Token.L_PAR, "'(' expected");
      expression();
      while(token.code == Token.COMMA){
         token = scanner.nextToken();
         expression();
      }
      accept(Token.R_PAR, "')' expected");
      
   }

}
