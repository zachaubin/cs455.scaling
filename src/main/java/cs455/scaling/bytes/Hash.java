package cs455.scaling.bytes;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {

    public String SHA1FromBytes(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        byte[] hash  = digest.digest(data);
        BigInteger hashInt = new BigInteger(1, hash);

//        pad with leading 0's so size == 40,
//         it may have been stripped of leading 0's
        String hashString = hashInt.toString(16);
        while(hashString.length() < 40){
            hashString = "0" + hashString;
        }
        return hashString;
    }



}
