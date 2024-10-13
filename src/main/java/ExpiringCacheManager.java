import java.util.Map;
public class ExpiringCacheManager{

    // method to get value based on key if key is not expired
    public  Object getValue(Map<String, Object[]> map, String key) {
        Object[] value = map.get(key);
        if(value.length == 1){

            return  value[0];   // return key which has no expiry
        }

        // check if key got expired
        else if (checkKeyExpiry((long)value[1], (long) value[2])) {

            return value[0]; // return value after checking expiry
        } else {
            map.remove(key); // remove key if got expired
            return null;
        }
    }
    
    // Method for inserting the key with expiry time
    public void setKeyWithExpiry(Map<String,Object[]> map, String key,String value, long expiryTime){

        if(expiryTime !=-1){
            long insertionTimeOfKey = System.currentTimeMillis();
            map.put(key,new Object[]{value,expiryTime,insertionTimeOfKey});
        }else{

            map.put(key,new Object[]{value});
        }
    }

    // method to check weather the key has expired or not
    private boolean checkKeyExpiry(long expiryTime , long insertionTimeOfKey){

        return System.currentTimeMillis() - insertionTimeOfKey < expiryTime;
    }
}
