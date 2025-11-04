package sudark2.Sudark.craftGod;
import java.io.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class RandomCodeGenerator {
    private static final int CODE_LENGTH = 6;  // 6位码长度
    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";  // 字母组合（大写A-Z）

    private Random random = new Random();
    private Set<String> existingCodes = new HashSet<>();  // 内存中的查重Set

    /**
     * 生成唯一码（带查重）
     * @param maxRetries 最大重试次数，防无限循环
     * @return 唯一码
     */
    public String generateUniqueCode(int maxRetries) {
        int retries = 0;
        while (retries < maxRetries) {
            String code = generateRandomCode();
            if (!existingCodes.contains(code)) {  // O(1)查重
                existingCodes.add(code);  // 添加到Set
                return code;
            }
            retries++;
        }
        throw new RuntimeException("生成唯一码失败，已重试 " + maxRetries + " 次，请检查码库大小。");
    }

    /**
     * 生成一个6位随机字母码
     */
    public String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHARSET.length());
            sb.append(CHARSET.charAt(index));
        }
        return sb.toString();
    }
}
