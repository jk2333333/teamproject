package structures.subcard.player2;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.subcard.CreatureCard;
import structures.subcard.ability.OpeningGambit;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

/**
 * 银卫侍从 (Silverguard Squire)
 * 费用：1
 * 攻击：1
 * 生命：1
 * 能力：开战效果 - 为化身前方或后方的相邻友方单位提供+1/+1
 */
public class SilverguardSquire extends CreatureCard implements OpeningGambit {
    
    public SilverguardSquire() {
        setCardname("Silverguard Squire");
        setManacost(1);
        setAttack(1);
        setHealth(1);
        setUnitConfig("conf/gameconfs/units/silverguard_squire.json");
    }
    
    @Override
    public void onSummon(ActorRef out, GameState gameState, Unit unit, Tile tile) {
        // 触发开战效果
        onUnitSummoned(out, gameState, unit, tile);
    }
    
    @Override
    public void onUnitSummoned(ActorRef out, GameState gameState, Unit unit, Tile tile) {
        // 获取拥有者的化身
        Unit avatar = (unit.getOwner() == 1) ? gameState.player1Avatar : gameState.player2Avatar;
        
        if (avatar != null) {
            int avatarX = avatar.getPosition().getTilex();
            int avatarY = avatar.getPosition().getTiley();
            
            // 检查化身前方的单位（对于玩家2来说是右侧，x+1）
            checkAndBuffUnit(out, gameState, avatarX + 1, avatarY, unit.getOwner());
            
            // 检查化身后方的单位（对于玩家2来说是左侧，x-1）
            checkAndBuffUnit(out, gameState, avatarX - 1, avatarY, unit.getOwner());
        }
    }
    
    /**
     * 检查指定位置是否有友方单位，并为其提供+1/+1
     * @param out WebSocket通信通道
     * @param gameState 当前游戏状态
     * @param x 格子的x坐标
     * @param y 格子的y坐标
     * @param owner 单位所有者
     */
    private void checkAndBuffUnit(ActorRef out, GameState gameState, int x, int y, int owner) {
        // 检查坐标是否合法
        if (x >= 0 && x < 9 && y >= 0 && y < 5) {
            Tile targetTile = gameState.board[x][y];
            Unit targetUnit = targetTile.getUnit();
            
            // 如果格子有单位，且是友方单位
            if (targetUnit != null && targetUnit.getOwner() == owner) {
                // 提供+1/+1
                int newAttack = targetUnit.getAttack() + 1;
                int newHealth = targetUnit.getHealth() + 1;
                
                targetUnit.setAttack(newAttack);
                targetUnit.setHealth(newHealth);
                
                // 更新UI
                BasicCommands.setUnitAttack(out, targetUnit, newAttack);
                BasicCommands.setUnitHealth(out, targetUnit, newHealth);
                
                // 播放buff特效
                try {
                    BasicCommands.playEffectAnimation(out, BasicObjectBuilders.loadEffect(StaticConfFiles.f1_buff), targetTile);
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}