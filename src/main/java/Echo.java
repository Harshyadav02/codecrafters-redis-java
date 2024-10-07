import java.util.Arrays;
import java.util.stream.Collectors;

public class Echo {


    public  static  void printMsg(String msg){

        String msgArray[] = msg.split("[^a-zA-Z]+");

        msg = Arrays.stream(msgArray).filter(word -> !word.isEmpty()).collect(Collectors.joining(" "));

        System.out.println(msg);
    }
}
