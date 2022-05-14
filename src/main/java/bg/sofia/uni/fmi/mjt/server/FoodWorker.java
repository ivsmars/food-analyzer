package bg.sofia.uni.fmi.mjt.server;

import bg.sofia.uni.fmi.mjt.cache.FoodCache;
import bg.sofia.uni.fmi.mjt.exceptions.HttpRequestException;
import bg.sofia.uni.fmi.mjt.exceptions.InvalidRequestException;
import bg.sofia.uni.fmi.mjt.logger.Level;
import bg.sofia.uni.fmi.mjt.logger.Logger;
import bg.sofia.uni.fmi.mjt.requests.RequestHandler;
import bg.sofia.uni.fmi.mjt.result.ResultData;
import com.google.gson.Gson;

import java.net.http.HttpClient;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class FoodWorker implements Runnable {

    private final SelectionKey key;
    private final String request;
    private final Gson gson = new Gson();

    private final FoodCache cache;
    private final Logger logger;


    private static final ResultData SERVER_ERROR_RESULT_DATA = new ResultData(ResultData.STATUS_ERROR,
            "An error occurred while server was processing request");

    private static final ResultData FOOD_NOT_FOUND_RESULT_DATA = new ResultData(ResultData.STATUS_ERROR,
            "No results were found matching request");

    private static final ResultData RESULT_DATA_TOO_BIG = new ResultData(ResultData.STATUS_ERROR,
            "Result data is too big");

    public FoodWorker(SelectionKey selectionKey, byte[] input, FoodCache cache, Logger logger) {
        this.key = selectionKey;
        this.request = new String(input, StandardCharsets.UTF_8).replace("\r", "").replace("\n", "");
        this.cache = cache;
        this.logger = logger;
    }

    private void putInBuffer(ResultData resultData) {
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        buffer.clear();
        byte[] data = (gson.toJson(resultData) + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
        buffer.put(data);
        buffer.flip();
    }

    @Override
    public void run() {
        try {
            RequestHandler requestHandler = new RequestHandler(HttpClient.newHttpClient(), cache);
            ResultData res = requestHandler.fetchFood(request);
            putInBuffer(Objects.requireNonNullElse(res, FOOD_NOT_FOUND_RESULT_DATA));

        } catch (HttpRequestException | InvalidRequestException e) {
            logger.log(Level.WARN, e);
            putInBuffer(SERVER_ERROR_RESULT_DATA);
            System.err.println("An error occurred while trying to get results from API");

        } catch (BufferOverflowException e) {
            putInBuffer(RESULT_DATA_TOO_BIG);
            logger.log(Level.WARN, e);
            System.err.println("Buffer overflow");
        } finally {
            if (key.isValid()) {
                key.interestOps(SelectionKey.OP_WRITE);
                key.selector().wakeup();
            }
        }

    }
}
