import java.util.HashMap;
import java.util.Map;
public class GetSet {


    public static Object getValue(Map<String,Object> map, String key){

            return map.get(key);
    }

    public static void setKey(Map<String,Object> map, String key,String value){

        map.put(key,value);
    }
}
