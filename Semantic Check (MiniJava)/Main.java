
import syntaxtree.*;
import visitor.*;
import java.io.*;

public class Main {
    public static void main (String [] args){
    if(args.length < 1){
        System.err.println("Usage1: java [MainClassName] [file1] [file2] ... [fileN]");
        System.exit(1);
    }
    FileInputStream fis = null;
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

	      TC_Visitor tcv = new TC_Visitor();
	      root.accept(tcv, st);
        System.err.println("Program's sematic check was successful.");
        st.offset_calc();
    }
    catch(ParseException ex){
        System.out.println(ex.getMessage());
    }
    catch(FileNotFoundException ex){
        System.err.println(ex.getMessage());
    }
    finally{
        try{
        if(fis != null) fis.close();
        }
        catch(IOException ex){
        System.err.println(ex.getMessage());
        }
    }
  }
}
}
