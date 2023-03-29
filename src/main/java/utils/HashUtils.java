package utils;

public class HashUtils {

  public static String addHash(String a, String b) {
    int hashLen = a.length();
    char[] aCharList = a.toCharArray();
    char[] bCharList = b.toCharArray();

    int[] result = new int[hashLen];
    for (int i = hashLen - 1; i >= 0; i--) {
      int aDigit = Integer.valueOf(String.valueOf(aCharList[i]), 16);
      int bDigit = Integer.valueOf(String.valueOf(bCharList[i]), 16);
      result[i] = (aDigit + bDigit) % 16;
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < hashLen; i++) {
      sb.append(Integer.toHexString(result[i]));
    }
    return sb.toString();
  }

}
