package cs455.scaling.bytes;

public class HashSingleton {
    private static HashSingleton single_instance = null;

    // variable of type String
    public String s;

    // private constructor restricted to this class itself
    private HashSingleton()
    {
        s = "Hello I am a string part of Singleton class";
    }

    // static method to create instance of Singleton class
    public static HashSingleton getInstance()
    {
        if (single_instance == null)
            single_instance = new HashSingleton();

        return single_instance;
    }

}
