package ClientCore;

import Common.Client;
import Common.ClientConnectionException;
import Common.Connector;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

class Terminal{
    public static Socket socket=null;
    private Thread readthread=null;
    private BufferedReader reader=null;
    private PrintWriter writer=null;
    private BufferedReader consolreader=null;
    private Timer audittime=null;
    private ConnectAudit conaudit=null;
    private Client client=null;
    public static boolean status=false;
    public static Terminal consol=null;
    public  static Thread maint=null;
    private String[] msgs=null;
    private Connector connector=null;
    Terminal(){
        client=new Client();
        connector=new Connector();
        audittime=new Timer();
        conaudit=new ConnectAudit();
        audittime.schedule(conaudit,0,1000);
    }
    class ConnectAudit extends TimerTask{
        public synchronized void runThread() throws ClientConnectionException {
            try {
                socket=new Socket(Connector.ADDRESS,Connector.PORT);
                status=true;
                conaudit.cancel();
                audittime.purge();
            } catch (IOException e) {
                status=false;
                throw new ClientConnectionException();
            }
        }
        @Override
        public void run() {
            try {
                runThread();
            } catch (ClientConnectionException e) {
                e.toString();
            }
        }
    }
    public synchronized void  run() throws IOException {
        client.setSocket(socket);
        System.out.println("Client:"+client.getSocket().getLocalAddress()+
                " was connected.");
        readthread=new Thread(new ReadThread());
        readthread.setName("ReadThread");
        client.setThread(readthread);
        reader=new BufferedReader(new InputStreamReader(
                client.getSocket().getInputStream()));
        writer=new PrintWriter(client.getSocket().getOutputStream(),true);
        consolreader=new BufferedReader(new InputStreamReader(System.in));
        client.setConsolReader(consolreader);
        client.setStreamReader(reader);
        client.setStreamwriter(writer);
        client.getThread().start();
        iamOnLine();
        msgs=new String[]{Connector.BEGINMSG,Connector.NAME,Connector.ENDMSG,
                Connector.ENDCON,Connector.CLOSEPROG};
        while(true){
            connector.transmitter(client,connector.readFromConsol(msgs,client));
        }
    }
    public void iamOnLine(){
        StringBuilder msg=new StringBuilder();
        msg.append(Connector.BEGINMSG).append("\n");
        msg.append(Connector.NAME).append("\n");
        msg.append("I am online.").append("\n");
        msg.append("**********").append("\n");
        msg.append(Connector.ENDMSG);
        connector.transmitter(client,msg);
    }
    class ReadThread implements Runnable {
        public synchronized void runThread() throws IOException, InterruptedException {
            StringBuilder msg = connector.clientReceiver(client, Connector.ENDMSG);
            String[] msgarray = msg.toString().split("\n");
            Date date = new Date();
            String time = String.format("%tr", date);
            if (msg.toString().length() > 0 && !msgarray[2].equals(Connector.CLOSEMSG)) {
                System.out.println(time + ":" + msgarray[1] + " sad:");
                for (int i = 2; i < msgarray.length - 1; i++) {
                    System.out.println(msgarray[i]);
                }
            } else {
                stopRun();
            }
        }

        public synchronized void stopRun() throws InterruptedException, IOException {
            status = false;
            audittime = new Timer();
            conaudit = new ConnectAudit();
            audittime.schedule(conaudit, 0, 1000);
            while (true) {
                Thread.sleep(100);
                if (status) {
                    status = false;
                    client.setSocket(socket);
                    System.out.println("Client:" + client.getSocket().getLocalAddress() +
                            " was connected");
                    reader = new BufferedReader(new InputStreamReader(
                            client.getSocket().getInputStream()));
                    writer = new PrintWriter(client.getSocket().getOutputStream(),true);
                    client.setStreamReader(reader);
                    client.setStreamwriter(writer);
                    iamOnLine();
                    break;
                }
            }
        }

        public void readThread() throws ClientConnectionException {
            try {
                runThread();
            } catch (IOException | InterruptedException e) {
                throw new ClientConnectionException();
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    readThread();
                } catch (ClientConnectionException e) {
                    e.toString();
                }
            }
        }
    }
    static class mainRun implements Runnable{
        public synchronized void runProc() throws InterruptedException, IOException {
            consol=new Terminal();
            while(true){
                Thread.sleep(100);
                if (status){
                    status=false;
                    consol.run();
                    break;
                }
            }
        }
        @Override
        public void run() {
            try {
                runProc();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args){
        if (args.length>0){
            if (args[0]!=null){
                Connector.ADDRESS=args[0];
            }
        }
        if (args.length>1){
            if (args[1]!=null){
                Connector.PORT=Integer.parseInt(args[1]);
            }
        }
        if (args.length>2){
            if (args[2]!=null){
                Connector.NAME=args[2];
            }
        }
        maint=new Thread(new mainRun());
        maint.setName("ClientThread");
        maint.start();
    }
}
