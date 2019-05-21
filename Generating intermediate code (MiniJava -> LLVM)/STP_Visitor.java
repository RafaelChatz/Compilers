import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;


public class STP_Visitor extends GJDepthFirst<String, SymbolTable> {


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
  public String visit(MainClass n, SymbolTable s_table)
   {

    String class_name = n.f1.accept(this, s_table);

  //  System.out.println("Main class "+ class_name);

    s_table.insert_class(class_name);

    String return_type = n.f5.toString();
    String method_name = n.f6.toString();

    s_table.insert_method(return_type, method_name);

    //System.out.println("\tMain class method: "+ method_name);

    String f_8=  n.f8.toString();
    String f_9=  n.f9.toString();
    String f_10=  n.f10.toString();
    String paramType = f_8+f_9+f_10;
  //  System.out.println("\t\t params");
    //System.out.println("\t\t\t "+ paramType);

    String paramId = n.f11.accept(this, s_table);

    s_table.insert_parameter(paramType, paramId);
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

     if(!s_table.insert_class(class_name))
         throw new IllegalArgumentException("Error: Line: " + n.f0.beginLine + ". Class " + class_name + " already exists");


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
    String parent_class_name = n.f3.accept(this, s_table);
//    System.out.println("Class "+ class_name + "  extends "+ parent_class_name);

    int s_i=s_table.insert_class(class_name,parent_class_name);
    if(s_i==-1)
        throw new IllegalArgumentException("Error. Line: " + n.f0.beginLine + ". Class " + class_name + " already exists");
    else if(s_i==-2)
        throw new IllegalArgumentException("Error. Line: " + n.f0.beginLine + ". Parent Class " + parent_class_name + " does not exist");

     n.f5.accept(this, s_table);
     n.f6.accept(this, s_table);

     s_table.class_null();

     return null;
  }

  /**
   * f0 -> Type()
   * f1 -> Identifier()
   * f2 -> ";"
   */
  public String visit(VarDeclaration n, SymbolTable s_table) {

    String type=n.f0.accept(this, s_table);
    String name=n.f1.accept(this, s_table);

    if(s_table.is_m_null()){
      //System.out.println("\t Class var "+ name);
      if(!s_table.insert_attribute(name,type))
        throw new IllegalArgumentException("Error: Line: " + n.f2.beginLine + ". Variable " + name + " already exists in the class");
    }
    else{
    //  System.out.println("\t\tMethod var "+ name);

      if(!s_table.insert_variable(name,type))
        throw new IllegalArgumentException("Error: Line: " + n.f2.beginLine + ". Variable " + name + " already exists ");
    }

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
  public String visit(MethodDeclaration n,SymbolTable s_table) {

    String return_type=n.f1.accept(this, s_table);
    String name=n.f2.accept(this, s_table);
    if(name.equals("main"))
    throw new IllegalArgumentException("Error: Line: " + n.f0.beginLine + ". Method " + name + " already exists (main cannot exist twice)");

  //  System.out.println("\tMethod  "+ name);

    if(!s_table.insert_method(return_type,name))
        throw new IllegalArgumentException("Error: Line: " + n.f0.beginLine + ". Method " + name + " already exists");


     n.f4.accept(this, s_table);//check superclass
     if(!s_table.checkMethod_over())
        throw new IllegalArgumentException("Error: Line: " + n.f0.beginLine + ". Overriding Method " + name + " does not have same return type and parameters");



     n.f7.accept(this, s_table);
     n.f8.accept(this, s_table);

     n.f10.accept(this, s_table);
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
     //System.out.println("\t\tmethod Param "+ name);

     if(!s_table.insert_parameter(name, type))
      throw new IllegalArgumentException("Error: Line: " + 0 + ". Parameter " + name + " already exists");


     return null;
  }


///////////////////////////////////////////////////////

  /**
   * f0 -> <IDENTIFIER>
   */
  public String visit(Identifier n, SymbolTable s_table) {
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
}
