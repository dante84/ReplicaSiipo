
package controladores;

// @author Daniel.Meza

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import javax.sql.DataSource;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import modelos.ModeloPrueba;
import org.apache.commons.io.FilenameUtils;
import org.apache.myfaces.custom.fileupload.UploadedFile;

public class CArchivo implements Serializable {        
       
       @Resource(name="jdbc/siipo")
       private DataSource ds;
       private UploadedFile archivo;
       
       public void subir(){
                     
              String nombreArchivo = FilenameUtils.getName(archivo.getName());
              System.out.println(nombreArchivo);                                                                                    
               
              String linea = "";                                                 
              int temp = 0; 
              
              try{
                  
                  byte[] bytes = archivo.getBytes();
              
                  for( int i = 0; i <= (bytes.length - 1); i++){
                   
                       temp = (char)bytes[i];
                     
                       if( temp == -1 ){                              
                
                           break;
                       }
                    
                       linea += (char)temp;    
                    
                       if( temp == '\n' ){ 
                           
                           Properties pro = new Properties();
                           pro.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("tiposexamenclaves.properties"));
                           String claveExamen = linea.substring(0,3);                           
                           
                           if( pro.containsKey(claveExamen) ){
                               System.out.println( "Clave del examen " + claveExamen + " valor " + pro.get(claveExamen)); 
                           }
                           
                           FacesContext.getCurrentInstance().addMessage(
                                       null,
                                       //new FacesMessage(String.format("El archivo '%s' fue subido correctamente", nombreArchivo)
                                       new FacesMessage( " " + nombreArchivo + " agregado a la base")
                           );              
                         
                       }    
                    
                  }
                  
                 /* Connection conexion = ds.getConnection();                  
                  Statement s = conexion.createStatement();
                  s.execute("insert into lectura_originales(desc_ident) values('" + linea +"')");
                  s.close();
                  conexion.close();*/
                  
              }catch(Exception e){ e.printStackTrace(); }
                            
       }   
       
       public ArrayList<ModeloPrueba> getDatosDats(){
            
              ArrayList<ModeloPrueba> almp = new ArrayList<ModeloPrueba>();
              
              try{
                  
                  Connection conexion = ds.getConnection();                  
                  Statement s = conexion.createStatement();
                  ResultSet rs = s.executeQuery("select desc_ident from lectura_originales");
                  
                  while(rs.next()){                       
                        ModeloPrueba mp = new ModeloPrueba();
                        mp.setDesc_ident(rs.getString(1));
                        almp.add(mp);
                  }
                  
                  s.close();
                  conexion.close();
              }catch(Exception e){ e.printStackTrace(); } 
               
              return almp;
            
       }
       
       public void validarArchivo(FacesContext fc,UIComponent uic,Object o){
           
              System.out.println(o.getClass());
              /*if( !((File)o).getName().endsWith(".dat") ){
                  fc.addMessage("",new FacesMessage("Archivo invalido"));
              }*/
           
       }
   
       public UploadedFile getArchivo() { return archivo; }

       public void setArchivo(UploadedFile archivo) { this.archivo = archivo; }
       
}
