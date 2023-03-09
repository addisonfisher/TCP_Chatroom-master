import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;

    @Override
    public void run() {
        try {
            Socket client = new Socket("192.168.1.72", 9999);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inputHandler = new InputHandler();
            Thread t = new Thread(inputHandler);
            t.start();

            String inMessage;
            while ((inMessage = in.readLine()) != null) {
                System.out.println(inMessage);
            }
        } catch (IOException e) {
            Shutdown();
        }
    }

    public void Shutdown() {
        done = true;
        try {
            in.close();
            out.close();

            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
            // ignore
        }
    }

    class InputHandler implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String message = inReader.readLine();
                    if (message.equals("/quit")) {
                        inReader.close();
                        Shutdown();
                    } else if (message.equals("/help")) {
                        System.out.println("""
                                Commands:
                                /quit: quits the program
                                /user [Name] : renames the user
                                /pm [user] [message]: privately messages a specified user
                                @[user] [message]: highlights your message for a specific user
                                /help: shows this message""");
                    }else {
                        out.println(message);
                    }
                }

            } catch (IOException e) {
                Shutdown();
            }
        }

    }


    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}

