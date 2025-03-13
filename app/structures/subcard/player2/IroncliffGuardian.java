package structures.subcard.player2;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.subcard.CreatureCard;
import structures.subcard.ability.Airdrop;
import structures.subcard.ability.Provoke;

import java.util.ArrayList;
import java.util.List;

/**
 * 铁崖守护者 (Ironcliff Guardian)
 * 费用：5
 * 攻击：3
 * 生命：10
 * 能力：
 * - 嘲讽
 * - 空投：可以被召唤到棋盘上的任何空格
 */
public class IroncliffGuardian extends CreatureCard implements Provoke, Airdrop {
    
    public IroncliffGuardian() {
        setCardname("Ironcliff Guardian");
        setManacost(5);
        setAttack(3);
        setHealth(10);
        setUnitConfig("conf/gameconfs/units/ironcliff_guardian.json");
    }
    
    @Override
    public void onSummon(ActorRef out, GameState gameState, Unit unit, Tile tile) {
        // 注册为嘲讽单位
        registerProvoke(unit, gameState);
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
    public List<Tile> getSummonableTiles(GameState gameState) {
        List<Tile> summonableTiles = new ArrayList<>();
        
        // 空投能力允许单位被召唤到棋盘上的任何空格
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 5; y++) {
                Tile tile = gameState.board[x][y];
                if (tile.getUnit() == null) {
                    summonableTiles.add(tile);
                }
            }
        }
        
        return summonableTiles;
    }
    
    @Override
    public List<Tile> getValidTargets(GameState gameState) {
        // 对于具有空投能力的单位，重写基类的getValidTargets方法
        // 使用空投的getSummonableTiles方法获取可召唤的格子
        return getSummonableTiles(gameState);
    }
}