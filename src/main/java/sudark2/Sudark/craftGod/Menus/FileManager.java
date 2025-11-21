package sudark2.Sudark.craftGod.Menus;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import sudark2.Sudark.craftGod.Mark.Mark;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static sudark2.Sudark.craftGod.CraftGod.get;

public class FileManager {

    static File folder = get().getDataFolder();
    static File templateFolder = new File(folder, "templates");
    static File markFolder = new File(folder, "marks");

    public static void init() {
        if (!folder.exists()) folder.mkdir();
        if (!templateFolder.exists()) templateFolder.mkdir();
        if (!markFolder.exists()) markFolder.mkdir();
    }

    public static void saveTemplate(String templateName, Pair<Location, List<Mark>> markPair) {
        File templateFile = new File(templateFolder, templateName + ".yml");
        while(templateFile.exists()) {
            templateName += "-";
            templateFile = new File(templateFolder, templateName + ".yml");
        }

        Location startLoc = markPair.left();
        List<Mark> marks = markPair.right();

        YamlConfiguration config = new YamlConfiguration();
        // 存储绝对起始坐标
        config.set("start.x", startLoc.getBlockX());
        config.set("start.y", startLoc.getBlockY());
        config.set("start.z", startLoc.getBlockZ());

        // 3. 存储 Mark 列表
        int index = 0;
        for (Mark mark : marks) {
            String path = "marks." + index;
            config.set(path + ".dx", mark.getDx());
            config.set(path + ".dy", mark.getDy());
            config.set(path + ".dz", mark.getDz());

            // 关键：存储 BlockData 的字符串表示
            config.set(path + ".data", mark.getDataAsString());
            index++;
        }

        // 4. 保存到磁盘
        try {
            config.save(templateFile);
            System.out.println("成功保存模板: " + templateName);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("保存模板失败: " + templateName);
        }
    }

    public static Pair<Location, List<Mark>> loadTemplate(String templateName, World targetWorld) {
        // 1. 检查文件是否存在
        File templateFile = new File(templateFolder, templateName);
        if (!templateFile.exists()) {
            System.err.println("模板文件不存在: " + templateName);
            return null;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(templateFile);
        List<Mark> marks = new ArrayList<>();

        // 2. 加载起始点
        int startX = config.getInt("start.x", 0);
        int startY = config.getInt("start.y", 0);
        int startZ = config.getInt("start.z", 0);
        Location startLoc = new Location(targetWorld, startX, startY, startZ);

        // 3. 加载 Mark 列表
        ConfigurationSection marksSection = config.getConfigurationSection("marks");
        if (marksSection != null) {
            for (String key : marksSection.getKeys(false)) {
                ConfigurationSection markSection = marksSection.getConfigurationSection(key);
                if (markSection == null) continue;

                int dx = markSection.getInt("dx");
                int dy = markSection.getInt("dy");
                int dz = markSection.getInt("dz");
                String dataString = markSection.getString("data");

                if (dataString != null) {
                    try {
                        // 关键：将字符串反序列化为 BlockData
                        BlockData data = Bukkit.createBlockData(dataString);
                        marks.add(new Mark(dx, dy, dz, data));
                    } catch (IllegalArgumentException e) {
                        System.err.println("无效的 BlockData 字符串: " + dataString + "，跳过该 Mark。");
                    }
                }
            }
        }

        return Pair.of(startLoc, marks);
    }

    public static List<String> loadAllTemplateNames() {
        File[] allEntries = templateFolder.listFiles();

        if (allEntries == null) {
            return new ArrayList<>();
        }

        // 使用 Stream API 过滤出文件并提取文件名
        return Arrays.stream(allEntries)
                .filter(File::isFile) // 过滤掉子目录
                .map(File::getName)   // 提取文件名
                .collect(Collectors.toList());
    }
}
