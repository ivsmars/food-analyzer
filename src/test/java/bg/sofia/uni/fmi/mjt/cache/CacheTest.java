package bg.sofia.uni.fmi.mjt.cache;

import bg.sofia.uni.fmi.mjt.exceptions.CacheException;
import bg.sofia.uni.fmi.mjt.json.Food;
import bg.sofia.uni.fmi.mjt.logger.Logger;
import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class CacheTest {
    private final Gson gson = new Gson();

    private static Logger logger;
    private static FoodCache foodCache;
    private static final String CACHE_TEST_FILE = "./src/test/resources/cacheTest.txt";

    @BeforeAll
    static void init() {
        logger = mock(Logger.class);
        assertDoesNotThrow(() -> foodCache = new FoodCache(Path.of(CACHE_TEST_FILE), logger));
    }

    @AfterAll
    static void clear() {
        foodCache.close();
    }

    @Test
    public void testIllegalArguments() {
        assertThrows(IllegalArgumentException.class, () -> new FoodCache(null,logger));
        assertThrows(IllegalArgumentException.class, () -> new FoodCache(Path.of("test"),null));
    }

    @Test
    public void testSubmitNullFoodThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> foodCache.submit(null));
    }

    @Test
    public void testGetFromCacheIllegalArguments() {
        assertThrows(IllegalArgumentException.class, () -> foodCache.getByKeywords(null));
        assertThrows(IllegalArgumentException.class, () -> foodCache.getByKeywords(" "));
        assertThrows(IllegalArgumentException.class, () -> foodCache.getByGtinUpc(null));
        assertThrows(IllegalArgumentException.class, () -> foodCache.getByGtinUpc(" "));
    }


    @Test
    public void testCacheGet() {

        List<Food> contentsInCache = new ArrayList<>();
        contentsInCache.add(gson.fromJson("{\"fdcId\":2015943,\"dataType\":\"Branded\",\"description\":\"CHEDDAR CHEESE\",\"foodNutrients\":[{\"number\":\"203\",\"name\":\"Protein\",\"amount\":25.0,\"unitName\":\"G\"},{\"number\":\"204\",\"name\":\"Total lipid (fat)\",\"amount\":32.1,\"unitName\":\"G\"},{\"number\":\"205\",\"name\":\"Carbohydrate, by difference\",\"amount\":3.57,\"unitName\":\"G\"},{\"number\":\"208\",\"name\":\"Energy\",\"amount\":393.0,\"unitName\":\"KCAL\"},{\"number\":\"269\",\"name\":\"Sugars, total including NLEA\",\"amount\":0.0,\"unitName\":\"G\"},{\"number\":\"291\",\"name\":\"Fiber, total dietary\",\"amount\":0.0,\"unitName\":\"G\"},{\"number\":\"301\",\"name\":\"Calcium, Ca\",\"amount\":714.0,\"unitName\":\"MG\"},{\"number\":\"303\",\"name\":\"Iron, Fe\",\"amount\":0.0,\"unitName\":\"MG\"},{\"number\":\"307\",\"name\":\"Sodium, Na\",\"amount\":607.0,\"unitName\":\"MG\"},{\"number\":\"318\",\"name\":\"Vitamin A, IU\",\"amount\":1070.0,\"unitName\":\"IU\"},{\"number\":\"401\",\"name\":\"Vitamin C, total ascorbic acid\",\"amount\":0.0,\"unitName\":\"MG\"},{\"number\":\"601\",\"name\":\"Cholesterol\",\"amount\":107.0,\"unitName\":\"MG\"},{\"number\":\"605\",\"name\":\"Fatty acids, total trans\",\"amount\":0.0,\"unitName\":\"G\"},{\"number\":\"606\",\"name\":\"Fatty acids, total saturated\",\"amount\":21.4,\"unitName\":\"G\"}],\"gtinUpc\":\"75925306223\",\"ingredients\":\"CHEDDAR CHEESE (PASTEURIZED MILK, CHEESE CULTURE, SALT, ENZYMES, ANNATTO COLOR), POTATO STARCH, STARCH AND CELLULOSE POWDER TO PREVENT CAKING, NATAMYCIN (MOLD INHIBITOR).\",\"query\":\"cheddar cheese\"}\n",
                Food.class));
        contentsInCache.add(gson.fromJson("{\"fdcId\":2118224,\"dataType\":\"Branded\",\"description\":\"CHEDDAR CHEESE\",\"foodNutrients\":[{\"number\":\"203\",\"name\":\"Protein\",\"amount\":21.4,\"unitName\":\"G\"},{\"number\":\"204\",\"name\":\"Total lipid (fat)\",\"amount\":32.1,\"unitName\":\"G\"},{\"number\":\"205\",\"name\":\"Carbohydrate, by difference\",\"amount\":7.14,\"unitName\":\"G\"},{\"number\":\"208\",\"name\":\"Energy\",\"amount\":393.0,\"unitName\":\"KCAL\"},{\"number\":\"269\",\"name\":\"Sugars, total including NLEA\",\"amount\":0.0,\"unitName\":\"G\"},{\"number\":\"291\",\"name\":\"Fiber, total dietary\",\"amount\":0.0,\"unitName\":\"G\"},{\"number\":\"301\",\"name\":\"Calcium, Ca\",\"amount\":714.0,\"unitName\":\"MG\"},{\"number\":\"303\",\"name\":\"Iron, Fe\",\"amount\":0.0,\"unitName\":\"MG\"},{\"number\":\"307\",\"name\":\"Sodium, Na\",\"amount\":679.0,\"unitName\":\"MG\"},{\"number\":\"318\",\"name\":\"Vitamin A, IU\",\"amount\":1070.0,\"unitName\":\"IU\"},{\"number\":\"401\",\"name\":\"Vitamin C, total ascorbic acid\",\"amount\":0.0,\"unitName\":\"MG\"},{\"number\":\"601\",\"name\":\"Cholesterol\",\"amount\":107.0,\"unitName\":\"MG\"},{\"number\":\"605\",\"name\":\"Fatty acids, total trans\",\"amount\":0.0,\"unitName\":\"G\"},{\"number\":\"606\",\"name\":\"Fatty acids, total saturated\",\"amount\":21.4,\"unitName\":\"G\"}],\"gtinUpc\":\"075925300009\",\"ingredients\":\"CHEDDAR CHEESE (PASTEURIZED MILK, CHEESE CULTURE, SALT, ENZYMES, ANNATTO COLOR), POTATO STARCH, STARCH AND CELLULOSE POWDER TO PREVENT CAKING, NATAMYCIN (MOLD INHIBITOR).\",\"query\":\"cheddar cheese\"}\n",
                Food.class));
        contentsInCache.add(gson.fromJson("{\"fdcId\":2095236,\"dataType\":\"Branded\",\"description\":\"CHEDDAR CHEESE\",\"foodNutrients\":[{\"number\":\"203\",\"name\":\"Protein\",\"amount\":25.0,\"unitName\":\"G\"},{\"number\":\"204\",\"name\":\"Total lipid (fat)\",\"amount\":35.7,\"unitName\":\"G\"},{\"number\":\"205\",\"name\":\"Carbohydrate, by difference\",\"amount\":0.0,\"unitName\":\"G\"},{\"number\":\"208\",\"name\":\"Energy\",\"amount\":432.0,\"unitName\":\"KCAL\"},{\"number\":\"269\",\"name\":\"Sugars, total including NLEA\",\"amount\":0.0,\"unitName\":\"G\"},{\"number\":\"291\",\"name\":\"Fiber, total dietary\",\"amount\":0.0,\"unitName\":\"G\"},{\"number\":\"307\",\"name\":\"Sodium, Na\",\"amount\":686.0,\"unitName\":\"MG\"},{\"number\":\"601\",\"name\":\"Cholesterol\",\"amount\":107.0,\"unitName\":\"MG\"},{\"number\":\"605\",\"name\":\"Fatty acids, total trans\",\"amount\":0.0,\"unitName\":\"G\"},{\"number\":\"606\",\"name\":\"Fatty acids, total saturated\",\"amount\":21.4,\"unitName\":\"G\"}],\"gtinUpc\":\"828653282457\",\"ingredients\":\"MILK, CHEESE CULTURES, SALT, ENZYMES AND ANNATTO COLOR IF NECESSARY.\",\"query\":\"cheddar cheese\"}\n",
                Food.class));

        assertEquals(contentsInCache.get(0), foodCache.getByFdcId(2015943).getFood());
        assertEquals(contentsInCache.get(1), foodCache.getByFdcId(2118224).getFood());
        assertEquals(contentsInCache.get(2), foodCache.getByFdcId(2095236).getFood());

        assertEquals(contentsInCache.get(0), foodCache.getByGtinUpc("75925306223").getFood());
        assertEquals(contentsInCache.get(1), foodCache.getByGtinUpc("075925300009").getFood());
        assertEquals(contentsInCache.get(2), foodCache.getByGtinUpc("828653282457").getFood());

        List<Food> actual = foodCache.getByKeywords("cheddar cheese").getFoods();
        assertTrue(actual.containsAll(contentsInCache));
        assertTrue(contentsInCache.containsAll(actual));

    }

    @Test
    public void testCacheGetDoesntExist() {
        assertNull(foodCache.getByFdcId(788888));
        assertNull(foodCache.getByKeywords("kiwi"));
        assertNull(foodCache.getByGtinUpc("27318213"));
    }

    @Test
    public void testStoreNullValueInCacheThrowsCacheException() {
        assertThrows(CacheException.class, () -> foodCache.storeInCache(null));
    }

    @Test
    public void testStore() throws IOException, CacheException, InterruptedException {
        final String tempCacheFile = "./tempCacheFile.txt";
        FoodCache storeFoodCache = new FoodCache(Path.of(tempCacheFile), logger);

        List<Food> foodList = new ArrayList<>();
        foodList.add(gson.fromJson("{\"fdcId\":2015943,\"dataType\":\"Branded\",\"description\":\"CHEDDAR CHEESE\",\"foodNutrients\":[{\"number\":\"203\",\"name\":\"Protein\",\"amount\":25.0,\"unitName\":\"G\"},{\"number\":\"204\",\"name\":\"Total lipid (fat)\",\"amount\":32.1,\"unitName\":\"G\"},{\"number\":\"205\",\"name\":\"Carbohydrate, by difference\",\"amount\":3.57,\"unitName\":\"G\"},{\"number\":\"208\",\"name\":\"Energy\",\"amount\":393.0,\"unitName\":\"KCAL\"},{\"number\":\"269\",\"name\":\"Sugars, total including NLEA\",\"amount\":0.0,\"unitName\":\"G\"},{\"number\":\"291\",\"name\":\"Fiber, total dietary\",\"amount\":0.0,\"unitName\":\"G\"},{\"number\":\"301\",\"name\":\"Calcium, Ca\",\"amount\":714.0,\"unitName\":\"MG\"},{\"number\":\"303\",\"name\":\"Iron, Fe\",\"amount\":0.0,\"unitName\":\"MG\"},{\"number\":\"307\",\"name\":\"Sodium, Na\",\"amount\":607.0,\"unitName\":\"MG\"},{\"number\":\"318\",\"name\":\"Vitamin A, IU\",\"amount\":1070.0,\"unitName\":\"IU\"},{\"number\":\"401\",\"name\":\"Vitamin C, total ascorbic acid\",\"amount\":0.0,\"unitName\":\"MG\"},{\"number\":\"601\",\"name\":\"Cholesterol\",\"amount\":107.0,\"unitName\":\"MG\"},{\"number\":\"605\",\"name\":\"Fatty acids, total trans\",\"amount\":0.0,\"unitName\":\"G\"},{\"number\":\"606\",\"name\":\"Fatty acids, total saturated\",\"amount\":21.4,\"unitName\":\"G\"}],\"gtinUpc\":\"75925306223\",\"ingredients\":\"CHEDDAR CHEESE (PASTEURIZED MILK, CHEESE CULTURE, SALT, ENZYMES, ANNATTO COLOR), POTATO STARCH, STARCH AND CELLULOSE POWDER TO PREVENT CAKING, NATAMYCIN (MOLD INHIBITOR).\",\"query\":\"cheddar cheese\"}\n",
                Food.class));
        foodList.add(gson.fromJson("{\"fdcId\":2118224,\"dataType\":\"Branded\",\"description\":\"CHEDDAR CHEESE\",\"foodNutrients\":[{\"number\":\"203\",\"name\":\"Protein\",\"amount\":21.4,\"unitName\":\"G\"},{\"number\":\"204\",\"name\":\"Total lipid (fat)\",\"amount\":32.1,\"unitName\":\"G\"},{\"number\":\"205\",\"name\":\"Carbohydrate, by difference\",\"amount\":7.14,\"unitName\":\"G\"},{\"number\":\"208\",\"name\":\"Energy\",\"amount\":393.0,\"unitName\":\"KCAL\"},{\"number\":\"269\",\"name\":\"Sugars, total including NLEA\",\"amount\":0.0,\"unitName\":\"G\"},{\"number\":\"291\",\"name\":\"Fiber, total dietary\",\"amount\":0.0,\"unitName\":\"G\"},{\"number\":\"301\",\"name\":\"Calcium, Ca\",\"amount\":714.0,\"unitName\":\"MG\"},{\"number\":\"303\",\"name\":\"Iron, Fe\",\"amount\":0.0,\"unitName\":\"MG\"},{\"number\":\"307\",\"name\":\"Sodium, Na\",\"amount\":679.0,\"unitName\":\"MG\"},{\"number\":\"318\",\"name\":\"Vitamin A, IU\",\"amount\":1070.0,\"unitName\":\"IU\"},{\"number\":\"401\",\"name\":\"Vitamin C, total ascorbic acid\",\"amount\":0.0,\"unitName\":\"MG\"},{\"number\":\"601\",\"name\":\"Cholesterol\",\"amount\":107.0,\"unitName\":\"MG\"},{\"number\":\"605\",\"name\":\"Fatty acids, total trans\",\"amount\":0.0,\"unitName\":\"G\"},{\"number\":\"606\",\"name\":\"Fatty acids, total saturated\",\"amount\":21.4,\"unitName\":\"G\"}],\"gtinUpc\":\"075925300009\",\"ingredients\":\"CHEDDAR CHEESE (PASTEURIZED MILK, CHEESE CULTURE, SALT, ENZYMES, ANNATTO COLOR), POTATO STARCH, STARCH AND CELLULOSE POWDER TO PREVENT CAKING, NATAMYCIN (MOLD INHIBITOR).\",\"query\":\"cheddar cheese\"}\n",
                Food.class));
        foodList.add(gson.fromJson("{\"fdcId\":2095236,\"dataType\":\"Branded\",\"description\":\"CHEDDAR CHEESE\",\"foodNutrients\":[{\"number\":\"203\",\"name\":\"Protein\",\"amount\":25.0,\"unitName\":\"G\"},{\"number\":\"204\",\"name\":\"Total lipid (fat)\",\"amount\":35.7,\"unitName\":\"G\"},{\"number\":\"205\",\"name\":\"Carbohydrate, by difference\",\"amount\":0.0,\"unitName\":\"G\"},{\"number\":\"208\",\"name\":\"Energy\",\"amount\":432.0,\"unitName\":\"KCAL\"},{\"number\":\"269\",\"name\":\"Sugars, total including NLEA\",\"amount\":0.0,\"unitName\":\"G\"},{\"number\":\"291\",\"name\":\"Fiber, total dietary\",\"amount\":0.0,\"unitName\":\"G\"},{\"number\":\"307\",\"name\":\"Sodium, Na\",\"amount\":686.0,\"unitName\":\"MG\"},{\"number\":\"601\",\"name\":\"Cholesterol\",\"amount\":107.0,\"unitName\":\"MG\"},{\"number\":\"605\",\"name\":\"Fatty acids, total trans\",\"amount\":0.0,\"unitName\":\"G\"},{\"number\":\"606\",\"name\":\"Fatty acids, total saturated\",\"amount\":21.4,\"unitName\":\"G\"}],\"gtinUpc\":\"828653282457\",\"ingredients\":\"MILK, CHEESE CULTURES, SALT, ENZYMES AND ANNATTO COLOR IF NECESSARY.\",\"query\":\"cheddar cheese\"}\n",
                Food.class));


        storeFoodCache.submitAll(foodList);
        storeFoodCache.close();

        for (var a : storeFoodCache.getWriterThreads()) {
            a.join();
        }

        assertEquals(foodList.get(0), storeFoodCache.getByFdcId(2015943).getFood());
        assertEquals(foodList.get(1), storeFoodCache.getByFdcId(2118224).getFood());
        assertEquals(foodList.get(2), storeFoodCache.getByFdcId(2095236).getFood());

        assertEquals(foodList.get(0), storeFoodCache.getByGtinUpc("75925306223").getFood());
        assertEquals(foodList.get(1), storeFoodCache.getByGtinUpc("075925300009").getFood());
        assertEquals(foodList.get(2), storeFoodCache.getByGtinUpc("828653282457").getFood());

        List<Food> actual = storeFoodCache.getByKeywords("cheddar cheese").getFoods();
        assertEquals(actual.size(), 3);
        assertTrue(actual.containsAll(foodList));
        assertTrue(foodList.containsAll(actual));

        Files.delete(Path.of(tempCacheFile));
    }
}
