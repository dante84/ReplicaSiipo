
package controladores;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.sql.DataSource;

// @author Daniel.Meza
 
public class CLogin implements Serializable {
     
       @Resource(name="jdbc/siipo")
       private DataSource ds;
       private String nombre;
       private FacesContext context = FacesContext.getCurrentInstance();
       private Connection conexion;
       private Statement select;
       
       public String validarLogin(){
            
              try{
                  conexion = ds.getConnection();
                  select = conexion.createStatement();
                  String squery = "select nombre from usuarios where nombre like '" + nombre + "%'";
                  String cquery = "select count(*) from usuarios where nombre like '" + nombre + "%'";
                  
                  ResultSet rs = select.executeQuery(cquery);
                  rs.first();
                  int contadorUsuarios = rs.getInt(1);
                  System.out.println(contadorUsuarios);
                  
                  System.out.println(squery);
                  
                  rs = select.executeQuery(squery);
                  
                  if( contadorUsuarios == 0 ){
                             context.addMessage(null, new FacesMessage("El nombre de usuario esta mal escrito o no existe.Verifica por favor"));
                  }else{                                          
                        while( rs.next() ){
                               context.addMessage(null, new FacesMessage("El nombre de usuario " + rs.getString("nombre") + " existe.Redirecionando"));
                               return "name";                 
                        }
                  }
                  
                  rs.close();
                  select.close();
                  conexion.close();
                          
              }catch(Exception e){ e.printStackTrace(); }
             
              return "";
              
       }
   
       public String getNombre() {
              return nombre;
       }

       public void setNombre(String nombre) {
              this.nombre = nombre;
       }
    
}
