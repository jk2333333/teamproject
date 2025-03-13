package structures.subcard.player1;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.EffectAnimation;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.subcard.CreatureCard;
import structures.subcard.ability.Deathwatch;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

/**
 * 暗影舞者 (Shadowdancer)
 * 费用：5
 * 攻击：5
 * 生命：4
 * 能力：死亡守望 - 对敌方化身造成1点伤害并为自己恢复1点生命
 */
public class Shadowdancer extends CreatureCard implements Deathwatch {
    
    public Shadowdancer() {
        setCardname("Shadowdancer");
        setManacost(5);
        setAttack(5);
        setHealth(4);
        setUnitConfig("conf/gameconfs/units/shadowdancer.json");
    }
    
    @Override
    public void onSummon(ActorRef out, GameState gameState, Unit unit, Tile tile) {
        // 注册死亡守望监听器
        gameState.addDeathwatchListener(unit, deadUnit -> onUnitDeath(out, gameState, unit));
    }
    
    @Override
    public void onUnitDeath(ActorRef out, GameState gameState, Unit unit) {
        // 获取敌方化身
        Unit enemyAvatar = (unit.getOwner() == 1) ? gameState.player2Avatar : gameState.player1Avatar;
        
        if (enemyAvatar != null) {
            // 播放攻击特效
            try {
                EffectAnimation projectile = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_projectiles);
                BasicCommands.playProjectileAnimation(out, projectile, 0, unit.getTile(), enemyAvatar.getTile());
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // 对敌方化身造成1点伤害
            int newEnemyHealth = enemyAvatar.getHealth() - 1;
            if (newEnemyHealth < 0) newEnemyHealth = 0;
            enemyAvatar.setHealth(newEnemyHealth);
            
            // 更新敌方化身的生命值
            BasicCommands.setUnitHealth(out, enemyAvatar, newEnemyHealth);
            
            // 更新玩家生命值
            if (enemyAvatar.getIsAvartar(1)) {
                gameState.player1.setHealth(newEnemyHealth);
                BasicCommands.setPlayer1Health(out, gameState.player1);
            } else {
                gameState.player2.setHealth(newEnemyHealth);
                BasicCommands.setPlayer2Health(out, gameState.player2);
            }
            
            // 为自己恢复1点生命
            int newHealth = unit.getHealth() + 1;
            // 假设最大生命值为初始生命值4
            if (newHealth > 4) newHealth = 4;
            unit.setHealth(newHealth);
            BasicCommands.setUnitHealth(out, unit, newHealth);
            
            // 播放治疗特效
            try {
                BasicCommands.playEffectAnimation(out, BasicObjectBuilders.loadEffect(StaticConfFiles.f1_buff), unit.getTile());
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}