import java.util.HashMap;
import java.util.Map;
public class GetSet {


    public static Object getValue(Map<String,Object[]> map, String key){


             Object[] value = map.get(key);
             if(value[2] !=null) {

                 if (checkKeyExpiry((Long) value[1], (Long)value[2])) {

                     return map.get(key);
                 }else{
                     map.remove(key);
                     return null;
                 }
             }
            return map.get(key);
    }

    public static void setKeyWithExpiry(Map<String,Object[]> map, String key,String value, Long expiryTime){
        long insertionTimeOfKey = System.currentTimeMillis();
        map.put(key,new Object[]{value,expiryTime,insertionTimeOfKey});
    }


    private static boolean checkKeyExpiry(Long expiryTime , Long insertionTimeOfKey){

        return System.currentTimeMillis() - insertionTimeOfKey < expiryTime;
    }
}
