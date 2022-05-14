package bg.sofia.uni.fmi.mjt.client;

import bg.sofia.uni.fmi.mjt.exceptions.InvalidRequestException;
import bg.sofia.uni.fmi.mjt.exceptions.LogException;
import bg.sofia.uni.fmi.mjt.logger.Level;
import bg.sofia.uni.fmi.mjt.logger.Logger;
import bg.sofia.uni.fmi.mjt.result.ResultData;
import com.google.gson.Gson;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Scanner;

public class Client {
    private final Gson gson = new Gson();
    private static final String QUIT_MESSAGE = "quit";

    private static final String HOST = "localhost";
    private static final int PORT;
    private SocketChannel socketChannel;

    private BufferedReader br;
    private PrintWriter pw;

    private final Logger clientLogger;

    public Client(Logger logger) {
        checkNull(logger, "Logger");
        this.clientLogger = logger;
    }

    static {
        PORT = setPort();
    }

    private static int setPort(){
        try(InputStream in = new FileInputStream("./api.properties")) {
            Properties properties = new Properties();
            properties.load(in);
            return Integer.parseInt(properties.getProperty("server.port"));
        }catch (Exception ex) {
            throw new RuntimeException("Could not set server port.");
        }
    }

    public void connect() throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(HOST, PORT));
        br = new BufferedReader(Channels.newReader(socketChannel, StandardCharsets.UTF_8));
        pw = new PrintWriter(Channels.newWriter(socketChannel, StandardCharsets.UTF_8), true);
    }

    public ResultData send(String request) {
        checkNull(request, "Request");

        ClientRequest clientRequest;
        try {
            clientRequest = new ClientRequest(request);
        } catch (InvalidRequestException e) {
            clientLogger.log(Level.INFO, e);
            System.out.println(e.getMessage());
            return null;
        }

        pw.println(clientRequest.getRequest());
        ResultData resultData = null;
        try {
            resultData = gson.fromJson(br.readLine(), ResultData.class);
        } catch (IOException e) {
            clientLogger.log(Level.INFO, e);
            System.out.println("There was a problem reading result data, please try again.");
        }
        return resultData;
    }

    public void disconnect() {
        try {
            pw.close();
            br.close();
            socketChannel.close();
        } catch (IOException e) {
            System.out.println("An error occurred while trying to close the connection");
            clientLogger.log(Level.WARN, e);
        }
        try {
            clientLogger.close();
        } catch (LogException e) {
            System.out.println("An error occurred while closing program");
        }
    }

    private void checkNull(Object object, String name) {
        if (object == null) {
            throw new IllegalArgumentException(String.format("%s can't be null", name));
        }
    }

    public static void main(String[] args) {
        final String logDir = "./clientLogs";

        Client client;
        try {
            client = new Client(new Logger(Path.of(logDir)));
        } catch (LogException e) {
            System.out.println(e.getMessage());
            return;
        }

        try {
            client.connect();
        } catch (IOException e) {
            System.out.println("Unable to connect to server, please try again later");
            return;
        }

        System.out.println("Connected to the server");
        try (Scanner scanner = new Scanner(System.in)) {
            String request;
            while (true) {
                System.out.print("Enter request: ");
                request = scanner.nextLine();
                if (QUIT_MESSAGE.equals(request)) {
                    break;
                }
                if (request.isBlank()) {
                    continue;
                }
                ResultData resultData = client.send(request.trim());
                if (resultData != null) {
                    System.out.println("Sending request to server...");
                    ClientResponse clientResponse = new ClientResponse(resultData);
                    System.out.println(clientResponse.toHumanReadableString());
                }
            }
            client.disconnect();
        }
    }
}
