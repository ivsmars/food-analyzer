package bg.sofia.uni.fmi.mjt.server;
import bg.sofia.uni.fmi.mjt.cache.FoodCache;
import bg.sofia.uni.fmi.mjt.exceptions.CacheException;
import bg.sofia.uni.fmi.mjt.exceptions.LogException;
import bg.sofia.uni.fmi.mjt.exceptions.ServerException;
import bg.sofia.uni.fmi.mjt.logger.Level;
import bg.sofia.uni.fmi.mjt.logger.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    public static final int SERVER_PORT;
    private static final String SERVER_HOST = "localhost";

    private static final int READ_BUFFER_SIZE = 512;
    private static final int WRITE_BUFFER_SIZE = 1024 * 256;

    private static final int MAX_EXECUTOR_THREADS = 9;
    private final ByteBuffer buffer = ByteBuffer.allocate(READ_BUFFER_SIZE);

    private final FoodCache cache;
    private final Logger logger;

    private ExecutorService executorService;
    private Selector selector;

    public Server(FoodCache cache, Logger logger) {
        this.cache = cache;
        this.logger = logger;
    }

    static {
        SERVER_PORT = setServerPort();
    }

    private static int setServerPort(){
        try(InputStream in = new FileInputStream("./api.properties")) {
            Properties properties = new Properties();
            properties.load(in);
            return Integer.parseInt(properties.getProperty("server.port"));
        }catch (Exception ex) {
            throw new RuntimeException("Could not set server port.");
        }
    }

    @Override
    public void run() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            executorService = Executors.newFixedThreadPool(MAX_EXECUTOR_THREADS);

            serverSocketChannel.bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            serverSocketChannel.configureBlocking(false);

            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                int readyChannels = selector.select();

                if (!selector.isOpen()) {
                    break;
                }

                if (readyChannels == 0) {
                    // select() is blocking but may still return with 0, check javadoc
                    continue;
                }
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isReadable()) {
                        read(key);
                    } else if (key.isWritable()) {
                        write(key);
                    } else if (key.isAcceptable()) {
                        accept(key);
                    }
                }

            }
            executorService.shutdown();

        } catch (IOException e) {
            logger.log(Level.FATAL, e);
            shutdown();
            throw new ServerException("Fatal server error occurred", e);
        }
        System.out.println("Exiting server...");
    }

    public void read(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        buffer.clear();
        int r = sc.read(buffer);
        if (r < 0) {
            sc.close();
            return;
        }
        buffer.flip();
        byte[] arr = new byte[buffer.remaining()];
        buffer.get(arr);
        FoodWorker foodClientHandler = new FoodWorker(key, arr, cache, logger);
        executorService.execute(foodClientHandler);
    }

    public void write(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        ByteBuffer bb = (ByteBuffer) key.attachment();
        sc.write(bb);
        if (!bb.hasRemaining()) {
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    public void accept(SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();
        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(WRITE_BUFFER_SIZE));
    }

    public void shutdown() {
        cache.close();
        try {
            logger.close();
            selector.close();
        } catch (LogException | IOException e) {
            throw new ServerException("Failed to shut server down", e);
        }
    }

    public static void main(String[] args) {
        final String logDir = "./serverLogs";
        final String cachePath = "./src/main/resources/cache.txt";
        Logger logger;
        FoodCache cache;

        try {
            logger = new Logger(Path.of(logDir));
            cache = new FoodCache(Path.of(cachePath), logger);
        } catch (LogException | CacheException e) {
            throw new ServerException(e);
        }

        Server server = new Server(cache, logger);
        Thread t = new Thread(server);
        t.start();

    }
}
