package Common;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private Thread thread=null;
    private Socket socket=null;
    private BufferedReader consolreader=null;
    private BufferedReader streamreader=null;
    private PrintWriter streamwriter=null;
    public Client(){
    }
    public void setThread(Thread thread){
        this.thread=thread;
    }
    public void setSocket(Socket socket){
        this.socket=socket;
    }
    public void setConsolReader(BufferedReader consolReader){
        this.consolreader=consolReader;
    }
    public void setStreamReader(BufferedReader streamreader){
        this.streamreader=streamreader;
    }
    public void setStreamwriter(PrintWriter streamwriter){
        this.streamwriter=streamwriter;
    }
    public Thread getThread(){
        return  this.thread;
    }
    public  Socket getSocket(){
        return this.socket;
    }
    public  BufferedReader getConsolreader(){
        return  this.consolreader;
    }
    public BufferedReader getStreamreader(){
        return this.streamreader;
    }
    public PrintWriter getStreamwriter(){
        return this.streamwriter;
    }
}
