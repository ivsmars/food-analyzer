package bg.sofia.uni.fmi.mjt.server;

import bg.sofia.uni.fmi.mjt.cache.FoodCache;
import bg.sofia.uni.fmi.mjt.client.Client;
import bg.sofia.uni.fmi.mjt.json.Food;
import bg.sofia.uni.fmi.mjt.logger.Logger;
import bg.sofia.uni.fmi.mjt.result.ResultData;
import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ServerTest {
    private static Server server;
    private final Gson gson = new Gson();

    private static FoodCache cache;
    private static Logger logger;

    @BeforeAll
    static void setup() {
        cache = mock(FoodCache.class);
        logger = mock(Logger.class);
        server = new Server(cache, logger);
        final Thread thread = new Thread(server);
        thread.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    static void tearDown() {
        server.shutdown();
    }

    Client startClient() throws IOException {
        final Client client = new Client(logger);
        client.connect();
        return client;
    }


    @Test
    public void testResultSingleRequest() throws IOException {
        Client client = startClient();
        String json = "{\"fdcId\":2095236,\"dataType\":\"Branded\",\"description\":\"CHEDDAR CHEESE\",\"foodNutrients\":[{\"number\":\"203\",\"name\":\"Protein\",\"amount\":25.0,\"unitName\":\"G\"},{\"number\":\"269\",\"name\":\"Sugars, total including NLEA\",\"amount\":0.0,\"unitName\":\"G\"}],\"gtinUpc\":\"828653282457\",\"ingredients\":\"MILK, CHEESE CULTURES, SALT, ENZYMES AND ANNATTO COLOR IF NECESSARY.\",\"query\":\"cheddar cheese\"}\n";
        Food food = gson.fromJson(json, Food.class);
        when(cache.getByFdcId(anyInt())).thenReturn(new ResultData(ResultData.STATUS_OK, food));


        ResultData expected = new ResultData(ResultData.STATUS_OK, food);
        ResultData actual = client.send("get-food-report 166");
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(gson.toJson(expected.getFood(), Food.class), gson.toJson(actual.getFood(), Food.class));
        client.disconnect();
    }

    @Test
    public void testResultMultipleRequestsSingleClient() throws IOException {
        Client client = startClient();
        String json = "{\"fdcId\":2095236,\"dataType\":\"Branded\",\"description\":\"CHEDDAR CHEESE\",\"foodNutrients\":[{\"number\":\"203\",\"name\":\"Protein\",\"amount\":25.0,\"unitName\":\"G\"},{\"number\":\"269\",\"name\":\"Sugars, total including NLEA\",\"amount\":0.0,\"unitName\":\"G\"}],\"gtinUpc\":\"828653282457\",\"ingredients\":\"MILK, CHEESE CULTURES, SALT, ENZYMES AND ANNATTO COLOR IF NECESSARY.\",\"query\":\"cheddar cheese\"}\n";
        Food food = gson.fromJson(json, Food.class);
        ResultData expected = new ResultData("OK", food);
        when(cache.getByFdcId(anyInt())).thenReturn(new ResultData(ResultData.STATUS_OK, food));

        for (int i = 0; i < 10; ++i) {
            ResultData actual = client.send("get-food-report 166");
            assertEquals(expected.getStatus(), actual.getStatus());
            assertEquals(gson.toJson(expected.getFood(), Food.class), gson.toJson(actual.getFood(), Food.class));
        }
        client.disconnect();
    }

    @Test
    public void testMultipleClients() throws InterruptedException, ExecutionException {
        List<Callable<ResultData>> tasks = new ArrayList<>();
        final int tasksNum = 1000;
        final ExecutorService executorService = Executors.newFixedThreadPool(9);

        String json = "{\"fdcId\":2095236,\"dataType\":\"Branded\",\"description\":\"CHEDDAR CHEESE\",\"foodNutrients\":[{\"number\":\"203\",\"name\":\"Protein\",\"amount\":25.0,\"unitName\":\"G\"},{\"number\":\"269\",\"name\":\"Sugars, total including NLEA\",\"amount\":0.0,\"unitName\":\"G\"}],\"gtinUpc\":\"828653282457\",\"ingredients\":\"MILK, CHEESE CULTURES, SALT, ENZYMES AND ANNATTO COLOR IF NECESSARY.\",\"query\":\"cheddar cheese\"}\n";
        Food food = gson.fromJson(json, Food.class);
        final String foodJson = gson.toJson(food, Food.class);


        ResultData expected = new ResultData("OK", food);
        when(cache.getByFdcId(anyInt())).thenReturn(new ResultData("OK", food));

        for (int i = 0; i < tasksNum; i++) {
            tasks.add(() -> {
                Client client = startClient();
                ResultData resultData = client.send("get-food-report 124123");
                client.disconnect();
                return resultData;
            });
        }
        final List<Future<ResultData>> futures = executorService.invokeAll(tasks, 10, TimeUnit.SECONDS);
        assertFalse(futures.isEmpty());
        assertEquals(tasksNum, futures.size());
        for (var future : futures) {
            assertEquals(future.get().getStatus(), expected.getStatus());
            assertEquals(gson.toJson(future.get().getFood()), foodJson);
        }
    }

    @Test
    public void testClientInvalidRequest() throws IOException {
        Client client = startClient();
        assertNull(client.send("invalid request"));
    }

}
