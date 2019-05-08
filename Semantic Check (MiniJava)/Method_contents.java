import java.util.*;


//storage class for methods

public class Method_contents {

  private final String return_type;
  private final String name;  //method name

  // Map<parameter_name,parameter_info >
  private Map<String, String> parameters;
  // Map<variable_name,variable_type >
  private Map<String, String> variables;


  public  Map<String, String> get_parameters(){
      return parameters;
  }

  public Method_contents(String r_type,String id) {
    this.return_type=r_type;
    this.name=id;

    parameters = new LinkedHashMap<String, String>();
    variables = new LinkedHashMap<String, String>();

   }

   public boolean insert_parameter(String name,String type){

     if (this.parameters.containsKey(name)) {
       return false;
     } else {
       this.parameters.put(name,type);
       return true;
     }

   }

   public boolean insert_variable(String name,String type){

     if (this.variables.containsKey(name)) {
       return false;
     } else {
       if(this.parameters.containsKey(name))
        return false;
       this.variables.put(name,type);
       return true;
     }

   }

   public int getParameter_num(){
     return this.parameters.size();
   }

   public String get_return_type(){
     return this.return_type;
   }

   public String get_name(){
     return this.name;
   }

   public String get_parameter_type(String par){
     if (this.parameters.containsKey(par)) {
       return this.parameters.get(par);
     }

     return null;
   }

   public String get_variable_type(String var){

     if (this.variables.containsKey(var)) {
       return this.variables.get(var);
     }

     return null;
   }

   public boolean method_equal(Method_contents meth){

      if(this.getParameter_num()==meth.getParameter_num()){

        Set<String> keys = parameters.keySet();
        Set<String> keys1 =meth.get_parameters().keySet();
        List<String> list = new ArrayList<>();
        List<String> list1 = new ArrayList<>();

        for(String k:keys){
          list.add(parameters.get(k));
        }

        for(String k:keys1){
          list1.add(meth.get_parameters().get(k));
        }

        for (int i = 0; i < list.size(); i++) {
            if(list.get(i)!=list1.get(i))
              return false;
		    }
      }else{
      return false;
    }
    return true;
   }
}
