import syntaxtree.*;
import visitor.GJDepthFirst;
import java.io.*;
import java.io.BufferedWriter;
import java.util.*;

public class LLVM_Visitor extends GJDepthFirst<String, SymbolTable> {

 private final BufferedWriter Bw;
 private final Map<String,String> types_to_in=Map.of(
    "int", "i32",
    "boolean", "i1",
    "int[]","i32*"
    );

 private List<String> FormalParamList;
 private List<String> VarDeclList;
 private int global_tab;

 public LLVM_Visitor(BufferedWriter bw, SymbolTable symTable) {
        Bw=bw;
        FormalParamList=new ArrayList<String>();
        VarDeclList=new ArrayList<String>();
        global_tab=0;
        Map<String, Class_contents> classes=symTable.get_classes();
        List<String> class_keys = new ArrayList<>(classes.keySet());

        emit("@."+class_keys.get(0) +"_vtable = global [0 x i8*] []",1,0);

        for (int i = 1; i < class_keys.size(); i++) {
          symTable.set_current_class(class_keys.get(i));
          Map<String, Method_contents> methods=symTable.get_current_class_methods();
          List<String> method_keys = new ArrayList<>(methods.keySet());
          emit("@."+class_keys.get(i) +"_vtable = global ["+method_keys.size()+" x i8*] [",0,0);
          for (int j = 0; j < method_keys.size(); j++) {
            if(j!=0)
              emit(", ",0,0);
            emit("i8* bitcast ",0,0);
            Method_contents method = methods.get(method_keys.get(j));
            Map<String, String> params=method.get_parameters();
            List<String> method_params = new ArrayList<>(params.values());
            String typer=types_to_in.get(method.get_return_type());
            if(typer!=null)
              emit("("+typer+" (i8*",0,0);
            else
              emit("("+"i8*"+" (i8*",0,0);

            for (int k=0; k < method_params.size();k++){
              String typep=types_to_in.get(method_params.get(k));
              if(typep!=null)
                emit(","+typep,0,0);
              else
                emit(","+"i8*",0,0);
              if(k!=method_params.size()-1)
                emit(" ",0,0);
            }
            emit(")* @"+class_keys.get(i)+"."+method_keys.get(j)+" to i8*)",0,0);


          }
          emit("]",1,0);
        }
        emit("",2,0);
        emit_helper_methods();
  }

  private void emit(String llout,int add_lines,int add_tabs) {

    try{
      for(int i=0;i<add_tabs;i++)
        Bw.write("\t");
      Bw.write(llout);
      for(int i=0;i<add_lines;i++)
        Bw.write("\n");
    }
    catch (IOException ex) {
        ex.printStackTrace();
    }


  }

  private void emit_helper_methods(){

    emit("declare i8* @calloc(i32, i32)\n" +
         "declare i32 @printf(i8*, ...)\n" +
         "declare void @exit(i32)\n" +
         "\n" +
         "@_cint = constant [4 x i8] c\"%d\\0a\\00\\\"\n" +
         "@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n" +
         "define void @print_int(i32 %i) {\n" +
         "\t%_str = bitcast [4 x i8]* @_cint to i8*\n" +
         "\tcall i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n" +
         "\tret void\n" +
         "}\n" +
         "\n" +
         "define void @throw_oob() {\n" +
         "\t%_str = bitcast [15 x i8]* @_cOOB to i8*\n" +
         "\tcall i32 (i8*, ...) @printf(i8* %_str)\n" +
         "\tcall void @exit(i32 1)\n" +
         "\tret void\n" +
         "}\n",2,0
        );
  }

  private void emit_main_def(int fun_num){
    emit("define i32 @main() {",1,0);
    emit("%_0 = call i8* @calloc(i32 1, i32 20)",1,1);
  	emit("%_1 = bitcast i8* %_0 to i8***",1,1);
  	emit("%_2 = getelementptr [4 x i8*], [4 x i8*]* @.BBS_vtable, i32 0, i32 0",1,1);
	  emit("store i8** %_2, i8*** %_1",1,1);
  }

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
  //  emit_main_def(4);
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
   String parent_class_name = n.f3.accept(this, s_table);
   s_table.set_current_class(class_name);

   n.f5.accept(this, s_table);
   n.f6.accept(this, s_table);

   s_table.class_null();

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
   s_table.set_current_method(name);
   String class_name=s_table.get_current_class_name();
   emit("define "+types_to_in.get(return_type)+" @"+class_name+"."+name+"(i8* %this",0,0);
   n.f4.accept(this, s_table);

   for (String param : FormalParamList) {
     String[] parts = param.split(" ", 2);
     String typ = parts[0];
     String nam = parts[1];
     emit(", "+typ+" %."+nam,0,global_tab);
   }
   emit(") {",1,global_tab);
   global_tab++;
   for (String param : FormalParamList) {
     String[] parts = param.split(" ", 2);
     String typ = parts[0];
     String nam = parts[1];
     emit("%"+nam+" = alloca "+ typ,1,global_tab);
     emit("store "+typ+" %."+ nam+", "+typ+"* %"+nam,1,global_tab);

   }
   FormalParamList.clear();
   n.f7.accept(this, s_table);
   for (String var : VarDeclList) {
     String[] parts = var.split(" ", 2);
     String typ = parts[0];
     String nam = parts[1];
     emit("%"+nam+" = alloca "+typ,1,global_tab);
   }
   VarDeclList.clear();
   n.f8.accept(this, s_table);

   n.f10.accept(this, s_table);
   s_table.method_null();
   emit("}",2,0);

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

    System.out.println(n.f2.accept(this, s_table));

    return null;
 }

 /**
  * f0 -> Type()
  * f1 -> Identifier()
  * f2 -> ";"
  */
 public String visit(VarDeclaration n, SymbolTable s_table) {
   String type=types_to_in.get(n.f0.accept(this, s_table));
   String name=n.f1.accept(this, s_table);
   VarDeclList.add(type+" "+name);

   return null;
 }

 /**
  * f0 -> FormalParameter()
  * f1 -> FormalParameterTail()
  */
 public String visit(FormalParameterList n,SymbolTable s_table) {
    n.f0.accept(this, s_table);
    n.f1.accept(this, s_table);
    return null;
 }

 /**
  * f0 -> Type()
  * f1 -> Identifier()
  */
 public String visit(FormalParameter n,SymbolTable s_table) {

    String type=types_to_in.get(n.f0.accept(this, s_table));
    String name=n.f1.accept(this, s_table);
    FormalParamList.add(type+" "+name);
    return null;
 }

 /**
  * f0 -> ( FormalParameterTerm() )*
  */
 public String visit(FormalParameterTail n, SymbolTable s_table) {
  String s=n.f0.accept(this, s_table);
  return null;
 }

 /**
  * f0 -> ","
  * f1 -> FormalParameter()
  */
 public String visit(FormalParameterTerm n,SymbolTable s_table) {
   n.f1.accept(this, s_table);
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
    String f_0= n.f0.toString();
    String f_1= n.f1.toString();
    String f_2= n.f2.toString();
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
