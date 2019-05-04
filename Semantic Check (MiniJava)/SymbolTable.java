import java.util.*;

public class SymbolTable {

  private Map<String, Class_contents> classes;

  private int current_line;
  private Class_contents current_class;
  private Method_contents current_method;

  public SymbolTable() {
      classes = new LinkedHashMap<String, Class_contents>();
      current_line=-1;
  }

  public boolean insert_class(String name) {
    if (classes.containsKey(name)) {
      return false;
    }
    else{
      Class_contents classs = new Class_contents(name);
      this.current_class=classs;
      this.classes.put(name, classs);
      return true;
    }
  }

  public int insert_class(String name,String parent) {
    if (classes.containsKey(name)) {
      return -1;
    }
    else{
      if (!classes.containsKey(parent)) {
        return -2;
      }
      Class_contents classs = new Class_contents(name,parent);
      this.current_class=classs;
      this.classes.put(name, classs);
      return 0;
    }
  }

  public boolean insert_method(String return_type,String name) {
        if (current_class != null){
          if (current_class.insert_method(return_type, name)){
              current_method=current_class.get_method_info(name);
              return true;
          }
          else
            return false;
        }
        else
            return false;
  }

  public boolean insert_attribute(String name,String type){
    if (current_class != null)
        return current_class.insert_attribute(name, type);
    else
        return false;
  }


  public boolean insert_parameter(String name,String type){
    if (current_method != null){
      return current_method.insert_parameter(name, type);
    }
    else
      return false;
  }

  public boolean insert_variable(String name,String type){

    if (current_method != null){
      return current_method.insert_variable(name, type);
    }
    else
      return false;

  }

}
