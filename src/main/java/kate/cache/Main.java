package kate.cache;

public class Main {

    public static void main(String args[]) {

        Cache cache = new Cache();
        if(args[0] != null) {
            cache.setSize(Integer.parseInt(args[0]));
        }
        if(args[1] != null) {
            cache.setLifetime(Integer.parseInt(args[1]));
        }

        System.out.println(cache.toString());
    }

}
