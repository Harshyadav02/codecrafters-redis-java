import java.util.Arrays;
public class Echo {
    StringBuilder sb = new StringBuilder();

    public static void printMsg(String msg){

        String msgArray[] = msg.split("[^a-zA-Z]+");

        msgArray = Arrays.stream(msgArray).filter(word -> !word.isEmpty()).toArray(String[]::new);

        System.out.println(new String(Arrays.toString(Arrays.copyOfRange(msgArray,1, msgArray.length))));
    }
}
