package bg.sofia.uni.fmi.mjt.cache;

import bg.sofia.uni.fmi.mjt.exceptions.CacheException;
import bg.sofia.uni.fmi.mjt.json.Food;
import bg.sofia.uni.fmi.mjt.logger.Level;
import bg.sofia.uni.fmi.mjt.logger.Logger;
import bg.sofia.uni.fmi.mjt.result.ResultData;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FoodCache {
    private final Logger logger;

    private final Path cacheFile;

    private FoodCacheWriter[] foodCacheWriters;
    private final static int MAX_CACHE_THREADS = 4;

    private final ConcurrentHashMap<String, Integer> gtinUpcToFdcId;
    private final ConcurrentHashMap<String, List<Integer>> keyWordsToFdcId;
    private final ConcurrentHashMap<Integer, Food> fdcIdToFood;

    private final Gson gson = new Gson();
    private final Queue<Food> queue = new ArrayDeque<>();

    private boolean isClosed = false;

    public FoodCache(Path cacheFile, Logger logger) throws CacheException {
        checkNull(cacheFile, "Cache path");
        checkNull(logger, "Logger");

        this.logger = logger;
        this.cacheFile = cacheFile;

        gtinUpcToFdcId = new ConcurrentHashMap<>();
        keyWordsToFdcId = new ConcurrentHashMap<>();
        fdcIdToFood = new ConcurrentHashMap<>();

        if (Files.exists(cacheFile)) {
            initCache();
        } else {
            try {
                Files.createDirectories(cacheFile.getParent());
                Files.createFile(cacheFile);
            } catch (IOException e) {
                throw new CacheException("Error while creating cache file");
            }
        }
        initWriters();
    }

    public synchronized void submit(Food food) {
        checkNull(food, "food");

        if (!isClosed) {
            queue.add(food);
        }
        this.notifyAll();
    }


    public synchronized void submitAll(Collection<Food> foods) {
        checkNull(foods, "Food list");

        if (!isClosed) {
            queue.addAll(foods);
        }
        this.notifyAll();
    }

    public synchronized Food getNextFood() throws CacheException {
        while (queue.isEmpty() && !isClosed) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                throw new CacheException("An error occurred while trying to poll next food item", e);
            }
        }
        return queue.poll();
    }


    private void initCache() throws CacheException {
        String line;
        Food currFood;
        try (BufferedReader br = Files.newBufferedReader(cacheFile)) {
            while ((line = br.readLine()) != null) {
                currFood = gson.fromJson(line, Food.class);
                int fdcId = currFood.getFdcId();

                if (currFood.getGtinUpc() != null) {
                    gtinUpcToFdcId.putIfAbsent(currFood.getGtinUpc(), fdcId);
                }

                if (currFood.getQuery() != null) {
                    if (keyWordsToFdcId.get(currFood.getQuery()) != null) {
                        List<Integer> ids = keyWordsToFdcId.get(currFood.getQuery());
                        ids.add(fdcId);
                        keyWordsToFdcId.putIfAbsent(currFood.getQuery(), Collections.synchronizedList(ids));
                    } else {
                        List<Integer> newId = new ArrayList<>();
                        newId.add(fdcId);
                        keyWordsToFdcId.putIfAbsent(currFood.getQuery(), Collections.synchronizedList(newId));
                    }
                }
                fdcIdToFood.putIfAbsent(fdcId, currFood);
            }
        } catch (IOException e) {
            throw new CacheException("An error occurred while initializing cache", e);
        }
    }

    private void initWriters() throws CacheException {
        foodCacheWriters = new FoodCacheWriter[MAX_CACHE_THREADS];
        Thread.UncaughtExceptionHandler handler = (t, e) -> logger.log(Level.WARN, e);
        try {
            for (int i = 0; i < MAX_CACHE_THREADS; i++) {
                foodCacheWriters[i] = new FoodCacheWriter(this);
                foodCacheWriters[i].setUncaughtExceptionHandler(handler);
                foodCacheWriters[i].start();
            }

        } catch (IOException e) {
            throw new CacheException("Failed to open cache file for writing", e);
        }
    }


    public ResultData getByFdcId(int fdcId) {
        if (fdcIdToFood.containsKey(fdcId)) {
            return new ResultData(ResultData.STATUS_OK, fdcIdToFood.get(fdcId));
        } else {
            return null;
        }
    }

    public ResultData getByGtinUpc(String gtinUpc) {
        checkNull(gtinUpc, "Gtin/UPC");
        checkEmpty(gtinUpc, "Gtin/Upc");

        if (gtinUpcToFdcId.containsKey(gtinUpc)) {
            return new ResultData(ResultData.STATUS_OK, fdcIdToFood.get(gtinUpcToFdcId.get(gtinUpc)));
        } else {
            return null;
        }
    }

    public ResultData getByKeywords(String keywords) {
        checkNull(keywords, "keywords");
        checkEmpty(keywords, "keywords");

        List<Food> result = new ArrayList<>();
        if (keyWordsToFdcId.containsKey(keywords)) {
            List<Integer> ids;
            synchronized (keyWordsToFdcId) {
                ids = keyWordsToFdcId.get(keywords);
            }
            for (Integer id : ids) {
                result.add(fdcIdToFood.get(id));
            }
        } else {
            return null;
        }
        return new ResultData(ResultData.STATUS_OK, result);
    }


    void storeInCache(Food food) throws CacheException {
        if (food == null) {
            throw new CacheException("Attempted to store null value in cache");
        }

        fdcIdToFood.putIfAbsent(food.getFdcId(), food);
        if (food.getQuery() != null) {

            //the keywords map is thread safe, but the value for a query is a list
            //which is not thread safe, so we must synchronize the threads that
            //manipulate the lists
            synchronized (keyWordsToFdcId) {
                if (keyWordsToFdcId.containsKey(food.getQuery())) {
                    List<Integer> ids = keyWordsToFdcId.get(food.getQuery());
                    ids.add(food.getFdcId());
                    keyWordsToFdcId.putIfAbsent(food.getQuery(), ids);
                } else {
                    List<Integer> newIds = new ArrayList<>();
                    newIds.add(food.getFdcId());
                    keyWordsToFdcId.putIfAbsent(food.getQuery(), newIds);
                }
            }

        }
        if (food.getGtinUpc() != null) {
            gtinUpcToFdcId.putIfAbsent(food.getGtinUpc(), food.getFdcId());
        }
    }

    public synchronized void close() {
        isClosed = true;
        this.notifyAll();
    }


    public Path getCacheFilePath() {
        return cacheFile;
    }

    public FoodCacheWriter[] getWriterThreads() {
        return foodCacheWriters;
    }

    private void checkNull(Object object, String name) {
        if (object == null) {
            throw new IllegalArgumentException(String.format("%s can't be null", name));
        }
    }

    private void checkEmpty(String instance, String name) {
        if (instance.isBlank()) {
            throw new IllegalArgumentException(String.format("%s can't be blank", name));
        }
    }
}
