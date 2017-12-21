package kate.cache;

public class Main {

    /** Первым параметром указывается стратегия:
    *       MN - в памяти хранятся самые новые объекты;
    *       MR - в памяти хранятся самые запрашиваемые;
    *   Вторым параметром указывается размер массива кэша в памяти
    *   Третьим - длина жизни объектов в кэше в мили секундах  */
    public static void main(String args[]) {

        Cache cache;

        switch (args[0]){
            case "MR" : cache = new MostRequiredCache(); break;
            default :  cache = new MostNewCache();
        }

        if(args[1] != null) {
            cache.setSize(Integer.parseInt(args[1]));
        }
        if(args[2] != null) {
            cache.setLifetime(Integer.parseInt(args[2]));
        }

        System.out.println(cache.toString());
    }

}
