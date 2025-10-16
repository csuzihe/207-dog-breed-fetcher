package dogapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * BreedFetcher implementation that relies on the dog.ceo API.
 * Note that all failures get reported as BreedNotFoundException
 * exceptions to align with the requirements of the BreedFetcher interface.
 */
public class DogApiBreedFetcher implements BreedFetcher {
    private static final String BASE = "https://dog.ceo/api/breed/";
    private static final String SUFFIX = "/list";

    private final OkHttpClient client;

    public DogApiBreedFetcher() {
        this.client = new OkHttpClient();
    }


    /**
     * Fetch the list of sub breeds for the given breed from the dog.ceo API.
     * @param breed the breed to fetch sub breeds for
     * @return list of sub breeds for the given breed
     * @throws BreedNotFoundException if the breed does not exist (or if the API call fails for any reason)
     */
    @Override
    public List<String> getSubBreeds(String breed) throws BreedNotFoundException {
        String norm = breed == null ? "" : breed.trim().toLowerCase();
        if (norm.isEmpty()) {
            throw new BreedNotFoundException(breed);
        }

        String url = BASE + norm + SUFFIX;
        Request req = new Request.Builder().url(url).build();

        try (Response resp = client.newCall(req).execute()) {
            if (resp.body() == null) {
                throw new BreedNotFoundException(breed);
            }
            String body = resp.body().string();

            JSONObject json = new JSONObject(body);
            String status = json.optString("status", "error");
            if (!"success".equalsIgnoreCase(status)) {
                throw new BreedNotFoundException(breed);
            }

            JSONArray arr = json.getJSONArray("message");
            List<String> result = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                result.add(arr.getString(i));
            }
            return result;
        } catch (IOException e) {
            throw new BreedNotFoundException(breed, e);
        }
    }
}