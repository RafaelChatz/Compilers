
import syntaxtree.*;
import visitor.GJDepthFirst;

public class TC_Visitor extends GJDepthFirst<String, SymbolTable> {

  /**
   * f0 -> "class"
   * f1 -> Identifier()
   * f2 -> "{"
   * f3 -> "public"
   * f4 -> "static"
   * f5 -> "void"
   * f6 -> "main"
   * f7 -> "("
   * f8 -> "String"
   * f9 -> "["
   * f10 -> "]"
   * f11 -> Identifier()
   * f12 -> ")"
   * f13 -> "{"
   * f14 -> ( VarDeclaration() )*
   * f15 -> ( Statement() )*
   * f16 -> "}"
   * f17 -> "}"
   */
  public String visit(MainClass n, SymbolTable s_table) {
    String class_name = n.f1.accept(this, s_table);
    s_table.set_current_class(class_name);
    String method_name = n.f6.toString();
    s_table.set_current_method(method_name);

    n.f11.accept(this, s_table);
    n.f14.accept(this, s_table);
    n.f15.accept(this, s_table);

    s_table.method_null();
    s_table.class_null();
    return null;
  }


  /**
   * f0 -> "class"
   * f1 -> Identifier()
   * f2 -> "{"
   * f3 -> ( VarDeclaration() )*
   * f4 -> ( MethodDeclaration() )*
   * f5 -> "}"
   */
  public String visit(ClassDeclaration n, SymbolTable s_table) {
    String class_name = n.f1.accept(this, s_table);
    s_table.set_current_class(class_name);

    n.f3.accept(this, s_table);
    n.f4.accept(this, s_table);

    s_table.class_null();
    return null;
  }

  /**
   * f0 -> "class"
   * f1 -> Identifier()
   * f2 -> "extends"
   * f3 -> Identifier()
   * f4 -> "{"
   * f5 -> ( VarDeclaration() )*
   * f6 -> ( MethodDeclaration() )*
   * f7 -> "}"
   */
  public String visit(ClassExtendsDeclaration n, SymbolTable s_table) {
    String class_name = n.f1.accept(this, s_table);
    s_table.set_current_class(class_name);

    n.f5.accept(this, s_table);
    n.f6.accept(this, s_table);

    return null;
  }

  /**
   * f0 -> "public"
   * f1 -> Type()
   * f2 -> Identifier()
   * f3 -> "("
   * f4 -> ( FormalParameterList() )?
   * f5 -> ")"
   * f6 -> "{"
   * f7 -> ( VarDeclaration() )*
   * f8 -> ( Statement() )*
   * f9 -> "return"
   * f10 -> Expression()
   * f11 -> ";"
   * f12 -> "}"
   */
  public String visit(MethodDeclaration n, SymbolTable s_table) {

    String return_type=n.f1.accept(this, s_table);
    String method_name=n.f2.accept(this, s_table);
    s_table.set_current_method(method_name);
    s_table.set_current_line(n.f3.beginLine);
    n.f4.accept(this, s_table);

    n.f7.accept(this, s_table);
    n.f8.accept(this, s_table);

    String method_return=n.f10.accept(this, s_table);


    if (method_return.equals("boolean") ||method_return.equals("int") ||method_return.equals("int[]") ){
      if(!method_return.equals(return_type))
        throw new IllegalArgumentException("Error: Line: " + n.f9.beginLine + ". Method " + method_name + " tries to return a wrong type");
    }
    else if(method_return.equals("this")){
      if(!return_type.equals(s_table.get_current_class_name()) )
        throw new IllegalArgumentException("Error: Line: " + n.f9.beginLine + ". Method " + method_name + " tries to return a wrong type");

    }
    else{
      String res=s_table.find_variable_type(method_return);
      if(res==null)
        throw new IllegalArgumentException("Error: Line: " + n.f9.beginLine + ". Variable " + method_return + " has not been declared");

      if(!return_type.equals(res) )
        throw new IllegalArgumentException("Error: Line: " + n.f9.beginLine + ". Method " + method_name + " tries to return a wrong type");

    }


    s_table.method_null();
    return null;
  }


  /**
   * f0 -> Type()
   * f1 -> Identifier()
   */
  public String visit(FormalParameter n,SymbolTable s_table) {

     String type=n.f0.accept(this, s_table);
     String name=n.f1.accept(this, s_table);
     if (type.equals("boolean") ||type.equals("int") ||type.equals("int[]") )
      return null;
     if(!s_table.contains_class(type))
      throw new IllegalArgumentException("Error: Line: " + s_table.get_current_line() + ". Type " + type + " does not exist");
      return null;

  }

  /**
   * f0 -> Identifier()
   * f1 -> "="
   * f2 -> Expression()
   * f3 -> ";"
   */
  public String visit(AssignmentStatement n, SymbolTable s_table) {

    String Statementid = n.f0.accept(this, s_table);
    String res=s_table.find_variable_type(Statementid);

    if(res==null)
      throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ". Variable " + Statementid + " has not been declared.");

    String expr = n.f2.accept(this, s_table);
    if (expr.equals("boolean") ||expr.equals("int") ||expr.equals("int[]") ){
      if(!expr.equals(res))
      throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ".  " + " tried to assign a wrong type.Correct type :"+res);
    }
    else if(expr.equals("this")){
      if(!res.equals(s_table.get_current_class_name()) )
      throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ".  " + " tried to assign a wrong type.Correct type :"+res);

    }
    else if(expr.startsWith("new")){

      if(!s_table.contains_class(expr.substring(expr.lastIndexOf(" ") + 1)) )
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ".  " + " tried to assign a wrong type.Correct type :"+res);

    }
    else{
      String ress=s_table.find_variable_type(expr);
      if(ress==null&&(!res.equals(expr)))
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ". aVariable " + expr + " has not been declared");

      if(!s_table.checktype(res,ress)&&(!res.equals(expr)) )
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ".  " + " tried to assign a wrong type.Correct type :"+res);

    }

    return null;
  }

  /**
   * f0 -> Identifier()
   * f1 -> "["
   * f2 -> Expression()
   * f3 -> "]"
   * f4 -> "="
   * f5 -> Expression()
   * f6 -> ";"
   */
  public String visit(ArrayAssignmentStatement n, SymbolTable s_table) {
    String Statementid = n.f0.accept(this, s_table);
    String res=s_table.find_variable_type(Statementid);
    if(res==null)
      throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine +
      ". Variable " + Statementid + " has not been declared.");
    if (!res.equals("int[]"))
      throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine +
      ". Variable " + Statementid + " is not of  type int[].");

    String Statement_brack_expr = n.f2.accept(this, s_table);
    if(!Statement_brack_expr.equals("int"))
    {
      String ress=s_table.find_variable_type(Statement_brack_expr);
      if(ress==null)
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ". Variable " + Statement_brack_expr + " has not been declared");

      if(!ress.equals("int"))
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ".  " + " tried to assign a wrong type.Correct type :"+res);

    }
    String expr = n.f5.accept(this, s_table);
    if(!expr.equals("int"))
    {
      String ress=s_table.find_variable_type(expr);
      if(ress==null)
        throw new IllegalArgumentException("Error: Line: " + n.f4.beginLine + ". Variable " + expr + " has not been declared");

      if(!ress.equals("int"))
        throw new IllegalArgumentException("Error: Line: " + n.f4.beginLine + ".  " + " tried to assign a wrong type.Correct type :"+res);

    }

    return null;
  }

  /**
   * f0 -> "if"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> Statement()
   * f5 -> "else"
   * f6 -> Statement()
   */
  public String visit(IfStatement n, SymbolTable s_table) {

    String if_expr = n.f2.accept(this, s_table);
    if(!if_expr.equals("boolean"))
    {
      String ress=s_table.find_variable_type(if_expr);
      if(ress==null)
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ". Variable " + if_expr + " has not been declared");

      if(!ress.equals("boolean"))
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ".  " + "  wrong expression type in if statement .Correct type :"+"boolean");

    }

    n.f4.accept(this, s_table);
    n.f6.accept(this, s_table);

     return null;
  }


  /**
   * f0 -> "while"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> Statement()
   */
  public String visit(WhileStatement n, SymbolTable s_table) {
    String while_expr = n.f2.accept(this, s_table);
    if(!while_expr.equals("boolean"))
    {
      String ress=s_table.find_variable_type(while_expr);
      if(ress==null)
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ". Variable " + while_expr + " has not been declared");

      if(!ress.equals("boolean"))
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ".  " + "  wrong expression type in while statement .Correct type :"+"boolean");

    }

    n.f4.accept(this, s_table);
    return null;
  }

  /**
   * f0 -> "System.out.println"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> ";"
   */
  public String visit(PrintStatement n, SymbolTable s_table) {

    String print_expr = n.f2.accept(this, s_table);

    if((!print_expr.equals("boolean"))&&(!print_expr.equals("int")))
    {
      String ress=s_table.find_variable_type(print_expr);
      if(ress==null)
        throw new IllegalArgumentException("Error: Line: " + n.f4.beginLine + ". Variable " + print_expr + " has not been declared");

      if((!ress.equals("boolean"))&&(!ress.equals("int")))
        throw new IllegalArgumentException("Error: Line: " + n.f4.beginLine + ".  " + " wrong type in print statement.Correct type :"+"int or boolean");

    }
    return null;
  }

  /**
   * f0 -> Clause()
   * f1 -> "&&"
   * f2 -> Clause()
   */
  public String visit(AndExpression n, SymbolTable s_table) {

     String Clausef0=n.f0.accept(this, s_table);
     if(!Clausef0.equals("boolean"))
     {
       String ress=s_table.find_variable_type(Clausef0);
       if(ress==null)
         throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ". Variable " + Clausef0 + " has not been declared");

       if(!ress.equals("boolean"))
         throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ".  " + "  wrong expression type  .Correct type :"+"boolean");

     }
     String Clausef2=n.f2.accept(this, s_table);
     if(!Clausef2.equals("boolean"))
     {
       String ress=s_table.find_variable_type(Clausef2);
       if(ress==null)
         throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ". Variable " + Clausef2 + " has not been declared");

       if(!ress.equals("boolean"))
         throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ".  " + "  wrong expression type .Correct type :"+"boolean");

     }
     return "boolean";
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "<"
   * f2 -> PrimaryExpression()
   */
  public String visit(CompareExpression n, SymbolTable s_table) {
    String PrimaryExpressionf0=n.f0.accept(this, s_table);
    if((!PrimaryExpressionf0.equals("boolean"))&&(!PrimaryExpressionf0.equals("int")))
    {
      String ress=s_table.find_variable_type(PrimaryExpressionf0);
      if(ress==null)
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ". Variable " + PrimaryExpressionf0 + " has not been declared");

      if((!ress.equals("boolean"))&&(!ress.equals("int")))
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ".  " + "  wrong expression type .Correct type :"+"boolean or int ");

    }
    String PrimaryExpressionf2=n.f2.accept(this, s_table);
    if((!PrimaryExpressionf0.equals("boolean"))&&(!PrimaryExpressionf0.equals("int")))
    {
      String ress=s_table.find_variable_type(PrimaryExpressionf2);
      if(ress==null)
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ". Variable " + PrimaryExpressionf2 + " has not been declared");

      if((!ress.equals("boolean"))&&(!ress.equals("int")))
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ".  " + "  wrong expression type .Correct type :"+"boolean or int");

    }
    return "boolean";
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "+"
   * f2 -> PrimaryExpression()
   */
  public String visit(PlusExpression n, SymbolTable s_table) {
    String PrimaryExpressionf0=n.f0.accept(this, s_table);
    if(!PrimaryExpressionf0.equals("int"))
    {
      String ress=s_table.find_variable_type(PrimaryExpressionf0);
      if(ress==null)
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ". Variable " + PrimaryExpressionf0 + " has not been declared");

      if(!ress.equals("int"))
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ".  " + "  wrong expression type .Correct type :"+"int");

    }
    String PrimaryExpressionf2=n.f2.accept(this, s_table);
    if(!PrimaryExpressionf2.equals("int"))
    {
      String ress=s_table.find_variable_type(PrimaryExpressionf2);
      if(ress==null)
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ". Variable " + PrimaryExpressionf2 + " has not been declared");

      if(!ress.equals("int"))
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ".  " + "  wrong expression type .Correct type :"+"int");

    }
    return "int";
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "-"
   * f2 -> PrimaryExpression()
   */
  public String visit(MinusExpression n, SymbolTable s_table) {
    String PrimaryExpressionf0=n.f0.accept(this, s_table);
    if(!PrimaryExpressionf0.equals("int"))
    {
      String ress=s_table.find_variable_type(PrimaryExpressionf0);
      if(ress==null)
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ". Variable " + PrimaryExpressionf0 + " has not been declared");

      if(!ress.equals("int"))
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ".  " + "  wrong expression type .Correct type :"+"int");

    }
    String PrimaryExpressionf2=n.f2.accept(this, s_table);
    if(!PrimaryExpressionf2.equals("int"))
    {
      String ress=s_table.find_variable_type(PrimaryExpressionf2);
      if(ress==null)
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ". Variable " + PrimaryExpressionf2 + " has not been declared");

      if(!ress.equals("int"))
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ".  " + "  wrong expression type .Correct type :"+"int");

    }
    return "int";
  }


  /**
   * f0 -> PrimaryExpression()
   * f1 -> "*"
   * f2 -> PrimaryExpression()
   */
  public String visit(TimesExpression n, SymbolTable s_table) {
    String PrimaryExpressionf0=n.f0.accept(this, s_table);
    if(!PrimaryExpressionf0.equals("int"))
    {
      String ress=s_table.find_variable_type(PrimaryExpressionf0);
      if(ress==null)
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ". Variable " + PrimaryExpressionf0 + " has not been declared");

      if(!ress.equals("int"))
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ".  " + "  wrong expression type .Correct type :"+"int");

    }
    String PrimaryExpressionf2=n.f2.accept(this, s_table);
    if(!PrimaryExpressionf2.equals("int"))
    {
      String ress=s_table.find_variable_type(PrimaryExpressionf2);
      if(ress==null)
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ". Variable " + PrimaryExpressionf2 + " has not been declared");

      if(!ress.equals("int"))
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ".  " + "  wrong expression type  .Correct type :"+"int");

    }
    return "int";
  }


  /**
   * f0 -> PrimaryExpression()
   * f1 -> "["
   * f2 -> PrimaryExpression()
   * f3 -> "]"
   */
  public String visit(ArrayLookup n, SymbolTable s_table) {

    String PrimaryExpressionf0=n.f0.accept(this, s_table);
    if(!PrimaryExpressionf0.equals("int[]"))
    {
      String ress=s_table.find_variable_type(PrimaryExpressionf0);
      if(ress==null)
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ". Variable " + PrimaryExpressionf0 + " has not been declared");

      if(!ress.equals("int[]"))
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ".  " + "  wrong expression type .Correct type :"+"int");

    }
    String PrimaryExpressionf2=n.f2.accept(this, s_table);
    if(!PrimaryExpressionf2.equals("int"))
    {
      String ress=s_table.find_variable_type(PrimaryExpressionf2);
      if(ress==null)
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ". Variable " + PrimaryExpressionf2 + " has not been declared");

      if(!ress.equals("int"))
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ".  " + "  wrong expression type .Correct type :"+"int");

    }
     return "int";
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "."
   * f2 -> "length"
   */
  public String visit(ArrayLength n, SymbolTable s_table) {

    String PrimaryExpressionf0=n.f0.accept(this, s_table);
    if(!PrimaryExpressionf0.equals("int[]"))
    {
      String ress=s_table.find_variable_type(PrimaryExpressionf0);
      if(ress==null)
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ". Variable " + PrimaryExpressionf0 + " has not been declared");

      if(!ress.equals("int[]"))
        throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ".  " + "  wrong expression type .Correct type :"+"int");

    }
    n.f2.accept(this, s_table);

     return "int";
  }


  /**
   * f0 -> PrimaryExpression()
   * f1 -> "."
   * f2 -> Identifier()
   * f3 -> "("
   * f4 -> ( ExpressionList() )?
   * f5 -> ")"
   */
  public String visit(MessageSend n, SymbolTable s_table) {
    String PrimaryExpressionf0=n.f0.accept(this, s_table);
    String Priexpr=PrimaryExpressionf0.substring(PrimaryExpressionf0.lastIndexOf(" ") + 1);
    String class_name=s_table.find_variable_type(Priexpr);


    if(class_name==null)
      if(s_table.contains_class(Priexpr))
        class_name=Priexpr;
    if(Priexpr.equals("this"))
      class_name=s_table.get_current_class_name();

    if(class_name==null)
      throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ".  " + Priexpr + " has not been declared");

    String method=n.f2.accept(this, s_table);
    String return_type=s_table.check_visit_method(class_name,method);
    if(return_type==null)
      throw new IllegalArgumentException("Error: Line: " + n.f3.beginLine + ". Method " + method + " is not a part of "+class_name);

    s_table.set_current_line(n.f3.beginLine);
    n.f4.accept(this, s_table);

    if(!s_table.check_param_list(class_name,method))
    throw new IllegalArgumentException("Error: Line: " + n.f3.beginLine + ". Method " + method + " is not defined with the given parameters ");


    s_table.empty_list();

    return return_type;
  }

  /**
   * f0 -> Expression()
   * f1 -> ExpressionTail()
   */
  public String visit(ExpressionList n, SymbolTable s_table) {

    String expr = n.f0.accept(this, s_table);

    expr=expr.substring(expr.lastIndexOf(" ") + 1);

    String type;

     if (expr.equals("boolean") ||expr.equals("int") ||expr.equals("int[]") ){
       type=expr;
     }
     else if(expr.equals("this")){
       type=s_table.get_current_class_name();
     }
     else if(s_table.contains_class(expr)){
       type=expr;
     }
     else{
       type=s_table.find_variable_type(expr);
       if(type==null)
         throw new IllegalArgumentException("Error: Line: " + s_table.get_current_line() + ". Variable " + expr + " has not been declared");

     }

     n.f1.accept(this, s_table);
     s_table.add_to_list_beg(type,true);

     return null;
  }


  /**
   * f0 -> ","
   * f1 -> Expression()
   */
  public String visit(ExpressionTerm n, SymbolTable s_table) {

    String expr = n.f1.accept(this, s_table);
    expr=expr.substring(expr.lastIndexOf(" ") + 1);

    String type;

     if (expr.equals("boolean") ||expr.equals("int") ||expr.equals("int[]") ){
       type=expr;
     }
     else if(expr.equals("this")){
       type=s_table.get_current_class_name();
     }
     else{
       type=s_table.find_variable_type(expr);
       if(type==null)
         throw new IllegalArgumentException("Error: Line: " +n.f0.beginLine + ". Variable " + expr + " has not been declared");

     }
     s_table.add_to_list_beg(type,false);
     return null;
  }

  /**
   * f0 -> "new"
   * f1 -> "int"
   * f2 -> "["
   * f3 -> Expression()
   * f4 -> "]"
   */
  public String visit(ArrayAllocationExpression n, SymbolTable s_table) {

     String expr=n.f3.accept(this, s_table);
     if(!expr.equals("int"))
     {
       String ress=s_table.find_variable_type(expr);
       if(ress==null)
         throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ". Variable " + expr + " has not been declared");

       if(!ress.equals("int"))
         throw new IllegalArgumentException("Error: Line: " + n.f1.beginLine + ".  " + "  wrong expression type .Correct type :"+"int");

     }
     return "int[]";
  }

  /**
   * f0 -> "new"
   * f1 -> Identifier()
   * f2 -> "("
   * f3 -> ")"
   */
  public String visit(AllocationExpression n, SymbolTable s_table) {

     String identifier=n.f1.accept(this, s_table);

     if(s_table.contains_class(identifier))
      return "new "+identifier;

    throw new IllegalArgumentException("Error: Line: " + n.f2.beginLine + ".  " + identifier + " has not been declared");

  }

  /**
   * f0 -> "!"
   * f1 -> Clause()
   */
  public String visit(NotExpression n, SymbolTable s_table) {
    String Clausef0 = n.f1.accept(this, s_table);
    if(!Clausef0.equals("boolean"))
    {
      String ress=s_table.find_variable_type(Clausef0);
      if(ress==null)
        throw new IllegalArgumentException("Error: Line: " + n.f0.beginLine + ". Variable " + Clausef0 + " has not been declared");

      if(!ress.equals("boolean"))
        throw new IllegalArgumentException("Error: Line: " + n.f0.beginLine + ".  " + "  wrong expression type  .Correct type :"+"boolean");

    }
     return "boolean";
  }

  ///////////////////////////////////////////////////////
  /*return type*/
  /**
   * f0 -> <INTEGER_LITERAL>
   */
  public String visit(IntegerLiteral n, SymbolTable s_table) {
    return "int";
  }

  /**
   * f0 -> "true"
   */
  public String visit(TrueLiteral n, SymbolTable s_table) {
    return "boolean";
  }

  /**
   * f0 -> "false"
   */
  public String visit(FalseLiteral n, SymbolTable s_table) {
    return "boolean";
  }
  //////////////
  /*return value*/

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, SymbolTable s_table) {
       return n.f0.toString();
    }

    /**
     * f0 -> "this"
     */
    public String visit(ThisExpression n, SymbolTable s_table) {
       return n.f0.toString();
    }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(ArrayType n, SymbolTable s_table) {
       String f_0=  n.f0.toString();
       String f_1=  n.f1.toString();
       String f_2=  n.f2.toString();
       return f_0+f_1+f_2;
    }

    /**
     * f0 -> "boolean"
     */
    public String visit(BooleanType n, SymbolTable s_table) {
      return n.f0.toString();
    }

    /**
     * f0 -> "int"
     */
    public String visit(IntegerType n, SymbolTable s_table) {
      return n.f0.toString();
    }
//////////////

    /**
     * f0 -> NotExpression()
     *       | PrimaryExpression()
     */
    public String visit(Clause n, SymbolTable s_table) {
       return n.f0.accept(this, s_table);
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public String visit(BracketExpression n, SymbolTable s_table) {
        return n.f1.accept(this, s_table);
    }

    /**
     * f0 -> Block()
     *       | AssignmentStatement()
     *       | ArrayAssignmentStatement()
     *       | IfStatement()
     *       | WhileStatement()
     *       | PrintStatement()
     */
    public String visit(Statement n, SymbolTable s_table) {
        return n.f0.accept(this, s_table);
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */
    public String visit(Block n, SymbolTable s_table)  {
        return n.f1.accept(this, s_table);
    }

    /**
     * f0 -> AndExpression()
     *       | CompareExpression()
     *       | PlusExpression()
     *       | MinusExpression()
     *       | TimesExpression()
     *       | ArrayLookup()
     *       | ArrayLength()
     *       | MessageSend()
     *       | Clause()
     */
    public String visit(Expression n, SymbolTable s_table) {


        return n.f0.accept(this, s_table);
    }

    /**
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | BracketExpression()
     */
    public String visit(PrimaryExpression n, SymbolTable s_table) {
        return n.f0.accept(this, s_table);
    }

}
