package beans;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ConfigHandler {
    public Map<String, Object> getConfigs() {
        return configs;
    }


    private Map<String,Object> configs = new HashMap<>();
    public void loadConf(String confName){
        if(configs.isEmpty()){
            InputStream ymlStream = ConfigHandler.class.getClassLoader().getResourceAsStream(confName);
            Yaml yaml = new Yaml();
            this.configs = yaml.loadAs(ymlStream,Map.class);
        }
    }
}
