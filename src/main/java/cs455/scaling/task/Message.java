package cs455.scaling.task;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Message {

    public void write(SocketChannel socketChannel, byte[] msg) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(msg);
        while( buffer.hasRemaining() ){
            if( socketChannel.write( buffer ) == 0){
                //register write interest with socket
                // break out of loop
            }
        }
        //turn off write interest here
    }
}
