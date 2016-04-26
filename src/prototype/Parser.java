package prototype;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLStructure;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import prototype.object.ParameterMap;
import prototype.object.Turtle;
import prototype.settings.Configuration;

public class Parser {
  public List<Turtle> parse(String fileName ) throws Exception {
    String name = "replay";
    List<Turtle> turtles = new ArrayList();
    ParameterMap parameterMap = new ParameterMap();
    Configuration configuration = new Configuration();
    int mappingSize = parameterMap.GetMappingSize();
    
    try {
        MatFileReader mfr = new MatFileReader(fileName);
        
        MLStructure mlArrayRetrived = (MLStructure)mfr.getMLArray( name );
        MLDouble data = (MLDouble)mlArrayRetrived.getField("data");
        double[][] dataMatrix = data.getArray();

        if(dataMatrix.length > 0) {
          
          int rowOffest = 1;
          
          for(int turtleID = 0; turtleID < configuration.MaxTurtles; turtleID++) {
            double[][] turtleData = Arrays.copyOfRange(dataMatrix, (mappingSize * turtleID) + rowOffest, (mappingSize * (turtleID + 1)) + rowOffest);            
            Turtle turtle = new Turtle(turtleID, turtleData);
            turtles.add(turtle);
          }          
        }
                  
        return turtles;
    } catch (Exception ex) {
      Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
      throw ex;
    }
  }
}
