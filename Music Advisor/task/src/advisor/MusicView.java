package advisor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import java.util.List;

public class MusicView {

    public static void printNew() {
        List<JsonObject> paginatedResults = MusicModel.createPaginatedResults();
        for (JsonObject album : paginatedResults) {
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
        System.out.printf("---PAGE %d OF %d---", MusicModel.getCurrentPage(), MusicModel.getNumOfPages());
    }

    public static void printFeatured() {
        List<JsonObject> paginatedResults = MusicModel.createPaginatedResults();
        for (JsonObject featured : paginatedResults) {
            System.out.println(featured.get("name").getAsString());
            System.out.println(featured.getAsJsonObject().get("external_urls")
                    .getAsJsonObject().get("spotify").getAsString() + "\n");
        }

        System.out.printf("---PAGE %d OF %d---", MusicModel.getCurrentPage(), MusicModel.getNumOfPages());
    }

    public static void printCategories() {
        List<JsonObject> paginatedResults = MusicModel.createPaginatedResults();
        for (JsonObject category : paginatedResults) {
            System.out.println((category.get("name").getAsString() + " " +
                    category.get("id").getAsString()));
        }

        System.out.printf("---PAGE %d OF %d---", MusicModel.getCurrentPage(), MusicModel.getNumOfPages());
    }

    public static void printPlaylists() {
        List<JsonObject> paginatedResults = MusicModel.createPaginatedResults();
        for (JsonObject featured : paginatedResults) {
            System.out.println(featured.get("name").getAsString());
            System.out.println(featured.getAsJsonObject().get("external_urls")
                    .getAsJsonObject().get("spotify").getAsString() + "\n");
        }

        System.out.printf("---PAGE %d OF %d---", MusicModel.getCurrentPage(), MusicModel.getNumOfPages());
    }

}
