package structures.subcard.ability;

import java.util.List;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * 飞行能力接口
 * 可以移动到棋盘上的任何空格
 */
public interface Flying {
    /**
     * 获取单位可以移动到的格子
     * @param unit 拥有飞行能力的单位
     * @param gameState 当前游戏状态
     * @return 可以移动到的格子列表
     */
    List<Tile> getMovableTiles(Unit unit, GameState gameState);
    
    /**
     * 高亮可以移动到的格子
     * @param out WebSocket通信通道
     * @param unit 拥有飞行能力的单位
     * @param gameState 当前游戏状态
     */
    void highlightMovableTiles(ActorRef out, Unit unit, GameState gameState);
}