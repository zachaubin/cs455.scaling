package cs455.scaling.client;

/*
The client is expected to send messages at the rate specified during start-up. The client sends a byte[] to the server.
The size of this array is 8 KB and the contents of this array are randomly generated. The client generates a new byte
array for every transmission and also tracks the hash codes associated with the  data  that  it  transmits.  Hashes
will  be  generated  with  the  SHA-1  algorithm.    The  following  code snippet computes the SHA-1 hash of a byte
array, and returns its representation as a hex string:

publicString SHA1FromBytes(byte[] data) {MessageDigest digest = MessageDigest.getInstance("SHA1");byte[] hash  =
digest.digest(data);BigInteger hashInt = newBigInteger(1, hash);returnhashInt.toString(16);}

A client maintains these hash codes in a linked list. For every data packet that is published, the client adds the
corresponding hashcode to the linked list. Upon receiving the data, the server will compute the hash code for the data
packet and send this back to the client. When an acknowledgement is received from the server, the client checks the
hashcode in the acknowledgement by scanning through the linked list. Once the hashcode has been verified, it can be
removed from the linked list
 */

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashes {

    public String SHA1FromBytes(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA1");byte[] hash  = digest.digest(data);
        BigInteger hashInt = new BigInteger(1, hash);
        return hashInt.toString(16);}
}
