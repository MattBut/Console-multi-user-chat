package Common;

import java.io.IOException;

public class Connector {
    public static String ADDRESS="localhost";
    public static Integer PORT=3490;
    public static Integer NUMCON=10;
    public static String NAME="user";
    public static final String BEGINMSG="*begin*";
    public static final String ENDMSG="*end*";
    public static final String CLOSEMSG="*close*";
    public static final String ENDCON="ok";
    public static final String CLOSEPROG="close";
    public  Connector(){
    }
    public void transmitter(Client client,StringBuilder msg){
        if (msg.length()>0)
            client.getStreamwriter().println(msg);
    }
    public void specialSend(Client client){
        StringBuilder sb=new StringBuilder();
        sb.append(BEGINMSG).append("\n");
        sb.append(NAME).append("\n");
        sb.append(CLOSEMSG).append("\n");
        sb.append("**********").append("\n");
        sb.append(ENDMSG);
        transmitter(client,sb);
    }
    public void closeClient(Client client){
        System.out.println("Close connection...");
        specialSend(client);
        System.exit(0);
    }
    public StringBuilder readFromConsol(String[] msgs,Client client) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append(msgs[0]).append("\n");
        sb.append(msgs[1]).append("\n");
        String line=null;
        while(!(line=client.getConsolreader().readLine()).equals(msgs[3])){
            if (line.equals(msgs[4]))closeClient(client);
            sb.append(line).append("\n");
        }
        sb.append("**********").append("\n");
        sb.append(msgs[2]);
        return sb;
    }
    public  StringBuilder clientReceiver(Client client,String endmsg) throws IOException {
        String msg=null;
        StringBuilder sb=new StringBuilder();
        if ((msg=client.getStreamreader().readLine())!=null){
            sb.append(msg).append("\n");
            while(!(msg=client.getStreamreader().readLine()).equals(endmsg)){
                if (msg.length()>0)sb.append(msg).append("\n");
            }
            sb.append(msg);
        }
        return sb;
    }
    public StringBuilder serverReceiver(String[] msgs,Client client) throws IOException {
        String msg=null;
        StringBuilder sb=new StringBuilder();
        if (client.getStreamreader().readLine()!=null){
            sb.append(msgs[0]).append("\n");
            while(!(msg=client.getStreamreader().readLine()).equals(msgs[1])){
                sb.append(msg).append("\n");
            }
            sb.append(msgs[1]);
        }
        return sb;
    }
}
