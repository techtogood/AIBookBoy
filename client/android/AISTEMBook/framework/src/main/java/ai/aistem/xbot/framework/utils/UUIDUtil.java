package ai.aistem.xbot.framework.utils;


import java.net.NetworkInterface;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Formatter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class UUIDUtil {

    @Override
    public String toString() {
        return encrypt(getWiFiMacAddress());
        //return getWiFiMacAddress().toString();
    }


    public static String getMacAddress() {
        return encrypt(getWiFiMacAddress());
    }

    /**
     * 加密
     *
     * @param plaintext 明文
     * @return ciphertext 密文
     */
    private static String encrypt(String plaintext) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            byte[] btInput = plaintext.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Get the active MAC address on the current machine as a byte array. This is called when generating a new UUID.
     * Note that a machine can have multiple or no active MAC addresses. This method works by iterating through the list
     * of network interfaces, ignoring the loopback interface and any virtual interfaces (which often have made-up
     * addresses), and returning the first one we find. If no valid addresses are found, then a byte array of the same
     * length with all zeros is returned.
     *
     * @return 6-byte array for first active MAC address, or 6-byte zeroed array if no interfaces are active.
     */
    private static String getWiFiMacAddress() {

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            byte[] mac = null;

            while (interfaces.hasMoreElements() && (mac == null || mac.length != 6)) {
                NetworkInterface netInterface = interfaces.nextElement();
                if (netInterface.getDisplayName().contains("wlan"))
                    mac = netInterface.getHardwareAddress();
            }

            // if the machine is not connected to a network it has no active MAC address
            if (mac == null)
                mac = new byte[]{0, 0, 0, 0, 0, 0};

            char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    'a', 'b', 'c', 'd', 'e', 'f'};
            int j = mac.length;

            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = mac[i];
                str[k++] = hexDigits[byte0 >> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            throw new RuntimeException("Could not get MAC address");
        }
    }

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();

        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        return formatter.toString();
    }

    public static String hmacSha1(String value, String key) {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = null;
        try {
            mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            return toHexString(mac.doFinal(value.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }
}