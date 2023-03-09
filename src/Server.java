import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public Server(){
        connections = new ArrayList<>();
        done = false;

    }

    @Override
    public void run(){
        try{
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            Shutdown();
        }


    }

    public void Broadcast (String message){
        for (ConnectionHandler ch : connections){
            if(ch != null){
                ch.SendMessage(message);
            }
        }

    }

    public void Shutdown(){
        try {
            done = true;
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler ch : connections){
                ch.Shutdown();
            }
        } catch (IOException e){
            // ignore
        }

    }

    class ConnectionHandler implements Runnable{

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String username;

        public ConnectionHandler(Socket clientP){
            this.client = clientP;

        }

        @Override
        public void run(){
            try{
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Please enter a username: ");
                username = in.readLine();
                //add catching statements in case an invalid username is provided
                System.out.println(username + " connected!");
                Broadcast(username + " joined the chat!");
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/user ")) {
                        String[] messageSplit = message.split(" ", 2);
                        if(messageSplit.length == 2){
                            Broadcast(username + " renamed themselves to " + messageSplit[1]);
                            System.out.println(username + " renamed themselves to " + messageSplit[1]);
                            username = messageSplit[1];
                            out.println("Successfully changed username to " + username);
                        } else {
                            out.println("No username provided!");
                        }
                    } else if (message.startsWith("/quit")) {
                        Broadcast(username + " left the chat!");
                        Shutdown();

                    } else if (message.startsWith("/pm")) {
                        String[] messageSplit = message.split(" ", 3);
                        if(messageSplit.length == 3){
                            for (ConnectionHandler ch : connections){
                                if(ch.username.equals(messageSplit[1])){
                                    ch.SendMessage("PM from " + username + ": " + messageSplit[2]);
                                    out.println("PM sent to " + messageSplit[1]);
                                }
                            }

                        } else {
                            out.println("Invalid PM command!");
                        }
                    } else if (message.startsWith("@")){
                        String[] messageSplit = message.split(" ", 2);
                        if(messageSplit.length == 2){
                            for (ConnectionHandler ch : connections){
                                if(messageSplit[0].substring(1).equals("everyone")){
                                    Broadcast(ANSI_YELLOW + username + ": " + message + ANSI_RESET);
                                }
                                else if(ch.username.equals(messageSplit[0].substring(1))){
                                    ch.SendMessage(ANSI_YELLOW + username + ": " + message + ANSI_RESET);
                                    out.println("Ping sent to " + messageSplit[0].substring(1));
                                    Broadcast(username + ": " + message);
                                }
                            }
                        }
                    } else {
                        Broadcast(username + ": " + message);
                    }
                }
            } catch (IOException e) {
                Shutdown();
            }

        }

        public void SendMessage (String message){
            out.println(message);
        }

        public void Shutdown(){
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e){
                // ignore
            }
        }



    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }


}
