import java.util.*;


//storage class for Classes

public class Class_contents {

  private final String name;  //class name
  private final String extend; // parent class name (if exist)

  // Map<attribute_name,attribute_type >
  private Map<String, String> attributes;
  // Map<method_name,method_info >
  private Map<String, Method_contents> methods;

  public Class_contents(String id) {
    name=id;
    extend=null;
    attributes = new LinkedHashMap<String, String>();
    methods    = new LinkedHashMap<String, Method_contents>();
  }

   public Class_contents(String id,String parent) {
     name=id;
     extend=parent;
     attributes = new LinkedHashMap<String, String>();
     methods    = new LinkedHashMap<String, Method_contents>();
   }

   public boolean insert_attribute(String name,String type){

     if (this.attributes.containsKey(name)) {
       return false;
     } else {
       this.attributes.put(name,type);
       return true;
     }

   }

   public boolean insert_method(String return_type,String name){

     if (this.methods.containsKey(name)) {
       return false;
     } else {
       Method_contents method = new Method_contents(return_type, name);
       this.methods.put(name,method);
       return true;
     }

   }

   public String get_name(){
     return this.name;
   }

   public String getParent(){
     return this.extend;
   }

   public String get_attribute_type(String att){
     if (this.attributes.containsKey(att)) {
       return this.attributes.get(att);
     }

     return null;
   }

   public Method_contents get_method_info(String method_name){
     if (this.methods.containsKey(method_name)) {

       return this.methods.get(method_name);
     }
     return null;
   }

   public  Map<String, String> get_attributes(){
     return attributes;
   }
   public  Map<String, Method_contents> get_methods(){
     return methods;
   }

}
