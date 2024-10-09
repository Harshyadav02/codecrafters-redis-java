import java.util.Map;
public class GetSet {

    // method to get value based on key if key is not expired
    public static Object getValue(Map<String, Object[]> map, String key) {
        Object[] value = map.get(key);

        if(value.length ==1){
            return  value[0];
        }

        // check if key got expired
        else if (checkKeyExpiry((long)value[1], (long) value[2])) {
            //return value
            return value[0];
        } else {
            map.remove(key);
            return null;
        }
    }
    // Method for inserting the key with expiry time
    public static void setKeyWithExpiry(Map<String,Object[]> map, String key,String value, long expiryTime){

        if(expiryTime !=-1){
            long insertionTimeOfKey = System.currentTimeMillis();
            map.put(key,new Object[]{value,expiryTime,insertionTimeOfKey});
        }else{
//            System.out.println("setting up no expiry time ");
            map.put(key,new Object[]{value});
        }
    }

    // method to check weather the key has expired or not
    private static boolean checkKeyExpiry(long expiryTime , long insertionTimeOfKey){

        return System.currentTimeMillis() - insertionTimeOfKey < expiryTime;
    }
}
