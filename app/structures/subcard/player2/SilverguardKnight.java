package structures.subcard.player2;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.subcard.CreatureCard;
import structures.subcard.ability.Provoke;
import structures.subcard.ability.Zeal;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

/**
 * 银卫骑士 (Silverguard Knight)
 * 费用：3
 * 攻击：1
 * 生命：5
 * 能力：
 * - 嘲讽
 * - 热情：当你的化身受到伤害时，该单位永久获得+2攻击
 */
public class SilverguardKnight extends CreatureCard implements Provoke, Zeal {
    
    public SilverguardKnight() {
        setCardname("Silverguard Knight");
        setManacost(3);
        setAttack(1);
        setHealth(5);
        setUnitConfig("conf/gameconfs/units/silverguard_knight.json");
    }
    
    @Override
    public void onSummon(ActorRef out, GameState gameState, Unit unit, Tile tile) {
        // 注册为嘲讽单位
        registerProvoke(unit, gameState);
        
        // 注册热情效果（这需要在GameState中添加专门的方法，这里我们使用已有的机制）
        // 热情效果在UnitManager的attackUnit方法中，通过调用triggerAvatarDamageEvents方法触发
    }
    
    @Override
    public void registerProvoke(Unit unit, GameState gameState) {
        // 将单位注册为嘲讽单位
        gameState.registerProvokeUnit(unit);
    }
    
    @Override
    public void unregisterProvoke(Unit unit, GameState gameState) {
        // 取消注册嘲讽单位
        gameState.unregisterProvokeUnit(unit);
    }
    
    @Override
    public void onAvatarDamaged(ActorRef out, GameState gameState, Unit unit) {
        // 当友方化身受到伤害时，该单位获得+2攻击
        int newAttack = unit.getAttack() + 2;
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