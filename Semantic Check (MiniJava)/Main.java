import syntaxtree.*;
import visitor.*;
import java.io.*;

public class Main {
    public static void main (String [] args){
    if(args.length != 1){
        System.err.println("Usage1: java [MainClassName] [file1] [file2] ... [fileN]");        		System.err.println("Usage1: java [MainClassName] -f [Folder]  ");
        System.exit(1);
    }
    FileInputStream fis = null;
    try{
        fis = new FileInputStream(args[0]);
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
