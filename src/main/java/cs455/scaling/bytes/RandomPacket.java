package cs455.scaling.bytes;

// maybe make one, use a generator function within it to avoid overhead

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

    public static void main(String[] args){
        RandomPacket r = new RandomPacket();
        printMessage(r.generate());
    }
}