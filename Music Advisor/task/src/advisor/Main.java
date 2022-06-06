package advisor;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<String> commands = Arrays.asList(args);
        String SERVER_PATH = commands.contains("-access") ?
                args[1] : "https://accounts.spotify.com";
        MusicModel.setServerPath(SERVER_PATH);
                String API_PATH = commands.contains("-resource") ?
                args[3] : "https://api.spotify.com";
        MusicModel.setApiPath(API_PATH);

        boolean quit = false;
        boolean authOrExit;
        boolean started = true;
        while (!quit) {

            String choice = scanner.nextLine();
            authOrExit = choice.equals("auth") || choice.equals("exit");

            while (started && !authOrExit) {
                System.out.println("Please, provide access for application.");
                choice = scanner.nextLine();
                authOrExit = choice.equals("auth") || choice.equals("exit");
            }
            started = false;
            String[] command = choice.split(" ");
            switch (command[0]) {
                case "auth":
                    MusicModel.createServer();
                    MusicModel.getToken();
                    System.out.println("Success!");
                    break;
                case "new":
                    printNew();
                    break;
                case "featured":
                    printFeatured();
                    break;
                case "categories":
                    printCategories();
                    break;
                case "playlists":
                    String categoryName = choice.replace("playlists ", "");
                    printPlaylists(categoryName);
                    break;
                case "exit":
                    quit = true;
                    System.out.println("---GOODBYE!---");
                    break;
                default:
                    System.out.println("No such choice.");
            }
        }
    }

    public static void printNew() {
        String apiUrl = MusicModel.getApiPath() + "/v1/browse/new-releases";
        String json = makeGetRequest(apiUrl);
        List<JsonObject> albumItems = new ArrayList<>();

        JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
        JsonObject albumsObj = jo.getAsJsonObject("albums");

        for (JsonElement album : albumsObj.getAsJsonArray("items")) {
            albumItems.add(album.getAsJsonObject());
        }

        for (JsonObject album : albumItems) {
            System.out.println(album.get("name").getAsString());
            List<String> artistList = new ArrayList<>();
            for (JsonElement el : album.getAsJsonArray("artists")) {
                //take object from JSON Array "artists" and print "name" and "spotify" elements from it
                artistList.add(el.getAsJsonObject().get("name").getAsString());
            }
            System.out.println(artistList);
            System.out.println(album.getAsJsonObject().get("external_urls")
                    .getAsJsonObject().get("spotify").getAsString() + "\n");
        }
    }

    public static void printFeatured() {
        String apiUrl = MusicModel.getApiPath() + "/v1/browse/featured-playlists";
        String json = makeGetRequest(apiUrl);
        List<JsonObject> featuredItems = new ArrayList<>();
        JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
        JsonObject featuredObj = jo.getAsJsonObject("playlists");
        for (JsonElement featured : featuredObj.getAsJsonArray("items")) {
            featuredItems.add(featured.getAsJsonObject());
        }

        for (JsonObject featured : featuredItems) {
            System.out.println(featured.get("name").getAsString());
            System.out.println(featured.getAsJsonObject().get("external_urls")
                    .getAsJsonObject().get("spotify").getAsString() + "\n");
        }
    }

    public static void printCategories() {
        Map<String, String> categoriesMap = createMapOfCategories();
        categoriesMap.forEach((k, v) -> System.out.println(k + "  " + v));
    }

    public static void printPlaylists(String categoryName) {
        Map<String, String> categoriesMap = createMapOfCategories();
        if (!categoriesMap.containsKey(categoryName)) {
            System.out.println("Unknown category name.");
            return;
        }
        String categoryId = categoriesMap.get(categoryName);

        String apiUrl = MusicModel.getApiPath() + "/v1/browse/categories/" + categoryId + "/playlists";
        String json = makeGetRequest(apiUrl);
        if (json.contains("error")) {
            String errorMsg = JsonParser.parseString(json).getAsJsonObject()
                    .getAsJsonObject("error").get("message").getAsString();
            System.out.println(errorMsg);
        } else {
            List<JsonObject> featuredItems = new ArrayList<>();
            JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
            JsonObject featuredObj = jo.getAsJsonObject("playlists");
            for (JsonElement featured : featuredObj.getAsJsonArray("items")) {
                featuredItems.add(featured.getAsJsonObject());
            }

            for (JsonObject featured : featuredItems) {
                System.out.println(featured.get("name").getAsString());
                System.out.println(featured.getAsJsonObject().get("external_urls")
                        .getAsJsonObject().get("spotify").getAsString() + "\n");
            }
        }
    }

    public static Map<String, String> createMapOfCategories() {
        String apiUrl = MusicModel.getApiPath() + "/v1/browse/categories";
        String json = makeGetRequest(apiUrl);
        Map<String, String> categoriesMap = new HashMap<>();
        List<JsonObject> categoryItems = new ArrayList<>();
        JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
        JsonObject categoriesObj = jo.getAsJsonObject("categories");
        for (JsonElement category : categoriesObj.getAsJsonArray("items")) {
            categoryItems.add(category.getAsJsonObject());
        }

        for (JsonObject category : categoryItems) {
            categoriesMap.put(category.get("name").getAsString(), category.get("id").getAsString());
        }
        return categoriesMap;
    }


    public static String makeGetRequest(String apiUrl) {
        String json = null;
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + MusicModel.getApiPath())
                .uri(URI.create(apiUrl))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            json = response.body();
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
        return json;
    }
}

