
import syntaxtree.*;
import visitor.*;
import java.io.*;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class Main {
    public static void main (String [] args){
    if(args.length < 1){
        System.err.println("Usage1: java [MainClassName] [file1] [file2] ... [fileN]");
        System.exit(1);
    }
    FileInputStream fis = null;
    BufferedWriter Bw = null;
    for (int i = 0; i < args.length; i++) {
    try{
      	System.out.println("----------------------\n\nJava file : " + args[i]);
        fis = new FileInputStream(args[i]);
        MiniJavaParser parser = new MiniJavaParser(fis);
        Goal root = parser.Goal();
        System.err.println("Program parsed successfully.");

 	      SymbolTable st = new SymbolTable();
 	      STP_Visitor stpv = new STP_Visitor();
	      root.accept(stpv, st);
 	      System.err.println("Program's symbol table populated successfully.");
        st.offset_calc();

        String filename=args[i].substring(0,args[i].indexOf("."))+".ll";
        Bw = new BufferedWriter(new FileWriter(filename));
        LLVM_Visitor llvmv = new LLVM_Visitor(Bw,st);
        root.accept(llvmv, st);
    }
    catch(ParseException ex){
        System.out.println(ex.getMessage());
    }
    catch(FileNotFoundException ex){
        System.err.println(ex.getMessage());
    }catch(IOException ex){
         ex.printStackTrace();
     }
    finally{
        try{
          if(fis != null) fis.close();
          if (Bw != null) Bw.close();
        }
        catch(IOException ex){
        System.err.println(ex.getMessage());
        }
    }
  }
}
}
