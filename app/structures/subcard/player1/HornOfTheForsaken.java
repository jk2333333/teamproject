package structures.subcard.player1;

import akka.actor.ActorRef;
import commands.BasicCommands;
import managers.UnitManager;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.subcard.SpellCard;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import java.util.ArrayList;
import java.util.List;

/**
 * 被遗忘者之角 (Horn of the Forsaken)
 * 费用：1
 * 效果：神器(3) - 当你的化身造成伤害时，在随机相邻空格召唤一个幽灵
 */
public class HornOfTheForsaken extends SpellCard {
    
    public HornOfTheForsaken() {
        setCardname("Horn of the Forsaken");
        setManacost(1);
    }
    
    @Override
    public void onCardPlayed(ActorRef out, GameState gameState, Tile tile) {
        // 获取玩家化身
        Unit avatar = gameState.player1Avatar;
        
        if (avatar != null) {
            // 设置神器耐久度
            gameState.setArtifactDurability(avatar, 3);
            
            // 添加神器触发监听
            // 注意：这需要在UnitManager的attackUnit方法中添加对神器的检查和触发逻辑
            // 例如，每次头像攻击后，检查是否有神器，并触发相应效果
            
            // 播放特效
            try {
                BasicCommands.playEffectAnimation(out, BasicObjectBuilders.loadEffect(StaticConfFiles.f1_buff), avatar.getTile());
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // 显示神器效果提示
            BasicCommands.addPlayer1Notification(out, "Horn of the Forsaken equipped (3)", 2);
        }
    }
    
    @Override
    public List<Tile> getValidTargets(GameState gameState) {
        List<Tile> validTargets = new ArrayList<>();
        
        // 只有当前玩家的回合才能使用卡牌
        if (gameState.currentPlayer != 1) {
            return validTargets;
        }
        
        // 神器只能对自己的化身使用
        if (gameState.player1Avatar != null) {
            validTargets.add(gameState.player1Avatar.getTile());
        }
        
        return validTargets;
    }
    
    /**
     * 当玩家化身造成伤害时触发的效果
     * 此方法应该在UnitManager的attackUnit方法中被调用
     * @param out WebSocket通信通道
     * @param gameState 当前游戏状态
     * @param avatar 玩家化身
     */
    public static void onAvatarDealDamage(ActorRef out, GameState gameState, Unit avatar) {
        // 检查是否有神器
        if (gameState.hasArtifact(avatar)) {
            // 获取随机相邻空格
            Tile targetTile = UnitManager.getRandomAdjacentEmptyTile(gameState, avatar);
            
            if (targetTile != null) {
                // 召唤幽灵
                UnitManager.summonWraithling(out, gameState, targetTile);
            }
            
            // 减少神器耐久度
            int remainingDurability = gameState.decreaseArtifactDurability(avatar);
            
            // 更新神器状态提示
            if (remainingDurability > 0) {
                BasicCommands.addPlayer1Notification(out, "Horn of the Forsaken (" + remainingDurability + ")", 1);
            } else {
                BasicCommands.addPlayer1Notification(out, "Horn of the Forsaken broken", 2);
            }
        }
    }
}