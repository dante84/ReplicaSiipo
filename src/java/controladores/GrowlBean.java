
package controladores;

// @author Daniel.Meza

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.primefaces.event.FileUploadEvent;

 
public class GrowlBean {
 
       private String mensaje;  
                     
       public GrowlBean() {                          
           
       }
       
       public String getMensaje() { return mensaje; }

       public void setMensaje(String mensaje) { this.mensaje = mensaje; }
       
       public void ListenerFU(FileUploadEvent fue){
             
              FacesContext context = FacesContext.getCurrentInstance();              
              context.addMessage("Exito",new FacesMessage("", fue.getFile().getFileName() + " fue subido exitosamente"));
            
       }
       
}
