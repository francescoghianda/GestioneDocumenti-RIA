package it.polimi.utils.hash;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class PasswordHashGenerator
{
    private static final Random random = new Random();

    private PasswordHashGenerator() {}

    public static String digest(String password)
    {
        byte[] salt = new byte[64];
        random.nextBytes(salt);
        String saltString = DatatypeConverter.printHexBinary(salt);
        return saltString+":"+generateHash(salt, password);
    }

    public static boolean check(String digest, String password)
    {
        String[] parts = digest.split(":");
        if(parts.length < 2)return false;
        byte[] salt = DatatypeConverter.parseHexBinary(parts[0]);
        String passwordHash = parts[1];
        return generateHash(salt, password).equals(passwordHash);
    }

    private static String generateHash(byte[] salt, String password)
    {
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");

            byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
            byte[] bytes = new byte[salt.length+passwordBytes.length];
            for(int i = 0; i < bytes.length; i++)
                bytes[i] = i < salt.length ? salt[i] : passwordBytes[i-salt.length];
            return DatatypeConverter.printHexBinary(messageDigest.digest(bytes));
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            throw new RuntimeException("MD5 algorithm not found!");
        }
    }
}
