package cs455.scaling.bytes;

// maybe make one, use a generator function within it to avoid overhead

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class RandomPacket {

    public Random random = new Random();

    public byte[] generate(){
        byte[] packet = new byte[8000];
        random.nextBytes(packet);
        return packet;
    }

    public static void printMessage(byte[] msg){
        for(byte b : msg){
            System.out.println(b);
        }
    }

    public String hash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        byte[] hash  = digest.digest(data);
//        System.out.println("hash.len: " + hash.length);
        BigInteger hashInt = new BigInteger(1, hash);

//        pad with leading 0's so size == 40,
//         it may have been stripped of leading 0's
        String hashString = hashInt.toString(16);
//        System.out.println("computing:: hash string = " + hashString);
        while(hashString.length() < 40){
            hashString = "0" + hashString;
        }
        return hashString;
    }
    public void printBytes(byte[] bytes){
        int fourcount = 4;
        System.out.println("---- BEGIN ----");
        System.out.println(">------<");
        for(byte b : bytes){
            System.out.println(b);
            fourcount--;
            if(fourcount == 0){
                System.out.println(">------<");
                fourcount = 4;
            }
        }
        System.out.println("----  END  ----");

    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        RandomPacket r = new RandomPacket();
//        printMessage(r.generate());
        byte[] msg = r.generate();
        Hash h = new Hash();
        for(int i = 0 ; i < 100; i++) {
            msg = r.generate();
            String s = h.SHA1FromBytes(msg);
            System.out.println(s);
        }
    }
}