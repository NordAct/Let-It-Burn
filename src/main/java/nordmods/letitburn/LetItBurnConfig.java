package nordmods.letitburn;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@Config(name = "let_it_burn")
public class LetItBurnConfig implements ConfigData {
    public boolean enabled = false;
    public List<FlammabilityInfo> flammableBlocks = new ArrayList<>();

    public record FlammabilityInfo(String id, int burnChance, int spreadChance) {}
}
