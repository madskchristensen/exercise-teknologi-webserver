import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

// https://stackoverflow.com/questions/12386509/java-how-to-handle-http-get-request-after-establishing-tcp-connection

public class WebServer {
    public static void main(String[] args) {
        try {
            boolean keepRunning = true;
            String httpVersion = "HTTP/1.1";

            ServerSocket serverSocket = new ServerSocket(1337);
            System.out.println("Server er klar");

            // dirty måde at loope da hver iteration vil lave et nyt 3-way handshake
            // apparently skal man lave en tråd til at håndtere en socket/klient? så en tråd tildelt til hver unik klient eller noget
            while(keepRunning) {
                Socket socket = serverSocket.accept(); // blokerer indtil der kommer en forbindelse. Koden stopper indtil der kommer en forbindelse.
                String request = acceptRequest(socket);
                matchRequest(httpVersion, socket, request);
            }

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static String acceptRequest(Socket socket) throws IOException {
        Scanner scanner = new Scanner(socket.getInputStream());

        String request = scanner.nextLine();

        System.out.println("from client: " + request);

        return request;
    }

    public static void matchRequest(String httpVersion, Socket socket, String request) throws IOException {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

        // Address matching. Returning HTTP responses depending on request.
        if(request.equals("GET / " + httpVersion)) {
            sendResponse200WithBody(dos, "<h1>Hej fra index</h1>");
        } else if(request.equals("GET /about " + httpVersion)) {
            sendResponse200WithBody(dos, "<h1>Hej fra about</h1>");
        } else if(request.equals("GET /htmlside " + httpVersion)) {
            sendResponseHtmlPage(dos, "htmlside");
        } else if(request.equals("GET /htmlside2 " + httpVersion)) {
            sendResponseHtmlPage(dos, "htmlside2");
        }
        dos.close();
    }

    public static void sendResponse200WithBody(DataOutputStream dos, String text) throws IOException {
        dos.writeBytes("HTTP/1.1 200 OK\n" +
                "\n" +
                text);
    }

    public static void sendResponseHtmlPage(DataOutputStream dos, String htmlPage) throws IOException {
        File file = new File(htmlPage + ".html");

        if(file.exists()) {
            FileInputStream fi = new FileInputStream(file);
            int length = (int) file.length();
            byte[] bytes = new byte[length];
            fi.read(bytes);

            dos.writeBytes("HTTP/1.1 200 OK\n" +
                    "Content-Length:" + length + "\n" +
                    "\n");
            dos.write(bytes);
            dos.writeBytes("\n");
        } else {
            dos.writeBytes("HTTP/1.1 404 Not Found");
            System.out.println("Fejl ved " + file);
        }
    }
}