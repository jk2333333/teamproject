package structures.subcard.player2;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;  // 添加这一行导入Unit类
import structures.subcard.SpellCard;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import java.util.ArrayList;
import java.util.List;

/**
 * 日滴万能药 (Sundrop Elixir)
 * 费用：1
 * 效果：为友方单位恢复4点生命
 */
public class SundropElixir extends SpellCard {
    
    public SundropElixir() {
        setCardname("Sundrop Elixir");
        setManacost(1);
    }
    
    @Override
    public void onCardPlayed(ActorRef out, GameState gameState, Tile tile) {
        // 获取目标单位
        Unit targetUnit = tile.getUnit();
        if (targetUnit == null) return;
        
        // 判断单位的最大生命值
        int maxHealth = getMaxHealth(targetUnit);
        
        // 计算恢复后的生命值
        int newHealth = Math.min(targetUnit.getHealth() + 4, maxHealth);
        
        // 更新单位生命值
        targetUnit.setHealth(newHealth);
        BasicCommands.setUnitHealth(out, targetUnit, newHealth);
        
        // 如果是化身，同时更新玩家生命值
        if (targetUnit.getIsAvartar(1)) {
            gameState.player1.setHealth(newHealth);
            BasicCommands.setPlayer1Health(out, gameState.player1);
        } else if (targetUnit.getIsAvartar(2)) {
            gameState.player2.setHealth(newHealth);
            BasicCommands.setPlayer2Health(out, gameState.player2);
        }
        
        // 播放治疗特效
        try {
            BasicCommands.playEffectAnimation(out, BasicObjectBuilders.loadEffect(StaticConfFiles.f1_buff), tile);
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public List<Tile> getValidTargets(GameState gameState) {
        List<Tile> validTargets = new ArrayList<>();
        
        // 只有当前玩家的回合才能使用卡牌
        if (gameState.currentPlayer != 1) {
            return validTargets;
        }
        
        // 查找所有友方单位
        for (Unit unit : gameState.playerUnits) {
            if (unit.getOwner() == gameState.currentPlayer) {
                // 只有生命值低于最大值的单位才是有效目标
                if (unit.getHealth() < getMaxHealth(unit)) {
                    validTargets.add(unit.getTile());
                }
            }
        }
        
        return validTargets;
    }
    
    /**
     * 获取单位的最大生命值
     * @param unit 目标单位
     * @return 估计的最大生命值
     */
    private int getMaxHealth(Unit unit) {
        // 对于头像，我们知道最大生命值是20
        if (unit.getIsAvartar(1) || unit.getIsAvartar(2)) {
            return 20;
        }
        
        // 对于其他单位，我们使用卡牌配置中的初始生命值
        String unitConfig = unit.getUnitConfig();
        if (unitConfig != null) {
            // 这里可以根据unitConfig查找对应的卡牌配置
            // 简化起见，我们使用一些已知的单位配置
            if (unitConfig.contains("ironcliff_guardian")) return 10;
            if (unitConfig.contains("silverguard_knight")) return 5;
            if (unitConfig.contains("young_flamewing")) return 4;
            if (unitConfig.contains("saberspine_tiger")) return 2;
            if (unitConfig.contains("swamp_entangler")) return 3;
            if (unitConfig.contains("skyrock_golem")) return 2;
            if (unitConfig.contains("silverguard_squire")) return 1;
            
            // 对于玩家1的单位
            if (unitConfig.contains("shadowdancer")) return 4;
            if (unitConfig.contains("bloodmoon_priestess")) return 3;
            if (unitConfig.contains("nightsorrow_assassin")) return 2;
            if (unitConfig.contains("shadow_watcher")) return 2;
            if (unitConfig.contains("rock_pulveriser")) return 4;
            if (unitConfig.contains("gloom_chaser")) return 1;
            if (unitConfig.contains("bad_omen")) return 1;
            if (unitConfig.contains("wraithling")) return 1;
        }
        
        // 如果无法确定，则使用当前生命值作为最大值
        return unit.getHealth();
    }
}