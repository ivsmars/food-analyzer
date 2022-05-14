package bg.sofia.uni.fmi.mjt.cache;

import bg.sofia.uni.fmi.mjt.exceptions.CacheException;
import bg.sofia.uni.fmi.mjt.exceptions.ServerException;
import bg.sofia.uni.fmi.mjt.json.Food;
import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class FoodCacheWriter extends Thread {
    private final FoodCache cache;
    private final BufferedWriter writer;
    private final Gson gson;

    public FoodCacheWriter(FoodCache cache) throws IOException {
        this.cache = cache;
        writer = Files.newBufferedWriter(cache.getCacheFilePath(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        gson = new Gson();
    }

    @Override
    public void run() {
        Food food;
        try {
            while ((food = cache.getNextFood()) != null) {
                cache.storeInCache(food);
                synchronized (cache) {
                    writer.write(gson.toJson(food, Food.class));
                    writer.write(System.lineSeparator());
                    writer.flush();
                }

            }
            close();
        } catch (IOException | CacheException e) {
            throw new ServerException("Cache writer error", e);
        }
    }

    private void close() throws IOException {
        writer.close();
    }
}
