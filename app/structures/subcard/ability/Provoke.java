package structures.subcard.ability;

import java.util.List;

import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * 嘲讽能力接口
 * 相邻的敌方单位必须优先攻击具有嘲讽的单位
 */
public interface Provoke {
    /**
     * 当单位被召唤到场上时，注册为嘲讽单位
     * @param unit 拥有嘲讽能力的单位
     * @param gameState 当前游戏状态
     */
    void registerProvoke(Unit unit, GameState gameState);
    
    /**
     * 当单位死亡时，取消注册嘲讽能力
     * @param unit 拥有嘲讽能力的单位
     * @param gameState 当前游戏状态
     */
    void unregisterProvoke(Unit unit, GameState gameState);
}