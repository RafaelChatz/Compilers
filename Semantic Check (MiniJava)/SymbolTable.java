import java.util.*;

public class SymbolTable {

  private Map<String, Class_contents> classes;

  private int current_line;
  private Class_contents current_class;
  private Method_contents current_method;

  private List<String> visit_method_parameters;

  public SymbolTable() {
      current_class=null;
      current_method=null;
      classes = new LinkedHashMap<String, Class_contents>();
      visit_method_parameters = new ArrayList<String>();
  }

  public void set_current_class(String name){
    current_class=classes.get(name);
  }


  public void set_current_method(String name){
    current_method=current_class.get_method_info(name);
  }


  public void class_null(){
    current_class=null;
  }

  public void method_null(){
    current_method=null;
  }

  public boolean is_m_null(){
    return current_method==null;
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

  public String get_current_class_name(){
    return current_class.get_name();
  }

  public String find_variable_type(String name){

    if (name.equals("boolean") ||name.equals("int") ||name.equals("int[]") ){
      return name;
    }
    String type=current_method.get_variable_type(name);
    if(type!=null)
      return type;
    type=current_method.get_parameter_type(name);
    if(type!=null)
      return type;

     type=current_class.get_attribute_type(name);
    if(type!=null)
      return type;


    Class_contents par=classes.get(current_class.getParent());
    while(par!=null){
      type=par.get_attribute_type(name);
      if(type==null){
        par=classes.get(par.getParent());
        continue;
      }
      return type;

    }
    if(par==null)
      return null;

    return null;
  }

  public boolean contains_class(String name){
    return classes.containsKey(name);
  }

  public String check_visit_method(String classs,String method){

    Class_contents cl=classes.get(classs);
    while(cl!=null){
      Method_contents visit_method=cl.get_method_info(method);
      if(visit_method==null){
        cl=classes.get(cl.getParent());
        continue;
      }
      return visit_method.get_return_type();
    }
    return null;

  }

  public void set_current_line(int line){
    current_line=line;
  }

  public int get_current_line(){
    return current_line;
  }

  public void add_to_list_beg(String type,boolean be){
    if(be)
      visit_method_parameters.add(0, type);
    else
    visit_method_parameters.add(type);

  }

  public void empty_list(){
    visit_method_parameters.clear();
  }
  public void print_list(){
    System.out.println(Arrays.toString(visit_method_parameters.toArray()));
  }

  public boolean check_param_list(String classs,String method){
    Class_contents cls=classes.get(classs);
    Method_contents visit_method;
    while(cls!=null){
      visit_method=cls.get_method_info(method);
      if(visit_method==null){
        cls=classes.get(cls.getParent());
        continue;
      }
      break;
    }

    Map<String, String> visit_method_param=cls.get_method_info(method).get_parameters();

    List<String> list = new ArrayList<String>(visit_method_param.values());
    if(list.size()!=visit_method_parameters.size())
      return false;
    for (int i = 0; i < list.size(); i++) {
      if(list.get(i)==visit_method_parameters.get(i))
		    continue;
      Class_contents cl=classes.get(visit_method_parameters.get(i));
      while(cl!=null){
        if(list.get(i)==cl.getParent())
          break;
        cl=classes.get(cl.getParent());
      }
      if(cl==null)
        return false;
      if(list.get(i)==cl.getParent())
        continue;
      return false;
	  }
    return true;
  }

  public void offset_calc(){
    Map<String, Integer> att_offsets;
    Map<String, Integer> meth_offsets;
    att_offsets = new LinkedHashMap<String, Integer>();
    meth_offsets = new LinkedHashMap<String, Integer>();

    for (Map.Entry<String, Class_contents> cl : classes.entrySet()) {
      int att_offset=0;
      int meth_offset=0;
      String cl_name = cl.getKey();
      Class_contents classs = cl.getValue();
      String par=classs.getParent();
      if(par!=null){
        att_offset=att_offsets.get(par);
        meth_offset=meth_offsets.get(par);
      }

      for (Map.Entry<String, String> att : classs.get_attributes().entrySet()) {
        String name = att.getKey();
        String type = att.getValue();
        System.out.println(cl_name+"."+name+" : "+att_offset);
        if(type.equals("int"))
          att_offset+=4;
        else if(type.equals("boolean"))
          att_offset+=1;
        else
          att_offset+=8;
      }
      for (Map.Entry<String, Method_contents> meth : classs.get_methods().entrySet()) {
        String name = meth.getKey();
        if(check_parents_method(cl_name,name))
          continue;

        System.out.println(cl_name+"."+name+" : "+meth_offset);
        meth_offset+=8;
      }
      att_offsets.put(cl_name,att_offset);
      meth_offsets.put(cl_name,meth_offset);

    }
  }


public boolean check_parents_method(String cl_name ,String name){

  String parent_class=classes.get(cl_name).getParent();
  Method_contents cur_method=classes.get(cl_name).get_methods().get(name);
  while(parent_class!=null){
    Class_contents cl=classes.get(parent_class);
    Method_contents parent_method=cl.get_method_info(name);
    if(parent_method!=null)
      return cur_method.method_equal(parent_method);
    parent_class=cl.getParent();
  }
  return false;
}

  public boolean checkMethod_over(){

    String parent_class=current_class.getParent();
    while(parent_class!=null){
      Class_contents cl=classes.get(parent_class);
      Method_contents parent_method=cl.get_method_info(current_method.get_name());
      if(parent_method!=null)
        return current_method.method_equal(parent_method);
      parent_class=cl.getParent();
    }
    return true;

  }
  public boolean checktype(String name1,String name2){
    if(name1.equals(name2))
      return true;
    Class_contents cls=classes.get(name2);
    if(cls==null)
      return false;

    String cl=cls.getParent();
    while(cl!=null){
      if(cl.equals(name1))
        return true;
      cl=classes.get(cl).getParent();
    }
    return false;
  }
}
