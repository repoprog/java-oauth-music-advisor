package advisor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<String> commands = Arrays.asList(args);
        Main.SERVER_PATH = commands.contains("-access") ?
                args[1] : "https://accounts.spotify.com";
        Main.API_PATH = commands.contains("-resource") ?
                args[3] : "https://api.spotify.com";

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
                    createServer();
                    getToken();
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

    public static String SERVER_PATH;
    public static String REDIRECT_URI = "http://localhost:8080";
    public static String CLIENT_ID = "fe5116329022495595d5b72fc1076a82";
    public static String CLIENT_SECRET = "2b27ffc709364928bd6e3a8e802224d0";
    public static String ACCESS_CODE = "";
    public static String accessToken;
    public static String API_PATH;

    public static void createServer() {
        String uri = SERVER_PATH + "/authorize"
                + "?client_id=" + CLIENT_ID
                + "&redirect_uri=" + REDIRECT_URI
                + "&response_type=code";

        try {
            HttpServer server = HttpServer.create();

            server.bind(new InetSocketAddress(8080), 0);
            server.start();
            server.createContext("/",
                    new HttpHandler() {
                        public void handle(HttpExchange exchange) throws IOException {
                            String query = exchange.getRequestURI().getQuery();
                            String request;
                            if (query != null && query.contains("code")) {
                                ACCESS_CODE = query.substring(5);
                                System.out.println("code received");
                                System.out.println(ACCESS_CODE);
                                request = "Got the code. Return back to your program.";
                            } else {
                                request = "Authorization code not found. Try again.";
                            }
                            exchange.sendResponseHeaders(200, request.length());
                            exchange.getResponseBody().write(request.getBytes());
                            exchange.getResponseBody().close();
                        }
                    }
            );

            System.out.println("use this link to request the access code:");
            System.out.println(uri);
            System.out.println("waiting for code...");
            while (ACCESS_CODE.equals("")) {
                Thread.sleep(100);
            }
            server.stop(10);
        } catch (IOException | InterruptedException e) {
            System.out.println();

        }
        System.out.println();
    }

    public static void getToken() {
        System.out.println("Making http request for access_token...");
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(SERVER_PATH + "/api/token"))
                .POST(HttpRequest.BodyPublishers.ofString(
                        "grant_type=authorization_code"
                                + "&code=" + ACCESS_CODE
                                + "&client_id=" + CLIENT_ID
                                + "&client_secret=" + CLIENT_SECRET
                                + "&redirect_uri=" + REDIRECT_URI))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("response:");
            String json = response.body();
            JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
            accessToken = jo.get("access_token").getAsString();
            System.out.println(response.body());
        } catch (IOException | InterruptedException e) {

            System.out.println(e.getMessage());
        }
    }

    public static void printNew() {
        String apiUrl = API_PATH + "/v1/browse/new-releases";
        String json = makeGetRequest(apiUrl);
        List<JsonObject> albumItems = new ArrayList<>();
        JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
        JsonObject albumsObj = jo.getAsJsonObject("albums");
        for (JsonElement album : albumsObj.getAsJsonArray("items")) {
            albumItems.add(album.getAsJsonObject());
        }

        for (JsonObject album : albumItems) {
            System.out.println(album.get("name").getAsString());
            for (JsonElement el : album.getAsJsonArray("artists")) {
                //take object from JSON Array "artists" and print "name" and "spotify" elements from it
                System.out.println("[" + el.getAsJsonObject().get("name").getAsString() + "]");
                System.out.println(el.getAsJsonObject().get("external_urls")
                        .getAsJsonObject().get("spotify").getAsString() + "\n");
            }
        }
    }

    public static void printFeatured() {
        String apiUrl = API_PATH + "/v1/browse/featured-playlists";
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
        categoriesMap.forEach((k, v) -> System.out.println(k + " " + v));
    }

    public static void printPlaylists(String categoryName) {
        Map<String, String> categoriesMap = createMapOfCategories();
        if (!categoriesMap.containsKey(categoryName)) {
            System.out.println("Unknown category name.");
            return;
        }
        String categoryId = categoriesMap.get(categoryName);

        String apiUrl = API_PATH + "/v1/browse/categories/" + categoryId + "/playlists";
        String json = makeGetRequest(apiUrl);
        if (json.contains("error")) {
            System.out.println("Specified id doesn't exist");
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
        String apiUrl = API_PATH + "/v1/browse/categories";
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
                .header("Authorization", "Bearer " + accessToken)
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

