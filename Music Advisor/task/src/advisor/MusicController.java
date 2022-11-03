package advisor;

import java.util.Map;
import java.util.Scanner;

public class MusicController {
    public static void menu() {
        Scanner scanner = new Scanner(System.in);
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
                    getNew();
                    break;
                case "featured":
                    getFeatured();
                    break;
                case "categories":
                    getCategories();
                    break;
                case "playlists":
                    String categoryName = choice.replace("playlists ", "");
                    getPlaylists(categoryName);
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

    public static void getNew() {
        String apiUrl = MusicModel.getApiPath() + "/v1/browse/new-releases";
        String json = MusicModel.makeGetRequest(apiUrl);
        MusicView.printNew(json);
    }

    public static void getFeatured() {
        String apiUrl = MusicModel.getApiPath() + "/v1/browse/featured-playlists";
        String json = MusicModel.makeGetRequest(apiUrl);
        MusicView.printFeatured(json);
    }

    public static void getCategories() {
        Map<String, String> categoriesMap = MusicModel.createMapOfCategories();
        MusicView.printCategories(categoriesMap);
    }

    public static void getPlaylists(String categoryName) {
        Map<String, String> categoriesMap = MusicModel.createMapOfCategories();
        if (!categoriesMap.containsKey(categoryName)) {
            System.out.println("Unknown category name.");
            return;
        }
        String categoryId = categoriesMap.get(categoryName);

        String apiUrl = MusicModel.getApiPath() + "/v1/browse/categories/" + categoryId + "/playlists";
        String json = MusicModel.makeGetRequest(apiUrl);

        MusicView.printPlaylists(json);
    }
}
