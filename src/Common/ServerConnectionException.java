package Common;

public class ServerConnectionException extends Exception{
    public ServerConnectionException(){
    }
    @Override
    public String toString() {
        System.out.println("Connection was closed.");
        return super.toString();
    }
}
