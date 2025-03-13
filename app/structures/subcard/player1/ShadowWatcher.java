package structures.subcard.player1;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.subcard.CreatureCard;
import structures.subcard.ability.Deathwatch;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

/**
 * 暗影守望者 (Shadow Watcher)
 * 费用：3
 * 攻击：3
 * 生命：2
 * 能力：死亡守望 - 当任何单位死亡时，该单位永久获得+1/+1
 */
public class ShadowWatcher extends CreatureCard implements Deathwatch {
    
    public ShadowWatcher() {
        setCardname("Shadow Watcher");
        setManacost(3);
        setAttack(3);
        setHealth(2);
        setUnitConfig("conf/gameconfs/units/shadow_watcher.json");
    }
    
    @Override
    public void onSummon(ActorRef out, GameState gameState, Unit unit, Tile tile) {
        // 注册死亡守望监听器
        gameState.addDeathwatchListener(unit, deadUnit -> onUnitDeath(out, gameState, unit));
    }
    
    @Override
    public void onUnitDeath(ActorRef out, GameState gameState, Unit unit) {
        // 当任何单位死亡时，该单位获得+1/+1
        int newAttack = unit.getAttack() + 1;
        int newHealth = unit.getHealth() + 1;
        
        unit.setAttack(newAttack);
        unit.setHealth(newHealth);
        
        // 更新UI
        BasicCommands.setUnitAttack(out, unit, newAttack);
        BasicCommands.setUnitHealth(out, unit, newHealth);
        
        // 播放buff特效
        try {
            BasicCommands.playEffectAnimation(out, BasicObjectBuilders.loadEffect(StaticConfFiles.f1_buff), unit.getTile());
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}