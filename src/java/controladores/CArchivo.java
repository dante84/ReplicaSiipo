
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
       private FacesContext context = FacesContext.getCurrentInstance();
               
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
                           
                           Properties ptec = new Properties();
                           Properties ppd  = new Properties();
                           
                           ptec.load( Thread.currentThread().getContextClassLoader().getResourceAsStream("tiposexamenclaves.properties") );
                           ppd.load(  Thread.currentThread().getContextClassLoader().getResourceAsStream("posicionesdats.properties")    );
                                                      
                           String claveExamen = linea.substring(0,3);                                                      
                           
                           if( ptec.containsKey(claveExamen) ){
                               
                               String ceo = ((String)ptec.get(claveExamen)).trim();
                               int posIniApli = Integer.valueOf((String)ppd.get(ceo));
                               
                               System.out.println( "Aplicacion " + linea.substring(posIniApli,(posIniApli + 10)) ); 
                               
                               char[] caracteresLinea = linea.toCharArray();                              
                               for( int k = posIniApli; k <= (caracteresLinea.length - 1); k++ ){
                                    if( caracteresLinea[k] == ' '){ continue; }
                                    else{ System.out.print(caracteresLinea[k] + " "); }
                               }                               
                               
                               System.out.println(); 
                               
                           }else{ 
                                 context.addMessage(null,new FacesMessage( "El archivo proporcionado no es valido. Verifica " )); 
                                 return;
                           }
                                                                                          
                       }    
                    
                  }
                  
                  context.addMessage( null, new FacesMessage( " " + nombreArchivo + " agregado a la base"));  
                  
                  /*
                     
                     Connection conexion = ds.getConnection();                  
                     Statement s = conexion.createStatement();
                     s.execute("insert into lectura_originales(desc_ident) values('" + linea +"')");
                     s.close();
                     conexion.close();
                      
                  */
                  
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
