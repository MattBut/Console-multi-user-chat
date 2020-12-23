package Common;

public class ClientConnectionException extends Exception {
    public ClientConnectionException(){
    }

    @Override
    public String toString() {
        System.out.println("Server unreachable...");
        return super.toString();
    }
}
