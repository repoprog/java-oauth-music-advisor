package advisor;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicModel {
    private static String SERVER_PATH;
    private static String REDIRECT_URI = "http://localhost:8080";
    private static String CLIENT_ID = "fe5116329022495595d5b72fc1076a82";
    private static String CLIENT_SECRET = "2b27ffc709364928bd6e3a8e802224d0";
    private static String ACCESS_CODE = "";
    private static String accessToken;
    private static String API_PATH;
    private static Integer numOfPages;
    private static int recordsPerPage;
    private static List<JsonObject> itemsList;
    // zrób jako lokalną w metodach
    private static int currentPage = 1;


    public static void setRecordsPerPage(int showPages) {
        recordsPerPage = showPages;
    }

    public static void setServerPath(String serverPath) {
        SERVER_PATH = serverPath;
    }

    public static String getApiPath() {
        return API_PATH;
    }

    public static void setApiPath(String apiPath) {
        API_PATH = apiPath;
    }

    public static int getRecordsPerPage() {
        return recordsPerPage;
    }

    public static int getCurrentPage() {
        return currentPage;
    }

    public static void setCurrentPage(int currentPage) {
        MusicModel.currentPage = currentPage;
    }

    public static int getNumOfPages() {
        return numOfPages;
    }

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

    public static void creteItemsList(String json, String wantedItem) {
        itemsList = new ArrayList<>();
        JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
        JsonObject itemObj = jo.getAsJsonObject(wantedItem);

        for (JsonElement i : itemObj.getAsJsonArray("items")) {
            itemsList.add(i.getAsJsonObject());
        }
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

    public static List<JsonObject> createPaginatedResults() {

        int lastPageResults = itemsList.size() % getRecordsPerPage();
        int pages = itemsList.size() / getRecordsPerPage();
        numOfPages = lastPageResults == 0 ? pages : pages + 1;
        int currentEndResult = getCurrentPage() * getRecordsPerPage();
        int startFrom = (getCurrentPage() - 1) * getRecordsPerPage();
        int endTo = lastPageResults != 0 && getCurrentPage() == numOfPages
                ? numOfPages - getRecordsPerPage() + lastPageResults : currentEndResult;
        return itemsList.subList(startFrom, endTo);
    }

    public static void showNextPage(String item) {
        if (MusicModel.getCurrentPage() == numOfPages) {
            System.out.println("No more pages.");
            return;
        }
        setCurrentPage(getCurrentPage() + 1);
        printItem(item);

    }

    public static void showPrevPage(String item) {
        if (MusicModel.getCurrentPage() == 1) {
            System.out.println("No more pages.");
            return;
        }
        setCurrentPage(getCurrentPage() - 1);
        printItem(item);
    }


    public static void printItem(String item) {
        switch (item) {
            case "new" -> MusicView.printNew();
            case "featured" -> MusicView.printFeatured();
            case "categories" -> MusicView.printCategories();
            case "playlists" -> MusicView.printPlaylists();
        }
    }
}
