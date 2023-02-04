package advisor;

import com.google.gson.JsonParser;

import java.util.Map;
import java.util.Scanner;

public class MusicController {
    private static final Scanner scanner = new Scanner(System.in);

    public static void menu() {
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
                case "auth" -> {
                    MusicModel.createServer();
                    MusicModel.getToken();
                    System.out.println("Success!");
                }
                case "new" -> getNew("new");
                case "featured" -> getFeatured("featured");
                case "categories" -> getCategories("categories");
                case "playlists" -> {
                    String categoryName = choice.replace("playlists ", "");
                    getPlaylists("playlists", categoryName);
                }
                case "exit" -> {
                    quit = true;
                    System.out.println("---GOODBYE!---");
                }
                default -> System.out.println("No such choice.");
            }
        }
    }

    public static void getNew(String item) {
        String apiUrl = MusicModel.getApiPath() + "/v1/browse/new-releases";
        String json = MusicModel.makeGetRequest(apiUrl);
        MusicModel.creteItemsList(json, "albums");
        navigateResults(item);
    }

    public static void getFeatured(String item) {
        String apiUrl = MusicModel.getApiPath() + "/v1/browse/featured-playlists";
        String json = MusicModel.makeGetRequest(apiUrl);
        MusicModel.creteItemsList(json, "playlists");
        navigateResults(item);
    }

    public static void getCategories(String item) {
        String apiUrl = MusicModel.getApiPath() + "/v1/browse/categories";
        String json = MusicModel.makeGetRequest(apiUrl);
        MusicModel.creteItemsList(json, "categories");
        navigateResults(item);
    }

    public static void getPlaylists(String item, String categoryName) {
        Map<String, String> categoriesMap = MusicModel.createMapOfCategories();
        if (!categoriesMap.containsKey(categoryName)) {
            System.out.println("Unknown category name.");
            return;
        }
        String categoryId = categoriesMap.get(categoryName);

        String apiUrl = MusicModel.getApiPath() + "/v1/browse/categories/" + categoryId + "/playlists";
        String json = MusicModel.makeGetRequest(apiUrl);
        if (json.contains("error")) {
            String errorMsg = JsonParser.parseString(json).getAsJsonObject()
                    .getAsJsonObject("error").get("message").getAsString();
            System.out.println(errorMsg);
        } else {
            MusicModel.creteItemsList(json, "playlists");
            navigateResults(item);
        }
    }

    public static void navigateResults(String item) {
        MusicModel.printItem(item);
        boolean isPagination;
        do {
            String navigation = scanner.nextLine().toLowerCase();
            isPagination = !navigation.equals("exit");
            if (navigation.equals("next")) {
                MusicModel.showNextPage(item);
            } else if (navigation.equals("prev")) {
                MusicModel.showPrevPage(item);
            } else {
                MusicModel.setCurrentPage(1);
            }
        } while (isPagination);
    }
}
