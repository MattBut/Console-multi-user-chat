package ServerCore;

import Common.Client;
import Common.Connector;
import Common.ServerConnectionException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

public class Server {
    private ArrayList<Client> clients=null;
    private ServerSocket serverSocket=null;
    private Socket socket=null;
    private Client client=null;
    private BufferedReader reader=null;
    private PrintWriter writer=null;
    private Connector connector=null;
    Server() throws IOException {
        clients=new ArrayList<>();
        connector=new Connector();
        serverSocket=new ServerSocket(Connector.PORT,Connector.NUMCON);
    }
    public void run(){
        System.out.println("Server started.");
        Thread connection=new Thread(new Connection());
        connection.start();
    }
    class Connection implements Runnable{
        public synchronized void runThread() throws IOException {
            while(true){
                socket=serverSocket.accept();
                client=new Client();
                client.setSocket(socket);
                System.out.println("Client:"+client.getSocket().getRemoteSocketAddress()
                +" was connected.");
                reader=new BufferedReader(new InputStreamReader(
                        client.getSocket().getInputStream()));
                writer=new PrintWriter(client.getSocket().getOutputStream(),true);
                client.setStreamReader(reader);
                client.setStreamwriter(writer);
                Thread rwthread=new Thread(new RWThread(client));
                client.setThread(rwthread);
                clients.add(client);
                client.getThread().start();
            }
        }
        @Override
        public void run() {
            try {
                runThread();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    class RWThread implements Runnable{
        private Client client=null;
        RWThread(Client client){
            this.client=client;
        }
        public synchronized void runThread() throws IOException {
            String[] msgs=new String[]{Connector.BEGINMSG,Connector.ENDMSG};
            StringBuilder msg=new StringBuilder();
            msg=connector.serverReceiver(msgs,client);
            String[] msgarray=msg.toString().split("\n");
            if (msg.toString().length()>0 && !msgarray[2].equals(Connector.CLOSEMSG)){
                System.out.println("Client:"+client.getSocket().getRemoteSocketAddress());
                Date date=new Date();
                System.out.print(String.format("%tr",date)+"\n");
                for(int i=1;i<msgarray.length-1;i++){
                    System.out.println(msgarray[i]);
                }
                System.out.print(" was received");
                for(int i=0;i<clients.size();i++){
                    connector.transmitter(clients.get(i),msg);
                }
                System.out.println(" and transmitted.");
            }else{
                System.out.println("Client:\n"+client.getSocket().getRemoteSocketAddress()+
                        "/user:"+msgarray[1]+" was disconnected.");
                client.getStreamreader().close();
                client.getStreamwriter().close();
                client.getSocket().close();
                client.getThread().interrupt();
                for(int i=0;i<clients.size();i++){
                    if (clients.get(i).equals(client)){
                        clients.remove(i);
                    }
                }
                StringBuilder str=new StringBuilder();
                for(int i=0;i<msgarray.length;i++){
                    if (msgarray[i].equals("*close*"))
                        msgarray[i]="I went away from chat.";
                    str.append(msgarray[i]).append("\n");
                }
                str.append(msgarray.length-1);
                for(int i=0;i<clients.size();i++){
                    connector.transmitter(clients.get(i),str);
                }
            }
        }
        public void rwThread() throws ServerConnectionException {
            while(true){
                try {
                    runThread();
                } catch (IOException e) {
                    throw new ServerConnectionException();
                }
            }
        }
        @Override
        public void run() {
            try {
                rwThread();
            } catch (ServerConnectionException e) {
                e.toString();
            }
        }
    }
    public static void main(String[] args){
        if (args.length>0){
            if (args[0]!=null)
                Connector.PORT=Integer.parseInt(args[0]);
        }
        if (args.length>1){
            if (args[1]!=null)
                Connector.NUMCON=Integer.parseInt(args[1]);
        }
        Server server= null;
        try {
            server = new Server();
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.run();
    }
}
