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
 * 坏兆 (Bad Omen)
 * 费用：0
 * 攻击：0
 * 生命：1
 * 能力：死亡守望 - 当任何单位死亡时，该单位永久获得+1攻击
 */
public class BadOmen extends CreatureCard implements Deathwatch {
    
    public BadOmen() {
        setCardname("Bad Omen");
        setManacost(0);
        setAttack(0);
        setHealth(1);
        setUnitConfig("conf/gameconfs/units/bad_omen.json");
    }
    
    @Override
    public void onSummon(ActorRef out, GameState gameState, Unit unit, Tile tile) {
        // 注册死亡守望监听器
        gameState.addDeathwatchListener(unit, deadUnit -> onUnitDeath(out, gameState, unit));
    }
    
    @Override
    public void onUnitDeath(ActorRef out, GameState gameState, Unit unit) {
        // 当任何单位死亡时，该单位获得+1攻击
        int newAttack = unit.getAttack() + 1;
        unit.setAttack(newAttack);
        BasicCommands.setUnitAttack(out, unit, newAttack);
        
        // 播放buff特效
        try {
            BasicCommands.playEffectAnimation(out, BasicObjectBuilders.loadEffect(StaticConfFiles.f1_buff), unit.getTile());
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}