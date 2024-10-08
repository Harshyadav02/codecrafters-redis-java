import java.util.HashMap;
import java.util.Map;
public class GetSet {

    // method to get value based on key if key is not expired
    public static Object getValue(Map<String,Object[]> map, String key){

             Object[] value = map.get(key);

             // check if expiry time is given or not
             System.out.println(value[2]);
             if(value[2] !=null) {
                    // check if key got expired
                 if (checkKeyExpiry((Long) value[1], (Long)value[2])) {
                     //return value
                     return value[0];
                 }
                 else{
                     map.remove(key);
                     return null;
                 }
             }
             //return value
            return value[0];
    }
    // Method for inserting the key with expiry time
    public static void setKeyWithExpiry(Map<String,Object[]> map, String key,String value, Long expiryTime){
        long insertionTimeOfKey = System.currentTimeMillis();
        map.put(key,new Object[]{value,expiryTime,insertionTimeOfKey});
    }

    // method to check weather the key has expired or not
    private static boolean checkKeyExpiry(Long expiryTime , Long insertionTimeOfKey){

        return System.currentTimeMillis() - insertionTimeOfKey < expiryTime;
    }
}
